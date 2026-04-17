# ServiceLens — Sprint 1–2 (observabilidad + incidentes + UI)

Monorepo Maven con microservicios demo (**edge-api** → **checkout** → **payment**), API **platform** (Postgres + Flyway, incidentes/eventos/evidencia, JWT) y stack local: **OpenTelemetry Collector**, **Prometheus**, **Loki**, **Tempo**, **Grafana**. Frontend **Angular** en `frontend/` (lista y detalle de incidente con timeline).

Coste: **0 €** en local (imágenes Docker públicas; sin API de LLM en esta fase).

## Requisitos

- **JDK 17 o 21** (el proyecto compila con **17** por defecto; si tienes solo 17, no hace falta cambiar nada)
- **Maven** no es obligatorio: el repo incluye **Maven Wrapper** (`mvnw.cmd`)
- **Docker Desktop** (Compose v2)
- **Node.js 20+** y **npm** (solo para el frontend Angular en `frontend/`)

## Compilar JARs

En la raíz del repositorio (`ServiceLens`, donde está `pom.xml` y `mvnw.cmd`):

**Recomendado (sin instalar Maven):**

```powershell
.\mvnw.cmd clean package -DskipTests
```

La primera vez puede tardar: descarga Apache Maven 3.9.9 a tu carpeta de usuario (`.m2/wrapper`).

Si ya tienes **Maven** instalado y en el `PATH`, puedes usar también:

```powershell
mvn clean package -DskipTests
```

## Arrancar todo

```powershell
docker compose up --build
```

La primera vez descarga imágenes y construye los contenedores de las apps (puede tardar varios minutos).

## Puertos útiles

El **edge** está expuesto en el host como **18080** (no 8080) para no chocar con otras apps que suelen usar 8080 en Windows.

| Servicio    | URL / puerto |
|------------|----------------|
| Flujo demo | http://localhost:18080/api/flow |
| Flujo con fallo simulado en payment | http://localhost:18080/api/flow?failPayment=true |
| Latencia simulada en payment (ms) | http://localhost:18080/api/flow?paymentDelayMs=2000 |
| Platform meta | http://localhost:8090/api/v1/meta |
| Platform login (JWT) | `POST http://localhost:8090/api/v1/auth/login` (JSON `username` / `password`) |
| Incidentes (requiere `Authorization: Bearer …`) | `GET http://localhost:8090/api/v1/incidents` |
| Actuator health | http://localhost:8090/actuator/health |
| UI Angular (dev) | http://localhost:4200 (tras `npm start` en `frontend/`) |
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Tempo API | http://localhost:3200 |

## Usuarios demo (API y UI)

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `viewer` | `viewer` | lectura de incidentes |
| `editor` | `editor` | lectura + creación/edición |

La API usa **JWT** (`accessToken` en la respuesta de login). En Docker puedes fijar el secreto con la variable de entorno **`JWT_SECRET`** (mínimo **32 caracteres**); si no se define, Compose usa un valor de desarrollo por defecto.

## Frontend Angular

En el directorio `frontend/`:

```powershell
npm install
npm start
```

Abre **http://localhost:4200**, inicia sesión con **viewer**/**viewer** o **editor**/**editor**, y navega por el listado y el detalle (timeline de eventos). La URL base de la API está en `frontend/src/environments/environment.ts` (`http://localhost:8090`); el backend permite CORS desde `http://localhost:4200`.

## Qué comprobar (Sprint 1)

1. Tras arrancar, abre **Grafana** → **Explore** → datasource **Tempo** y busca trazas recientes.
2. Ejecuta varias veces: `GET http://localhost:18080/api/flow`
3. Deberías ver un trace que atraviesa **edge-api**, **checkout** y **payment**.
4. En **Explore** → **Loki**, prueba `{service="edge-api"}` o el nombre que salga en etiquetas (los logs JSON incluyen `traceId` cuando Micrometer rellena MDC).
5. En **Prometheus**, comprueba métricas `http_server_requests_seconds_*` por instancia.

## Qué comprobar (Sprint 2)

1. Arranca **platform** y **postgres** (p. ej. `docker compose up --build postgres platform` o el `compose` completo).
2. Opcional: crea un incidente con **editor** (`POST /api/v1/incidents` con JWT) o usa datos que hayas cargado por Flyway/seed.
3. Con la UI en **:4200**, verifica listado → detalle → timeline de eventos.
4. Con **viewer**, comprueba que solo puedes leer; con **editor**, que puedes mutar según el contrato (`CONTRATO-API.txt`).

## Servicios Docker (red interna)

- `edge-api:8080`, `checkout:8081`, `payment:8082`, `platform:8090`
- Trazas: apps → **OTel Collector** (`:4318`) → **Tempo**
- Métricas: Prometheus scrapea `/actuator/prometheus` de cada app

## Documentación de producto (sin código)

En la raíz del repo: `OBJETIVOS-Y-BACKLOG.txt`, `CONTRATO-API.txt`, `GUION-DEMO.txt`, `POLITICA-IA.txt`.

## Problemas frecuentes

- **Promtail retirado del compose**: con Docker Desktop actual, Promtail 2.9.x falla con *client version 1.42 is too old* (API mínima 1.44). El stack **no incluye Promtail** para evitar spam en logs; **Loki** sigue arrancando (vacío) y puedes usar **Tempo + Prometheus** como antes. Para ver logs de una app: `docker logs edge-api` (o el nombre del contenedor). Más adelante se puede usar **Grafana Alloy** o **Promtail 3.x** con config nueva.
- **Sin trazas en Tempo**: comprueba que `otel-collector` y `tempo` estén arriba y que el perfil `docker` esté activo (`SPRING_PROFILES_ACTIVE=docker` ya va en Compose).
- **Puerto 5432 ocupado**: cambia el mapeo de `postgres` en `docker-compose.yml` o detén otra instancia de Postgres local.
- **Puerto 8080 ocupado** (`bind: Only one usage of each socket address`): otra aplicación ya usa 8080. Este proyecto mapea **edge-api** a **18080** en el host (`18080:8080`). Si 18080 también falla, cámbialo en `docker-compose.yml` por otro libre (p. ej. `28080:8080`).
- **`connection failed` / `ERR_CONNECTION_REFUSED` en el navegador**: (1) Comprueba que usas **http://localhost:18080** para el flujo demo, **no 8080** (salvo que hayas cambiado el mapeo). (2) Si los contenedores `edge-api`, `checkout` o `platform` están **Exited**, mira `docker logs edge-api`: si sale **`no main manifest attribute, in /app/app.jar`**, el build de la imagen copió el JAR equivocado; vuelve a compilar con `.\mvnw.cmd clean package -DskipTests` y reconstruye sin caché: `docker compose build --no-cache edge-api checkout payment platform` y luego `docker compose up`.
