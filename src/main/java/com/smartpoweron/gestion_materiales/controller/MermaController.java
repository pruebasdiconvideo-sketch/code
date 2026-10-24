package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.service.MermaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador MVC del módulo mermas y devoluciones (RF24–RF27).
 * Accesible por ALMACENERO, OPERARIO_ENSAMBLE, JEFE_PRODUCCION y ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/merma")
@RequiredArgsConstructor
public class MermaController {

    private final MermaService mermaService;

    @GetMapping({"", "/", "/devoluciones"})
    @PreAuthorize("hasAnyRole('ALMACENERO', 'OPERARIO_ENSAMBLE', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String devoluciones(Model model) {
        model.addAttribute("mermas", mermaService.listarTodasMermas());
        model.addAttribute("pendientes", mermaService.listarMermasPendientes());
        return "merma/devoluciones";
    }

    @GetMapping("/reportes")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String reportes(
            @RequestParam(required = false) Long idProyecto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {
        model.addAttribute("mermas", mermaService.reporteMermas(idProyecto, fechaInicio, fechaFin));
        return "reportes/mermas";
    }
}
