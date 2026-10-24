package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.service.DespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador MVC del módulo despacho (RF15–RF19).
 * Accesible por ALMACENERO, OPERARIO_ENSAMBLE y ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/despacho")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;

    @GetMapping({"", "/", "/bandeja"})
    @PreAuthorize("hasAnyRole('ALMACENERO', 'OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public String bandeja(Model model) {
        model.addAttribute("despachos", despachoService.listarTodosDespachos());
        return "despacho/bandeja";
    }

    @GetMapping("/detalle/{id}")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("despacho", despachoService.obtenerDespacho(id).orElse(null));
        return "despacho/detalle";
    }
}
