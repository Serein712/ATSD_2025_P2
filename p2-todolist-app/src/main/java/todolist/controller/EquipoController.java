package todolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todolist.authentication.ManagerUserSession;
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
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipos);
        return "listaEquipos";
    }


}

