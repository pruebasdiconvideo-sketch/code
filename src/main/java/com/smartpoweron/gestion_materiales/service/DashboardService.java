package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para el dashboard principal (home).
 * Agrega métricas rápidas para la vista home.html.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MaterialService materialService;
    private final ProyectoService proyectoService;
    private final DespachoService despachoService;
    private final MermaService mermaService;

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerMetricas() {
        Map<String, Object> m = new HashMap<>();
        List<Material> todos = materialService.listarMateriales();
        m.put("totalMateriales", todos.size());
        m.put("alertasStockBajo", materialService.listarAlertasStockBajo().size());
        m.put("solicitudesPendientesDespacho", proyectoService.listarPendientesDespacho().size());
        m.put("totalProyectos", proyectoService.listarProyectos().size());
        m.put("despachosHoy", despachoService.listarTodosDespachos().size());
        m.put("mermasPendientes", mermaService.listarMermasPendientes().size());
        return m;
    }
}
