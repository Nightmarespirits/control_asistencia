package com.asistencia.controller;

import com.asistencia.dto.ReporteAsistenciaDTO;
import com.asistencia.dto.ReporteRequestDTO;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.service.ReporteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ReporteService reporteService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ReporteRequestDTO request;
    private ReporteAsistenciaDTO reporteDTO;
    
    @BeforeEach
    void setUp() {
        request = new ReporteRequestDTO();
        request.setFechaInicio(LocalDate.of(2025, 1, 15));
        request.setFechaFin(LocalDate.of(2025, 1, 15));
        
        reporteDTO = new ReporteAsistenciaDTO();
        reporteDTO.setId(1L);
        reporteDTO.setEmpleadoNombres("Juan Carlos");
        reporteDTO.setEmpleadoApellidos("Pérez López");
        reporteDTO.setEmpleadoDni("12345678");
        reporteDTO.setEmpleadoCargo("Desarrollador");
        reporteDTO.setEmpleadoArea("TI");
        reporteDTO.setFechaHora(LocalDateTime.of(2025, 1, 15, 8, 0));
        reporteDTO.setTipo(TipoMarcacion.ENTRADA);
        reporteDTO.setEstado(EstadoMarcacion.PUNTUAL);
        reporteDTO.setObservaciones("Entrada puntual");
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeObtenerReporteAsistencias() throws Exception {
        // Given
        List<ReporteAsistenciaDTO> reportes = Arrays.asList(reporteDTO);
        Page<ReporteAsistenciaDTO> page = new PageImpl<>(reportes, PageRequest.of(0, 20), 1);
        
        when(reporteService.obtenerReporteAsistencias(any(ReporteRequestDTO.class), any()))
                .thenReturn(page);
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/asistencias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].empleadoNombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.content[0].empleadoApellidos").value("Pérez López"))
                .andExpect(jsonPath("$.content[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeExportarReporteExcel() throws Exception {
        // Given
        byte[] excelData = "fake excel data".getBytes();
        
        when(reporteService.generarReporteExcel(any(ReporteRequestDTO.class)))
                .thenReturn(excelData);
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/export/excel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", 
                        "form-data; name=\"attachment\"; filename=\"reporte_asistencias_20250115_20250115.xlsx\""))
                .andExpect(content().bytes(excelData));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeExportarReportePDF() throws Exception {
        // Given
        byte[] pdfData = "fake pdf data".getBytes();
        
        when(reporteService.generarReportePDF(any(ReporteRequestDTO.class)))
                .thenReturn(pdfData);
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/export/pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", 
                        "form-data; name=\"attachment\"; filename=\"reporte_asistencias_20250115_20250115.pdf\""))
                .andExpect(content().bytes(pdfData));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeValidarFechasObligatorias() throws Exception {
        // Given
        ReporteRequestDTO requestInvalido = new ReporteRequestDTO();
        // No se establecen fechas
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/asistencias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeManejarErrorEnExportacionExcel() throws Exception {
        // Given
        when(reporteService.generarReporteExcel(any(ReporteRequestDTO.class)))
                .thenThrow(new IOException("Error al generar Excel"));
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/export/excel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeManejarErrorEnExportacionPDF() throws Exception {
        // Given
        when(reporteService.generarReportePDF(any(ReporteRequestDTO.class)))
                .thenThrow(new IOException("Error al generar PDF"));
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/export/pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void debeRechazarAccesoSinRolAdmin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/asistencias")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void debeRechazarAccesoSinAutenticacion() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/asistencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void debeGenerarNombreArchivoConFiltros() throws Exception {
        // Given
        request.setTipoMarcacion("ENTRADA");
        byte[] excelData = "fake excel data".getBytes();
        
        when(reporteService.generarReporteExcel(any(ReporteRequestDTO.class)))
                .thenReturn(excelData);
        
        // When & Then
        mockMvc.perform(post("/api/admin/reportes/export/excel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        "form-data; name=\"attachment\"; filename=\"reporte_asistencias_20250115_20250115_entrada.xlsx\""));
    }
}