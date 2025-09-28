package com.asistencia.service;

import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HorarioServiceIntegrationTest {

    @Autowired
    private HorarioService horarioService;

    @Test
    void testDeterminarTipoMarcacion_ConHorariosReales() {
        // Test con hora de entrada (8:00 AM)
        LocalTime horaEntrada = LocalTime.of(8, 0);
        TipoMarcacion tipoEntrada = horarioService.determinarTipoMarcacion(horaEntrada);
        assertEquals(TipoMarcacion.ENTRADA, tipoEntrada);

        // Test con hora de salida a almuerzo (12:45 PM)
        LocalTime horaSalidaAlmuerzo = LocalTime.of(12, 45);
        TipoMarcacion tipoSalidaAlmuerzo = horarioService.determinarTipoMarcacion(horaSalidaAlmuerzo);
        assertEquals(TipoMarcacion.SALIDA_ALMUERZO, tipoSalidaAlmuerzo);

        // Test con hora de retorno de almuerzo (2:15 PM)
        LocalTime horaRetornoAlmuerzo = LocalTime.of(14, 15);
        TipoMarcacion tipoRetornoAlmuerzo = horarioService.determinarTipoMarcacion(horaRetornoAlmuerzo);
        assertEquals(TipoMarcacion.RETORNO_ALMUERZO, tipoRetornoAlmuerzo);

        // Test con hora de salida (5:45 PM)
        LocalTime horaSalida = LocalTime.of(17, 45);
        TipoMarcacion tipoSalida = horarioService.determinarTipoMarcacion(horaSalida);
        assertEquals(TipoMarcacion.SALIDA, tipoSalida);
    }

    @Test
    void testCalcularEstadoMarcacion_ConHorariosReales() {
        // Test marcación puntual de entrada
        LocalTime horaEntradaPuntual = LocalTime.of(8, 5);
        EstadoMarcacion estadoPuntual = horarioService.calcularEstadoMarcacion(
                horaEntradaPuntual, TipoMarcacion.ENTRADA);
        assertEquals(EstadoMarcacion.PUNTUAL, estadoPuntual);

        // Test marcación tardía de entrada
        LocalTime horaEntradaTarde = LocalTime.of(8, 30);
        EstadoMarcacion estadoTarde = horarioService.calcularEstadoMarcacion(
                horaEntradaTarde, TipoMarcacion.ENTRADA);
        assertEquals(EstadoMarcacion.TARDANZA, estadoTarde);

        // Test marcación fuera de horario
        LocalTime horaFueraHorario = LocalTime.of(3, 0);
        EstadoMarcacion estadoFuera = horarioService.calcularEstadoMarcacion(
                horaFueraHorario, TipoMarcacion.FUERA_HORARIO);
        assertEquals(EstadoMarcacion.FUERA_HORARIO, estadoFuera);
    }

    @Test
    void testGenerarMensajeMarcacion_ConHorariosReales() {
        // Test mensaje de entrada puntual
        LocalTime horaEntrada = LocalTime.of(8, 5);
        String mensajePuntual = horarioService.generarMensajeMarcacion(
                horaEntrada, TipoMarcacion.ENTRADA, EstadoMarcacion.PUNTUAL);
        assertTrue(mensajePuntual.contains("puntual 🎉"));

        // Test mensaje de tardanza
        LocalTime horaTarde = LocalTime.of(8, 30);
        String mensajeTarde = horarioService.generarMensajeMarcacion(
                horaTarde, TipoMarcacion.ENTRADA, EstadoMarcacion.TARDANZA);
        assertTrue(mensajeTarde.contains("tarde"));
        assertTrue(mensajeTarde.contains("⏰"));

        // Test mensaje fuera de horario
        LocalTime horaFuera = LocalTime.of(3, 0);
        String mensajeFuera = horarioService.generarMensajeMarcacion(
                horaFuera, TipoMarcacion.FUERA_HORARIO, EstadoMarcacion.FUERA_HORARIO);
        assertTrue(mensajeFuera.contains("fuera de horario"));
        assertTrue(mensajeFuera.contains("⚠️"));
    }

    @Test
    void testTieneHorariosCompletos_ConDatosIniciales() {
        // Verificar que los horarios por defecto están configurados
        boolean tieneHorariosCompletos = horarioService.tieneHorariosCompletos();
        assertTrue(tieneHorariosCompletos, "Deberían existir horarios para todos los tipos básicos");
    }

    @Test
    void testCalcularMinutosDiferencia_ConHorariosReales() {
        // Test dentro del rango (sin diferencia)
        LocalTime horaEnRango = LocalTime.of(8, 5);
        int diferenciaCero = horarioService.calcularMinutosDiferencia(horaEnRango, TipoMarcacion.ENTRADA);
        assertEquals(0, diferenciaCero);

        // Test fuera del rango (con diferencia)
        LocalTime horaTarde = LocalTime.of(8, 30);
        int diferenciaTarde = horarioService.calcularMinutosDiferencia(horaTarde, TipoMarcacion.ENTRADA);
        assertTrue(diferenciaTarde > 0, "Debería haber diferencia positiva para llegada tarde");
    }

    @Test
    void testEstaEnHorario_ConHorariosReales() {
        // Test hora dentro del horario de entrada
        LocalTime horaEnHorario = LocalTime.of(8, 5);
        boolean estaEnHorario = horarioService.estaEnHorario(horaEnHorario, TipoMarcacion.ENTRADA);
        assertTrue(estaEnHorario);

        // Test hora fuera del horario de entrada
        LocalTime horaFueraHorario = LocalTime.of(8, 30);
        boolean estaFueraHorario = horarioService.estaEnHorario(horaFueraHorario, TipoMarcacion.ENTRADA);
        assertFalse(estaFueraHorario);
    }
}