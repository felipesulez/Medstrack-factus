# Medstrack Facturación — Integración Factus / DIAN

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Deploy-Railway-0B0D0E?style=flat-square&logo=railway&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docs-Swagger_UI-85EA2D?style=flat-square&logo=swagger&logoColor=black"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square"/>
</p>

<p align="center">
  API REST en Spring Boot para emitir, consultar y descargar <strong>facturas electrónicas</strong> validadas ante la DIAN a través de la API de Factus.<br/>
  Backend de integración para la plataforma <strong>Medstrack</strong>.
</p>

---

## Arquitectura

![Diagrama de secuencia](docs/architecture.svg)

---

## Funcionalidades

- **Emisión de facturas electrónicas** validadas ante la DIAN vía Factus
- **Cálculo automático del DV** del NIT del cliente (algoritmo oficial DIAN)
- **Enriquecimiento de defaults**: IVA 19%, forma de pago, tipo de operación y tipo de documento se completan automáticamente si se omiten
- **Renovación automática del token OAuth2**: si el token expira mid-request, el interceptor lo refresca y reintenta de forma transparente
- **Auto-limpieza del sandbox**: ante un 409 (rango bloqueado por factura pendiente), el sistema la elimina y reintenta sin intervención manual
- **Descarga de PDF** de cualquier factura por número DIAN
- **Listado y filtrado paginado** de facturas por referencia, número, NIT o estado

---

## Endpoints

Base URL: `/api/v1/invoices`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/send` | Emite y valida una factura electrónica |
| `GET` | `/` | Lista facturas con filtros opcionales |
| `GET` | `/download-pdf/{number}` | Descarga el PDF binario de una factura |
| `GET` | `/debug/token` | Muestra el token activo *(solo perfil `dev`)* |

Documentación interactiva: [`/swagger-ui/index.html`](https://medstrack-factus-production.up.railway.app/swagger-ui/index.html)

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.4.3 |
| HTTP cliente | RestTemplate + interceptor OAuth2 |
| Validación | Spring Validation (`@Valid`) |
| Documentación | Springdoc OpenAPI / Swagger UI 2.8.5 |
| Monitoreo | Spring Actuator (`/actuator/health`) |
| Build & Deploy | Maven + Railway (Nixpacks) |

---

## Estructura del proyecto

```
src/main/java/.../reto_facturacion/
├── controller/
│   └── InvoiceController.java       # Endpoints REST con anotaciones Swagger
├── service/
│   └── FactusService.java           # Lógica de negocio, tokens, retry y limpieza sandbox
├── config/
│   ├── FactusProperties.java        # Propiedades tipadas (@ConfigurationProperties)
│   ├── RestTemplateConfig.java      # Bean RestTemplate con timeouts e interceptor
│   └── TokenInterceptor.java        # Renovación automática de token en 401
├── dto/
│   ├── InvoiceRequest.java          # Payload de entrada con validaciones @Valid
│   ├── InvoiceResponse.java         # Respuesta mapeada desde Factus
│   ├── CustomerDTO.java             # Datos del cliente receptor
│   ├── ItemDTO.java                 # Líneas de producto/servicio
│   └── factus/                      # DTOs que mapean la respuesta interna de Factus
├── exception/
│   ├── GlobalExceptionHandler.java  # Manejo centralizado de errores (4xx, 5xx, @Valid)
│   └── ErrorResponse.java           # Estructura uniforme de error
└── util/
    └── NitUtils.java                # Algoritmo DIAN para cálculo del dígito verificador
```

---

## Configuración

Todas las credenciales se inyectan como **variables de entorno**. Nunca se hardcodean en el código ni en archivos de configuración commiteados.

> ⚠️ Asegúrate de añadir `src/main/resources/application-dev.yaml` a tu `.gitignore`.

### Variables requeridas

| Variable | Descripción |
|---|---|
| `FACTUS_API_URL` | URL base de la API de Factus |
| `FACTUS_CLIENT_ID` | Client ID OAuth2 |
| `FACTUS_CLIENT_SECRET` | Client Secret OAuth2 |
| `FACTUS_USERNAME` | Usuario de la cuenta Factus |
| `FACTUS_PASSWORD` | Contraseña de la cuenta Factus |

### Variables opcionales

| Variable | Descripción | Defecto |
|---|---|---|
| `FACTUS_RANGE_ID` | ID del rango de numeración DIAN | `8` |
| `FACTUS_MUN_ID` | ID de municipio por defecto | `980` |
| `FACTUS_CONNECT_TIMEOUT` | Timeout de conexión (ms) | `5000` |
| `FACTUS_READ_TIMEOUT` | Timeout de lectura (ms) | `15000` |

---

## Ejecución local

```bash
# 1. Clonar y compilar
git clone https://github.com/felipesulez/Medstrack-factus.git
cd Medstrack-factus
mvn clean package -DskipTests

# 2. Definir variables de entorno (nunca en el YAML)
export FACTUS_API_URL=https://api-sandbox.factus.com.co
export FACTUS_CLIENT_ID=tu_client_id
export FACTUS_CLIENT_SECRET=tu_client_secret
export FACTUS_USERNAME=tu_usuario
export FACTUS_PASSWORD=tu_password

# 3. Ejecutar en perfil dev
java -Dspring.profiles.active=dev -jar target/*.jar

# 4. Abrir Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

Al arrancar en perfil `dev`, `FactusRunner` ejecuta automáticamente un smoke test que emite una factura de prueba y confirma la conexión con Factus.

---

## Despliegue en Railway

El archivo `railway.toml` ya está configurado:

```toml
[build]
builder = "NIXPACKS"

[deploy]
startCommand = "java -Dspring.profiles.active=prod -jar target/*.jar"
healthcheckPath = "/actuator/health"
healthcheckTimeout = 30
restartPolicyType = "ON_FAILURE"
restartPolicyMaxRetries = 3
```

Solo define las variables de entorno en el panel de Railway y haz push. El health check apunta a `/actuator/health`.

---

## Ejemplo de petición

```bash
curl -X POST https://medstrack-factus-production.up.railway.app/api/v1/invoices/send \
  -H "Content-Type: application/json" \
  -d '{
    "reference_code": "MEDS-2026-001",
    "observation": "Servicio de consultoría técnica",
    "customer": {
      "identification": "901234567",
      "company": "Medstrack SAS",
      "names": "Felipe Sulez",
      "address": "Calle 5 # 2-10, Popayán",
      "email": "contacto@medstrack.com.co"
    },
    "items": [
      {
        "code_reference": "SRV-001",
        "name": "Consultoría plataforma Medstrack",
        "quantity": "1.00",
        "price": "500000.00"
      }
    ]
  }'
```

Los campos opcionales (`numbering_range_id`, `payment_form`, `payment_method`, `operation_type`, `dv`, `municipality_id`) se completan automáticamente si se omiten.

---

## Tests

```bash
mvn test
```

Incluye `NitUtilsTest` con casos de prueba para el algoritmo de cálculo del DV del NIT.

---

## Seguridad

- Las credenciales se gestionan exclusivamente mediante variables de entorno
- `application-dev.yaml` está en `.gitignore` y nunca se commitea
- El endpoint `/debug/token` solo está activo en el perfil `dev`, nunca en producción
- En producción, Actuator expone únicamente `/actuator/health` e `/actuator/info`

---

## Autor

**Felipe Sulez** — [@felipesulez](https://github.com/felipesulez)