package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.service.CalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador MVC del módulo de calidad (RF20–RF23).
 * Accesible por INSPECTOR_CALIDAD, JEFE_PRODUCCION y ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/calidad")
@RequiredArgsConstructor
public class CalidadController {

    private final CalidadService calidadService;

    @GetMapping({"", "/", "/pruebas"})
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String pruebas(Model model) {
        model.addAttribute("parametros", calidadService.listarParametros());
        return "calidad/pruebas";
    }

    @GetMapping("/proyecto/{idProyecto}")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String pruebasProyecto(@PathVariable Long idProyecto, Model model) {
        model.addAttribute("idProyecto", idProyecto);
        model.addAttribute("pruebas", calidadService.listarPruebasPorProyecto(idProyecto));
        model.addAttribute("ordenesObservacion", calidadService.listarOrdenesObservacion(idProyecto));
        model.addAttribute("puedeCertificar", calidadService.puedeGenerarCertificado(idProyecto));
        return "calidad/detalle-proyecto";
    }

    @GetMapping("/parametros")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String parametros(Model model) {
        model.addAttribute("parametros", calidadService.listarParametros());
        return "calidad/parametros";
    }
}
