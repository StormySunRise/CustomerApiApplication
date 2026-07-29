# Customer API — Pruebas de carga con JMeter

Plan de pruebas JMeter para verificar el comportamiento concurrente de la API REST de clientes.

## Requisitos

- Aplicación ejecutándose localmente
- Apache JMeter 5.x instalado ([descargar](https://jmeter.apache.org/download_jmeter.cgi))
- JDK 17 (compatible con JMeter 5.x)

## Iniciar la API

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

## Ejecutar JMeter

```powershell
jmeter -n `
  -t performance/customer-api-load-test.jmx `
  -Jhost=localhost `
  -Jport=8080 `
  -l performance/results.jtl
```

### Generar reporte HTML

```powershell
jmeter -g performance/results.jtl -o performance/report
```

### Ejecutar con valores diferentes

```powershell
jmeter -n -t performance/customer-api-load-test.jmx -Jhost=192.168.1.100 -Jport=8080 -l results.jtl
```

## Escenarios

### 1. Creación con emails únicos

- **Hilos**: 30
- **Ramp-up**: 10 segundos
- **Iteraciones**: 5 por hilo
- **Total de solicitudes**: 150
- **Email**: generado con `${__UUID()}` para evitar duplicados
- **Assertions**: status `201`, campo `$.id` presente
- **Resultado esperado**: 100 % de respuestas `201`

### 2. Email duplicado concurrente

- **Hilos**: 10
- **Ramp-up**: 2 segundos
- **Iteraciones**: 1 por hilo
- **Email fijo**: `concurrent@example.com` (todos los hilos envían el mismo)
- **Assertions**: status `201` o `409` (regex); conteo mediante JSR223 PostProcessor con Groovy
- **Resultado esperado**: exactamente una respuesta `201`, el resto `409`

### 3. Consultas concurrentes

- **Hilos**: 10
- **Ramp-up**: 5 segundos
- **Iteraciones**: 3 por hilo
- **Total de solicitudes**: 30
- **Endpoint**: `GET /customers`
- **Assertions**: status `200`
- **Resultado esperado**: 100 % de respuestas `200`

### TearDown — Verificación

- 1 hilo, 1 iteración después de todos los grupos anteriores
- Realiza `GET /customers` y mediante JSR223 Assertion verifica que exista exactamente un cliente con email `concurrent@example.com`
- Verifica también que el contador de respuestas `201` del escenario 2 sea exactamente 1

## Variables configurables

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `host` | `localhost` | Host de la API |
| `port` | `8080` | Puerto de la API |
| `protocol` | `http` | Protocolo HTTP |

Se sobrescriben desde línea de comandos con `-Jnombre=valor`.

## Archivos

| Archivo | Propósito |
|---|---|
| `customer-api-load-test.jmx` | Plan de pruebas JMeter |
| `data/customers.csv` | Datos de ejemplo para escenarios basados en CSV |
| `README.md` | Esta documentación |

## Resultados esperados

- Escenario 1: 150 respuestas `201 Created`
- Escenario 2: exactamente 1 respuesta `201`, 9 respuestas `409 Conflict`
- Escenario 3: 30 respuestas `200 OK`
- Escenario TearDown: assertion pasa si el contador `201` del escenario 2 es 1 y el cliente duplicado aparece exactamente una vez en el listado final
- Ninguna respuesta `500 Internal Server Error`

> **Nota**: estos son resultados esperados según el diseño de la aplicación. No se ejecutó la prueba porque JMeter no está instalado en el entorno actual.

## Archivos generados (no incluidos en el repositorio)

- `results.jtl` — resultados en formato CSV
- `report/` — reporte HTML generado
- `jmeter.log` — bitácora de JMeter
- `*.log` — otros archivos de log

Ver `.gitignore` para las exclusiones correspondientes.
