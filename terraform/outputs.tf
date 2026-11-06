locals {
  # La cadena SRV de Atlas viene sin usuario ni base de datos: se arma la URI completa.
  srv_host = trimprefix(mongodbatlas_advanced_cluster.franquicias.connection_strings.standard_srv, "mongodb+srv://")

  mongodb_uri = "mongodb+srv://${mongodbatlas_database_user.app.username}:${random_password.app.result}@${local.srv_host}/${var.database_name}?retryWrites=true&w=majority"
}

output "mongodb_uri" {
  description = "URI de conexion para la variable MONGODB_URI de la aplicacion."
  value       = local.mongodb_uri
  sensitive   = true
}
