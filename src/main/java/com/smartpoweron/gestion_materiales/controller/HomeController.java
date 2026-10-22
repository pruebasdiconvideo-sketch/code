package com.smartpoweron.gestion_materiales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //Mapea la ruta raíz y las alternativas comunes para que todo parta desde aquí

    @GetMapping({"/", "/home", "/dashboard"})
    public String mostrarHome(Model model) {
        model.addAttribute("mensaje", "Bienvenido al Sistema de Gestión de Procesos - Smart Power On S.A.C.");
        return "home"; // Esto buscará un archivo llamado home.html en la carpeta templates
    }
}