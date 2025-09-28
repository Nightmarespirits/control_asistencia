package com.asistencia.repository;

import com.asistencia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email especificado
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios activos
     */
    List<Usuario> findByActivoTrue();
    
    /**
     * Busca un usuario activo por nombre de usuario
     */
    Optional<Usuario> findByUsernameAndActivoTrue(String username);
    
    /**
     * Busca usuarios por nombre de usuario (búsqueda parcial)
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<Usuario> findByUsernameContainingIgnoreCase(@Param("username") String username);
    
    /**
     * Cuenta usuarios activos
     */
    long countByActivoTrue();
}