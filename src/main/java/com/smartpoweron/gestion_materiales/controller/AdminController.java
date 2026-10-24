package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador MVC del módulo de administración de usuarios (RF03, OE6).
 * Solo accesible por ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping({"", "/", "/usuarios"})
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("roles", usuarioService.listarRoles());
        model.addAttribute("usuarioNuevo", new Usuario());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.obtenerPorId(id));
        model.addAttribute("roles", usuarioService.listarRoles());
        return "admin/editar-usuario";
    }
}
