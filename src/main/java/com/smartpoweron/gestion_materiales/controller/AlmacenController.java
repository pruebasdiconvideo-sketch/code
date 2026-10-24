package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.model.OrdenCompra;
import com.smartpoweron.gestion_materiales.repository.OrdenCompraRepository;
import com.smartpoweron.gestion_materiales.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador MVC del módulo almacén (RF05–RF09, P1-RN01, P1-RN02).
 * Solo accesible por ALMACENERO y ADMINISTRADOR.
 *
 * @author Grupo 8 - UTP
 */
@Controller
@RequestMapping("/almacen")
@RequiredArgsConstructor
public class AlmacenController {

    private final MaterialService materialService;
    private final OrdenCompraRepository ordenCompraRepository;

    @GetMapping({"", "/", "/inventario"})
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public String inventario(@RequestParam(required = false) String codigo,
                              @RequestParam(required = false) String nombre,
                              @RequestParam(required = false) String categoria,
                              Model model) {
        List<Material> materiales = (codigo == null && nombre == null && categoria == null)
                ? materialService.listarMateriales()
                : materialService.consultarCatalogo(codigo, nombre, categoria);
        model.addAttribute("materiales", materiales);
        model.addAttribute("materialNuevo", new Material());
        model.addAttribute("alertasStock", materialService.listarAlertasStockBajo());
        return "almacen/inventario";
    }

    @GetMapping("/trazabilidad/{codigo}")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public String trazabilidadMaterial(@PathVariable String codigo, Model model) {
        model.addAttribute("codigo", codigo);
        model.addAttribute("movimientos", materialService.historialMovimientos(
                materialService.obtenerPorCodigo(codigo)
                        .orElseThrow().getIdMaterial()));
        return "trazabilidad/detalle";
    }

    @GetMapping("/ingreso")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public String formularioIngreso(Model model) {
        model.addAttribute("ordenesCompra", ordenCompraRepository.findByEstado("PENDIENTE"));
        return "almacen/ingreso";
    }
}
