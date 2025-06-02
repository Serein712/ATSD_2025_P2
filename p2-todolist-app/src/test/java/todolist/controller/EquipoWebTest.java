package todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import todolist.authentication.ManagerUserSession;
import todolist.dto.EquipoData;
import todolist.dto.TareaData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.TareaService;
import todolist.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Metodo para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores

    Map<String, Long> addUsuarioEquiposBD() {
        // Añadimos un usuario a la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setNombre("Richard");
        usuario.setEmail("richard@umh.es");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);

        EquipoData equipo = new EquipoData();
        equipo.setNombre("Richard Team");
        equipo.setId(1L);
        equipo = equipoService.registrar(equipo);

        Map<String, Long> ids = new HashMap<>();
        ids.put("equipoId", equipo.getId());
        ids.put("usuarioId", usuario.getId());
        return ids;
    }

    @Test
    public void listaEquipos() throws Exception {
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/usuarios/" + usuarioId.toString() + "/equipos";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("Listado de equipos de Richard"),
                        containsString("Richard Team")
                ))));
    }

    @Test
    public void formNuevoEquipoTest() throws Exception {
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId.toString() + "/equipos/nuevo";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/equipos";

        this.mockMvc.perform(post(urlPost)
                        .param("nombre", "TestNuevoEquipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y si después consultamos el listado de equipos con una petición
        // GET el HTML contiene el equipo añadido.

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("TestNuevoEquipo"))));
    }

    @Test
    public void unirseAlEquipoTest() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setNombre("no_Richard");
        usuario.setEmail("no_richard@umh.es");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        Long equipoId = addUsuarioEquiposBD().get("equipoId");

        String urlPost = "/usuarios/" + usuario.getId().toString() + "/equipos/" + equipoId + "/unirse";
        String urlRedirect = "/equipos/" + equipoId;

        this.mockMvc.perform(post(urlPost)
                        .param(String.valueOf(usuario.getId()), "id")
                        .param(String.valueOf(equipoId), "id_equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get("/equipos/" + equipoId))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                containsString("no_Richard"),
                                containsString("no_richard@umh.es"))));
    }

    @Test
    public void abandonarEquipoTest() throws Exception {
        //Crear nuevo equipo
        EquipoData equipo = new EquipoData();
        equipo.setNombre("TestAbandonarEquipo");
        equipo.setId(2L);
        equipo = equipoService.registrar(equipo);

        //Añadir Richard al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(),addUsuarioEquiposBD().get("usuarioId"));

        //Crear nuevo usuario no_Richard
        UsuarioData usuario = new UsuarioData();
        usuario.setNombre("no_Richard");
        usuario.setEmail("no_richard@umh.es");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        //Añadir no_Richard al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(),usuario.getId());

        //Comprobar que ambos estan en el equipo
        this.mockMvc.perform(get("/equipos/" + equipo.getId()))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                containsString("Richard"),
                                containsString("no_Richard"))));

        //Abandonar el grupo (cuenta de no_Richard)
        String urlPost = "/usuarios/" + usuario.getId().toString() + "/equipos/" + equipo.getId() + "/abandonar";
        String urlRedirect = "/equipos/" + equipo.getId();

        this.mockMvc.perform(post(urlPost)
                        .param(String.valueOf(usuario.getId()), "id")
                        .param(String.valueOf(equipo.getId()), "id_equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Comprobar que Richard esta en el equipo y no_Richard no
        this.mockMvc.perform(get("/equipos/" + equipo.getId()))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                not(containsString("no_Richard")),
                                containsString("Richard"))));
    }
}
