-- =====================================================================
--  Sistema de Gestión de Procesos para Smart Power On S.A.C.
--  Script: db_smart_power_on.sql
--  Motor: MySQL 8.0 (Community Server) - InnoDB - utf8mb4
--  Total: 16 tablas (15 de negocio + 1 asociativa M:N)
--  Normalización: 3FN (RNF08)
--  El script debe ejecutarse ANTES de arrancar la aplicación, ya que
--  application.properties usa spring.jpa.hibernate.ddl-auto=validate
-- =====================================================================

DROP DATABASE IF EXISTS db_smart_power_on;
CREATE DATABASE db_smart_power_on
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE db_smart_power_on;

-- ---------------------------------------------------------------------
-- Tabla 1: rol  (5 roles definidos en OE6)
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id_rol           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(50)  NOT NULL UNIQUE,
    descripcion      VARCHAR(200),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 2: permiso  (17 permisos que cubren los 28 RF)
-- ---------------------------------------------------------------------
CREATE TABLE permiso (
    id_permiso   BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo       VARCHAR(50)  NOT NULL UNIQUE,
    descripcion  VARCHAR(200),
    modulo       VARCHAR(50)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 3 (asociativa M:N): rol_permiso
-- ---------------------------------------------------------------------
CREATE TABLE rol_permiso (
    id_rol       BIGINT NOT NULL,
    id_permiso   BIGINT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rol_permiso_rol       FOREIGN KEY (id_rol)     REFERENCES rol(id_rol)     ON DELETE RESTRICT,
    CONSTRAINT fk_rol_permiso_permiso   FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE RESTRICT,
    INDEX idx_rol_permiso_rol (id_rol),
    INDEX idx_rol_permiso_permiso (id_permiso)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 4: usuario  (RF01, RF03, RNF04)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_rol             BIGINT        NOT NULL,
    nombre_completo    VARCHAR(150)  NOT NULL,
    correo             VARCHAR(150)  NOT NULL UNIQUE,
    password_hash      VARCHAR(255)  NOT NULL,   -- BCrypt (rounds=10)
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    ultimo_acceso      DATETIME,
    fecha_creacion     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE RESTRICT,
    INDEX idx_usuario_correo (correo),
    INDEX idx_usuario_rol (id_rol)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 5: proyecto  (RF10, RF11, RF14, P2-RN02)
-- ---------------------------------------------------------------------
CREATE TABLE proyecto (
    id_proyecto     BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(50)   NOT NULL UNIQUE,
    cliente         VARCHAR(150)  NOT NULL,
    descripcion     VARCHAR(500),
    presupuesto     DECIMAL(12,2) NOT NULL,
    fecha_inicio    DATE          NOT NULL,
    estado          VARCHAR(30)   NOT NULL DEFAULT 'BORRADOR',
    fecha_creacion  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_proyecto_codigo (codigo),
    INDEX idx_proyecto_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 6: orden_compra  (RF06, R-INV-05)
-- ---------------------------------------------------------------------
CREATE TABLE orden_compra (
    id_orden_compra BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(50)   NOT NULL UNIQUE,
    proveedor       VARCHAR(150)  NOT NULL,
    fecha_emision   DATE          NOT NULL,
    estado          VARCHAR(50)   NOT NULL DEFAULT 'PENDIENTE',
    monto_total     DECIMAL(12,2),
    fecha_creacion  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_oc_codigo (codigo),
    INDEX idx_oc_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 7: material  (RF05-RF09, P1-RN01, P1-RN02)
-- ---------------------------------------------------------------------
CREATE TABLE material (
    id_material           BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                VARCHAR(50)  NOT NULL UNIQUE,
    nombre                VARCHAR(150) NOT NULL,
    especificacion_tecnica TEXT        NOT NULL,
    categoria             VARCHAR(50),
    marca                 VARCHAR(50),
    modelo                VARCHAR(100),
    unidad_medida         VARCHAR(20),
    stock_actual          INT          NOT NULL DEFAULT 0,
    stock_minimo          INT          NOT NULL DEFAULT 0,
    precio_unitario       DECIMAL(12,2),
    pasillo               VARCHAR(20),
    estante               VARCHAR(20),
    nivel                 VARCHAR(20),
    estado                VARCHAR(20) DEFAULT 'ACTIVO',
    CONSTRAINT chk_material_stock CHECK (stock_actual >= 0 AND stock_minimo >= 0),
    INDEX idx_material_codigo (codigo),
    INDEX idx_material_stock_minimo (stock_actual, stock_minimo),   -- RF07
    INDEX idx_material_categoria (categoria)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 8: solicitud  (RF11-RF14, P2-RN02)
-- ---------------------------------------------------------------------
CREATE TABLE solicitud (
    id_solicitud            BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto             BIGINT        NOT NULL,
    id_usuario_solicitante  BIGINT        NOT NULL,
    id_usuario_aprobador    BIGINT,
    estado                  VARCHAR(30)   NOT NULL DEFAULT 'BORRADOR',
    costo_total             DECIMAL(12,2) DEFAULT 0.0,
    motivo_retencion        VARCHAR(200),
    fecha_solicitud         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion        DATETIME,
    CONSTRAINT fk_solicitud_proyecto   FOREIGN KEY (id_proyecto)            REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_solicitante FOREIGN KEY (id_usuario_solicitante) REFERENCES usuario(id_usuario)    ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_aprobador   FOREIGN KEY (id_usuario_aprobador)   REFERENCES usuario(id_usuario)    ON DELETE SET NULL,
    INDEX idx_solicitud_estado (estado),
    INDEX idx_solicitud_fecha (fecha_solicitud),                       -- RF15
    INDEX idx_solicitud_proyecto (id_proyecto)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 9: detalle_solicitud  (RF11, RF12, R-INV-02)
-- ---------------------------------------------------------------------
CREATE TABLE detalle_solicitud (
    id_detalle_solicitud BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud          BIGINT   NOT NULL,
    id_material           BIGINT   NOT NULL,
    cantidad_solicitada   INT      NOT NULL,
    cantidad_despachada   INT      NOT NULL DEFAULT 0,
    precio_unitario       DECIMAL(12,2),
    CONSTRAINT uk_detalle_solicitud UNIQUE (id_solicitud, id_material),
    CONSTRAINT fk_detalle_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_material FOREIGN KEY (id_material)  REFERENCES material(id_material)   ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad_solicitada > 0 AND cantidad_despachada >= 0),
    INDEX idx_ds_solicitud (id_solicitud),
    INDEX idx_ds_material (id_material)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 10: despacho  (RF15, RF18, P3-RN01, P3-RN02)
-- ---------------------------------------------------------------------
CREATE TABLE despacho (
    id_despacho          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud         BIGINT       NOT NULL,
    id_usuario_almacenero BIGINT      NOT NULL,
    tipo                 VARCHAR(20) NOT NULL,           -- COMPLETO / PARCIAL
    estado               VARCHAR(20) NOT NULL DEFAULT 'PREPARANDO',
    observacion          VARCHAR(300),
    fecha_despacho       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_despacho_solicitud  FOREIGN KEY (id_solicitud)          REFERENCES solicitud(id_solicitud) ON DELETE RESTRICT,
    CONSTRAINT fk_despacho_almacenero FOREIGN KEY (id_usuario_almacenero) REFERENCES usuario(id_usuario)      ON DELETE RESTRICT,
    INDEX idx_despacho_solicitud (id_solicitud),
    INDEX idx_despacho_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 11: detalle_despacho  (RF16, P3-RN01)
-- ---------------------------------------------------------------------
CREATE TABLE detalle_despacho (
    id_detalle_despacho BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_despacho          BIGINT       NOT NULL,
    id_detalle_solicitud BIGINT      NOT NULL,
    id_material          BIGINT       NOT NULL,
    codigo_digitado      VARCHAR(150) NOT NULL,
    cantidad_entregada   INT          NOT NULL,
    CONSTRAINT fk_detdespacho_despacho   FOREIGN KEY (id_despacho)          REFERENCES despacho(id_despacho)          ON DELETE CASCADE,
    CONSTRAINT fk_detdespacho_detsolicitud FOREIGN KEY (id_detalle_solicitud) REFERENCES detalle_solicitud(id_detalle_solicitud) ON DELETE RESTRICT,
    CONSTRAINT fk_detdespacho_material   FOREIGN KEY (id_material)          REFERENCES material(id_material)           ON DELETE RESTRICT,
    CONSTRAINT chk_detdespacho_cantidad CHECK (cantidad_entregada > 0),
    INDEX idx_dd_despacho (id_despacho),
    INDEX idx_dd_material (id_material)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 12: entrega  (RF17, RF19, R-INV-03 - UNIQUE 1:1)
-- ---------------------------------------------------------------------
CREATE TABLE entrega (
    id_entrega            BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_despacho           BIGINT  NOT NULL UNIQUE,        -- 1:0..1
    id_usuario_operario   BIGINT  NOT NULL,
    confirmado            BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_entrega         DATETIME,
    observacion           VARCHAR(300),
    CONSTRAINT fk_entrega_despacho FOREIGN KEY (id_despacho)         REFERENCES despacho(id_despacho) ON DELETE RESTRICT,
    CONSTRAINT fk_entrega_operario FOREIGN KEY (id_usuario_operario) REFERENCES usuario(id_usuario)    ON DELETE RESTRICT,
    INDEX idx_entrega_despacho (id_despacho)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 13: parametro_prueba  (RF21, R-CAL-01)
-- ---------------------------------------------------------------------
CREATE TABLE parametro_prueba (
    id_parametro  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_prueba   VARCHAR(50)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    valor_minimo  DECIMAL(10,3) NOT NULL,
    valor_maximo  DECIMAL(10,3) NOT NULL,
    unidad        VARCHAR(20),
    descripcion   VARCHAR(200),
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_parametro_rango CHECK (valor_minimo <= valor_maximo),
    INDEX idx_parametro_tipo (tipo_prueba)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 14: prueba_calidad  (RF20-RF23, R-CAL-01..04)
-- ---------------------------------------------------------------------
CREATE TABLE prueba_calidad (
    id_prueba            BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto          BIGINT       NOT NULL,
    id_parametro         BIGINT       NOT NULL,
    id_usuario_inspector BIGINT       NOT NULL,
    valor_medido         DECIMAL(10,3) NOT NULL,
    resultado            VARCHAR(20)  NOT NULL,    -- CONFORME / NO_CONFORME
    observacion          VARCHAR(500),
    vigente              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_prueba         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prueba_proyecto   FOREIGN KEY (id_proyecto)          REFERENCES proyecto(id_proyecto)          ON DELETE RESTRICT,
    CONSTRAINT fk_prueba_parametro  FOREIGN KEY (id_parametro)         REFERENCES parametro_prueba(id_parametro) ON DELETE RESTRICT,
    CONSTRAINT fk_prueba_inspector  FOREIGN KEY (id_usuario_inspector) REFERENCES usuario(id_usuario)            ON DELETE RESTRICT,
    INDEX idx_prueba_proyecto (id_proyecto),
    INDEX idx_prueba_vigente (vigente)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 15: merma_devolucion  (RF24-RF27, P5-RN01, P5-RN02)
-- ---------------------------------------------------------------------
CREATE TABLE merma_devolucion (
    id_merma              BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proyecto           BIGINT       NOT NULL,
    id_material           BIGINT       NOT NULL,
    id_usuario_registra   BIGINT       NOT NULL,
    id_usuario_validador  BIGINT,
    tipo                  VARCHAR(20)  NOT NULL,    -- DEVOLUCION / MERMA
    estado                VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    cantidad              INT          NOT NULL,
    motivo                VARCHAR(300),
    estado_fisico         VARCHAR(50),
    fecha_registro        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_validacion      DATETIME,
    CONSTRAINT fk_merma_proyecto   FOREIGN KEY (id_proyecto)         REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT,
    CONSTRAINT fk_merma_material   FOREIGN KEY (id_material)        REFERENCES material(id_material) ON DELETE RESTRICT,
    CONSTRAINT fk_merma_registra   FOREIGN KEY (id_usuario_registra) REFERENCES usuario(id_usuario)    ON DELETE RESTRICT,
    CONSTRAINT fk_merma_validador  FOREIGN KEY (id_usuario_validador) REFERENCES usuario(id_usuario)  ON DELETE SET NULL,
    CONSTRAINT chk_merma_cantidad CHECK (cantidad > 0),
    INDEX idx_merma_proyecto (id_proyecto),
    INDEX idx_merma_estado (estado),
    INDEX idx_merma_tipo (tipo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla 16: trazabilidad  (RF09, RF17, RF25, RF28, R-INV-07)
-- ---------------------------------------------------------------------
CREATE TABLE trazabilidad (
    id_trazabilidad    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_material        BIGINT       NOT NULL,
    id_usuario         BIGINT       NOT NULL,
    id_proyecto        BIGINT,                                    -- nullable
    id_orden_compra    BIGINT,                                    -- nullable (solo ENTRADA)
    tipo_movimiento    VARCHAR(30)  NOT NULL,                     -- ENTRADA/SALIDA/DEVOLUCION/MERMA
    cantidad           INT          NOT NULL,
    saldo_resultante   INT,
    descripcion        VARCHAR(300),
    fecha_movimiento   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_despacho_origen BIGINT,
    CONSTRAINT fk_traza_material  FOREIGN KEY (id_material)     REFERENCES material(id_material)     ON DELETE RESTRICT,
    CONSTRAINT fk_traza_usuario   FOREIGN KEY (id_usuario)      REFERENCES usuario(id_usuario)        ON DELETE RESTRICT,
    CONSTRAINT fk_traza_proyecto  FOREIGN KEY (id_proyecto)     REFERENCES proyecto(id_proyecto)     ON DELETE SET NULL,
    CONSTRAINT fk_traza_oc        FOREIGN KEY (id_orden_compra) REFERENCES orden_compra(id_orden_compra) ON DELETE SET NULL,
    CONSTRAINT chk_traza_cantidad CHECK (cantidad > 0),
    INDEX idx_tr_material (id_material),                              -- RF28
    INDEX idx_tr_fecha (fecha_movimiento),                           -- RF28
    INDEX idx_tr_proyecto (id_proyecto),
    INDEX idx_tr_tipo (tipo_movimiento)
) ENGINE=InnoDB;

-- =====================================================================
--                          DATOS SEMILLA (SEED DATA)
-- =====================================================================
-- 5 roles definidos en OE6 (Administrador, Jefe Producción, Almacenero,
-- Operario Ensamble, Inspector de Calidad)
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR',     'Gestiona usuarios, roles y parámetros técnicos; aprueba solicitudes retenidas'),
    ('JEFE_PRODUCCION',   'Crea proyectos, registra solicitudes con BOM, valida bajas por merma'),
    ('ALMACENERO',        'Administra catálogo e inventario, registra ingresos, prepara y despacha kits'),
    ('OPERARIO_ENSAMBLE', 'Confirma digitalmente la recepción del kit y registra devoluciones de sobrantes'),
    ('INSPECTOR_CALIDAD', 'Registra y valida pruebas funcionales y de aislamiento, emite certificado de conformidad');

-- 17 permisos que cubren los 28 RF
INSERT INTO permiso (codigo, descripcion, modulo) VALUES
    ('ACCESO_USUARIOS',     'Gestionar cuentas de usuario', 'ADMIN'),
    ('ACCESO_ROLES',        'Gestionar roles y permisos', 'ADMIN'),
    ('ACCESO_INVENTARIO',   'Acceso al catálogo de materiales', 'ALMACEN'),
    ('ACCESO_PROYECTOS',    'Gestionar proyectos', 'PRODUCCION'),
    ('ACCESO_SOLICITUDES',  'Crear solicitudes con BOM', 'PRODUCCION'),
    ('ACCESO_DESPACHO',     'Preparar y despachar kits', 'DESPACHO'),
    ('ACCESO_ENTREGA',      'Confirmar recepción de kits', 'DESPACHO'),
    ('ACCESO_CALIDAD',      'Registrar pruebas funcionales', 'CALIDAD'),
    ('ACCESO_CERTIFICADO',  'Emitir certificado PDF', 'CALIDAD'),
    ('ACCESO_MERMA',        'Registrar devoluciones y mermas', 'MERMA'),
    ('ACCESO_REINTEGRO',   'Reintegrar materiales al stock', 'MERMA'),
    ('ACCESO_VALIDA_MERMA', 'Validar baja por merma', 'MERMA'),
    ('ACCESO_REPORTE_MERMA', 'Generar reportes de mermas', 'MERMA'),
    ('ACCESO_TRAZABILIDAD', 'Consultar trazabilidad de materiales', 'TRAZABILIDAD'),
    ('ACCESO_PARAMETROS',   'Configurar parámetros de pruebas', 'ADMIN'),
    ('ACCESO_APROBAR_SOL',  'Aprobar solicitudes retenidas por presupuesto', 'ADMIN'),
    ('ACCESO_DASHBOARD',    'Ver dashboard principal', 'TODOS');

-- Rol - Permiso (matriz simplificada: ADMIN tiene todos)
INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),
    (2,4),(2,5),(2,8),(2,10),(2,12),(2,13),(2,14),(2,17),
    (3,3),(3,5),(3,6),(3,10),(3,11),(3,13),(3,14),(3,17),
    (4,7),(4,10),(4,14),(4,17),
    (5,8),(5,9),(5,14),(5,17);

-- 6 usuarios semilla con contraseñas BCrypt (rounds=10)
-- Contraseñas iniciales (NO se almacenan en texto plano, solo el hash):
--   admin@smartpoweron.pe     -> Clave123$    (Administrador)
--   jefe@smartpoweron.pe     -> Clave123$    (Jefe de Producción)
--   almacen@smartpoweron.pe  -> Clave123$    (Almacenero)
--   operario@smartpoweron.pe -> Clave123$    (Operario de Ensamble)
--   calidad@smartpoweron.pe  -> Clave123$    (Inspector de Calidad)
--   master@smartpoweron.pe   -> Master123$   (Super-administrador)
INSERT INTO usuario (id_rol, nombre_completo, correo, password_hash) VALUES
    (1, 'Melquiades Efrain Melgarejo Graciano', 'admin@smartpoweron.pe',     '$2a$10$J48WnQGJAYGE503vkMSmoujmNB1c1K6QhT80HI5BtgqSWUTKSpBDa'),
    (2, 'Izquierdo Rodriguez Walter Orlando',    'jefe@smartpoweron.pe',     '$2a$10$OgJZsTseAnT.h9RJ2pHhyu41oUgSkwmKoY0bK/uReElfa5RyBpNEy'),
    (3, 'Valencia Pando Briyit Sandra',          'almacen@smartpoweron.pe',  '$2a$10$DclVJSwxuZ4p1ZG84K7Ime853wvTmrp8rbTzLwsLGPe4wv1w9Ausa'),
    (4, 'Rodas Mohamen Yanpier Anthonio',        'operario@smartpoweron.pe', '$2a$10$eMOQfaOuqGzXa/0E2KnIhuHyWDJ1gBLLDAySIFD8T6IUl7n/KNnpi'),
    (5, 'Orellana Vásquez Sebastián',           'calidad@smartpoweron.pe',  '$2a$10$sDlqdNcRhMuFXj38V6dqGuEYC3lhFykOtYrg6agLGfi55pa3Ecvpi'),
    (1, 'Quispe Guevara Gian Alexis',           'master@smartpoweron.pe',   '$2a$10$kyF8Nrnax2DJEEXBOk4myeSGxi7RZqboUTXJsWD6yYXVbxXIKtAHe');

-- 3 órdenes de compra semilla
INSERT INTO orden_compra (codigo, proveedor, fecha_emision, estado, monto_total) VALUES
    ('OC-2026-001', 'Siemens S.A. Perú',         '2026-08-15', 'CONFORME',  45800.00),
    ('OC-2026-002', 'ABB Perú S.A.',              '2026-09-01', 'PENDIENTE', 12350.00),
    ('OC-2026-003', 'Distribuidora Eléctrica Andina', '2026-09-20', 'PENDIENTE', 8720.50);

-- 3 proyectos semilla
INSERT INTO proyecto (codigo, cliente, descripcion, presupuesto, fecha_inicio, estado) VALUES
    ('PROY-001', 'Minera Aurífera del Sur S.A.C.', 'Tablero de control de patio 22 kV', 48500.00, '2026-08-20', 'ACTIVO'),
    ('PROY-002', 'Enel Distribución Perú',           'Tablero de transferencia 380 V',  18900.00, '2026-09-05', 'ACTIVO'),
    ('PROY-003', 'Luz del Sur S.A.A.',               'Tablero de servicios auxiliares', 32750.00, '2026-09-15', 'ACTIVO');

-- 10 materiales típicos del taller eléctrico
INSERT INTO material (codigo, nombre, especificacion_tecnica, categoria, marca, modelo, unidad_medida, stock_actual, stock_minimo, precio_unitario, pasillo, estante, nivel, estado) VALUES
    ('7SJ62-5AA22-1FE0', 'Relé SIPROTEC 7SJ62',  'Relé de protección numérico para tableros de media tensión, 5 A, 110 V DC', 'EQUIPO',         'SIEMENS', '7SJ62', 'UNIDAD', 4, 2,  1245.00, 'A', '1', 'A', 'ACTIVO'),
    ('7S80-2AA22-1FE0',  'Relé SIPROTEC 7S80',   'Relé de protección numérico para tableros de media tensión, 5 A, 110 V DC', 'EQUIPO',         'SIEMENS', '7S80',  'UNIDAD', 3, 2,  980.00,  'A', '1', 'B', 'ACTIVO'),
    ('3VA1-100-4ED30',   'Breaker 3VA1100',      'Interruptor automático 3 polos, 100 A, curva D', 'BREAKER',         'SIEMENS', '3VA1100', 'UNIDAD', 8, 3,  358.00,  'B', '2', 'A', 'ACTIVO'),
    ('3VA1-250-4ED30',   'Breaker 3VA1250',     'Interruptor automático 3 polos, 250 A, curva D', 'BREAKER',         'SIEMENS', '3VA1250', 'UNIDAD', 2, 2,  615.00,  'B', '2', 'B', 'ACTIVO'),
    ('8WH12-0CC11-0CA1', 'Bornera de paso 16 A', 'Bornera de paso para cableado interno, sección 2.5 mm²', 'INSUMO', 'WAGO',    '281-6XX', 'UNIDAD', 200, 50,  12.50, 'C', '3', 'A', 'ACTIVO'),
    ('8WH12-0CC21-0CA1', 'Tope de bornera',     'Tope de cierre para borneras de paso', 'INSUMO', 'WAGO', '281-9XX', 'UNIDAD', 100, 30, 2.80,  'C', '3', 'A', 'ACTIVO'),
    ('DIN-35-GALV',     'Riel DIN 35 mm',       'Riel DIN galvanizado 35 mm x 1 m', 'INSUMO', 'GENÉRICO', 'DIN35', 'METRO', 50, 10, 18.00, 'D', '4', 'A', 'ACTIVO'),
    ('CANA-40X60',      'Canaleta 40x60 mm',    'Canaleta ranurada PVC 40x60 mm x 2 m', 'INSUMO', 'PANDUIT', 'FLC40', 'METRO', 30, 8,  22.00, 'D', '4', 'B', 'ACTIVO'),
    ('CABLE-2.5-ROJO',  'Cable 2.5 mm² rojo',   'Cable flexible 2.5 mm² color rojo, x metro', 'INSUMO', 'INDUCO', 'FH-2.5', 'METRO', 500, 100, 3.50, 'E', '5', 'A', 'ACTIVO'),
    ('RES-100-110V',    'Resistencia calefactora 100 W', 'Resistencia calefactora anti-condensación 100 W 110 V AC', 'EQUIPO', 'STEGO', 'F020-100', 'UNIDAD', 6, 3, 85.00, 'A', '1', 'C', 'ACTIVO');

-- 4 parámetros de prueba (R-CAL-01)
INSERT INTO parametro_prueba (tipo_prueba, nombre, valor_minimo, valor_maximo, unidad, descripcion, activo) VALUES
    ('TENSION',      'Prueba de tensión aplicada',       0.000, 1000.000, 'V AC', 'Tensión de aislamiento aplicada 1 kV AC por 1 min', TRUE),
    ('AISLAMIENTO',  'Resistencia de aislamiento',       100.000, 1000.000, 'MΩ', 'Resistencia de aislamiento mínimo 100 MΩ', TRUE),
    ('CONTINUIDAD',  'Continuidad del circuito',         0.000, 0.500, 'Ω', 'Continuidad eléctrica < 0.5 Ω', TRUE),
    ('RESISTENCIA',  'Resistencia de puesta a tierra',   0.000, 5.000, 'Ω', 'Resistencia de puesta a tierra < 5 Ω', TRUE);

-- =====================================================================
-- Verificación de integridad referencial
-- =====================================================================
SELECT 'Tablas creadas:' AS info;
SELECT COUNT(*) AS total_tablas FROM information_schema.tables WHERE table_schema = 'db_smart_power_on';
-- Debe mostrar 16

SELECT 'FKs creadas:' AS info;
SELECT COUNT(*) AS total_fks FROM information_schema.referential_constraints
WHERE constraint_schema = 'db_smart_power_on';
-- Debe mostrar 26

-- FIN DEL SCRIPT
