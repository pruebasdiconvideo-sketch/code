package com.smartpoweron.gestion_materiales;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de contexto de la aplicación Spring Boot.
 * Verifica que el contenedor arranque sin errores y que las dependencias
 * estén bien configuradas.
 *
 * @author Grupo 8 - UTP
 */
@SpringBootTest
@ActiveProfiles("test")
class GestionMaterialesApplicationTests {

	@Test
	void contextLoads() {
		// Si el contexto arranca sin errores, el test pasa.
	}

}
