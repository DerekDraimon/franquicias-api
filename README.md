# franquicias-api

API REST para administrar franquicias, sus sucursales y los productos de cada sucursal, con una consulta del producto con más stock en cada sucursal.

Java 21 · Spring Boot 4 (WebFlux) · MongoDB · Clean Architecture · Docker · Terraform · Google Cloud Run

**API desplegada:** https://franquicias-api-6cubf5m5aa-uc.a.run.app

## Ejecutar localmente

Solo se necesita Docker:

```bash
git clone https://github.com/DerekDraimon/franquicias-api.git
cd franquicias-api
docker compose up -d --build
```

La API queda en `http://localhost:8080`, junto con un MongoDB. Para detener todo: `docker compose down -v`.

Sin Docker: con el JDK 21 y un MongoDB accesible, `./gradlew bootRun`. La conexión se cambia con la variable `MONGODB_URI` (por defecto `mongodb://localhost:27017/franquicias`).

## Probar con Postman

En [`postman/`](postman) hay una colección y dos entornos para importar en Postman:

| Archivo | Uso |
|---|---|
| `franquicias-api.postman_collection.json` | todos los endpoints, con pruebas de cada respuesta |
| `local.postman_environment.json` | `http://localhost:8080` |
| `produccion.postman_environment.json` | la API desplegada en Cloud Run |

Cada request guarda el id que necesita el siguiente, así que la colección se puede ejecutar completa con el **Runner** de Postman.

## Endpoints

| Acción | Método y ruta | Body |
|---|---|---|
| Crear franquicia | `POST /api/franquicias` | `{ "nombre": "..." }` |
| Listar franquicias | `GET /api/franquicias` | - |
| Renombrar franquicia | `PATCH /api/franquicias/{id}` | `{ "nombre": "..." }` |
| Crear sucursal | `POST /api/franquicias/{id}/sucursales` | `{ "nombre": "..." }` |
| Listar sucursales de una franquicia | `GET /api/franquicias/{id}/sucursales` | - |
| Renombrar sucursal | `PATCH /api/sucursales/{id}` | `{ "nombre": "..." }` |
| Crear producto | `POST /api/sucursales/{id}/productos` | `{ "nombre": "...", "stock": 10 }` |
| Listar productos de una sucursal | `GET /api/sucursales/{id}/productos` | - |
| Modificar stock y/o nombre | `PATCH /api/productos/{id}` | `{ "stock": 50 }` o `{ "nombre": "..." }` |
| Eliminar producto | `DELETE /api/productos/{id}` | - |
| **Producto con más stock por sucursal** | `GET /api/franquicias/{id}/productos/mayor-stock` | - |

La consulta del producto con más stock devuelve uno por sucursal, con el nombre de la sucursal a la que pertenece:

```json
[
  { "productoId": "...", "nombre": "Pizza", "stock": 50, "sucursalId": "...", "sucursalNombre": "Centro" }
]
```

Los errores usan siempre el mismo formato: **404** si el recurso no existe, **400** si un dato no es válido (nombre vacío, stock negativo, JSON mal formado).

```json
{ "codigo": 404, "mensaje": "No existe la franquicia con id 123" }
```

## Tests

```bash
./gradlew test
```

Requiere Docker en ejecución: los tests de MongoDB levantan una base real con Testcontainers. Cubren tres niveles: dominio (con mocks), adaptadores (cada controlador y cada repositorio) e integración (la aplicación completa por HTTP).

## Diseño

**Arquitectura limpia** en un solo módulo:

```
domain           modelos, casos de uso, gateways (interfaces) y excepciones; sin Spring ni MongoDB
infrastructure   controladores REST y adaptadores de MongoDB que implementan los gateways
config           registro de los casos de uso como beans
```

Decisiones principales:

- **WebFlux y driver reactivo de MongoDB**: las peticiones no bloquean hilos mientras esperan a la base de datos. Toda la cadena es reactiva, incluido el acceso a datos.
- **Tres colecciones relacionadas por id** (franquicias, sucursales, productos) en lugar de documentos embebidos: cada operación modifica un solo documento y ninguno crece sin límite. A cambio, la aplicación valida que la franquicia o la sucursal existan.
- **Índice compuesto `sucursalId + stock`** para encontrar rápido el producto con más stock de cada sucursal.
- **Validaciones en el dominio** (nombre obligatorio, stock no negativo): no puede existir un objeto inválido.
- **API REST con anidamiento superficial**: las rutas se anidan para crear o listar dentro de un recurso, y se accede directo por id para modificar o eliminar.

## Despliegue en la nube

Toda la infraestructura está definida con Terraform en [`terraform/`](terraform):

- **MongoDB Atlas**: cluster gratuito, usuario con contraseña generada por Terraform.
- **Google Cloud**: Artifact Registry para la imagen, Secret Manager para la URI de MongoDB y Cloud Run para la API, con una cuenta de servicio que solo puede leer ese secreto.

Pasos:

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # completar los ids de Atlas y Google Cloud
terraform init
terraform apply                                # base de datos, repositorio y secreto

gcloud auth configure-docker us-central1-docker.pkg.dev
docker build -t us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.1.0 ..
docker push us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.1.0

# agregar imagen = "us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.1.0" en terraform.tfvars
terraform apply                                # crea el servicio de Cloud Run
```

Las credenciales se pasan por variables de entorno (`gcloud auth application-default login` y `MONGODB_ATLAS_CLIENT_ID` / `MONGODB_ATLAS_CLIENT_SECRET`), nunca en archivos del repositorio.

## Flujo de trabajo con Git

`main` para versiones estables, `develop` para integración y una rama `feature/...` o `docs/...` por tarea. Commits con Conventional Commits.
