#!/usr/bin/env bash
# Quick deployment helper for Metroll on Azure Spot VM
# Usage: ./deploy.sh -k <ssh_public_key> [-p prefix] [-r azure_region] [-u admin_user] [-i inventory] [-s subscription_id]
# Requires: terraform, ansible, Azure credentials exported as ARM_* env vars.

set -euo pipefail

PREFIX="metroll"
REGION="Southeast Asia"
ADMIN_USER="azureuser"
SSH_KEY=""
SUB_ID="${ARM_SUBSCRIPTION_ID:-}"  # Can be provided via env or -s flag

while getopts "k:p:r:u:s:h" opt; do
  case $opt in
    k) SSH_KEY="$OPTARG" ;;
    p) PREFIX="$OPTARG" ;;
    r) REGION="$OPTARG" ;;
    u) ADMIN_USER="$OPTARG" ;;
    s) SUB_ID="$OPTARG" ;;
    h)
      echo "Usage: $0 -k <ssh_public_key> [-p prefix] [-r azure_region] [-u admin_user] [-s subscription_id]"
      exit 0
      ;;
    *) echo "Invalid option"; exit 1 ;;
  esac
done

if [[ -z "$SSH_KEY" ]]; then
  echo "SSH public key is required. Use -k <path>."
  exit 1
fi
if [[ -z "$SUB_ID" ]]; then
  echo "Azure subscription ID is required. Provide via -s or ARM_SUBSCRIPTION_ID env var."
  exit 1
fi

pushd deploy/terraform > /dev/null

echo "[+] Initialising Terraform..."
terraform init -upgrade

echo "[+] Applying Terraform (this may take a few minutes)..."
terraform apply -auto-approve \
  -var="prefix=$PREFIX" \
  -var="azure_region=$REGION" \
  -var="ssh_public_key_path=$SSH_KEY" \
  -var="subscription_id=$SUB_ID"

PUBLIC_IP=$(terraform output -raw public_ip)
FQDN=$(terraform output -raw fqdn)

echo "[+] VM Public IP: $PUBLIC_IP, FQDN: $FQDN"

popd > /dev/null

INVENTORY_FILE="/tmp/metroll_inv_$(date +%s).ini"
echo "[+] Generating Ansible inventory at $INVENTORY_FILE"
cat <<EOF > "$INVENTORY_FILE"
[metroll]
$FQDN ansible_user=$ADMIN_USER ansible_ssh_private_key_file=~/.ssh/id_rsa
EOF

echo "[+] Running Ansible playbook..."
ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -vv -i "$INVENTORY_FILE" deploy/ansible/site.yml

echo "[+] Deployment completed successfully!" 