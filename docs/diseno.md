# Diseño de la solución

Este documento resume las decisiones tomadas antes de empezar a programar y el motivo de cada una. Se irá actualizando si algo cambia durante el desarrollo.

## 1. Problema

Se necesita una API para administrar franquicias. Cada franquicia tiene un nombre y varias sucursales; cada sucursal tiene un nombre y varios productos; cada producto tiene un nombre y una cantidad de stock.

Además de crear y modificar estos datos, la API debe responder cuál es el producto con más stock en cada sucursal de una franquicia.

## 2. Tecnologías

| Tema | Elección |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3 con WebFlux |
| Base de datos | MongoDB Atlas (driver reactivo de Spring Data) |
| Build | Gradle (Groovy) con wrapper |
| Contenedor | Docker |
| Infraestructura | Terraform |
| Nube | Google Cloud Run |

**WebFlux:** atiende las peticiones de forma no bloqueante. Mientras espera la respuesta de la base de datos, el hilo queda libre para atender otras solicitudes, así que con pocos hilos se pueden manejar muchas peticiones concurrentes. Para aprovechar esto, el acceso a datos también tiene que ser no bloqueante; por eso se usa el driver reactivo de MongoDB y no JPA/JDBC.

**MongoDB Atlas:** tiene soporte reactivo completo en Spring Data, ofrece un plan gratuito en la nube y cuenta con proveedor oficial de Terraform. Se evaluaron MySQL/Postgres con R2DBC, pero implican más código manual para las relaciones.

**Java 21:** es la versión LTS más usada actualmente en proyectos empresariales. Gradle se encarga de usar esa versión mediante `toolchain`.

## 3. Modelo de datos

Se usan tres colecciones separadas, relacionadas por id.

```
franquicias              sucursales                 productos
-----------              ----------                 ---------
id                       id                         id
nombre                   nombre                     nombre
                         franquiciaId  ───────►     stock
                                                    sucursalId  ───────►
```

Ejemplo de documentos:

```json
// franquicias
{ "_id": "f1", "nombre": "Pizzería Don Mario" }

// sucursales
{ "_id": "s1", "nombre": "Sede Centro", "franquiciaId": "f1" }

// productos
{ "_id": "p1", "nombre": "Pizza hawaiana", "stock": 25, "sucursalId": "s1" }
```

Índices:

- `sucursales.franquiciaId`
- `productos.sucursalId` + `stock` (descendente)

### Por qué colecciones separadas y no documentos embebidos

- Cada operación modifica un solo documento. Por ejemplo, cambiar el stock es actualizar un producto por su id, sin tener que recorrer arreglos anidados.
- Los documentos no crecen sin límite. Con todo embebido, una franquicia grande podría acercarse al límite de 16 MB por documento.
- Los repositorios reactivos quedan simples: uno por colección, con consultas derivadas como `findBySucursalId`.

Lo que se pierde:

- Consultar el producto con más stock requiere varias consultas en lugar de una.
- Mongo no valida referencias, así que la aplicación debe comprobar que la franquicia o la sucursal existan antes de crear algo dentro de ellas.

## 4. Endpoints

Todas las rutas empiezan por `/api`.

Se usa anidamiento superficial: la ruta se anida al crear un recurso que depende de otro, y cuando ya se tiene el id del recurso se accede directamente a él.

| Acción | Método y ruta | Body | Respuesta |
|---|---|---|---|
| Crear franquicia | `POST /api/franquicias` | `{ "nombre": "..." }` | 201 |
| Renombrar franquicia | `PATCH /api/franquicias/{id}` | `{ "nombre": "..." }` | 200 / 404 |
| Crear sucursal | `POST /api/franquicias/{id}/sucursales` | `{ "nombre": "..." }` | 201 / 404 |
| Renombrar sucursal | `PATCH /api/sucursales/{id}` | `{ "nombre": "..." }` | 200 / 404 |
| Crear producto | `POST /api/sucursales/{id}/productos` | `{ "nombre": "...", "stock": 10 }` | 201 / 404 |
| Modificar producto (stock y/o nombre) | `PATCH /api/productos/{id}` | `{ "stock": 50 }` o `{ "nombre": "..." }` | 200 / 404 |
| Eliminar producto | `DELETE /api/productos/{id}` | - | 204 / 404 |
| Producto con más stock por sucursal | `GET /api/franquicias/{id}/productos/mayor-stock` | - | 200 / 404 |

Se usa `PATCH` en lugar de `PUT` porque solo se cambia una parte del recurso.

### Respuesta del producto con más stock

```json
[
  {
    "productoId": "p1",
    "nombre": "Pizza hawaiana",
    "stock": 25,
    "sucursalId": "s1",
    "sucursalNombre": "Sede Centro"
  }
]
```

Reglas:

- Se devuelve un producto por sucursal.
- Las sucursales sin productos no aparecen en el resultado.
- Si dos productos de la misma sucursal tienen el mismo stock, se devuelve solo uno de ellos.
- Si la franquicia no existe se responde 404.

### Validaciones

- `nombre` es obligatorio y no puede estar vacío.
- `stock` no puede ser negativo.
- En el `PATCH` de producto debe venir al menos uno de los dos campos.

Formato de error:

```json
{ "codigo": 404, "mensaje": "No existe la franquicia con id f1" }
```

## 5. Arquitectura

Se sigue Clean Architecture en un solo módulo, separando paquetes:

```
com.derek.franquicias
├── domain
│   ├── model        Franquicia, Sucursal, Producto, ProductoMayorStock
│   ├── gateway      interfaces que el dominio necesita para guardar y consultar datos
│   ├── usecase      FranquiciaUseCase, SucursalUseCase, ProductoUseCase
│   └── exception    excepciones del negocio
├── infrastructure
│   ├── entrypoint   controladores REST, DTOs y manejo de errores
│   └── mongo        documentos, repositorios reactivos y adaptadores de los gateways
└── config           configuración de beans
```

Reglas:

1. `domain` no depende de Spring ni de Mongo.
2. `infrastructure` depende de `domain`, nunca al contrario.
3. Los documentos de Mongo son clases distintas a los modelos de dominio; se convierten con mappers.

Decisiones para no complicar la estructura más de lo necesario:

- Un solo módulo de Gradle en vez de un proyecto multimódulo.
- Un caso de uso por entidad en vez de uno por cada acción.
- Controladores con `@RestController` en vez de `RouterFunction`.

## 6. Despliegue

- **Local:** `docker-compose` con la API y un MongoDB local.
- **Nube:** la imagen Docker se publica en Google Artifact Registry y se ejecuta en Cloud Run. La base de datos es un cluster gratuito de MongoDB Atlas.
- **Infraestructura como código:** Terraform crea el cluster de Atlas y el servicio de Cloud Run.

## 7. Flujo de trabajo con Git

- `main`: versiones estables.
- `develop`: integración.
- Ramas `feature/...` o `docs/...` que salen de `develop` y se integran a ella con merge.
- Mensajes de commit con el formato Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`...).
