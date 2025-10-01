package com.asistencia.controller;

import com.asistencia.dto.HorarioDTO;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.service.HorarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorarioController.class)
class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HorarioService horarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private HorarioDTO horarioDTO;

    @BeforeEach
    void setUp() {
        horarioDTO = new HorarioDTO();
        horarioDTO.setId(1L);
        horarioDTO.setNombre("Horario de Entrada");
        horarioDTO.setHoraInicio(LocalTime.of(8, 0));
        horarioDTO.setHoraFin(LocalTime.of(8, 30));
        horarioDTO.setTipo(TipoMarcacion.ENTRADA);
        horarioDTO.setActivo(true);
    }

    @Test
    void getAllHorarios_ShouldReturnHorariosList() throws Exception {
        List<HorarioDTO> horarios = Arrays.asList(horarioDTO);
        when(horarioService.findAll()).thenReturn(horarios);

        mockMvc.perform(get("/api/admin/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getHorariosActivos_ShouldReturnActiveHorarios() throws Exception {
        List<HorarioDTO> horarios = Arrays.asList(horarioDTO);
        when(horarioService.findAllActivos()).thenReturn(horarios);

        mockMvc.perform(get("/api/admin/horarios/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getHorarioById_WhenExists_ShouldReturnHorario() throws Exception {
        when(horarioService.findById(1L)).thenReturn(Optional.of(horarioDTO));

        mockMvc.perform(get("/api/admin/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Horario de Entrada"));
    }

    @Test
    void getHorarioById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(horarioService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/horarios/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.mensaje").value("Horario no encontrado"));
    }

    @Test
    void createHorario_WithValidData_ShouldCreateHorario() throws Exception {
        when(horarioService.create(any(HorarioDTO.class))).thenReturn(horarioDTO);

        mockMvc.perform(post("/api/admin/horarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(horarioDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.mensaje").value("Horario creado exitosamente"))
                .andExpect(jsonPath("$.data.nombre").value("Horario de Entrada"));
    }

    @Test
    void createHorario_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        when(horarioService.create(any(HorarioDTO.class)))
                .thenThrow(new IllegalArgumentException("La hora de inicio debe ser menor que la hora de fin"));

        mockMvc.perform(post("/api/admin/horarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(horarioDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.mensaje").value("La hora de inicio debe ser menor que la hora de fin"));
    }

    @Test
    void updateHorario_WithValidData_ShouldUpdateHorario() throws Exception {
        when(horarioService.update(eq(1L), any(HorarioDTO.class))).thenReturn(horarioDTO);

        mockMvc.perform(put("/api/admin/horarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(horarioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.mensaje").value("Horario actualizado exitosamente"));
    }

    @Test
    void deleteHorario_WhenExists_ShouldDeleteHorario() throws Exception {
        mockMvc.perform(delete("/api/admin/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.mensaje").value("Horario eliminado exitosamente"));
    }

    @Test
    void checkOverlap_WithNoOverlap_ShouldReturnFalse() throws Exception {
        when(horarioService.existsOverlappingHorario(
                eq(TipoMarcacion.ENTRADA), 
                eq(LocalTime.of(8, 0)), 
                eq(LocalTime.of(8, 30)), 
                isNull()))
                .thenReturn(false);

        mockMvc.perform(get("/api/admin/horarios/check-overlap")
                .param("tipo", "ENTRADA")
                .param("horaInicio", "08:00")
                .param("horaFin", "08:30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.hasOverlap").value(false))
                .andExpect(jsonPath("$.mensaje").value("No hay solapamientos"));
    }

    @Test
    void checkOverlap_WithOverlap_ShouldReturnTrue() throws Exception {
        when(horarioService.existsOverlappingHorario(
                eq(TipoMarcacion.ENTRADA), 
                eq(LocalTime.of(8, 0)), 
                eq(LocalTime.of(8, 30)), 
                isNull()))
                .thenReturn(true);

        mockMvc.perform(get("/api/admin/horarios/check-overlap")
                .param("tipo", "ENTRADA")
                .param("horaInicio", "08:00")
                .param("horaFin", "08:30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.hasOverlap").value(true))
                .andExpect(jsonPath("$.mensaje").value("Existe un solapamiento con otro horario del mismo tipo"));
    }

    @Test
    void getHorariosByTipo_WhenExists_ShouldReturnHorario() throws Exception {
        when(horarioService.findByTipo(TipoMarcacion.ENTRADA)).thenReturn(Optional.of(horarioDTO));

        mockMvc.perform(get("/api/admin/horarios/tipo/ENTRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tipo").value("ENTRADA"));
    }

    @Test
    void searchHorarios_ShouldReturnMatchingHorarios() throws Exception {
        List<HorarioDTO> horarios = Arrays.asList(horarioDTO);
        when(horarioService.findByNombre("Entrada")).thenReturn(horarios);

        mockMvc.perform(get("/api/admin/horarios/buscar")
                .param("q", "Entrada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getEstadisticas_ShouldReturnStatistics() throws Exception {
        when(horarioService.countActivos()).thenReturn(5L);
        when(horarioService.findAll()).thenReturn(Arrays.asList(horarioDTO));
        when(horarioService.tieneHorariosCompletos()).thenReturn(true);

        mockMvc.perform(get("/api/admin/horarios/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalActivos").value(5))
                .andExpect(jsonPath("$.data.horariosCompletos").value(true));
    }
}