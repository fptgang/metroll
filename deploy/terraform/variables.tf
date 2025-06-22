variable "prefix" {
  description = "Prefix to use for naming Azure resources (also used as domain label)."
  type        = string
  default     = "metroll"
}

variable "azure_region" {
  description = "Azure region to deploy resources"
  type        = string
  default     = "Southeast Asia"
}

variable "domain_label" {
  description = "Domain label for the public IP FQDN (must be unique). Defaults to prefix if not set."
  type        = string
  default     = "metroll"
}

variable "vm_size" {
  description = "Size of the virtual machine. Spot instance size to deploy."
  type        = string
  default     = "Standard_DC1s_v3"
}

variable "admin_username" {
  description = "Admin username for the VM"
  type        = string
  default     = "azureuser"
}

variable "ssh_public_key_path" {
  description = "Path to your public SSH key (e.g. ~/.ssh/id_rsa.pub)"
  type        = string
}

variable "subscription_id" {
  description = "Azure Subscription ID to deploy resources"
  type        = string
} 