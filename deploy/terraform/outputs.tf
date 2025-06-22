output "public_ip" {
  description = "Public IP address of the VM"
  value       = azurerm_public_ip.pubip.ip_address
}

output "fqdn" {
  description = "FQDN assigned to the VM"
  value       = azurerm_public_ip.pubip.fqdn
} 