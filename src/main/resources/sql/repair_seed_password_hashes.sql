-- Repara las cuentas de demostración creadas con los hashes BCrypt inválidos.
-- Restablece estas seis cuentas a las contraseñas semilla documentadas.
-- Ejecutar una sola vez sobre db_smart_power_on si las cuentas ya existen.
START TRANSACTION;

UPDATE usuario
SET password_hash = CASE correo
    WHEN 'admin@smartpoweron.pe' THEN '$2a$10$J48WnQGJAYGE503vkMSmoujmNB1c1K6QhT80HI5BtgqSWUTKSpBDa'
    WHEN 'jefe@smartpoweron.pe' THEN '$2a$10$OgJZsTseAnT.h9RJ2pHhyu41oUgSkwmKoY0bK/uReElfa5RyBpNEy'
    WHEN 'almacen@smartpoweron.pe' THEN '$2a$10$DclVJSwxuZ4p1ZG84K7Ime853wvTmrp8rbTzLwsLGPe4wv1w9Ausa'
    WHEN 'operario@smartpoweron.pe' THEN '$2a$10$eMOQfaOuqGzXa/0E2KnIhuHyWDJ1gBLLDAySIFD8T6IUl7n/KNnpi'
    WHEN 'calidad@smartpoweron.pe' THEN '$2a$10$sDlqdNcRhMuFXj38V6dqGuEYC3lhFykOtYrg6agLGfi55pa3Ecvpi'
    WHEN 'master@smartpoweron.pe' THEN '$2a$10$kyF8Nrnax2DJEEXBOk4myeSGxi7RZqboUTXJsWD6yYXVbxXIKtAHe'
END
WHERE correo IN (
    'admin@smartpoweron.pe',
    'jefe@smartpoweron.pe',
    'almacen@smartpoweron.pe',
    'operario@smartpoweron.pe',
    'calidad@smartpoweron.pe',
    'master@smartpoweron.pe'
);

COMMIT;

-- Validar estructura BCrypt sin revelar los hashes:
SELECT correo, LENGTH(password_hash) AS longitud_hash, LEFT(password_hash, 7) AS prefijo
FROM usuario
WHERE correo IN (
    'admin@smartpoweron.pe',
    'jefe@smartpoweron.pe',
    'almacen@smartpoweron.pe',
    'operario@smartpoweron.pe',
    'calidad@smartpoweron.pe',
    'master@smartpoweron.pe'
);
