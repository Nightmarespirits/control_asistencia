package com.asistencia.repository;

import com.asistencia.entity.Asistencia;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.TipoMarcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long>, JpaSpecificationExecutor<Asistencia> {
    
    /**
     * Busca asistencias por empleado
     */
    List<Asistencia> findByEmpleadoOrderByFechaHoraDesc(Empleado empleado);
    
    /**
     * Busca asistencias por empleado en un rango de fechas
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado = :empleado AND " +
           "a.fechaHora BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaHora DESC")
    List<Asistencia> findByEmpleadoAndFechaHoraBetween(
            @Param("empleado") Empleado empleado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca asistencias en un rango de fechas
     */
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByFechaHoraBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca asistencias por tipo de marcación
     */
    List<Asistencia> findByTipoOrderByFechaHoraDesc(TipoMarcacion tipo);
    
    /**
     * Busca asistencias por empleado y tipo
     */
    List<Asistencia> findByEmpleadoAndTipoOrderByFechaHoraDesc(Empleado empleado, TipoMarcacion tipo);
    
    /**
     * Busca la última marcación de un empleado
     */
    Optional<Asistencia> findFirstByEmpleadoOrderByFechaHoraDesc(Empleado empleado);
    
    /**
     * Busca la última marcación de un empleado por tipo
     */
    Optional<Asistencia> findFirstByEmpleadoAndTipoOrderByFechaHoraDesc(Empleado empleado, TipoMarcacion tipo);
    
    /**
     * Verifica si existe una marcación reciente del mismo tipo (para evitar duplicados)
     */
    @Query("SELECT COUNT(a) > 0 FROM Asistencia a WHERE a.empleado = :empleado AND " +
           "a.tipo = :tipo AND a.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    boolean existsRecentMarcacion(
            @Param("empleado") Empleado empleado,
            @Param("tipo") TipoMarcacion tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca asistencias del día actual por empleado
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado = :empleado AND " +
           "a.fechaHora >= :inicioDelDia AND a.fechaHora < :finDelDia ORDER BY a.fechaHora ASC")
    List<Asistencia> findByEmpleadoAndToday(
            @Param("empleado") Empleado empleado,
            @Param("inicioDelDia") LocalDateTime inicioDelDia,
            @Param("finDelDia") LocalDateTime finDelDia);
    
    /**
     * Cuenta asistencias por empleado en un rango de fechas
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.empleado = :empleado AND " +
           "a.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    long countByEmpleadoAndFechaHoraBetween(
            @Param("empleado") Empleado empleado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca asistencias con filtros múltiples para reportes
     */
    @Query("SELECT a FROM Asistencia a WHERE " +
           "(:empleado IS NULL OR a.empleado = :empleado) AND " +
           "(:tipo IS NULL OR a.tipo = :tipo) AND " +
           "a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fechaHora DESC")
    List<Asistencia> findWithFilters(
            @Param("empleado") Empleado empleado,
            @Param("tipo") TipoMarcacion tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}