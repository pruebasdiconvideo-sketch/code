package com.smartpoweron.gestion_materiales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Sistema de Gestión de Procesos para Smart Power On S.A.C.
 * <p>
 * Esta aplicación implementa los cinco procesos de negocio modelados en BPMN 2.0:
 * <ol>
 *   <li>Gestión de Inventario y Catálogo de Materiales</li>
 *   <li>Solicitud y Validación de Materiales por Proyecto</li>
 *   <li>Despacho y Entrega de Kits en Almacén</li>
 *   <li>Control de Calidad y Registro de Pruebas Funcionales</li>
 *   <li>Trazabilidad y Gestión de Mermas / Devoluciones</li>
 * </ol>
 * <p>
 * Stack: Spring Boot 4.1.1, Java 21, Spring Data JPA, MySQL 8, Spring Security,
 * JWT (HS256), Thymeleaf, Bootstrap 5.3.3 y OpenHTMLtoPDF.
 *
 * @author Grupo 8 - UTP
 */
@SpringBootApplication
public class GestionMaterialesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionMaterialesApplication.class, args);
	}
}
