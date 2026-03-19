# Medstrack — API de Facturación Electrónica

Backend Spring Boot para integración con Factus/DIAN. Permite emitir, consultar y descargar facturas electrónicas desde Medstrack sin necesidad de acceder al portal de Factus.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.3 |
| Maven | 3.x |
| springdoc-openapi | 2.8.5 |
| Lombok | — |
| Spring Actuator | — |

---

## Estructura del proyecto

```
src/main/java/com/felipesulez/reto_facturacion/
├── config/
│   ├── FactusProperties.java       ← Configuración centralizada
│   ├── RestTemplateConfig.java     ← Timeouts y interceptor
│   └── TokenInterceptor.java       ← Renovación automática de token
├── controller/
│   └── InvoiceController.java      ← Endpoints REST
├── dto/
│   ├── CustomerDTO.java            ← Datos del cliente
│   ├── InvoiceRequest.java         ← Cuerpo del POST
│   ├── InvoiceResponse.java        ← Respuesta tipada al cliente
│   ├── ItemDTO.java                ← Productos/servicios
│   ├── PaymentDetailsDTO.java      ← Forma y método de pago
│   └── factus/
│       ├── FactusApiResponse.java  ← Wrapper POST validate
│       ├── FactusBill.java         ← Factura validada
│       ├── FactusBillListResponse.java ← Listado paginado
│       ├── FactusBillSummary.java  ← Resumen por factura
│       ├── FactusCodeName.java     ← {code, name} reutilizable
│       ├── FactusData.java         ← data del POST
│       ├── FactusPagination.java   ← Paginación del listado
│       └── FactusRelatedDocument.java ← Notas crédito/débito
├── exception/
│   ├── ErrorResponse.java          ← Estructura de error
│   └── GlobalExceptionHandler.java ← Manejo centralizado
├── service/
│   └── FactusService.java          ← Lógica de negocio
└── util/
    └── NitUtils.java               ← Cálculo dígito verificador
```

---

## Endpoints disponibles

### POST `/api/v1/invoices/send` — Emitir factura

Valida y registra la factura en Factus/DIAN.

El sistema aplica automáticamente:
- Cálculo del dígito de verificación (DV) del NIT
- Forma de pago por defecto (contado/efectivo)
- IVA del 19% si no se especifica
- Campos técnicos requeridos por la DIAN

Si el sandbox devuelve un 409 (rango bloqueado por factura pendiente), el sistema la elimina automáticamente y reintenta.

**Body mínimo:**
```json
{
  "reference_code": "MEDS-001",
  "customer": {
    "identification": "123456789",
    "company": "Empresa SAS",
    "names": "Juan Pérez",
    "address": "Calle 10 # 5-20, Bogotá",
    "email": "juan@empresa.com"
  },
  "items": [
    {
      "name": "Consultoría técnica",
      "quantity": 1,
      "price": 80000
    }
  ]
}
```

**Respuesta:**
```json
{
  "numero": "SETP990026624",
  "referenceCode": "MEDS-001",
  "cufe": "b4fb4f893592c863f3b3419c...",
  "publicUrl": "http://app-sandbox.factus.com.co/documents/bills/...",
  "estado": "VALIDADA",
  "fechaValidacion": "18-03-2026 05:13:10 PM",
  "total": 95200.00,
  "totalImpuestos": 15200.00
}
```

---

### GET `/api/v1/invoices` — Listar y filtrar facturas

Devuelve el listado paginado de facturas con filtros opcionales.

**Parámetros (todos opcionales):**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `referenceCode` | string | Código de referencia interno |
| `number` | string | Número de factura DIAN |
| `identification` | string | NIT o cédula del cliente |
| `status` | integer | 1=validadas, 0=pendientes |
| `page` | integer | Número de página (desde 1) |

