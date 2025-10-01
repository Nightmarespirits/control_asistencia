package com.asistencia.service;

import com.asistencia.dto.ReporteAsistenciaDTO;
import com.asistencia.dto.ReporteRequestDTO;
import com.asistencia.entity.Asistencia;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.AsistenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
    
    @Mock
    private AsistenciaRepository asistenciaRepository;
    
    @InjectMocks
    private ReporteService reporteService;
    
    private Empleado empleado;
    private Asistencia asistencia1;
    private Asistencia asistencia2;
    private ReporteRequestDTO request;
    
    @BeforeEach
    void setUp() {
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombres("Juan Carlos");
        empleado.setApellidos("Pérez López");
        empleado.setDni("12345678");
        empleado.setCargo("Desarrollador");
        empleado.setArea("TI");
        
        asistencia1 = new Asistencia();
        asistencia1.setId(1L);
        asistencia1.setEmpleado(empleado);
        asistencia1.setFechaHora(LocalDateTime.of(2025, 1, 15, 8, 0));
        asistencia1.setTipo(TipoMarcacion.ENTRADA);
        asistencia1.setEstado(EstadoMarcacion.PUNTUAL);
        asistencia1.setObservaciones("Entrada puntual");
        
        asistencia2 = new Asistencia();
        asistencia2.setId(2L);
        asistencia2.setEmpleado(empleado);
        asistencia2.setFechaHora(LocalDateTime.of(2025, 1, 15, 17, 0));
        asistencia2.setTipo(TipoMarcacion.SALIDA);
        asistencia2.setEstado(EstadoMarcacion.PUNTUAL);
        
        request = new ReporteRequestDTO();
        request.setFechaInicio(LocalDate.of(2025, 1, 15));
        request.setFechaFin(LocalDate.of(2025, 1, 15));
    }
    
    @Test
    void debeObtenerReporteAsistenciasConPaginacion() {
        // Given
        List<Asistencia> asistencias = Arrays.asList(asistencia1, asistencia2);
        Page<Asistencia> page = new PageImpl<>(asistencias);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(asistenciaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        
        // When
        Page<Asistencia> resultado = reporteService.obtenerReporteAsistencias(request, pageable);
        
        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        
        Asistencia asistencia = resultado.getContent().get(0);
        assertEquals("Juan Carlos", asistencia.getEmpleado().getNombres());
        assertEquals("Pérez López", asistencia.getEmpleado().getApellidos());
        assertEquals("12345678", asistencia.getEmpleado().getDni());
        assertEquals(TipoMarcacion.ENTRADA, asistencia.getTipo());
        
        verify(asistenciaRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    void debeObtenerReporteAsistenciasSinPaginacion() {
        // Given
        List<Asistencia> asistencias = Arrays.asList(asistencia1, asistencia2);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        ReporteAsistenciaDTO dto1 = resultado.get(0);
        assertEquals("Juan Carlos Pérez López", dto1.getNombreCompleto());
        assertEquals("Desarrollador", dto1.getEmpleadoCargo());
        assertEquals("TI", dto1.getEmpleadoArea());
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeGenerarReporteExcel() throws IOException {
        // Given
        List<Asistencia> asistencias = Arrays.asList(asistencia1, asistencia2);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        byte[] excelData = reporteService.generarReporteExcel(request);
        
        // Then
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeGenerarReportePDF() throws IOException {
        // Given
        List<Asistencia> asistencias = Arrays.asList(asistencia1, asistencia2);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        byte[] pdfData = reporteService.generarReportePDF(request);
        
        // Then
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeFiltrarPorEmpleado() {
        // Given
        request.setEmpleadoId(1L);
        List<Asistencia> asistencias = Arrays.asList(asistencia1);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeFiltrarPorTipoMarcacion() {
        // Given
        request.setTipoMarcacion("ENTRADA");
        List<Asistencia> asistencias = Arrays.asList(asistencia1);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoMarcacion.ENTRADA, resultado.get(0).getTipo());
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeIgnorarTipoMarcacionInvalido() {
        // Given
        request.setTipoMarcacion("TIPO_INVALIDO");
        List<Asistencia> asistencias = Arrays.asList(asistencia1, asistencia2);
        
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(asistencias);
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size()); // No se filtra por tipo inválido
        
        verify(asistenciaRepository).findAll(any(Specification.class));
    }
    
    @Test
    void debeGenerarReporteVacio() throws IOException {
        // Given
        when(asistenciaRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList());
        
        // When
        byte[] excelData = reporteService.generarReporteExcel(request);
        byte[] pdfData = reporteService.generarReportePDF(request);
        
        // Then
        assertNotNull(excelData);
        assertNotNull(pdfData);
        assertTrue(excelData.length > 0); // Archivo con solo encabezados
        assertTrue(pdfData.length > 0); // PDF con solo título
        
        verify(asistenciaRepository, times(2)).findAll(any(Specification.class));
    }
}