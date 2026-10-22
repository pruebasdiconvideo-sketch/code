package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    // Ruta a la que se accede al hacer clic en "Consultar Inventario" desde el home
    @GetMapping("/almacen/ver")
    public String consultarInventario(Model model) {
        List<Material> listaMateriales = materialService.listarMateriales();
        model.addAttribute("materiales", listaMateriales);
        
        // Objeto vacío para el formulario de nuevo ingreso/registro rápido
        model.addAttribute("materialNuevo", new Material());
        
        return "almacen/inventario"; // Busca el archivo inventario.html dentro de templates/almacen/
    }

    // Ruta para procesar el registro o actualización inteligente de un material (Proceso 1)
    @PostMapping("/almacen/guardar")
    public String guardarMaterial(@ModelAttribute Material material, @RequestParam Integer cantidadIngresada) {
        materialService.registrarOActualizarIngreso(material, cantidadIngresada);
        return "redirect:/almacen/ver";
    }
}