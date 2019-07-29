variable "product" {
  type    = "string"
  default = "ethos"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "tornado_url" {
  default = "http://tornado:8090/rs/render"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "capacity" {
  default = "1"
}

variable "deployment_namespace" {
  default = ""
}

variable "common_tags" {
  type = "map"
}

variable "autoheal" {
  description = "Enabling Proactive Auto Heal for Webapps"
  type        = "string"
  default     = "True"
}

variable "idam_api_url" {
  default = "http://idam-api:8080"
}

variable "ccd_data_store_api_url" {
  default = "http://ccd-data-store-api:4452"
}

variable "dm_url" {
  default = "http://dm-store:8080"
}

variable "s2s_url" {
  default = "http://service-auth-provider-api:8080"
}

variable "micro_service" {
  default = "ethos_repl_service"
}

variable "ccd_gateway_url" {
  default = "http://127.0.0.1:3453"
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default = ""
}