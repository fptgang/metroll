#!/bin/bash
clear
echo "Starting Python deployment menu..."

if ! command -v python3 &> /dev/null; then
    echo "Error: python3 is not installed. Please install python3 to continue."
    exit 1
fi

python3 dev-tool.py

if [ $? -ne 0 ]; then
    echo "Error: Failed to execute dev-tool.py. Please check the script for errors."
    exit 1
fi