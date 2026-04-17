# ServiceLens — Sprint 1 (observabilidad + demos)

Monorepo Maven con microservicios demo (**edge-api** → **checkout** → **payment**), API **platform** (Postgres + Flyway) y stack local: **OpenTelemetry Collector**, **Prometheus**, **Loki**, **Promtail**, **Tempo**, **Grafana**.

Coste: **0 €** en local (imágenes Docker públicas; sin API de LLM en esta fase).

## Requisitos

- **JDK 17 o 21** (el proyecto compila con **17** por defecto; si tienes solo 17, no hace falta cambiar nada)
- **Maven** no es obligatorio: el repo incluye **Maven Wrapper** (`mvnw.cmd`)
- **Docker Desktop** (Compose v2)

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

| Servicio    | URL / puerto |
|------------|----------------|
| Flujo demo | http://localhost:8080/api/flow |
| Flujo con fallo simulado en payment | http://localhost:8080/api/flow?failPayment=true |
| Latencia simulada en payment (ms) | http://localhost:8080/api/flow?paymentDelayMs=2000 |
| Platform meta | http://localhost:8090/api/v1/meta |
| Actuator health | http://localhost:8090/actuator/health |
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Tempo API | http://localhost:3200 |

## Qué comprobar (Sprint 1)

1. Tras arrancar, abre **Grafana** → **Explore** → datasource **Tempo** y busca trazas recientes.
2. Ejecuta varias veces: `GET http://localhost:8080/api/flow`
3. Deberías ver un trace que atraviesa **edge-api**, **checkout** y **payment**.
4. En **Explore** → **Loki**, prueba `{service="edge-api"}` o el nombre que salga en etiquetas (los logs JSON incluyen `traceId` cuando Micrometer rellena MDC).
5. En **Prometheus**, comprueba métricas `http_server_requests_seconds_*` por instancia.

## Servicios Docker (red interna)

- `edge-api:8080`, `checkout:8081`, `payment:8082`, `platform:8090`
- Trazas: apps → **OTel Collector** (`:4318`) → **Tempo**
- Métricas: Prometheus scrapea `/actuator/prometheus` de cada app

## Documentación de producto (sin código)

En la raíz del repo: `OBJETIVOS-Y-BACKLOG.txt`, `CONTRATO-API.txt`, `GUION-DEMO.txt`, `POLITICA-IA.txt`.

## Problemas frecuentes

- **Promtail / Loki**: si Promtail no puede leer el socket de Docker, revisa Docker Desktop → integración WSL. Los logs siguen viéndose con `docker logs <contenedor>`.
- **Sin trazas en Tempo**: comprueba que `otel-collector` y `tempo` estén arriba y que el perfil `docker` esté activo (`SPRING_PROFILES_ACTIVE=docker` ya va en Compose).
- **Puerto 5432 ocupado**: cambia el mapeo de `postgres` en `docker-compose.yml` o detén otra instancia de Postgres local.
