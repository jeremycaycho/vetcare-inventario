# VetCare Inventario — Backend (Spring Boot 4.1.1 / Java 21)

API REST para el control de inventario de la clínica veterinaria VetCare: catálogos, lotes con vencimiento,
entradas y salidas (FEFO), alertas de stock bajo y reportes, con seguridad JWT por roles.

## Requisitos
- JDK 21, MySQL 8 en `localhost:3306`, IntelliJ IDEA, Postman.

## Puesta en marcha
1. Crear la base (opcional, la app la crea si el usuario tiene permiso):
   `CREATE DATABASE vetcare_inventario;` (o ejecutar `docs/esquema_vetcare.sql`).
2. Editar `src/main/resources/application.properties` → `spring.datasource.password`
   (o definir las variables de entorno `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).
3. **Maven Wrapper**: este zip no incluye `mvnw`. Genere uno en 1 minuto:
   - Entrar a https://start.spring.io → Maven, Java, Spring Boot 4.1.1, Java 21, Group `com.vetcare`, Artifact `inventario` → *Generate*.
   - Copiar de ese zip a esta carpeta: `mvnw`, `mvnw.cmd` y la carpeta `.mvn/`.
   - Verificar: `./mvnw -v` (Windows: `mvnw.cmd -v`).
4. Ejecutar: `./mvnw spring-boot:run` (o ▶ en `InventarioApplication`).
5. Probar: http://localhost:8080/api/ping → `{"estado":"ok","sistema":"VetCare Inventario"}`

## Usuarios de prueba (solo desarrollo)
| Usuario | Clave | Rol |
|---|---|---|
| admin | Admin123* | ADMINISTRADOR |
| encargado | Encargado123* | ENCARGADO |
| ventas | Ventas123* | VENTAS |
| gerente | Gerente123* | GERENTE |

## Endpoints
| Método | Ruta | Roles |
|---|---|---|
| POST | /api/auth/login | público |
| GET | /api/ping | público |
| GET/POST/PUT/DELETE | /api/categorias, /api/proveedores, /api/productos | GET: autenticado; resto: ADMINISTRADOR |
| POST | /api/movimientos/entradas | ADMINISTRADOR, ENCARGADO |
| POST | /api/movimientos/salidas | ADMINISTRADOR, ENCARGADO, VENTAS |
| GET | /api/movimientos | ADMINISTRADOR, ENCARGADO, GERENTE |
| GET | /api/inventario/stock | autenticado |
| GET | /api/alertas/stock-bajo | autenticado |
| GET | /api/lotes/por-vencer?dias=30 | autenticado |
| GET | /api/reportes/existencias | ADMINISTRADOR, GERENTE |

Errores: siempre `{timestamp, status, error, mensaje, ruta, detalles}`.

## Pruebas
Importar `docs/postman/vetcare.postman_collection.json` en Postman y ejecutarla con **Collection Runner**
sobre una base recién creada (58 peticiones con aserciones de éxito y fallo).

## CORS
Orígenes permitidos en `app.cors.allowed-origins` (por defecto `http://localhost:4200`, Angular).
