package com.asistencia.service;

import com.asistencia.dto.ReporteAsistenciaDTO;
import com.asistencia.dto.ReporteRequestDTO;
import com.asistencia.entity.Asistencia;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.AsistenciaRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {
    
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    public Page<ReporteAsistenciaDTO> obtenerReporteAsistencias(ReporteRequestDTO request, Pageable pageable) {
        Specification<Asistencia> spec = createSpecification(request);
        Page<Asistencia> asistencias = asistenciaRepository.findAll(spec, pageable);
        
        return asistencias.map(this::convertToReporteDTO);
    }
    
    public List<ReporteAsistenciaDTO> obtenerReporteAsistencias(ReporteRequestDTO request) {
        Specification<Asistencia> spec = createSpecification(request);
        List<Asistencia> asistencias = asistenciaRepository.findAll(spec);
        
        return asistencias.stream()
                .map(this::convertToReporteDTO)
                .collect(Collectors.toList());
    }
    
    private Specification<Asistencia> createSpecification(ReporteRequestDTO request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por rango de fechas
            if (request.getFechaInicio() != null) {
                LocalDateTime fechaInicioDateTime = request.getFechaInicio().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaHora"), fechaInicioDateTime));
            }
            
            if (request.getFechaFin() != null) {
                LocalDateTime fechaFinDateTime = request.getFechaFin().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaHora"), fechaFinDateTime));
            }
            
            // Filtro por empleado
            if (request.getEmpleadoId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("empleado").get("id"), request.getEmpleadoId()));
            }
            
            // Filtro por tipo de marcación
            if (request.getTipoMarcacion() != null && !request.getTipoMarcacion().isEmpty()) {
                try {
                    TipoMarcacion tipo = TipoMarcacion.valueOf(request.getTipoMarcacion());
                    predicates.add(criteriaBuilder.equal(root.get("tipo"), tipo));
                } catch (IllegalArgumentException e) {
                    // Ignorar tipo inválido
                }
            }
            
            // Ordenar por fecha descendente
            query.orderBy(criteriaBuilder.desc(root.get("fechaHora")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private ReporteAsistenciaDTO convertToReporteDTO(Asistencia asistencia) {
        return new ReporteAsistenciaDTO(
                asistencia.getId(),
                asistencia.getEmpleado().getNombres(),
                asistencia.getEmpleado().getApellidos(),
                asistencia.getEmpleado().getDni(),
                asistencia.getEmpleado().getCargo(),
                asistencia.getEmpleado().getArea(),
                asistencia.getFechaHora(),
                asistencia.getTipo(),
                asistencia.getEstado(),
                asistencia.getObservaciones()
        );
    }
    
    public byte[] generarReporteExcel(ReporteRequestDTO request) throws IOException {
        List<ReporteAsistenciaDTO> datos = obtenerReporteAsistencias(request);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Reporte de Asistencias");
            
            // Crear estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Empleado", "DNI", "Cargo", "Área", "Fecha y Hora", "Tipo", "Estado", "Observaciones"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            
            for (ReporteAsistenciaDTO dto : datos) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(dto.getId());
                row.createCell(1).setCellValue(dto.getNombreCompleto());
                row.createCell(2).setCellValue(dto.getEmpleadoDni());
                row.createCell(3).setCellValue(dto.getEmpleadoCargo());
                row.createCell(4).setCellValue(dto.getEmpleadoArea());
                row.createCell(5).setCellValue(dto.getFechaHora().format(formatter));
                row.createCell(6).setCellValue(dto.getTipo().toString());
                row.createCell(7).setCellValue(dto.getEstado().toString());
                row.createCell(8).setCellValue(dto.getObservaciones() != null ? dto.getObservaciones() : "");
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    public byte[] generarReportePDF(ReporteRequestDTO request) throws IOException {
        List<ReporteAsistenciaDTO> datos = obtenerReporteAsistencias(request);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Título del reporte
            document.add(new Paragraph("Reporte de Asistencias")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold());
            
            // Información del filtro
            String periodo = "";
            if (request.getFechaInicio() != null && request.getFechaFin() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                periodo = "Período: " + request.getFechaInicio().format(dateFormatter) + 
                         " - " + request.getFechaFin().format(dateFormatter);
            }
            
            if (!periodo.isEmpty()) {
                document.add(new Paragraph(periodo)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12));
            }
            
            document.add(new Paragraph("\n"));
            
            // Crear tabla
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 3, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Encabezados
            String[] headers = {"ID", "Empleado", "DNI", "Cargo", "Área", "Fecha y Hora", "Tipo", "Estado"};
            
            for (String header : headers) {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(header).setBold()));
            }
            
            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (ReporteAsistenciaDTO dto : datos) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getId().toString())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getNombreCompleto())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getEmpleadoDni())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getEmpleadoCargo())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getEmpleadoArea())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getFechaHora().format(formatter))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getTipo().toString())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dto.getEstado().toString())));
            }
            
            document.add(table);
            
            // Pie de página
            document.add(new Paragraph("\nGenerado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));
            
            document.close();
            return outputStream.toByteArray();
        }
    }
}