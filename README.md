# franquicias-api

API REST para administrar franquicias, sus sucursales y los productos de cada sucursal. Permite consultar cuál es el producto con más stock en cada sucursal de una franquicia.

Construida con Java 21, Spring Boot 4 (WebFlux) y MongoDB, siguiendo Clean Architecture.

**API desplegada:** https://franquicias-api-6cubf5m5aa-uc.a.run.app

```bash
curl https://franquicias-api-6cubf5m5aa-uc.a.run.app/api/franquicias/6aee060fffb64b61679458ab/productos/mayor-stock
```

## Contenido

- [Ejecutar con Docker](#ejecutar-con-docker)
- [Ejecutar sin Docker](#ejecutar-sin-docker)
- [Tests](#tests)
- [Endpoints](#endpoints)
- [Diseño de la solución](#diseño-de-la-solución)
- [Despliegue en la nube](#despliegue-en-la-nube)
- [Flujo de trabajo con Git](#flujo-de-trabajo-con-git)

## Ejecutar con Docker

Es la forma más rápida. Solo se necesita [Docker](https://www.docker.com/) con Docker Compose.

```bash
git clone https://github.com/DerekDraimon/franquicias-api.git
cd franquicias-api
docker compose up -d --build
```

Esto levanta dos contenedores:

| Servicio | Puerto | Descripción |
|---|---|---|
| `api` | 8080 | la aplicación |
| `mongo` | 27017 | MongoDB 8 con un volumen para los datos |

La API espera a que MongoDB esté listo antes de arrancar. Para detener todo y borrar los datos:

```bash
docker compose down -v
```

## Ejecutar sin Docker

Se necesita el JDK 21 y un MongoDB accesible.

```bash
./gradlew bootRun
```

Por defecto se conecta a `mongodb://localhost:27017/franquicias`. Para usar otra base de datos se define la variable `MONGODB_URI`:

```bash
MONGODB_URI="mongodb+srv://usuario:clave@cluster.mongodb.net/franquicias" ./gradlew bootRun
```

| Variable | Valor por defecto | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/franquicias` | conexión a MongoDB |
| `PORT` | `8080` | puerto HTTP (Cloud Run lo define automáticamente) |

## Tests

```bash
./gradlew test
```

**Docker debe estar corriendo**: los tests de los adaptadores de Mongo y los de integración levantan un MongoDB real con [Testcontainers](https://testcontainers.com/).

| Nivel | Qué prueba | Cantidad |
|---|---|---|
| Dominio | modelos y casos de uso, con mocks | 31 |
| Adaptadores | cada controlador y cada adaptador de Mongo por separado | 24 |
| Integración | la aplicación completa por HTTP contra MongoDB, y la carga del contexto | 5 |

## Endpoints

Todas las rutas empiezan por `/api`.

| Acción | Método y ruta | Body | Respuesta |
|---|---|---|---|
| Crear franquicia | `POST /api/franquicias` | `{ "nombre": "..." }` | 201 |
| Renombrar franquicia | `PATCH /api/franquicias/{id}` | `{ "nombre": "..." }` | 200 / 404 |
| Crear sucursal | `POST /api/franquicias/{id}/sucursales` | `{ "nombre": "..." }` | 201 / 404 |
| Renombrar sucursal | `PATCH /api/sucursales/{id}` | `{ "nombre": "..." }` | 200 / 404 |
| Crear producto | `POST /api/sucursales/{id}/productos` | `{ "nombre": "...", "stock": 10 }` | 201 / 404 |
| Modificar stock y/o nombre de un producto | `PATCH /api/productos/{id}` | `{ "stock": 50 }` o `{ "nombre": "..." }` | 200 / 404 |
| Eliminar producto | `DELETE /api/productos/{id}` | - | 204 / 404 |
| Producto con más stock por sucursal | `GET /api/franquicias/{id}/productos/mayor-stock` | - | 200 / 404 |

### Ejemplo completo

```bash
# Crear una franquicia
curl -X POST localhost:8080/api/franquicias \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Don Mario"}'
# {"id":"6aed...d07d","nombre":"Don Mario"}

# Crear una sucursal en esa franquicia
curl -X POST localhost:8080/api/franquicias/{franquiciaId}/sucursales \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Centro"}'

# Agregar un producto a la sucursal
curl -X POST localhost:8080/api/sucursales/{sucursalId}/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Pizza", "stock": 30}'

# Cambiar el stock
curl -X PATCH localhost:8080/api/productos/{productoId} \
  -H "Content-Type: application/json" \
  -d '{"stock": 50}'

# Producto con más stock de cada sucursal
curl localhost:8080/api/franquicias/{franquiciaId}/productos/mayor-stock
```

Respuesta de la última consulta:

```json
[
  {
    "productoId": "6aed...b8fc",
    "nombre": "Pizza",
    "stock": 50,
    "sucursalId": "6aed...b8fa",
    "sucursalNombre": "Centro"
  }
]
```

Reglas de esta consulta:

- Se devuelve un producto por sucursal.
- Las sucursales sin productos no aparecen.
- Si dos productos de la misma sucursal tienen el mismo stock, se devuelve solo uno.

### Validaciones y errores

- `nombre` es obligatorio y no puede estar vacío.
- `stock` no puede ser negativo. Al crear un producto es opcional; si no se envía, vale 0.
- En el `PATCH` de un producto debe venir al menos el nombre o el stock.

Todos los errores tienen el mismo formato:

```json
{ "codigo": 404, "mensaje": "No existe la franquicia con id 123" }
```

| Código | Cuándo |
|---|---|
| 400 | un dato no cumple las validaciones o el JSON está mal formado |
| 404 | la franquicia, sucursal o producto no existe |

## Diseño de la solución

### Tecnologías

| Tema | Elección |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 con WebFlux |
| Base de datos | MongoDB (driver reactivo de Spring Data) |
| Build | Gradle con wrapper |
| Contenedor | Docker |
| Infraestructura | Terraform |
| Nube | Google Cloud Run + MongoDB Atlas |

**WebFlux:** atiende las peticiones de forma no bloqueante. Mientras espera la respuesta de la base de datos, el hilo queda libre para atender otras solicitudes, así que con pocos hilos se pueden manejar muchas peticiones concurrentes. Para aprovechar esto, el acceso a datos también tiene que ser no bloqueante; por eso se usa el driver reactivo de MongoDB y no JPA/JDBC.

**MongoDB:** tiene soporte reactivo completo en Spring Data, ofrece un plan gratuito en la nube (Atlas) y cuenta con proveedor oficial de Terraform. Se evaluaron MySQL/Postgres con R2DBC, pero implican más código manual para las relaciones.

### Modelo de datos

Se usan tres colecciones separadas, relacionadas por id.

```
franquicias              sucursales                 productos
-----------              ----------                 ---------
id                       id                         id
nombre                   nombre                     nombre
                         franquiciaId  ───────►     stock
                                                    sucursalId  ───────►
```

Índices:

- `sucursales.franquiciaId`, para buscar las sucursales de una franquicia.
- `productos.sucursalId` + `stock` descendente, para encontrar rápido el producto con más stock de una sucursal.

**Por qué colecciones separadas y no documentos embebidos:**

- Cada operación modifica un solo documento. Cambiar el stock es actualizar un producto por su id, sin recorrer arreglos anidados.
- Los documentos no crecen sin límite. Con todo embebido, una franquicia grande podría acercarse al límite de 16 MB por documento.
- Los repositorios quedan simples: uno por colección, con consultas derivadas como `findFirstBySucursalIdOrderByStockDesc`.

**Lo que se pierde:**

- Consultar el producto con más stock requiere varias consultas en lugar de una.
- MongoDB no valida referencias, así que la aplicación comprueba que la franquicia o la sucursal existan antes de crear algo dentro de ellas.

### Diseño de la API

Se usa anidamiento superficial: la ruta se anida al crear un recurso que depende de otro (`POST /franquicias/{id}/sucursales`), y cuando ya se tiene el id del recurso se accede directamente a él (`DELETE /productos/{id}`).

Se usa `PATCH` en lugar de `PUT` porque solo se cambia una parte del recurso.

### Arquitectura

Clean Architecture en un solo módulo, separando paquetes:

```
com.derek.franquicias
├── domain
│   ├── model        Franquicia, Sucursal, Producto, ProductoMayorStock
│   ├── gateway      interfaces que el dominio necesita para guardar y consultar datos
│   ├── usecase      FranquiciaUseCase, SucursalUseCase, ProductoUseCase, MayorStockUseCase
│   └── exception    excepciones del negocio
├── infrastructure
│   ├── entrypoint   controladores REST, DTOs y manejo de errores
│   └── mongo        documentos, repositorios reactivos y adaptadores de los gateways
└── config           registro de los casos de uso como beans
```

Reglas:

1. `domain` no depende de Spring ni de MongoDB. Los casos de uso se registran como beans en `config`.
2. `infrastructure` depende de `domain`, nunca al contrario.
3. Los documentos de MongoDB son clases distintas a los modelos de dominio y se convierten con mappers.
4. Las validaciones del negocio (nombre obligatorio, stock no negativo) viven en los modelos de dominio, así no puede existir un objeto inválido.

Para no complicar la estructura más de lo necesario:

- Un solo módulo de Gradle en vez de un proyecto multimódulo.
- Un caso de uso por entidad en vez de uno por cada acción. La consulta del producto con más stock va en su propio caso de uso porque usa las tres entidades.
- Controladores con `@RestController` en vez de `RouterFunction`.

## Despliegue en la nube

Toda la infraestructura se crea con Terraform, en la carpeta [`terraform/`](terraform).

```
               Google Cloud (us-central1)                    MongoDB Atlas
  ┌────────────────────────────────────────────┐     ┌─────────────────────────┐
  │  Artifact Registry ──imagen──► Cloud Run ──┼────►│  cluster M0 (GCP,       │
  │                                   ▲        │     │  CENTRAL_US)            │
  │  Secret Manager ──MONGODB_URI─────┘        │     └─────────────────────────┘
  └────────────────────────────────────────────┘
```

| Archivo | Qué crea |
|---|---|
| `atlas.tf` | proyecto y cluster gratuito de Atlas, usuario de la base con contraseña generada y lista de acceso |
| `cloud_run.tf` | APIs de Google Cloud, repositorio de imágenes, secreto con la URI de MongoDB, cuenta de servicio y el servicio de Cloud Run |

Decisiones:

- **La URI de MongoDB, que incluye la contraseña, se guarda en Secret Manager.** La genera Terraform y nunca se escribe en el repositorio ni en una variable de entorno visible en la consola.
- **Cloud Run usa una cuenta de servicio propia** que solo puede leer ese secreto.
- **Escala de 0 a 2 instancias**: sin tráfico no hay instancias corriendo.
- **Atlas permite conexiones desde cualquier IP** porque Cloud Run no tiene una IP de salida fija. El acceso queda protegido por usuario y contraseña. Para restringirlo habría que usar un conector VPC con Cloud NAT o Private Service Connect.

### Cómo desplegar

Requisitos: Terraform, Google Cloud CLI, Docker, un proyecto de Google Cloud con facturación activa y una service account de Atlas con rol *Organization Project Creator*.

1. Credenciales (no se guardan en ningún archivo):

   ```bash
   gcloud auth application-default login
   export MONGODB_ATLAS_CLIENT_ID="..."
   export MONGODB_ATLAS_CLIENT_SECRET="..."
   ```

2. Variables: copiar `terraform/terraform.tfvars.example` como `terraform/terraform.tfvars` y completar el id de la organización de Atlas y el id del proyecto de Google Cloud.

3. Crear la base de datos, el repositorio de imágenes y el secreto:

   ```bash
   cd terraform
   terraform init
   terraform apply
   ```

4. Construir y publicar la imagen:

   ```bash
   gcloud auth configure-docker us-central1-docker.pkg.dev
   docker build -t us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.0.0 ..
   docker push us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.0.0
   ```

5. Agregar `imagen = "us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.0.0"` a `terraform.tfvars` y volver a aplicar. Esto crea el servicio de Cloud Run y muestra su URL en el output `url_api`:

   ```bash
   terraform apply
   ```

El servicio se crea en un segundo paso porque Cloud Run necesita que la imagen ya exista en el repositorio, y el repositorio también lo crea Terraform.

Para borrar todo: `terraform destroy`.

## Flujo de trabajo con Git

- `main`: versiones estables.
- `develop`: integración.
- Una rama `feature/...` o `docs/...` por tarea, que sale de `develop` y se integra con merge.
- Mensajes de commit con el formato Conventional Commits (`feat:`, `test:`, `docs:`, `build:`...).
