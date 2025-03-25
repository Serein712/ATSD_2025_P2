package todolist.controller;

import todolist.authentication.ManagerUserSession;
import todolist.controller.exception.UsuarioNoLogeadoException;
import todolist.controller.exception.TareaNotFoundException;
import todolist.dto.LoginData;
import todolist.dto.TareaData;
import todolist.dto.UsuarioData;
import todolist.service.TareaService;
import todolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UsuariosController {

    @Autowired
    UsuarioService usuarioService;


    @GetMapping("/registered")
    public String registeredList(Model model) {

        List<UsuarioData> usuarios = usuarioService.getAllUsers();
        model.addAttribute("usuarios", usuarios);


        return "registered";
    }
}
