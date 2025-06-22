#!/usr/bin/env bash
# Quick script to restart services on Azure VM
# Usage: ./restart-services.sh

set -euo pipefail

FQDN="metroll.southeastasia.cloudapp.azure.com"
ADMIN_USER="azureuser"
APP_ROOT="/opt/metroll"

echo "[+] Connecting to Azure VM to restart services..."

# Create SSH command to restart services
ssh -o "StrictHostKeyChecking=no" "$ADMIN_USER@$FQDN" << 'EOF'
set -e

echo "[+] Checking current container status..."
cd /opt/metroll
sudo docker compose ps

echo "[+] Checking account-service logs..."
sudo docker compose logs account-service --tail=10 || echo "No logs available"

echo "[+] Restarting account-service..."
sudo docker compose restart account-service

echo "[+] Waiting for service to be healthy..."
sleep 30

echo "[+] Checking service status after restart..."
sudo docker compose ps

echo "[+] Testing API Gateway health..."
curl -f http://localhost:8080/actuator/health || echo "Gateway not responding"

echo "[+] Testing account service health..."
curl -f http://localhost:8080/account/actuator/health || echo "Account service not responding"

echo "[+] Service restart completed!"
EOF

echo "[+] Testing externally accessible endpoints..."
curl -f "https://$FQDN/actuator/health" && echo " - Gateway: OK" || echo " - Gateway: FAILED"
curl -f "https://$FQDN/account/actuator/health" && echo " - Account Service: OK" || echo " - Account Service: FAILED"

echo "[+] Restart operation completed!" 