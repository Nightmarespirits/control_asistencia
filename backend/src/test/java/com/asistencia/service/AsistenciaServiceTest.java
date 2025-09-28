package com.asistencia.service;

import com.asistencia.dto.MarcacionResponseDTO;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.MarcacionDuplicadaException;
import com.asistencia.repository.AsistenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceTest {
    
    @Mock
    private AsistenciaRepository asistenciaRepository;
    
    @Mock
    private EmpleadoService empleadoService;
    
    @Mock
    private HorarioService horarioService;
    
    @InjectMocks
    private AsistenciaService asistenciaService;
    
    private Empleado empleadoTest;
    
    @BeforeEach
    void setUp() {
        empleadoTest = new Empleado();
        empleadoTest.setId(1L);
        empleadoTest.setDni("12345678");
        empleadoTest.setNombres("Juan Carlos");
        empleadoTest.setApellidos("Pérez López");
        empleadoTest.setCargo("Desarrollador");
        empleadoTest.setArea("TI");
        empleadoTest.setActivo(true);
    }
    
    @Test
    void debeRegistrarMarcacionEntradaCorrectamente() {
        // Given
        String dni = "12345678";
        TipoMarcacion tipoEsperado = TipoMarcacion.ENTRADA;
        EstadoMarcacion estadoEsperado = EstadoMarcacion.PUNTUAL;
        String mensajeEsperado = "Entrada registrada a las 08:00, puntual 🎉";
        
        when(empleadoService.findEntityByDni(dni)).thenReturn(Optional.of(empleadoTest));
        when(horarioService.determinarTipoMarcacion(any(LocalTime.class))).thenReturn(tipoEsperado);
        when(horarioService.calcularEstadoMarcacion(any(LocalTime.class), eq(tipoEsperado))).thenReturn(estadoEsperado);
        when(horarioService.generarMensajeMarcacion(any(LocalTime.class), eq(tipoEsperado), eq(estadoEsperado))).thenReturn(mensajeEsperado);
        when(asistenciaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        MarcacionResponseDTO response = asistenciaService.registrarMarcacion(dni);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(mensajeEsperado, response.getMensaje());
        assertEquals(tipoEsperado, response.getTipo());
        assertEquals(estadoEsperado, response.getEstado());
        assertNotNull(response.getEmpleado());
        assertEquals("Juan Carlos", response.getEmpleado().getNombres());
        assertEquals("Pérez López", response.getEmpleado().getApellidos());
        assertEquals(dni, response.getEmpleado().getDni());
        
        verify(asistenciaRepository).save(any());
    }
    
    @Test
    void debeLanzarExcepcionCuandoEmpleadoNoExiste() {
        // Given
        String dniInexistente = "99999999";
        when(empleadoService.findEntityByDni(dniInexistente)).thenReturn(Optional.empty());
        
        // When & Then
        EmpleadoNotFoundException exception = assertThrows(EmpleadoNotFoundException.class, 
            () -> asistenciaService.registrarMarcacion(dniInexistente));
        
        assertEquals("Empleado no encontrado con DNI: " + dniInexistente, exception.getMessage());
        verify(asistenciaRepository, never()).save(any());
    }
    
    @Test
    void debeLanzarExcepcionCuandoMarcacionEsDuplicada() {
        // Given
        String dni = "12345678";
        TipoMarcacion tipoMarcacion = TipoMarcacion.ENTRADA;
        
        when(empleadoService.findEntityByDni(dni)).thenReturn(Optional.of(empleadoTest));
        when(horarioService.determinarTipoMarcacion(any(LocalTime.class))).thenReturn(tipoMarcacion);
        
        // Simular que existe una marcación reciente
        AsistenciaService spyService = spy(asistenciaService);
        doReturn(true).when(spyService).existsRecentMarcacion(eq(empleadoTest.getId()), eq(tipoMarcacion), eq(5));
        
        // When & Then
        MarcacionDuplicadaException exception = assertThrows(MarcacionDuplicadaException.class, 
            () -> spyService.registrarMarcacion(dni));
        
        assertTrue(exception.getMessage().contains("Ya existe una marcación reciente"));
        verify(asistenciaRepository, never()).save(any());
    }
    
    @Test
    void debeRegistrarMarcacionConTardanza() {
        // Given
        String dni = "12345678";
        TipoMarcacion tipoEsperado = TipoMarcacion.ENTRADA;
        EstadoMarcacion estadoEsperado = EstadoMarcacion.TARDANZA;
        String mensajeEsperado = "Entrada registrada, llegaste tarde por 15 min ⏰";
        int minutosTarde = 15;
        
        when(empleadoService.findEntityByDni(dni)).thenReturn(Optional.of(empleadoTest));
        when(horarioService.determinarTipoMarcacion(any(LocalTime.class))).thenReturn(tipoEsperado);
        when(horarioService.calcularEstadoMarcacion(any(LocalTime.class), eq(tipoEsperado))).thenReturn(estadoEsperado);
        when(horarioService.generarMensajeMarcacion(any(LocalTime.class), eq(tipoEsperado), eq(estadoEsperado))).thenReturn(mensajeEsperado);
        when(horarioService.calcularMinutosDiferencia(any(LocalTime.class), eq(tipoEsperado))).thenReturn(minutosTarde);
        when(asistenciaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        MarcacionResponseDTO response = asistenciaService.registrarMarcacion(dni);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(mensajeEsperado, response.getMensaje());
        assertEquals(tipoEsperado, response.getTipo());
        assertEquals(estadoEsperado, response.getEstado());
        assertEquals("Tardanza de " + minutosTarde + " minutos", response.getObservaciones());
        
        verify(asistenciaRepository).save(any());
    }
    
    @Test
    void debeRegistrarMarcacionFueraDeHorario() {
        // Given
        String dni = "12345678";
        TipoMarcacion tipoEsperado = TipoMarcacion.FUERA_HORARIO;
        EstadoMarcacion estadoEsperado = EstadoMarcacion.FUERA_HORARIO;
        String mensajeEsperado = "Fuera de horario registrada fuera de horario a las 22:30 ⚠️";
        
        when(empleadoService.findEntityByDni(dni)).thenReturn(Optional.of(empleadoTest));
        when(horarioService.determinarTipoMarcacion(any(LocalTime.class))).thenReturn(tipoEsperado);
        when(horarioService.calcularEstadoMarcacion(any(LocalTime.class), eq(tipoEsperado))).thenReturn(estadoEsperado);
        when(horarioService.generarMensajeMarcacion(any(LocalTime.class), eq(tipoEsperado), eq(estadoEsperado))).thenReturn(mensajeEsperado);
        when(asistenciaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        MarcacionResponseDTO response = asistenciaService.registrarMarcacion(dni);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(mensajeEsperado, response.getMensaje());
        assertEquals(tipoEsperado, response.getTipo());
        assertEquals(estadoEsperado, response.getEstado());
        assertEquals("Marcación fuera de horario laboral", response.getObservaciones());
        
        verify(asistenciaRepository).save(any());
    }
}