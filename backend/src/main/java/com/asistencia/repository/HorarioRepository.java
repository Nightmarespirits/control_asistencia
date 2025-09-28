package com.asistencia.repository;

import com.asistencia.entity.Horario;
import com.asistencia.entity.TipoMarcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    
    /**
     * Busca horarios activos
     */
    List<Horario> findByActivoTrueOrderByHoraInicio();
    
    /**
     * Busca horario por tipo de marcación
     */
    Optional<Horario> findByTipoAndActivoTrue(TipoMarcacion tipo);
    
    /**
     * Busca todos los horarios por tipo
     */
    List<Horario> findByTipoOrderByHoraInicio(TipoMarcacion tipo);
    
    /**
     * Busca horarios que contengan una hora específica
     */
    @Query("SELECT h FROM Horario h WHERE h.activo = true AND " +
           "h.horaInicio <= :hora AND h.horaFin >= :hora")
    List<Horario> findByHoraEnRango(@Param("hora") LocalTime hora);
    
    /**
     * Busca el horario más apropiado para una hora específica
     */
    @Query("SELECT h FROM Horario h WHERE h.activo = true AND " +
           "h.horaInicio <= :hora AND h.horaFin >= :hora " +
           "ORDER BY h.horaInicio ASC")
    Optional<Horario> findBestMatchForTime(@Param("hora") LocalTime hora);
    
    /**
     * Verifica si existe solapamiento de horarios para un tipo específico
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h WHERE h.activo = true AND " +
           "h.tipo = :tipo AND h.id != :excludeId AND " +
           "((h.horaInicio <= :horaInicio AND h.horaFin >= :horaInicio) OR " +
           "(h.horaInicio <= :horaFin AND h.horaFin >= :horaFin) OR " +
           "(h.horaInicio >= :horaInicio AND h.horaFin <= :horaFin))")
    boolean existsOverlappingHorario(
            @Param("tipo") TipoMarcacion tipo,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excludeId") Long excludeId);
    
    /**
     * Busca horarios ordenados por hora de inicio
     */
    List<Horario> findAllByOrderByHoraInicio();
    
    /**
     * Cuenta horarios activos
     */
    long countByActivoTrue();
    
    /**
     * Busca horarios por nombre (búsqueda parcial)
     */
    @Query("SELECT h FROM Horario h WHERE h.activo = true AND " +
           "LOWER(h.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Horario> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
}