package todolist.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import todolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import todolist.dto.UsuarioData;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Project BBB");
        equipoService.crearEquipo("Project AAA");
        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Project AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Project BBB");
    }

    @Test
    public void añadirUsuarioAEquipoTest() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@umh");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Project 1");
        EquipoData equipo2 = equipoService.crearEquipo("Project 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());
        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());
        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Project 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Project 2");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario
        // y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Project 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void quitarUsuarioDeEquipoTest() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@umh");

        // WHEN
        // Quitamos el usuario del equipo
        equipoService.quitarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario no pertenece mas al equipo
        usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(0);
    }

    @Test
    public void renombrarEquipoTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");
        assertThat(equipo.getNombre()).isEqualTo("Nombre A");

        // .. y un usuario admin
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario.setAdmin(true);
        usuario = usuarioService.registrar(usuario);

        // WHEN
        // se renombra el equipo
        equipoService.renombrarEquipo(equipo,usuario, "Nombre B");

        //THEN
        // el nombre del equipo ha cambiado
        EquipoData equipoActualizado = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoActualizado.getNombre()).isEqualTo("Nombre B");
    }

    @Test
    public void renombrarEquipoExcepcionTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");
        assertThat(equipo.getNombre()).isEqualTo("Nombre A");

        // .. y un usuario no admin
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user@umh");
        usuario2.setPassword("1234");
        usuario2.setAdmin(false);
        usuario2 = usuarioService.registrar(usuario2);

        UsuarioData finalUsuario = usuario2;
        assertThatThrownBy(() ->
                equipoService.renombrarEquipo(equipo, finalUsuario, "Nombre B")
        ).isInstanceOf(EquipoServiceException.class)
                .hasMessage("El usuario no es administrador");
    }

    @Test
    public void disolverEquipoTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");

        // un usuario miembro del equipo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // .. y un usuario admin
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@umh");
        admin.setPassword("1234");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        // WHEN
        // se elimina el equpo por el admin
        equipoService.disolverEquipo(equipo,admin);

        //THEN
        // el equipo no existe
        assertThatThrownBy(() ->
                equipoService.recuperarEquipo(equipo.getId())
        ).isInstanceOf(EquipoServiceException.class).hasMessage("El equipo no existe");

        // y asegurar que usuario no tiene equipo asignado
        assertThat(equipoService.equiposUsuario(usuario.getId())).isEmpty();

    }
}
