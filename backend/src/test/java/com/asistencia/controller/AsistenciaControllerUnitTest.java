package com.asistencia.controller;

import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.dto.MarcacionRequestDTO;
import com.asistencia.dto.MarcacionResponseDTO;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.MarcacionDuplicadaException;
import com.asistencia.service.AsistenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaControllerUnitTest {
    
    @Mock
    private AsistenciaService asistenciaService;
    
    @InjectMocks
    private AsistenciaController asistenciaController;
    
    private MarcacionRequestDTO validRequest;
    private EmpleadoDTO empleadoDTO;
    
    @BeforeEach
    void setUp() {
        validRequest = new MarcacionRequestDTO("12345678");
        
        empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setId(1L);
        empleadoDTO.setNombres("Juan Carlos");
        empleadoDTO.setApellidos("Pérez López");
        empleadoDTO.setDni("12345678");
    }
    
    @Test
    void debeRegistrarMarcacionExitosamente() {
        // Given
        MarcacionResponseDTO expectedResponse = MarcacionResponseDTO.success(
            "Entrada registrada a las 08:02, puntual 🎉",
            empleadoDTO,
            TipoMarcacion.ENTRADA,
            EstadoMarcacion.PUNTUAL,
            LocalDateTime.now()
        );
        
        when(asistenciaService.registrarMarcacion("12345678")).thenReturn(expectedResponse);
        
        // When
        ResponseEntity<MarcacionResponseDTO> response = asistenciaController.marcarAsistencia(validRequest);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Entrada registrada a las 08:02, puntual 🎉", response.getBody().getMensaje());
        assertEquals(TipoMarcacion.ENTRADA, response.getBody().getTipo());
        assertEquals(EstadoMarcacion.PUNTUAL, response.getBody().getEstado());
        assertEquals("Juan Carlos", response.getBody().getEmpleado().getNombres());
        
        verify(asistenciaService, times(1)).registrarMarcacion("12345678");
    }
    
    @Test
    void debeRetornarErrorCuandoEmpleadoNoExiste() {
        // Given
        when(asistenciaService.registrarMarcacion("99999999"))
            .thenThrow(EmpleadoNotFoundException.porDni("99999999"));
        
        MarcacionRequestDTO request = new MarcacionRequestDTO("99999999");
        
        // When
        ResponseEntity<MarcacionResponseDTO> response = asistenciaController.marcarAsistencia(request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Empleado no encontrado", response.getBody().getMensaje());
        
        verify(asistenciaService, times(1)).registrarMarcacion("99999999");
    }
    
    @Test
    void debeRetornarErrorCuandoMarcacionDuplicada() {
        // Given
        when(asistenciaService.registrarMarcacion("12345678"))
            .thenThrow(new MarcacionDuplicadaException("12345678", "ENTRADA"));
        
        // When
        ResponseEntity<MarcacionResponseDTO> response = asistenciaController.marcarAsistencia(validRequest);
        
        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Ya existe una marcación reciente", response.getBody().getMensaje());
        
        verify(asistenciaService, times(1)).registrarMarcacion("12345678");
    }
    
    @Test
    void debeRetornarErrorInternoParaExcepcionesInesperadas() {
        // Given
        when(asistenciaService.registrarMarcacion("12345678"))
            .thenThrow(new RuntimeException("Error inesperado"));
        
        // When
        ResponseEntity<MarcacionResponseDTO> response = asistenciaController.marcarAsistencia(validRequest);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Error interno del servidor", response.getBody().getMensaje());
        
        verify(asistenciaService, times(1)).registrarMarcacion("12345678");
    }
}