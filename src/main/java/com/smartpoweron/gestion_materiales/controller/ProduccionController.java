package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.SolicitudBomDto;
import com.smartpoweron.gestion_materiales.model.Proyecto;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador MVC del módulo producción (RF10–RF14).
 * Accesible por JEFE_PRODUCCION y ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/produccion")
@RequiredArgsConstructor
public class ProduccionController {

    private final ProyectoService proyectoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping({"", "/", "/proyectos"})
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String proyectos(Model model) {
        model.addAttribute("proyectos", proyectoService.listarProyectos());
        model.addAttribute("solicitudesPendientes", proyectoService.listarPendientesDespacho());
        model.addAttribute("proyectoNuevo", new Proyecto());
        return "produccion/proyectos";
    }

    @GetMapping("/solicitudes/{idProyecto}")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String verSolicitudesProyecto(@PathVariable Long idProyecto, Model model) {
        model.addAttribute("proyecto", proyectoService.obtenerProyecto(idProyecto));
        return "produccion/detalle-proyecto";
    }

    @GetMapping("/solicitud/nueva/{idProyecto}")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String nuevaSolicitud(@PathVariable Long idProyecto, Model model) {
        model.addAttribute("idProyecto", idProyecto);
        model.addAttribute("solicitudDto", new SolicitudBomDto());
        return "produccion/nueva-solicitud";
    }
}
