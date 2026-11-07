-- =====================================================================
-- VetCare Inventario - Esquema y datos iniciales (MySQL 8)
-- Curso: Soluciones Web y Aplicaciones Distribuidas
--
-- Qué contiene:
--   1. Creación de la base y de las 7 tablas (DDL)
--   2. Datos iniciales: roles, categorías, proveedores, productos, lotes y
--      movimientos de entrada (DML)
--   3. Consultas de verificación equivalentes a los endpoints de la API
--
-- Cómo usarlo en MySQL Workbench: abrir este archivo y ejecutarlo completo
-- (rayo con la marca "ejecutar todo"). Es seguro repetirlo: usa
-- IF NOT EXISTS e INSERT IGNORE, así que no duplica datos.
--
-- Nota sobre los usuarios: sus contraseñas se guardan con BCrypt, que solo
-- se calcula desde la aplicación. Por eso los 4 usuarios de prueba los crea
-- la aplicación al arrancar (clase DataInitializer). Ver sección 4.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS vetcare_inventario
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vetcare_inventario;

-- ---------------------------------------------------------------------
-- 1. TABLAS
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS usuarios (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  username         VARCHAR(50)  NOT NULL UNIQUE,
  password_hash    VARCHAR(100) NOT NULL,
  nombre_completo  VARCHAR(100) NOT NULL,
  activo           BIT NOT NULL DEFAULT 1,
  id_rol           BIGINT NOT NULL,
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS categorias (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(80) NOT NULL UNIQUE,
  descripcion  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS proveedores (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  razon_social  VARCHAR(150) NOT NULL,
  ruc           VARCHAR(11)  NOT NULL UNIQUE,
  telefono      VARCHAR(20),
  correo        VARCHAR(100),
  activo        BIT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS productos (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo         VARCHAR(30)  NOT NULL UNIQUE,
  nombre         VARCHAR(150) NOT NULL,
  unidad_medida  VARCHAR(20)  NOT NULL,
  precio         DECIMAL(10,2) NOT NULL,
  stock_minimo   INT NOT NULL,
  activo         BIT NOT NULL DEFAULT 1,
  id_categoria   BIGINT NOT NULL,
  CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS lotes (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto        BIGINT NOT NULL,
  id_proveedor       BIGINT NOT NULL,
  codigo_lote        VARCHAR(50) NOT NULL,
  fecha_ingreso      DATE NOT NULL,
  fecha_vencimiento  DATE NOT NULL,
  cantidad_inicial   INT NOT NULL,
  cantidad_actual    INT NOT NULL,
  CONSTRAINT uk_lote_producto_codigo UNIQUE (id_producto, codigo_lote),
  CONSTRAINT fk_lote_producto  FOREIGN KEY (id_producto)  REFERENCES productos(id),
  CONSTRAINT fk_lote_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores(id)
);

CREATE TABLE IF NOT EXISTS movimientos (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  tipo         VARCHAR(10) NOT NULL,   -- ENTRADA | SALIDA
  motivo       VARCHAR(20) NOT NULL,   -- COMPRA | VENTA | AJUSTE | MERMA | VENCIMIENTO
  cantidad     INT NOT NULL,
  fecha        DATETIME(6) NOT NULL,
  observacion  VARCHAR(255),
  id_lote      BIGINT NOT NULL,
  id_usuario   BIGINT,
  CONSTRAINT fk_mov_lote    FOREIGN KEY (id_lote)    REFERENCES lotes(id),
  CONSTRAINT fk_mov_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);
-- El stock de un producto = SUM(lotes.cantidad_actual). No existe columna "stock".

-- ---------------------------------------------------------------------
-- 2. DATOS INICIALES
-- ---------------------------------------------------------------------
INSERT IGNORE INTO roles (nombre) VALUES
  ('ADMINISTRADOR'), ('ENCARGADO'), ('VENTAS'), ('GERENTE');

INSERT IGNORE INTO categorias (nombre, descripcion) VALUES
  ('Alimentos',        'Alimento para mascotas'),
  ('Medicamentos',     'Medicamentos veterinarios'),
  ('Antiparasitarios', 'Control de pulgas, garrapatas y parásitos'),
  ('Vitaminas',        'Vitaminas y suplementos'),
  ('Higiene',          'Artículos de higiene y cuidado'),
  ('Accesorios',       'Collares, correas y otros accesorios');

INSERT IGNORE INTO proveedores (razon_social, ruc, telefono, correo) VALUES
  ('Distribuidora Pet Andina S.A.C.',        '20111111111', '014000001', 'ventas@petandina.example'),
  ('Laboratorios Vet Sur S.A.',              '20222222222', '014000002', 'pedidos@vetsur.example'),
  ('Importaciones Mascota Feliz E.I.R.L.',   '20333333333', '014000003', 'contacto@mascotafeliz.example');

-- Productos de ejemplo (datos ficticios de demostración)
INSERT IGNORE INTO productos (codigo, nombre, unidad_medida, precio, stock_minimo, activo, id_categoria) VALUES
  ('ALI-001', 'Alimento seco para perros adultos 15 kg', 'SACO',   189.90, 10, 1, (SELECT id FROM categorias WHERE nombre = 'Alimentos')),
  ('ALI-002', 'Alimento seco para gatos 7.5 kg',         'SACO',   129.50,  8, 1, (SELECT id FROM categorias WHERE nombre = 'Alimentos')),
  ('MED-001', 'Amoxicilina 500 mg x 10 tabletas',        'CAJA',    32.00, 15, 1, (SELECT id FROM categorias WHERE nombre = 'Medicamentos')),
  ('ANT-001', 'Antipulgas para perros de 10 a 20 kg',    'UND',     45.00, 10, 1, (SELECT id FROM categorias WHERE nombre = 'Antiparasitarios')),
  ('VIT-001', 'Multivitamínico para mascotas 120 ml',    'FRASCO',  38.50,  6, 1, (SELECT id FROM categorias WHERE nombre = 'Vitaminas')),
  ('HIG-001', 'Shampoo hipoalergénico 500 ml',           'FRASCO',  24.90, 10, 1, (SELECT id FROM categorias WHERE nombre = 'Higiene')),
  ('ACC-001', 'Correa retráctil 5 m',                    'UND',     29.90,  5, 1, (SELECT id FROM categorias WHERE nombre = 'Accesorios'));

-- Lotes de ejemplo (las fechas se calculan a partir del día de ejecución)
INSERT IGNORE INTO lotes (id_producto, id_proveedor, codigo_lote, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad_actual) VALUES
  ((SELECT id FROM productos WHERE codigo = 'ALI-001'), (SELECT id FROM proveedores WHERE ruc = '20111111111'), 'ALI1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 240 DAY), 40, 40),
  ((SELECT id FROM productos WHERE codigo = 'ALI-002'), (SELECT id FROM proveedores WHERE ruc = '20111111111'), 'ALI2-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 200 DAY), 12, 12),
  ((SELECT id FROM productos WHERE codigo = 'MED-001'), (SELECT id FROM proveedores WHERE ruc = '20222222222'), 'MED1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 25 DAY),  30, 30),
  ((SELECT id FROM productos WHERE codigo = 'MED-001'), (SELECT id FROM proveedores WHERE ruc = '20222222222'), 'MED1-L02', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 20, 20),
  ((SELECT id FROM productos WHERE codigo = 'ANT-001'), (SELECT id FROM proveedores WHERE ruc = '20222222222'), 'ANT1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 120 DAY),  5,  5),
  ((SELECT id FROM productos WHERE codigo = 'VIT-001'), (SELECT id FROM proveedores WHERE ruc = '20333333333'), 'VIT1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 150 DAY), 18, 18),
  ((SELECT id FROM productos WHERE codigo = 'HIG-001'), (SELECT id FROM proveedores WHERE ruc = '20333333333'), 'HIG1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 300 DAY), 25, 25),
  ((SELECT id FROM productos WHERE codigo = 'ACC-001'), (SELECT id FROM proveedores WHERE ruc = '20333333333'), 'ACC1-L01', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 720 DAY), 15, 15);

-- Un movimiento de ENTRADA por cada lote cargado (solo si el lote aún no tiene movimientos)
INSERT INTO movimientos (tipo, motivo, cantidad, fecha, observacion, id_lote, id_usuario)
SELECT 'ENTRADA', 'COMPRA', l.cantidad_inicial, NOW(6), 'Carga inicial de inventario', l.id,
       (SELECT u.id FROM usuarios u WHERE u.username = 'encargado')
FROM lotes l
WHERE NOT EXISTS (SELECT 1 FROM movimientos m WHERE m.id_lote = l.id);

-- ---------------------------------------------------------------------
-- 3. CONSULTAS DE VERIFICACIÓN (equivalen a los endpoints de la API)
-- ---------------------------------------------------------------------
-- 3.1 Stock por producto  (GET /api/inventario/stock)
SELECT p.id, p.codigo, p.nombre, p.stock_minimo,
       COALESCE(SUM(l.cantidad_actual), 0) AS stock_actual
FROM productos p
LEFT JOIN lotes l ON l.id_producto = p.id
WHERE p.activo = 1
GROUP BY p.id, p.codigo, p.nombre, p.stock_minimo
ORDER BY p.nombre;

-- 3.2 Productos con stock bajo  (GET /api/alertas/stock-bajo)
SELECT p.codigo, p.nombre, p.stock_minimo,
       COALESCE(SUM(l.cantidad_actual), 0) AS stock_actual
FROM productos p
LEFT JOIN lotes l ON l.id_producto = p.id
WHERE p.activo = 1
GROUP BY p.id, p.codigo, p.nombre, p.stock_minimo
HAVING COALESCE(SUM(l.cantidad_actual), 0) < p.stock_minimo
ORDER BY p.nombre;

-- 3.3 Lotes que vencen en los próximos 30 días  (GET /api/lotes/por-vencer?dias=30)
SELECT l.codigo_lote, p.nombre, l.fecha_vencimiento, l.cantidad_actual
FROM lotes l
JOIN productos p ON p.id = l.id_producto
WHERE l.cantidad_actual > 0
  AND l.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
ORDER BY l.fecha_vencimiento;

-- 3.4 Historial de movimientos  (GET /api/movimientos)
SELECT m.id, m.tipo, m.motivo, m.cantidad, m.fecha, p.nombre AS producto, l.codigo_lote
FROM movimientos m
JOIN lotes l ON l.id = m.id_lote
JOIN productos p ON p.id = l.id_producto
ORDER BY m.fecha DESC;

-- ---------------------------------------------------------------------
-- 4. USUARIOS DE PRUEBA (los crea la aplicación; consulta para documentarlos)
-- ---------------------------------------------------------------------
-- Usuarios y roles creados por DataInitializer (la contraseña está cifrada con BCrypt):
SELECT u.username, u.nombre_completo, r.nombre AS rol, LEFT(u.password_hash, 7) AS inicio_hash
FROM usuarios u JOIN roles r ON r.id = u.id_rol;

-- Para generar las sentencias INSERT de los usuarios a partir de tu propia base
-- (útil para anexar al informe), ejecuta:
-- SELECT CONCAT('INSERT IGNORE INTO usuarios (username, password_hash, nombre_completo, activo, id_rol) VALUES (''',
--        u.username, ''',''', u.password_hash, ''',''', u.nombre_completo, ''',1,(SELECT id FROM roles WHERE nombre=''', r.nombre, '''));') AS sentencia
-- FROM usuarios u JOIN roles r ON r.id = u.id_rol;
