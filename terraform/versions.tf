terraform {
  required_version = ">= 1.6"

  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 2.7"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

# Las credenciales se leen de variables de entorno, nunca del codigo:
#   MONGODB_ATLAS_CLIENT_ID y MONGODB_ATLAS_CLIENT_SECRET (service account), o
#   MONGODB_ATLAS_PUBLIC_API_KEY y MONGODB_ATLAS_PRIVATE_API_KEY (API key).
provider "mongodbatlas" {}
