package com.asistencia.repository;

import com.asistencia.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    
    /**
     * Busca un empleado por su DNI
     */
    Optional<Empleado> findByDni(String dni);
    
    /**
     * Busca un empleado por su código único
     */
    Optional<Empleado> findByCodigoUnico(String codigoUnico);
    
    /**
     * Verifica si existe un empleado con el DNI especificado
     */
    boolean existsByDni(String dni);
    
    /**
     * Verifica si existe un empleado con el código único especificado
     */
    boolean existsByCodigoUnico(String codigoUnico);
    
    /**
     * Busca empleados activos
     */
    List<Empleado> findByActivoTrue();
    
    /**
     * Busca empleados por área
     */
    List<Empleado> findByAreaAndActivoTrue(String area);
    
    /**
     * Busca empleados por cargo
     */
    List<Empleado> findByCargoAndActivoTrue(String cargo);
    
    /**
     * Busca empleados por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT e FROM Empleado e WHERE e.activo = true AND " +
           "(LOWER(e.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Empleado> findByNombreOrApellidoContainingIgnoreCase(@Param("termino") String termino);
    
    /**
     * Cuenta empleados activos
     */
    long countByActivoTrue();
    
    /**
     * Cuenta empleados por área
     */
    long countByAreaAndActivoTrue(String area);
}