**Ejemplos:**
```
GET /api/v1/invoices
GET /api/v1/invoices?number=SETP990026624
GET /api/v1/invoices?identification=123456789&status=1
GET /api/v1/invoices?referenceCode=MEDS-001
GET /api/v1/invoices?page=2
```

---

### GET `/api/v1/invoices/download-pdf/{number}` — Descargar PDF

Descarga el PDF binario de una factura validada.

```
GET /api/v1/invoices/download-pdf/SETP990026624
```

El archivo se sirve con `Content-Disposition: attachment` — el navegador lo descarga directamente.

---

## Configuración

### Perfiles

| Perfil | Uso | Credenciales |
|---|---|---|
| `dev` | Desarrollo local | Hardcodeadas en `application-dev.yaml` |
| `prod` | Railway/Producción | Variables de entorno |

### Variables de entorno (perfil prod)

| Variable | Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `FACTUS_API_URL` | URL de la API de Factus |
| `FACTUS_CLIENT_ID` | Client ID OAuth2 |
| `FACTUS_CLIENT_SECRET` | Client Secret OAuth2 |
| `FACTUS_USERNAME` | Usuario de Factus |
| `FACTUS_PASSWORD` | Contraseña de Factus |

### Ambientes de Factus

| Ambiente | URL |
|---|---|
| Sandbox (pruebas) | `https://api-sandbox.factus.com.co` |
| Producción DIAN | `https://api.factus.com.co` |

---

## Ejecución local

```bash
# Clonar el repositorio
git clone git@github.com:felipesulez/Medstrack-factus.git
cd Medstrack-factus

# Crear application-dev.yaml con credenciales sandbox (no está en el repo)
# Ver sección de configuración para las variables requeridas

# Ejecutar en perfil dev
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Swagger UI en `http://localhost:8080/swagger-ui.html`.

---

## Documentación de la API

Con el servidor corriendo, Swagger UI provee documentación interactiva completa:

```
http://localhost:8080/swagger-ui.html
```

Incluye ejemplos de request, esquemas de respuesta y la posibilidad de ejecutar cada endpoint directamente desde el navegador.

---

## Comportamiento automático del sistema

**Autenticación OAuth2**
El sistema hace login automáticamente al arrancar. Si el token expira (cada hora), el `TokenInterceptor` detecta el 401 y renueva el token sin interrumpir la operación.

**Defaults aplicados automáticamente**
Si no se envían en el request, el sistema completa:
- `numbering_range_id`: 8
- `payment_form`: 1 (contado)
- `payment_method`: 10 (efectivo)
- `operation_type`: 10 (estándar)
- `tax_rate`: 19%
- `municipality_id`: 980 (San Gil)
- `legal_organization_id`: 1 (persona jurídica)
- `tribute_id`: 21 (no responsable IVA)

**Cálculo de DV**
Si el cliente es persona jurídica (NIT con más de 9 dígitos), el sistema calcula el dígito de verificación automáticamente.

**Auto-sanación de sandbox bloqueado**
Si Factus devuelve 409 por factura pendiente, el sistema busca y elimina la factura pendiente automáticamente, luego reintenta el envío.

---

## Despliegue en Railway

Crear los siguientes archivos en la raíz del proyecto:

**`railway.toml`**
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

**`system.properties`**
```properties
java.runtime.version=17
```

Luego configurar las variables de entorno en Railway y conectar el repositorio.

El healthcheck `/actuator/health` responde automáticamente para que Railway sepa si el pod está disponible.

---

## Pendiente

- `GET /api/v1/invoices/{number}` — Detalle completo de una factura
- `POST /api/v1/invoices/{number}/send-email` — Reenviar correo al cliente
- `GET /api/v1/invoices/{number}/xml` — Descargar XML para contabilidad
- Deserializador tipado para campo `errors` (actualmente `Object`)
- Deshabilitar Swagger al pasar a producción DIAN real

---

## Repositorio

`git@github.com:felipesulez/Medstrack-factus.git`