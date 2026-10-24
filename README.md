# Sistema de Gestión de Procesos - Smart Power On S.A.C.

Proyecto académico del curso **Marcos de Desarrollo Web** (UTP) que implementa un sistema web para el control de entrega y registro de materiales en el ensamblaje de tableros eléctricos de la empresa Smart Power On S.A.C. (Lima, Perú).

## Stack tecnológico

| Capa | Tecnología | Versión |
|------|------------|---------|
| Frontend | HTML5 + CSS3 + JavaScript | Estándar W3C |
| Frontend | Bootstrap (CDN) | 5.3.3 |
| Frontend | Thymeleaf + extras Spring Security | 6 |
| Backend | Java JDK | 21 (LTS) |
| Backend | Spring Boot | 4.1.1 |
| Backend | Spring Data JPA + Hibernate | Incluido |
| Backend | Spring Security | 6 |
| Seguridad | JWT (JJWT) - HS256 | 0.12.6 |
| Seguridad | BCryptPasswordEncoder | rounds=10 |
| Persistencia | MySQL | 8.0 Community |
| PDF | OpenHTMLtoPDF | 1.1.24 |
| Build | Maven | 3.9+ |
| Lombok | Lombok | 1.18+ |

## Estructura del proyecto

```
gestion-materiales/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/smartpoweron/gestion_materiales/
    │   │   ├── GestionMaterialesApplication.java
    │   │   ├── model/                  (16 entidades JPA)
    │   │   ├── repository/             (15 interfaces JpaRepository)
    │   │   ├── service/                (8 servicios @Transactional)
    │   │   ├── controller/              (7 MVC + 6 REST)
    │   │   ├── security/                (JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, JpaUserDetailsService)
    │   │   ├── dto/                     (LoginRequest, LoginResponse, SolicitudBomDto, ItemBomDto, ApiResponse)
    │   │   └── exception/              (ReglaNegocioException, RecursoNoEncontradoException, GlobalExceptionHandler)
    │   └── resources/
    │       ├── application.properties
    │       ├── sql/db_smart_power_on.sql    (16 tablas + datos semilla)
    │       ├── templates/                   (12 plantillas Thymeleaf)
    │       └── static/css/styles.css, static/js/login.js
    └── test/java/.../GestionMaterialesApplicationTests.java
```

## Cómo ejecutarlo

1. **Instalar JDK 21, Maven 3.9+ y MySQL 8.0**
2. **Crear la base de datos** ejecutando el script `src/main/resources/sql/db_smart_power_on.sql`
3. Si la base ya existía con las semillas anteriores, ejecutar una sola vez `src/main/resources/sql/repair_seed_password_hashes.sql` para corregir los hashes BCrypt y restablecer las contraseñas semilla documentadas.
4. **Configurar** las credenciales en `application.properties` (sección `spring.datasource`)
5. **Ejecutar** con: `mvn spring-boot:run` o desde VS Code con la extensión Spring Boot Dashboard
6. **Acceder** a `http://localhost:8081/login` con las credenciales semilla:
   - `admin@smartpoweron.pe` / `Clave123$`
   - `jefe@smartpoweron.pe` / `Clave123$`
   - `almacen@smartpoweron.pe` / `Clave123$`
   - `operario@smartpoweron.pe` / `Clave123$`
   - `calidad@smartpoweron.pe` / `Clave123$`

## Documentación

- `Informe_Implementacion_VSCode.docx` (en la carpeta `download/`) — guía completa de instalación, configuración y despliegue desde Visual Studio Code.
- Informe académico principal del proyecto — ver el documento original del curso.

## Autoría

**Grupo 8 - UTP 2026**
- Izquierdo Rodriguez Walter Orlando (U20220811)
- Orellana Vásquez Sebastián (U22215513)
- Quispe Guevara Gian Alexis (U22305316)
- Rodas Mohamen Yanpier Anthonio (U22219743)
- Valencia Pando Briyit Sandra (U24232824)

Docente: Melquiades Efrain Melgarejo Graciano
