package com.smartpoweron.gestion_materiales.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.ParametroPrueba;
import com.smartpoweron.gestion_materiales.model.Proyecto;
import com.smartpoweron.gestion_materiales.model.PruebaCalidad;
import com.smartpoweron.gestion_materiales.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para generar el certificado de conformidad en PDF (RF22, R-CAL-02).
 * <p>
 * Usa OpenHTMLtoPDF para renderizar HTML+CSS a PDF. El certificado solo
 * se emite si todas las pruebas vigentes del proyecto son CONFORME.
 * 
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class PdfCertificadoService {

    private final CalidadService calidadService;
    private final ProyectoRepository proyectoRepository;

    /**
     * Genera el PDF del certificado de conformidad para un proyecto.
     * Lanza ReglaNegocioException si no se puede certificar (R-CAL-02).
     */
    @Transactional(readOnly = true)
    public byte[] generarCertificado(Long idProyecto) {
        if (!calidadService.puedeGenerarCertificado(idProyecto)) {
            throw new ReglaNegocioException(
                    "No se puede emitir el certificado: existen pruebas NO_CONFORMES vigentes",
                    "R-CAL-02");
        }
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));
        List<PruebaCalidad> pruebas = calidadService.listarPruebasPorProyecto(idProyecto);

        String html = construirHtmlCertificado(proyecto, pruebas);
        return renderizarPdf(html);
    }

    /**
     * Construye el HTML del certificado con los datos del proyecto y pruebas.
     */
    private String construirHtmlCertificado(Proyecto p, List<PruebaCalidad> pruebas) {
        StringBuilder filas = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (PruebaCalidad prueba : pruebas) {
            if (!Boolean.TRUE.equals(prueba.getVigente())) continue;
            ParametroPrueba param = prueba.getParametro();
            filas.append("<tr>")
                    .append("<td>").append(param.getTipoPrueba()).append("</td>")
                    .append("<td>").append(param.getNombre()).append("</td>")
                    .append("<td>").append(prueba.getValorMedido()).append(" ").append(param.getUnidad()).append("</td>")
                    .append("<td>").append(param.getValorMinimo()).append(" - ").append(param.getValorMaximo()).append(" ").append(param.getUnidad()).append("</td>")
                    .append("<td>").append(prueba.getResultado()).append("</td>")
                    .append("<td>").append(prueba.getFechaPrueba().format(fmt)).append("</td>")
                    .append("</tr>");
        }

        String fechaEmision = LocalDateTime.now().format(fmt);
        String presupuestoStr = String.format("%.2f", p.getPresupuesto());
        String codigoProyecto = p.getCodigo() != null ? p.getCodigo() : "";
        String clienteProyecto = p.getCliente() != null ? p.getCliente() : "";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8"/>
                <style>
                    body { font-family: 'Arial', sans-serif; font-size: 11pt; color: #1f2937; }
                    .header { text-align: center; border-bottom: 3px solid #1e3a8a; padding-bottom: 12px; margin-bottom: 20px; }
                    .header h1 { color: #1e3a8a; margin: 0; font-size: 18pt; }
                    .header h2 { color: #6b7280; margin: 5px 0 0; font-size: 12pt; font-weight: normal; }
                    .info-grid { display: table; width: 100%; margin-bottom: 20px; }
                    .info-row { display: table-row; }
                    .info-cell { display: table-cell; padding: 4px 12px 4px 0; font-size: 10pt; }
                    .info-label { font-weight: bold; color: #4b5563; width: 180px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                    th, td { border: 1px solid #d1d5db; padding: 6px 8px; font-size: 9.5pt; text-align: left; }
                    th { background-color: #1e3a8a; color: #ffffff; }
                    tr:nth-child(even) td { background-color: #f3f4f6; }
                    .seal { text-align: center; margin-top: 40px; font-size: 10pt; color: #6b7280; }
                    .signature { margin-top: 60px; text-align: center; }
                    .signature-line { display: inline-block; border-top: 1px solid #1f2937; padding-top: 4px; width: 280px; }
                    .footer { position: fixed; bottom: 0; width: 100%; text-align: center; font-size: 8pt; color: #9ca3af; border-top: 1px solid #e5e7eb; padding-top: 8px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>CERTIFICADO DE CONFORMIDAD</h1>
                    <h2>Smart Power On S.A.C. - RUC 20606120860</h2>
                </div>
                <div class="info-grid">
                    <div class="info-row">
                        <div class="info-cell info-label">Código de proyecto:</div>
                        <div class="info-cell">%s</div>
                    </div>
                    <div class="info-row">
                        <div class="info-cell info-label">Cliente:</div>
                        <div class="info-cell">%s</div>
                    </div>
                    <div class="info-row">
                        <div class="info-cell info-label">Fecha de emisión:</div>
                        <div class="info-cell">%s</div>
                    </div>
                    <div class="info-row">
                        <div class="info-cell info-label">Presupuesto:</div>
                        <div class="info-cell">S/ %s</div>
                    </div>
                </div>
                <h3 style="color:#1e3a8a;">Resultado de pruebas funcionales</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Parámetro</th>
                            <th>Valor medido</th>
                            <th>Rango técnico</th>
                            <th>Resultado</th>
                            <th>Fecha</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
                <p style="font-size:10pt; margin-top: 20px;">
                    Se certifica que el tablero eléctrico correspondiente al proyecto <strong>%s</strong>
                    ha superado todas las pruebas funcionales y de aislamiento conforme a los rangos técnicos
                    establecidos, quedando autorizado para su entrega al cliente.
                </p>
                <div class="signature">
                    <div class="signature-line">
                        Inspector de Calidad<br/>
                        Smart Power On S.A.C.
                    </div>
                </div>
                <div class="footer">
                    Smart Power On S.A.C. - Calle Manuel Villarán N° 367, Comas, Lima, Perú<br/>
                    Documento generado automáticamente por el Sistema de Gestión de Procesos
                </div>
            </body>
            </html>
            """.formatted(
                codigoProyecto,
                clienteProyecto,
                fechaEmision,
                presupuestoStr,
                filas.toString(),
                codigoProyecto
            );
    }

    /**
     * Renderiza el HTML a PDF usando OpenHTMLtoPDF.
     */
    private byte[] renderizarPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new ReglaNegocioException(
                    "Error al generar el certificado PDF: " + e.getMessage(), "PDF-001");
        }
    }
}