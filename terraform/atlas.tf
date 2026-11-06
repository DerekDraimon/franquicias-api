resource "mongodbatlas_project" "franquicias" {
  name   = var.atlas_project_name
  org_id = var.atlas_org_id
}

# Cluster gratuito (M0). Se aloja en Google Cloud, en la misma region que Cloud Run.
resource "mongodbatlas_advanced_cluster" "franquicias" {
  project_id   = mongodbatlas_project.franquicias.id
  name         = "franquicias"
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = "GCP"
          region_name           = var.atlas_region
          priority              = 7
        }
      ]
    }
  ]
}

# Contrasena generada por Terraform: no se escribe en ningun archivo del repositorio.
resource "random_password" "app" {
  length  = 24
  special = false
}

resource "mongodbatlas_database_user" "app" {
  project_id         = mongodbatlas_project.franquicias.id
  username           = "franquicias-app"
  password           = random_password.app.result
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }
}

# Cloud Run no tiene una IP de salida fija, por eso se permite cualquier IP.
# El acceso queda protegido por el usuario y la contrasena.
resource "mongodbatlas_project_ip_access_list" "todas" {
  project_id = mongodbatlas_project.franquicias.id
  cidr_block = "0.0.0.0/0"
  comment    = "Cloud Run no tiene IP de salida fija"
}
