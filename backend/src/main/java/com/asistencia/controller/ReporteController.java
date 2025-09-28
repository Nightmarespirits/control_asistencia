package com.asistencia.controller;

import com.asistencia.dto.ReporteAsistenciaDTO;
import com.asistencia.dto.ReporteRequestDTO;
import com.asistencia.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {
    
    @Autowired
    private ReporteService reporteService;
    
    @PostMapping("/asistencias")
    public ResponseEntity<Page<ReporteAsistenciaDTO>> obtenerReporteAsistencias(
            @Valid @RequestBody ReporteRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReporteAsistenciaDTO> reporte = reporteService.obtenerReporteAsistencias(request, pageable);
        
        return ResponseEntity.ok(reporte);
    }
    
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportarReporteExcel(@Valid @RequestBody ReporteRequestDTO request) {
        try {
            byte[] excelData = reporteService.generarReporteExcel(request);
            
            String filename = generarNombreArchivo("reporte_asistencias", "xlsx", request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportarReportePDF(@Valid @RequestBody ReporteRequestDTO request) {
        try {
            byte[] pdfData = reporteService.generarReportePDF(request);
            
            String filename = generarNombreArchivo("reporte_asistencias", "pdf", request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfData.length);
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String generarNombreArchivo(String base, String extension, ReporteRequestDTO request) {
        StringBuilder filename = new StringBuilder(base);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        if (request.getFechaInicio() != null) {
            filename.append("_").append(request.getFechaInicio().format(formatter));
        }
        
        if (request.getFechaFin() != null) {
            filename.append("_").append(request.getFechaFin().format(formatter));
        }
        
        if (request.getTipoMarcacion() != null && !request.getTipoMarcacion().isEmpty()) {
            filename.append("_").append(request.getTipoMarcacion().toLowerCase());
        }
        
        filename.append(".").append(extension);
        
        return filename.toString();
    }
}