# APIs necesarias. disable_on_destroy = false para no apagarlas si otros
# recursos del proyecto las usan.
resource "google_project_service" "apis" {
  for_each = toset([
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "secretmanager.googleapis.com",
  ])

  service            = each.value
  disable_on_destroy = false
}

resource "google_artifact_registry_repository" "franquicias" {
  repository_id = "franquicias"
  location      = var.gcp_region
  format        = "DOCKER"
  description   = "Imagenes de la API de franquicias"

  depends_on = [google_project_service.apis]
}

# La URI de Atlas incluye la contrasena, asi que se guarda en Secret Manager
# en lugar de ponerla como variable de entorno visible en la consola.
resource "google_secret_manager_secret" "mongodb_uri" {
  secret_id = "franquicias-mongodb-uri"

  replication {
    auto {}
  }

  depends_on = [google_project_service.apis]
}

resource "google_secret_manager_secret_version" "mongodb_uri" {
  secret      = google_secret_manager_secret.mongodb_uri.id
  secret_data = local.mongodb_uri
}

# Cuenta de servicio propia para la API, con permiso solo para leer su secreto.
resource "google_service_account" "api" {
  account_id   = "franquicias-api"
  display_name = "API de franquicias en Cloud Run"
}

resource "google_secret_manager_secret_iam_member" "api_lee_uri" {
  secret_id = google_secret_manager_secret.mongodb_uri.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.api.email}"
}

# El servicio se crea solo cuando ya hay una imagen publicada (variable imagen).
resource "google_cloud_run_v2_service" "api" {
  count = var.imagen == "" ? 0 : 1

  name                = "franquicias-api"
  location            = var.gcp_region
  ingress             = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false

  template {
    service_account = google_service_account.api.email

    scaling {
      min_instance_count = 0
      max_instance_count = 2
    }

    containers {
      image = var.imagen

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      env {
        name = "MONGODB_URI"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.mongodb_uri.secret_id
            version = "latest"
          }
        }
      }
    }
  }

  depends_on = [
    google_secret_manager_secret_version.mongodb_uri,
    google_secret_manager_secret_iam_member.api_lee_uri,
  ]
}

# La API es publica: cualquiera puede invocarla sin autenticarse.
resource "google_cloud_run_v2_service_iam_member" "publica" {
  count = var.imagen == "" ? 0 : 1

  name     = google_cloud_run_v2_service.api[0].name
  location = var.gcp_region
  role     = "roles/run.invoker"
  member   = "allUsers"
}
