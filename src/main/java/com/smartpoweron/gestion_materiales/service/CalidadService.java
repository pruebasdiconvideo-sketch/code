package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.ParametroPrueba;
import com.smartpoweron.gestion_materiales.model.PruebaCalidad;
import com.smartpoweron.gestion_materiales.model.Proyecto;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.ParametroPruebaRepository;
import com.smartpoweron.gestion_materiales.repository.PruebaCalidadRepository;
import com.smartpoweron.gestion_materiales.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de control de calidad y pruebas funcionales
 * (RF20–RF23, R-CAL-01, R-CAL-02, R-CAL-03, R-CAL-04).
 * <p>
 * Implementa el Proceso 4 (Control de Calidad y Registro de Pruebas Funcionales)
 * del BPMN modelado en la sección 5.4 del informe.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class CalidadService {

    private final PruebaCalidadRepository pruebaRepository;
    private final ParametroPruebaRepository parametroRepository;
    private final ProyectoRepository proyectoRepository;

    /**
     * RF20, RF21, R-CAL-01, R-CAL-04 - Registra una medición.
     * <p>
     * El resultado se calcula automáticamente a partir del rango del parámetro
     * (no se digita a mano). Las pruebas anteriores del mismo parámetro dejan
     * de ser vigentes (R-CAL-04).
     */
    @Transactional
    public PruebaCalidad registrarPrueba(Long idProyecto, Long idParametro,
                                            BigDecimal valorMedido, Usuario inspector,
                                            String observacion) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));
        ParametroPrueba parametro = parametroRepository.findById(idParametro)
                .orElseThrow(() -> new ReglaNegocioException("Parámetro de prueba no encontrado", "PAR-404"));

        // R-CAL-01: el resultado se calcula automáticamente
        boolean conforme = parametro.isConforme(valorMedido);
        String resultado = conforme ? "CONFORME" : "NO_CONFORME";

        // R-CAL-04: marcar pruebas anteriores como no vigentes
        List<PruebaCalidad> vigentes = pruebaRepository.findVigentesPorProyectoYParametro(idProyecto, idParametro);
        for (PruebaCalidad vieja : vigentes) {
            vieja.setVigente(false);
            pruebaRepository.save(vieja);
        }

        PruebaCalidad nueva = PruebaCalidad.builder()
                .proyecto(proyecto)
                .parametro(parametro)
                .usuarioInspector(inspector)
                .valorMedido(valorMedido)
                .resultado(resultado)
                .observacion(observacion)
                .vigente(true)
                .build();
        return pruebaRepository.save(nueva);
    }

    /**
     * RF22, R-CAL-02 - Verifica si el tablero puede recibir el certificado PDF.
     * <p>
     * Solo si todas las pruebas vigentes para cada tipo definido son CONFORME.
     * Si existe una NO_CONFORME vigente, se rechaza la generación del PDF.
     */
    @Transactional(readOnly = true)
    public boolean puedeGenerarCertificado(Long idProyecto) {
        List<PruebaCalidad> vigentes = pruebaRepository.findByProyectoIdProyectoAndVigenteTrue(idProyecto);
        if (vigentes.isEmpty()) {
            return false;
        }
        // Validar que exista al menos una prueba vigente por cada parámetro activo
        List<ParametroPrueba> parametros = parametroRepository.findByActivoTrue();
        for (ParametroPrueba p : parametros) {
            boolean existeConforme = vigentes.stream()
                    .anyMatch(pr -> pr.getParametro().getIdParametro().equals(p.getIdParametro())
                            && "CONFORME".equals(pr.getResultado()));
            if (!existeConforme) return false;
        }
        return true;
    }

    /**
     * R-CAL-03 - Lista las órdenes de observación (pruebas NO_CONFORMES vigentes).
     */
    @Transactional(readOnly = true)
    public List<PruebaCalidad> listarOrdenesObservacion(Long idProyecto) {
        return pruebaRepository.findByProyectoIdProyectoAndVigenteTrue(idProyecto).stream()
                .filter(p -> "NO_CONFORME".equals(p.getResultado()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PruebaCalidad> listarPruebasPorProyecto(Long idProyecto) {
        return pruebaRepository.findByProyectoIdProyecto(idProyecto);
    }

    @Transactional(readOnly = true)
    public List<ParametroPrueba> listarParametros() {
        return parametroRepository.findByActivoTrue();
    }

    @Transactional
    public ParametroPrueba crearParametro(ParametroPrueba p) {
        if (p.getValorMinimo().compareTo(p.getValorMaximo()) > 0) {
            throw new ReglaNegocioException(
                    "El valor mínimo no puede ser mayor que el valor máximo", "PAR-001");
        }
        if (p.getActivo() == null) p.setActivo(true);
        return parametroRepository.save(p);
    }
}
