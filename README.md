# ServiceLens

Herramienta de **análisis de incidencias** orientada a backends y microservicios: correlación de **logs, métricas y trazas**, modelo de **incidente** (estado, timeline, evidencia) y API REST gobernada. La IA, cuando exista en el roadmap, debe **apoyar** la correlación y citar evidencia, no sustituirla (véase `POLITICA-IA.txt`).

Este repositorio es un **monorepo** listo para ejecutarse en local con **Docker Compose**: servicios demo instrumentados, stack de observabilidad y una API **platform** con persistencia en **PostgreSQL**.

## Qué incluye el repositorio

| Parte | Descripción |
|--------|-------------|
| **demo-edge-api**, **demo-checkout**, **demo-payment** | Cadena HTTP de ejemplo; trazas y métricas vía OpenTelemetry. |
| **platform** | API Spring Boot: incidentes, eventos, referencias de evidencia, autenticación **JWT** y roles **VIEWER** / **EDITOR**. Migraciones con **Flyway**. |
| **Stack local** | Postgres, **OpenTelemetry Collector**, **Tempo**, **Prometheus**, **Loki**, **Grafana**. |
| **frontend** | Aplicación **Angular**: inicio de sesión, listado y detalle de incidente con **timeline** de eventos. |

En la raíz hay documentación de producto (objetivos, contrato REST, guión de demo, política de IA): `OBJETIVOS-Y-BACKLOG.txt`, `CONTRATO-API.txt`, `GUION-DEMO.txt`, `POLITICA-IA.txt`.

## Requisitos

- **JDK 17** (u 21); el proyecto fija **17** en el build.
- **Maven Wrapper** incluido (`mvnw.cmd`); no hace falta instalar Maven.
- **Docker Desktop** con Compose v2.
- **Node.js 20+** y **npm** para el frontend en `frontend/`.

## Compilar los JARs

Desde la raíz del repo (donde está `pom.xml`):

```powershell
.\mvnw.cmd clean package -DskipTests
```

Si tienes Maven en el `PATH`, puedes usar `mvn clean package -DskipTests`.

## Arrancar el entorno con Docker

```powershell
docker compose up --build
```

La primera vez descarga imágenes y construye imágenes locales; puede tardar varios minutos.

## Puertos útiles

El **edge** queda en el host en **18080** (no 8080) para evitar conflictos con otros servicios en Windows.

| Recurso | URL / notas |
|---------|-------------|
| Flujo demo | http://localhost:18080/api/flow |
| Fallo simulado en payment | http://localhost:18080/api/flow?failPayment=true |
| Latencia simulada (ms) | http://localhost:18080/api/flow?paymentDelayMs=2000 |
| Meta platform | http://localhost:8090/api/v1/meta |
| Login JWT | `POST http://localhost:8090/api/v1/auth/login` (cuerpo JSON: `username`, `password`) |
| Incidentes | `GET http://localhost:8090/api/v1/incidents` (cabecera `Authorization: Bearer …`) |
| Health | http://localhost:8090/actuator/health |
| UI Angular (desarrollo) | http://localhost:4200 tras `npm start` en `frontend/` |
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Tempo | http://localhost:3200 |

## Usuarios demo

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `viewer` | `viewer` | Solo lectura de incidentes |
| `editor` | `editor` | Lectura y operaciones de escritura según API |

La respuesta de login incluye el **accessToken** JWT. En Docker puedes definir **`JWT_SECRET`** (mínimo **32 caracteres**); si no se define, Compose usa un valor solo para desarrollo.

## Frontend Angular

```powershell
cd frontend
npm install
npm start
```

La URL base de la API está en `frontend/src/environments/environment.ts` (por defecto `http://localhost:8090`). El backend admite CORS desde `http://localhost:4200`.

## Cómo comprobar que todo encaja

**Observabilidad**

1. Abre **Grafana** → **Explore** → datasource **Tempo** y busca trazas recientes.
2. Llama varias veces a `GET http://localhost:18080/api/flow`.
3. Deberías ver un trace que cruza **edge-api**, **checkout** y **payment**.
4. En **Explore** → **Loki**, prueba etiquetas de servicio (p. ej. según lo que muestre tu configuración).
5. En **Prometheus**, revisa métricas HTTP por instancia.

**Plataforma e interfaz**

1. Con **Postgres** y **platform** en marcha (solo esos servicios o el `compose` completo), autentica con **editor** y crea un incidente vía API si lo necesitas, o usa datos ya presentes en base de datos.
2. En la UI (**:4200**), revisa listado → detalle → **timeline** de eventos.
3. Con **viewer**, confirma acceso de solo lectura; con **editor**, las operaciones permitidas por `CONTRATO-API.txt`.

## Red interna (Docker)

- Servicios de aplicación: `edge-api:8080`, `checkout:8081`, `payment:8082`, `platform:8090`.
- Trazas: aplicaciones → **OTel Collector** (`:4318`) → **Tempo**.
- Métricas: Prometheus hace scrape de `/actuator/prometheus` en cada app.

## Problemas frecuentes

- **Promtail no está en el compose**: versiones antiguas de Promtail chocan con la API de Docker reciente (*client version 1.42 is too old*). **Loki** puede arrancar vacío; para logs de una app usa `docker logs <contenedor>`. Alternativas futuras: Alloy o Promtail 3.x.
- **Sin trazas en Tempo**: comprueba que `otel-collector` y `tempo` estén arriba y que las apps usen el perfil `docker` (ya definido en Compose).
- **Puerto 5432 ocupado**: cambia el mapeo de `postgres` en `docker-compose.yml` o libera el puerto.
- **8080 ocupado en el host**: el edge usa **18080** en el host. Si falla, cambia el mapeo en `docker-compose.yml`.
- **Error al abrir el flujo demo o APIs**: confirma la URL (**18080** para el edge, **8090** para platform). Si un contenedor de app está **Exited**, revisa `docker logs`. Si aparece **`no main manifest attribute`** en el JAR, recompila con `.\mvnw.cmd clean package -DskipTests` y reconstruye la imagen (`docker compose build --no-cache` en los servicios afectados).
