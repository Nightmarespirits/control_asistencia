package com.asistencia.controller;

import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.service.EmpleadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class EmpleadoControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String baseUrl;
    private EmpleadoDTO empleadoDTO;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/admin/empleados";
        
        empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setDni("12345678");
        empleadoDTO.setNombres("Juan Carlos");
        empleadoDTO.setApellidos("Pérez López");
        empleadoDTO.setCargo("Desarrollador");
        empleadoDTO.setArea("Tecnología");
    }
    
    @Test
    void getAllEmpleados_SinAutenticacion_DebeRetornarForbidden() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    void createEmpleado_SinAutenticacion_DebeRetornarForbidden() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmpleadoDTO> request = new HttpEntity<>(empleadoDTO, headers);
        
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    void updateEmpleado_SinAutenticacion_DebeRetornarForbidden() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmpleadoDTO> request = new HttpEntity<>(empleadoDTO, headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1", 
                HttpMethod.PUT, 
                request, 
                String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    void deleteEmpleado_SinAutenticacion_DebeRetornarForbidden() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1", 
                HttpMethod.DELETE, 
                null, 
                String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    // Test de funcionalidad del servicio (sin autenticación)
    @Test
    void empleadoService_DebeCrearEmpleadoCorrectamente() {
        // When
        EmpleadoDTO resultado = empleadoService.create(empleadoDTO);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getCodigoUnico()).isNotNull();
        assertThat(resultado.getDni()).isEqualTo("12345678");
        assertThat(resultado.getNombres()).isEqualTo("Juan Carlos");
        assertThat(resultado.getActivo()).isTrue();
    }
    
    @Test
    void empleadoService_DebeValidarDniUnico() {
        // Given
        empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleadoDuplicado = new EmpleadoDTO();
        empleadoDuplicado.setDni("12345678");
        empleadoDuplicado.setNombres("María");
        empleadoDuplicado.setApellidos("González");
        empleadoDuplicado.setCargo("Analista");
        empleadoDuplicado.setArea("Finanzas");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDuplicado))
                .hasMessageContaining("Ya existe un empleado con el DNI: 12345678");
    }
    
    @Test
    void empleadoService_DebeGenerarCodigoUnicoAutomaticamente() {
        // When
        EmpleadoDTO empleado1 = empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        EmpleadoDTO empleado2 = empleadoService.create(empleado2DTO);
        
        // Then
        assertThat(empleado1.getCodigoUnico()).isNotNull();
        assertThat(empleado2.getCodigoUnico()).isNotNull();
        assertThat(empleado1.getCodigoUnico()).isNotEqualTo(empleado2.getCodigoUnico());
    }
    
    @Test
    void empleadoService_DebeImplementarSoftDelete() {
        // Given
        EmpleadoDTO empleadoCreado = empleadoService.create(empleadoDTO);
        
        // When
        empleadoService.delete(empleadoCreado.getId());
        
        // Then
        // El empleado debe seguir existiendo pero inactivo
        var empleadoEliminado = empleadoService.findById(empleadoCreado.getId());
        assertThat(empleadoEliminado).isPresent();
        assertThat(empleadoEliminado.get().getActivo()).isFalse();
        
        // No debe aparecer en la lista de activos
        var empleadosActivos = empleadoService.findAllActivos();
        assertThat(empleadosActivos).isEmpty();
        
        // Pero sí en la lista completa
        var todosEmpleados = empleadoService.findAll();
        assertThat(todosEmpleados).hasSize(1);
        assertThat(todosEmpleados.get(0).getActivo()).isFalse();
    }
    
    @Test
    void empleadoService_DebeValidarCamposObligatorios() {
        // Test DNI vacío
        empleadoDTO.setDni("");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El DNI es obligatorio");
        
        // Test nombres vacíos
        empleadoDTO.setDni("12345678");
        empleadoDTO.setNombres("");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("Los nombres son obligatorios");
        
        // Test apellidos vacíos
        empleadoDTO.setNombres("Juan");
        empleadoDTO.setApellidos("");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("Los apellidos son obligatorios");
        
        // Test cargo vacío
        empleadoDTO.setApellidos("Pérez");
        empleadoDTO.setCargo("");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El cargo es obligatorio");
        
        // Test área vacía
        empleadoDTO.setCargo("Desarrollador");
        empleadoDTO.setArea("");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El área es obligatoria");
    }
    
    @Test
    void empleadoService_DebeValidarFormatoDni() {
        // DNI con menos de 8 dígitos
        empleadoDTO.setDni("123");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El DNI debe tener exactamente 8 dígitos");
        
        // DNI con más de 8 dígitos
        empleadoDTO.setDni("123456789");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El DNI debe tener exactamente 8 dígitos");
        
        // DNI con caracteres no numéricos
        empleadoDTO.setDni("1234567A");
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .hasMessageContaining("El DNI debe tener exactamente 8 dígitos");
    }
}