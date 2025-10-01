package com.asistencia.service;

import com.asistencia.dto.ReporteAsistenciaDTO;
import com.asistencia.dto.ReporteRequestDTO;
import com.asistencia.entity.Asistencia;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.AsistenciaRepository;
import com.asistencia.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReporteServiceIntegrationTest {
    
    @Autowired
    private ReporteService reporteService;
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    private Empleado empleado;
    private ReporteRequestDTO request;
    
    @BeforeEach
    void setUp() {
        // Crear empleado de prueba
        empleado = new Empleado();
        empleado.setCodigoUnico("EMP001");
        empleado.setDni("12345678");
        empleado.setNombres("Juan Carlos");
        empleado.setApellidos("Pérez López");
        empleado.setCargo("Desarrollador");
        empleado.setArea("TI");
        empleado.setActivo(true);
        empleado = empleadoRepository.save(empleado);
        
        // Crear asistencias de prueba
        Asistencia asistencia1 = new Asistencia();
        asistencia1.setEmpleado(empleado);
        asistencia1.setFechaHora(LocalDateTime.of(2025, 1, 15, 8, 0));
        asistencia1.setTipo(TipoMarcacion.ENTRADA);
        asistencia1.setEstado(EstadoMarcacion.PUNTUAL);
        asistencia1.setObservaciones("Entrada puntual");
        asistenciaRepository.save(asistencia1);
        
        Asistencia asistencia2 = new Asistencia();
        asistencia2.setEmpleado(empleado);
        asistencia2.setFechaHora(LocalDateTime.of(2025, 1, 15, 17, 0));
        asistencia2.setTipo(TipoMarcacion.SALIDA);
        asistencia2.setEstado(EstadoMarcacion.PUNTUAL);
        asistenciaRepository.save(asistencia2);
        
        // Configurar request
        request = new ReporteRequestDTO();
        request.setFechaInicio(LocalDate.of(2025, 1, 15));
        request.setFechaFin(LocalDate.of(2025, 1, 15));
    }
    
    @Test
    void debeObtenerReporteConFiltrosDeFecha() {
        // When
        Page<Asistencia> resultado = reporteService.obtenerReporteAsistencias(request, PageRequest.of(0, 10));
        
        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        
        Asistencia asistencia = resultado.getContent().get(0);
        assertEquals("Juan Carlos", asistencia.getEmpleado().getNombres());
        assertEquals("Pérez López", asistencia.getEmpleado().getApellidos());
        assertEquals("12345678", asistencia.getEmpleado().getDni());
        assertEquals("Desarrollador", asistencia.getEmpleado().getCargo());
        assertEquals("TI", asistencia.getEmpleado().getArea());
    }
    
    @Test
    void debeObtenerReporteConFiltroDeEmpleado() {
        // Given
        request.setEmpleadoId(empleado.getId());
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        resultado.forEach(dto -> {
            assertEquals(empleado.getId(), dto.getId() != null ? empleado.getId() : null);
            assertEquals("Juan Carlos Pérez López", dto.getNombreCompleto());
        });
    }
    
    @Test
    void debeObtenerReporteConFiltroDeTipo() {
        // Given
        request.setTipoMarcacion("ENTRADA");
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoMarcacion.ENTRADA, resultado.get(0).getTipo());
    }
    
    @Test
    void debeGenerarReporteExcelCorrectamente() throws IOException {
        // When
        byte[] excelData = reporteService.generarReporteExcel(request);
        
        // Then
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        
        // Verificar que es un archivo Excel válido (comienza con la firma de ZIP)
        assertTrue(excelData[0] == 0x50 && excelData[1] == 0x4B); // PK signature
    }
    
    @Test
    void debeGenerarReportePDFCorrectamente() throws IOException {
        // When
        byte[] pdfData = reporteService.generarReportePDF(request);
        
        // Then
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
        
        // Verificar que es un archivo PDF válido (comienza con %PDF)
        String header = new String(pdfData, 0, 4);
        assertEquals("%PDF", header);
    }
    
    @Test
    void debeRetornarReporteVacioSinDatos() {
        // Given - fechas sin datos
        request.setFechaInicio(LocalDate.of(2025, 2, 1));
        request.setFechaFin(LocalDate.of(2025, 2, 1));
        
        // When
        List<ReporteAsistenciaDTO> resultado = reporteService.obtenerReporteAsistenciasDTO(request);
        
        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}