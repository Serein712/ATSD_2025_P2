package todolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todolist.authentication.ManagerUserSession;
import todolist.controller.exception.EquipoNotFoundException;
import todolist.controller.exception.TareaNotFoundException;
import todolist.controller.exception.UsuarioNoLogeadoException;
import todolist.dto.EquipoData;
import todolist.dto.TareaData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.TareaService;
import todolist.service.UsuarioService;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    EquipoService equipoService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Long userId = managerUserSession.getIdUsuario();
        if (userId != null) {
            model.addAttribute("userLoggedIn", true);
            model.addAttribute("userId", userId);
        }
        else {
            model.addAttribute("userLoggedIn", false);
        }
    }

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }


    @GetMapping("/usuarios/{id}/equipos")
    public String listadoEquipos(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<EquipoData> equipos_usuario = equipoService.equiposUsuario(usuario.getId());
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos_usuario", equipos_usuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipos);
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable(value="id") Long idEquipo, Model model, HttpSession session) {

        EquipoData equipo = equipoService.findById(idEquipo);
        if (equipo == null) {
            throw new EquipoNotFoundException();
        }

        List<UsuarioData> miembros_equipo = equipoService.usuariosEquipo(equipo.getId());


        model.addAttribute("equipo", equipo);
        model.addAttribute("miembros_equipo", miembros_equipo);
        return "detalleEquipo";
    }

    @GetMapping("/usuarios/{id}/equipos/nuevo")
    public String formNuevoEquipo(@PathVariable(value="id") Long idUsuario,
                                  @ModelAttribute EquipoData equipoData, Model model,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevoEquipo";
    }

    @PostMapping("/usuarios/{id}/equipos/nuevo")
    public String nuevoEquipo(@PathVariable(value="id") Long idUsuario,
                                  @ModelAttribute EquipoData equipoData, Model model,
                                  RedirectAttributes flash, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        equipoService.registrar(equipoData);
        equipoService.añadirUsuarioAEquipo(equipoData.getId(), idUsuario);

        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/equipos";
    }

    @PostMapping("/usuarios/{id}/equipos/{id_equipo}/unirse")
    public String unirseAlEquipo(@PathVariable(value="id") Long idUsuario,
                                 @PathVariable(value="id_equipo") Long idEquipo,
                                 Model model, RedirectAttributes flash,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);
        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);

        flash.addFlashAttribute("mensaje", "Se ha unido al equipo ");
        return "redirect:/equipos/" + idEquipo;
    }

    @PostMapping("/usuarios/{id}/equipos/{id_equipo}/abandonar")
    public String abandonarEquipo(@PathVariable(value="id") Long idUsuario,
                                  @PathVariable(value="id_equipo") Long idEquipo,
                                  Model model, RedirectAttributes flash,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);
        equipoService.quitarUsuarioDeEquipo(idEquipo, idUsuario);

        flash.addFlashAttribute("mensaje", "Ha abandonado este equipo ");
        return "redirect:/equipos/" + idEquipo;
    }


}

