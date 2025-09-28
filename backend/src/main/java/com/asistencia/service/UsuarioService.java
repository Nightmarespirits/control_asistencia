package com.asistencia.service;

import com.asistencia.dto.UsuarioDTO;
import com.asistencia.entity.Usuario;
import com.asistencia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Obtiene todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAllActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca un usuario por nombre de usuario
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca un usuario activo por nombre de usuario
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByUsernameAndActivo(String username) {
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca un usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca usuarios por nombre de usuario (búsqueda parcial)
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findByUsernameContaining(String username) {
        return usuarioRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un nuevo usuario
     */
    public UsuarioDTO create(UsuarioDTO usuarioDTO, String password) {
        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + usuarioDTO.getUsername());
        }
        
        // Validar que el email no exista (si se proporciona)
        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuarioDTO.getEmail());
            }
        }
        
        Usuario usuario = convertToEntity(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertToDTO(savedUsuario);
    }
    
    /**
     * Actualiza un usuario existente
     */
    public UsuarioDTO update(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        // Validar que el username no esté en uso por otro usuario
        Optional<Usuario> usuarioConMismoUsername = usuarioRepository.findByUsername(usuarioDTO.getUsername());
        if (usuarioConMismoUsername.isPresent() && !usuarioConMismoUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro usuario con el nombre: " + usuarioDTO.getUsername());
        }
        
        // Validar que el email no esté en uso por otro usuario (si se proporciona)
        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()) {
            Optional<Usuario> usuarioConMismoEmail = usuarioRepository.findByEmail(usuarioDTO.getEmail());
            if (usuarioConMismoEmail.isPresent() && !usuarioConMismoEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro usuario con el email: " + usuarioDTO.getEmail());
            }
        }
        
        // Actualizar campos
        usuarioExistente.setUsername(usuarioDTO.getUsername());
        usuarioExistente.setEmail(usuarioDTO.getEmail());
        
        if (usuarioDTO.getActivo() != null) {
            usuarioExistente.setActivo(usuarioDTO.getActivo());
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuarioExistente);
        return convertToDTO(updatedUsuario);
    }
    
    /**
     * Actualiza la contraseña de un usuario
     */
    public void updatePassword(Long id, String newPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }
    
    /**
     * Elimina un usuario (soft delete)
     */
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
    
    /**
     * Elimina permanentemente un usuario
     */
    public void deletePhysically(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    
    /**
     * Verifica si existe un usuario con el nombre especificado
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }
    
    /**
     * Verifica si existe un usuario con el email especificado
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    /**
     * Cuenta usuarios activos
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return usuarioRepository.countByActivoTrue();
    }
    
    /**
     * Valida las credenciales de un usuario
     */
    @Transactional(readOnly = true)
    public boolean validateCredentials(String username, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByUsernameAndActivoTrue(username);
        if (usuario.isPresent()) {
            return passwordEncoder.matches(password, usuario.get().getPassword());
        }
        return false;
    }
    
    // Métodos de conversión
    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        return dto;
    }
    
    private Usuario convertToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setActivo(dto.getActivo());
        return usuario;
    }
    
    /**
     * Obtiene la entidad Usuario por username (para uso interno)
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findEntityByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    /**
     * Obtiene la entidad Usuario activo por username (para uso interno)
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findActiveEntityByUsername(String username) {
        return usuarioRepository.findByUsernameAndActivoTrue(username);
    }
}