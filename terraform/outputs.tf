locals {
  # La cadena SRV de Atlas viene sin usuario ni base de datos: se arma la URI completa.
  srv_host = trimprefix(mongodbatlas_advanced_cluster.franquicias.connection_strings.standard_srv, "mongodb+srv://")

  mongodb_uri = "mongodb+srv://${mongodbatlas_database_user.app.username}:${random_password.app.result}@${local.srv_host}/${var.database_name}?retryWrites=true&w=majority"
}

output "repositorio_imagenes" {
  description = "Ruta del repositorio donde se publica la imagen de la API."
  value       = "${var.gcp_region}-docker.pkg.dev/${var.gcp_project_id}/${google_artifact_registry_repository.franquicias.repository_id}"
}

output "url_api" {
  description = "URL publica de la API en Cloud Run."
  value       = var.imagen == "" ? "" : google_cloud_run_v2_service.api[0].uri
}

output "mongodb_uri" {
  description = "URI de conexion para la variable MONGODB_URI de la aplicacion."
  value       = local.mongodb_uri
  sensitive   = true
}
