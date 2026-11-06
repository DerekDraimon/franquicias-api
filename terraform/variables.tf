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

variable "database_name" {
  description = "Nombre de la base de datos que usa la aplicacion."
  type        = string
  default     = "franquicias"
}
