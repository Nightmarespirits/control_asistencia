package com.asistencia.service;

import com.asistencia.dto.HorarioDTO;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.Horario;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private HorarioService horarioService;

    private Horario horarioEntrada;
    private Horario horarioSalidaAlmuerzo;
    private Horario horarioRetornoAlmuerzo;
    private Horario horarioSalida;

    @BeforeEach
    void setUp() {
        // Configurar horarios de prueba
        horarioEntrada = new Horario("Entrada", LocalTime.of(7, 50), LocalTime.of(8, 20), TipoMarcacion.ENTRADA);
        horarioEntrada.setId(1L);
        horarioEntrada.setActivo(true);

        horarioSalidaAlmuerzo = new Horario("Salida Almuerzo", LocalTime.of(12, 0), LocalTime.of(12, 30), TipoMarcacion.SALIDA_ALMUERZO);
        horarioSalidaAlmuerzo.setId(2L);
        horarioSalidaAlmuerzo.setActivo(true);

        horarioRetornoAlmuerzo = new Horario("Retorno Almuerzo", LocalTime.of(13, 0), LocalTime.of(13, 30), TipoMarcacion.RETORNO_ALMUERZO);
        horarioRetornoAlmuerzo.setId(3L);
        horarioRetornoAlmuerzo.setActivo(true);

        horarioSalida = new Horario("Salida", LocalTime.of(17, 0), LocalTime.of(17, 30), TipoMarcacion.SALIDA);
        horarioSalida.setId(4L);
        horarioSalida.setActivo(true);
    }

    @Test
    void testDeterminarTipoMarcacion_DentroDeRangoEntrada() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 5); // Dentro del rango de entrada
        when(horarioRepository.findByHoraEnRango(horaActual))
                .thenReturn(Arrays.asList(horarioEntrada));

        // When
        TipoMarcacion resultado = horarioService.determinarTipoMarcacion(horaActual);

        // Then
        assertEquals(TipoMarcacion.ENTRADA, resultado);
        verify(horarioRepository).findByHoraEnRango(horaActual);
    }

    @Test
    void testDeterminarTipoMarcacion_FueraDeRango_HorarioCercano() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 25); // Fuera del rango pero cerca de entrada
        when(horarioRepository.findByHoraEnRango(horaActual))
                .thenReturn(Arrays.asList()); // No hay horarios en rango exacto
        when(horarioRepository.findByActivoTrueOrderByHoraInicio())
                .thenReturn(Arrays.asList(horarioEntrada, horarioSalidaAlmuerzo, horarioRetornoAlmuerzo, horarioSalida));

        // When
        TipoMarcacion resultado = horarioService.determinarTipoMarcacion(horaActual);

        // Then
        assertEquals(TipoMarcacion.ENTRADA, resultado); // Debería ser el más cercano
        verify(horarioRepository).findByHoraEnRango(horaActual);
        verify(horarioRepository).findByActivoTrueOrderByHoraInicio();
    }

    @Test
    void testDeterminarTipoMarcacion_MuyLejosDeHorarios() {
        // Given
        LocalTime horaActual = LocalTime.of(3, 0); // Muy lejos de cualquier horario
        when(horarioRepository.findByHoraEnRango(horaActual))
                .thenReturn(Arrays.asList());
        when(horarioRepository.findByActivoTrueOrderByHoraInicio())
                .thenReturn(Arrays.asList(horarioEntrada, horarioSalidaAlmuerzo, horarioRetornoAlmuerzo, horarioSalida));

        // When
        TipoMarcacion resultado = horarioService.determinarTipoMarcacion(horaActual);

        // Then
        assertEquals(TipoMarcacion.FUERA_HORARIO, resultado);
    }

    @Test
    void testCalcularEstadoMarcacion_Puntual() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 5);
        TipoMarcacion tipoMarcacion = TipoMarcacion.ENTRADA;
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        EstadoMarcacion resultado = horarioService.calcularEstadoMarcacion(horaActual, tipoMarcacion);

        // Then
        assertEquals(EstadoMarcacion.PUNTUAL, resultado);
    }

    @Test
    void testCalcularEstadoMarcacion_Tardanza() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 25); // Después del rango de entrada
        TipoMarcacion tipoMarcacion = TipoMarcacion.ENTRADA;
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        EstadoMarcacion resultado = horarioService.calcularEstadoMarcacion(horaActual, tipoMarcacion);

        // Then
        assertEquals(EstadoMarcacion.TARDANZA, resultado);
    }

    @Test
    void testCalcularEstadoMarcacion_FueraDeHorario() {
        // Given
        LocalTime horaActual = LocalTime.of(3, 0);
        TipoMarcacion tipoMarcacion = TipoMarcacion.FUERA_HORARIO;

        // When
        EstadoMarcacion resultado = horarioService.calcularEstadoMarcacion(horaActual, tipoMarcacion);

        // Then
        assertEquals(EstadoMarcacion.FUERA_HORARIO, resultado);
    }

    @Test
    void testCalcularMinutosDiferencia_DentroDeRango() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 5);
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        int resultado = horarioService.calcularMinutosDiferencia(horaActual, TipoMarcacion.ENTRADA);

        // Then
        assertEquals(0, resultado); // Dentro del rango, no hay diferencia
    }

    @Test
    void testCalcularMinutosDiferencia_Tardanza() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 25); // 5 minutos después del fin del rango
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        int resultado = horarioService.calcularMinutosDiferencia(horaActual, TipoMarcacion.ENTRADA);

        // Then
        assertEquals(5, resultado); // 5 minutos de tardanza
    }

    @Test
    void testCalcularMinutosDiferencia_Temprano() {
        // Given
        LocalTime horaActual = LocalTime.of(7, 40); // 10 minutos antes del inicio del rango
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        int resultado = horarioService.calcularMinutosDiferencia(horaActual, TipoMarcacion.ENTRADA);

        // Then
        assertEquals(10, resultado); // 10 minutos temprano
    }

    @Test
    void testGenerarMensajeMarcacion_Puntual() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 5);
        TipoMarcacion tipoMarcacion = TipoMarcacion.ENTRADA;
        EstadoMarcacion estadoMarcacion = EstadoMarcacion.PUNTUAL;

        // When
        String resultado = horarioService.generarMensajeMarcacion(horaActual, tipoMarcacion, estadoMarcacion);

        // Then
        assertEquals("Entrada registrada a las 08:05, puntual 🎉", resultado);
    }

    @Test
    void testGenerarMensajeMarcacion_Tardanza() {
        // Given
        LocalTime horaActual = LocalTime.of(8, 25);
        TipoMarcacion tipoMarcacion = TipoMarcacion.ENTRADA;
        EstadoMarcacion estadoMarcacion = EstadoMarcacion.TARDANZA;
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        String resultado = horarioService.generarMensajeMarcacion(horaActual, tipoMarcacion, estadoMarcacion);

        // Then
        assertEquals("Entrada registrada, llegaste tarde por 5 min ⏰", resultado);
    }

    @Test
    void testGenerarMensajeMarcacion_FueraDeHorario() {
        // Given
        LocalTime horaActual = LocalTime.of(3, 0);
        TipoMarcacion tipoMarcacion = TipoMarcacion.FUERA_HORARIO;
        EstadoMarcacion estadoMarcacion = EstadoMarcacion.FUERA_HORARIO;

        // When
        String resultado = horarioService.generarMensajeMarcacion(horaActual, tipoMarcacion, estadoMarcacion);

        // Then
        assertEquals("Fuera de horario registrada fuera de horario a las 03:00 ⚠️", resultado);
    }

    @Test
    void testTieneHorariosCompletos_True() {
        // Given
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.SALIDA_ALMUERZO))
                .thenReturn(Optional.of(horarioSalidaAlmuerzo));
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.RETORNO_ALMUERZO))
                .thenReturn(Optional.of(horarioRetornoAlmuerzo));
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.SALIDA))
                .thenReturn(Optional.of(horarioSalida));

        // When
        boolean resultado = horarioService.tieneHorariosCompletos();

        // Then
        assertTrue(resultado);
    }

    @Test
    void testTieneHorariosCompletos_False() {
        // Given
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.SALIDA_ALMUERZO))
                .thenReturn(Optional.empty()); // Falta este horario

        // When
        boolean resultado = horarioService.tieneHorariosCompletos();

        // Then
        assertFalse(resultado);
    }

    @Test
    void testEstaEnHorario_True() {
        // Given
        LocalTime hora = LocalTime.of(8, 5);
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        boolean resultado = horarioService.estaEnHorario(hora, TipoMarcacion.ENTRADA);

        // Then
        assertTrue(resultado);
    }

    @Test
    void testEstaEnHorario_False() {
        // Given
        LocalTime hora = LocalTime.of(8, 25);
        when(horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA))
                .thenReturn(Optional.of(horarioEntrada));

        // When
        boolean resultado = horarioService.estaEnHorario(hora, TipoMarcacion.ENTRADA);

        // Then
        assertFalse(resultado);
    }

    @Test
    void testCreate_ValidHorario() {
        // Given
        HorarioDTO horarioDTO = new HorarioDTO();
        horarioDTO.setNombre("Nuevo Horario");
        horarioDTO.setHoraInicio(LocalTime.of(9, 0));
        horarioDTO.setHoraFin(LocalTime.of(9, 30));
        horarioDTO.setTipo(TipoMarcacion.ENTRADA);

        when(horarioRepository.existsOverlappingHorario(any(), any(), any(), any()))
                .thenReturn(false);
        when(horarioRepository.save(any(Horario.class)))
                .thenReturn(horarioEntrada);

        // When
        HorarioDTO resultado = horarioService.create(horarioDTO);

        // Then
        assertNotNull(resultado);
        verify(horarioRepository).save(any(Horario.class));
    }

    @Test
    void testCreate_OverlappingHorario() {
        // Given
        HorarioDTO horarioDTO = new HorarioDTO();
        horarioDTO.setNombre("Horario Solapado");
        horarioDTO.setHoraInicio(LocalTime.of(8, 0));
        horarioDTO.setHoraFin(LocalTime.of(8, 30));
        horarioDTO.setTipo(TipoMarcacion.ENTRADA);

        when(horarioRepository.existsOverlappingHorario(any(), any(), any(), any()))
                .thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> horarioService.create(horarioDTO));
        assertTrue(exception.getMessage().contains("se solapa"));
    }

    @Test
    void testCreate_InvalidTimeRange() {
        // Given
        HorarioDTO horarioDTO = new HorarioDTO();
        horarioDTO.setNombre("Horario Inválido");
        horarioDTO.setHoraInicio(LocalTime.of(9, 0));
        horarioDTO.setHoraFin(LocalTime.of(8, 0)); // Hora fin antes que hora inicio
        horarioDTO.setTipo(TipoMarcacion.ENTRADA);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> horarioService.create(horarioDTO));
        assertTrue(exception.getMessage().contains("hora de inicio debe ser menor"));
    }
}