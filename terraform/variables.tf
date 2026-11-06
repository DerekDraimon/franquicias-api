variable "atlas_org_id" {
  description = "Id de la organizacion de MongoDB Atlas donde se crea el proyecto."
  type        = string
}

variable "atlas_project_name" {
  description = "Nombre del proyecto de Atlas."
  type        = string
  default     = "franquicias"
}

variable "atlas_region" {
  description = "Region de Google Cloud donde Atlas crea el cluster gratuito."
  type        = string
  default     = "CENTRAL_US"
}

variable "gcp_project_id" {
  description = "Id del proyecto de Google Cloud donde se despliega la API."
  type        = string
}

variable "gcp_region" {
  description = "Region de Google Cloud. Coincide con la region del cluster de Atlas."
  type        = string
  default     = "us-central1"
}

variable "imagen" {
  description = "Imagen de la API en Artifact Registry, por ejemplo us-central1-docker.pkg.dev/<proyecto>/franquicias/api:1.0.0"
  type        = string
  default     = ""
}

variable "database_name" {
  description = "Nombre de la base de datos que usa la aplicacion."
  type        = string
  default     = "franquicias"
}
