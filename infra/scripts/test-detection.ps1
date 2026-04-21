# Requiere: docker compose con edge-api (puerto host 18080), platform (8090), prometheus, payment, etc.
$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$base = "http://localhost:8090"
$edge = "http://localhost:18080"

Write-Host "== Comprobando edge en $edge =="
try {
  $hEdge = Invoke-WebRequest -Uri "$edge/actuator/health" -UseBasicParsing -TimeoutSec 5
  Write-Host "   edge health: $($hEdge.StatusCode)"
} catch {
  Write-Host "ERROR: No se alcanza el edge en $edge"
  Write-Host "   Arranca el stack:  docker compose up -d"
  Write-Host "   Detalle: $($_.Exception.Message)"
  exit 1
}

Write-Host "== Generando trafico con fallo en payment (15x) =="
$codes = @()
for ($i = 1; $i -le 15; $i++) {
  try {
    Invoke-WebRequest -Uri "$edge/api/flow?failPayment=true" -UseBasicParsing -TimeoutSec 30 | Out-Null
    $codes += "??"
  } catch {
    $resp = $_.Exception.Response
    if ($null -ne $resp) {
      $codes += [int]$resp.StatusCode
    } else {
      $codes += 0
    }
  }
}
Write-Host "   Codigos HTTP (esperado 500 en cada uno): $($codes -join ' ')"
$zeros = ($codes | Where-Object { $_ -eq 0 }).Count
$fives = ($codes | Where-Object { $_ -eq 500 }).Count
if ($zeros -gt 0) {
  Write-Host "ERROR: Hay respuestas 000 (sin conexion). El edge no recibio las peticiones correctamente."
  exit 1
}
if ($fives -lt 1) {
  Write-Host "AVISO: No hubo ningun HTTP 500; la regla de error rate puede no dispararse."
}

Write-Host "== Esperando 35s (Prometheus scrape + ventana rate[2m]) =="
Start-Sleep -Seconds 35

$loginBody = '{"username":"editor","password":"editor"}'
$auth = Invoke-RestMethod -Uri "$base/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
$token = $auth.accessToken
$h = @{ Authorization = "Bearer $token" }

Write-Host "== Login OK (token length: $($token.Length)) =="

$ruleId = "a0000000-0000-4000-8000-000000000001"
$ruleInfo = Invoke-RestMethod -Uri "$base/api/v1/rules/$ruleId" -Headers $h
Write-Host "== Regla (API): metricKey=$($ruleInfo.metricKey)  (debe ser SIMULATED_PAYMENT_FAIL) =="
if ($ruleInfo.metricKey -ne "SIMULATED_PAYMENT_FAIL") {
  Write-Host "ERROR: La regla no usa el contador demo. Aplica migracion V4 y reconstruye la imagen platform."
  Write-Host "   docker compose build --no-cache platform && docker compose up -d platform"
}

$prom = "http://localhost:9090"
Write-Host "== Prometheus: serie del contador demo en payment =="
try {
  $pq = [uri]::EscapeDataString('servicelens_demo_payment_simulated_failures_total{job="demo-payment"}')
  $pr = Invoke-RestMethod -Uri "$prom/api/v1/query?query=$pq" -TimeoutSec 10
  $cnt = $pr.data.result.Count
  if ($cnt -eq 0) {
    Write-Host "   NINGUNA serie: el contador no existe en Prometheus."
    Write-Host "   Reconstruye SOLO payment: docker compose build --no-cache payment && docker compose up -d payment"
  } else {
    $v = $pr.data.result[0].value[1]
    Write-Host "   Series encontradas: $cnt  valor instantaneo (counter): $v"
  }
} catch {
  Write-Host "   No se pudo llamar a Prometheus en $prom : $($_.Exception.Message)"
}

$preview = Invoke-RestMethod -Uri "$base/api/v1/rules/$ruleId/preview" -Headers $h
Write-Host "== Preview (lo que ve platform): baseUrl=$($preview.prometheusBaseUrl) empty=$($preview.prometheusResultEmpty) observed=$($preview.observed) breach=$($preview.breach) threshold=$($preview.threshold) comparison=$($preview.comparison) =="
Write-Host "   promql: $($preview.promql)"

$eval = Invoke-WebRequest -Uri "$base/api/v1/rules/$ruleId/evaluate" -Method POST -Headers $h -UseBasicParsing
Write-Host "== POST evaluate: $($eval.StatusCode) =="

$incidents = Invoke-RestMethod -Uri "$base/api/v1/incidents?page=0&size=10" -Headers $h
Write-Host "== Incidents totalElements: $($incidents.totalElements) =="
if ($incidents.items -and $incidents.items.Count -gt 0) {
  $first = $incidents.items[0]
  Write-Host "   First incident: $($first.id) title=$($first.title)"
  $iid = $first.id
  $events = Invoke-RestMethod -Uri "$base/api/v1/incidents/$iid/events" -Headers $h
  $alert = $events | Where-Object { $_.type -eq "ALERT_TRIGGERED" }
  if ($alert) {
    Write-Host "== ALERT_TRIGGERED: ruleName=$($alert.payload.ruleName) observed=$($alert.payload.observedValue) =="
  } else {
    Write-Host "== No ALERT_TRIGGERED en eventos (count: $($events.Count)) =="
  }
} else {
  Write-Host "   Sin incidentes: revisa Prometheus (metricas payment) y que la imagen platform tenga el PromQL actualizado."
}

$signals = Invoke-RestMethod -Uri "$base/api/v1/signals?page=0&size=10" -Headers $h
Write-Host "== Signals totalElements: $($signals.totalElements) =="
