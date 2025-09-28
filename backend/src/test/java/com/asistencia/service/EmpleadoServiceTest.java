package com.asistencia.service;

import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.entity.Empleado;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.EmpleadoValidationException;
import com.asistencia.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmpleadoServiceTest {
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    private EmpleadoDTO empleadoDTO;
    
    @BeforeEach
    void setUp() {
        empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setDni("12345678");
        empleadoDTO.setNombres("Juan Carlos");
        empleadoDTO.setApellidos("Pérez López");
        empleadoDTO.setCargo("Desarrollador");
        empleadoDTO.setArea("Tecnología");
    }
    
    @Test
    void create_ConDatosValidos_DebeCrearEmpleadoConCodigoUnico() {
        // When
        EmpleadoDTO resultado = empleadoService.create(empleadoDTO);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getCodigoUnico()).isNotNull();
        assertThat(resultado.getCodigoUnico()).isNotEmpty();
        assertThat(resultado.getDni()).isEqualTo("12345678");
        assertThat(resultado.getNombres()).isEqualTo("Juan Carlos");
        assertThat(resultado.getApellidos()).isEqualTo("Pérez López");
        assertThat(resultado.getCargo()).isEqualTo("Desarrollador");
        assertThat(resultado.getArea()).isEqualTo("Tecnología");
        assertThat(resultado.getActivo()).isTrue();
        assertThat(resultado.getFechaCreacion()).isNotNull();
        assertThat(resultado.getFechaActualizacion()).isNotNull();
    }
    
    @Test
    void create_ConDniDuplicado_DebeLanzarExcepcion() {
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
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("Ya existe un empleado con el DNI: 12345678");
    }
    
    @Test
    void create_ConDniInvalido_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setDni("123"); // DNI inválido
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("El DNI debe tener exactamente 8 dígitos");
    }
    
    @Test
    void create_ConNombresVacios_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setNombres("");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("Los nombres son obligatorios");
    }
    
    @Test
    void create_ConApellidosVacios_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setApellidos("");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("Los apellidos son obligatorios");
    }
    
    @Test
    void create_ConCargoVacio_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setCargo("");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("El cargo es obligatorio");
    }
    
    @Test
    void create_ConAreaVacia_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setArea("");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("El área es obligatoria");
    }
    
    @Test
    void create_ConNombresMuyLargos_DebeLanzarExcepcion() {
        // Given
        empleadoDTO.setNombres("A".repeat(101)); // Más de 100 caracteres
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.create(empleadoDTO))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("Los nombres no pueden exceder 100 caracteres");
    }
    
    @Test
    void findById_CuandoExiste_DebeRetornarEmpleado() {
        // Given
        EmpleadoDTO empleadoCreado = empleadoService.create(empleadoDTO);
        
        // When
        Optional<EmpleadoDTO> resultado = empleadoService.findById(empleadoCreado.getId());
        
        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDni()).isEqualTo("12345678");
        assertThat(resultado.get().getNombres()).isEqualTo("Juan Carlos");
    }
    
    @Test
    void findById_CuandoNoExiste_DebeRetornarVacio() {
        // When
        Optional<EmpleadoDTO> resultado = empleadoService.findById(999L);
        
        // Then
        assertThat(resultado).isEmpty();
    }
    
    @Test
    void findByDni_CuandoExiste_DebeRetornarEmpleado() {
        // Given
        empleadoService.create(empleadoDTO);
        
        // When
        Optional<EmpleadoDTO> resultado = empleadoService.findByDni("12345678");
        
        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDni()).isEqualTo("12345678");
        assertThat(resultado.get().getNombres()).isEqualTo("Juan Carlos");
    }
    
    @Test
    void findByDni_CuandoNoExiste_DebeRetornarVacio() {
        // When
        Optional<EmpleadoDTO> resultado = empleadoService.findByDni("99999999");
        
        // Then
        assertThat(resultado).isEmpty();
    }
    
    @Test
    void findAllActivos_DebeRetornarSoloEmpleadosActivos() {
        // Given
        EmpleadoDTO empleado1 = empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        EmpleadoDTO empleado2 = empleadoService.create(empleado2DTO);
        
        // Desactivar el segundo empleado
        empleadoService.delete(empleado2.getId());
        
        // When
        List<EmpleadoDTO> resultado = empleadoService.findAllActivos();
        
        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(empleado1.getId());
        assertThat(resultado.get(0).getActivo()).isTrue();
    }
    
    @Test
    void findByArea_DebeRetornarEmpleadosDelArea() {
        // Given
        empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        empleadoService.create(empleado2DTO);
        
        // When
        List<EmpleadoDTO> resultado = empleadoService.findByArea("Tecnología");
        
        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getArea()).isEqualTo("Tecnología");
        assertThat(resultado.get(0).getDni()).isEqualTo("12345678");
    }
    
    @Test
    void findByCargo_DebeRetornarEmpleadosDelCargo() {
        // Given
        empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Desarrollador");
        empleado2DTO.setArea("Finanzas");
        empleadoService.create(empleado2DTO);
        
        // When
        List<EmpleadoDTO> resultado = empleadoService.findByCargo("Desarrollador");
        
        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(emp -> emp.getCargo().equals("Desarrollador"));
    }
    
    @Test
    void findByNombreOrApellido_DebeRetornarEmpleadosQueCoincidan() {
        // Given
        empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("Pérez");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        empleadoService.create(empleado2DTO);
        
        // When
        List<EmpleadoDTO> resultado = empleadoService.findByNombreOrApellido("Pérez");
        
        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(emp -> emp.getApellidos().contains("Pérez"));
    }
    
    @Test
    void update_ConDatosValidos_DebeActualizarEmpleado() {
        // Given
        EmpleadoDTO empleadoCreado = empleadoService.create(empleadoDTO);
        
        empleadoCreado.setCargo("Senior Developer");
        empleadoCreado.setArea("Arquitectura");
        
        // When
        EmpleadoDTO resultado = empleadoService.update(empleadoCreado.getId(), empleadoCreado);
        
        // Then
        assertThat(resultado.getCargo()).isEqualTo("Senior Developer");
        assertThat(resultado.getArea()).isEqualTo("Arquitectura");
        assertThat(resultado.getCodigoUnico()).isEqualTo(empleadoCreado.getCodigoUnico()); // No debe cambiar
        assertThat(resultado.getDni()).isEqualTo(empleadoCreado.getDni());
    }
    
    @Test
    void update_ConIdInexistente_DebeLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> empleadoService.update(999L, empleadoDTO))
                .isInstanceOf(EmpleadoNotFoundException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");
    }
    
    @Test
    void update_ConDniDuplicado_DebeLanzarExcepcion() {
        // Given
        EmpleadoDTO empleado1 = empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        EmpleadoDTO empleado2 = empleadoService.create(empleado2DTO);
        
        // Intentar cambiar el DNI del empleado2 al DNI del empleado1
        empleado2.setDni("12345678");
        
        // When & Then
        assertThatThrownBy(() -> empleadoService.update(empleado2.getId(), empleado2))
                .isInstanceOf(EmpleadoValidationException.class)
                .hasMessageContaining("Ya existe otro empleado con el DNI: 12345678");
    }
    
    @Test
    void delete_CuandoExiste_DebeDesactivarEmpleado() {
        // Given
        EmpleadoDTO empleadoCreado = empleadoService.create(empleadoDTO);
        
        // When
        empleadoService.delete(empleadoCreado.getId());
        
        // Then
        Optional<EmpleadoDTO> resultado = empleadoService.findById(empleadoCreado.getId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getActivo()).isFalse();
        
        // Verificar que no aparece en la lista de activos
        List<EmpleadoDTO> activos = empleadoService.findAllActivos();
        assertThat(activos).isEmpty();
        
        // Pero sí aparece en la lista completa
        List<EmpleadoDTO> todos = empleadoService.findAll();
        assertThat(todos).hasSize(1);
    }
    
    @Test
    void delete_ConIdInexistente_DebeLanzarExcepcion() {
        // When & Then
        assertThatThrownBy(() -> empleadoService.delete(999L))
                .isInstanceOf(EmpleadoNotFoundException.class)
                .hasMessageContaining("Empleado no encontrado con ID: 999");
    }
    
    @Test
    void existsByDni_CuandoExiste_DebeRetornarTrue() {
        // Given
        empleadoService.create(empleadoDTO);
        
        // When
        boolean resultado = empleadoService.existsByDni("12345678");
        
        // Then
        assertThat(resultado).isTrue();
    }
    
    @Test
    void existsByDni_CuandoNoExiste_DebeRetornarFalse() {
        // When
        boolean resultado = empleadoService.existsByDni("99999999");
        
        // Then
        assertThat(resultado).isFalse();
    }
    
    @Test
    void countActivos_DebeRetornarCantidadCorrecta() {
        // Given
        empleadoService.create(empleadoDTO);
        
        EmpleadoDTO empleado2DTO = new EmpleadoDTO();
        empleado2DTO.setDni("87654321");
        empleado2DTO.setNombres("María");
        empleado2DTO.setApellidos("González");
        empleado2DTO.setCargo("Analista");
        empleado2DTO.setArea("Finanzas");
        EmpleadoDTO empleado2 = empleadoService.create(empleado2DTO);
        
        // Desactivar uno
        empleadoService.delete(empleado2.getId());
        
        // When
        long resultado = empleadoService.countActivos();
        
        // Then
        assertThat(resultado).isEqualTo(1L);
    }
    
    @Test
    void codigoUnico_DebeSerUnicoParaCadaEmpleado() {
        // Given & When
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
        
        // Verificar que los códigos no existen en la base de datos antes de la creación
        assertThat(empleadoRepository.existsByCodigoUnico(empleado1.getCodigoUnico())).isTrue();
        assertThat(empleadoRepository.existsByCodigoUnico(empleado2.getCodigoUnico())).isTrue();
    }
}