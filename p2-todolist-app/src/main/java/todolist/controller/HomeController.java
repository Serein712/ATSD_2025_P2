package todolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import todolist.authentication.ManagerUserSession;

@Controller
public class HomeController {

    @Autowired
    ManagerUserSession managerUserSession;

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

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
}

