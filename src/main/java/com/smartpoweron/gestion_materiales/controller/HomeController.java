package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Controlador MVC principal (RF02 - dashboard por rol).
 * <p>
 * Maneja las rutas: /, /home, /dashboard. Muestra métricas básicas del
 * sistema accesibles para todos los roles autenticados.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;

    @GetMapping({"/", "/home", "/dashboard"})
    public String home(Model model) {
        Map<String, Object> metricas = dashboardService.obtenerMetricas();
        model.addAllAttributes(metricas);
        model.addAttribute("titulo", "Dashboard - Smart Power On S.A.C.");
        model.addAttribute("mensaje", "Sistema de Gestión de Procesos para el Mejoramiento de la Entrega y Registro de Materiales");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
