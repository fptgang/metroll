import os
import sys
import stat

# Define all services
services = ["config-server", "service-registry", "api-gateway", "account-service", "order-service", "ticket-service", "subway-service"]

# check os platform
if sys.platform.startswith("win"):
    gradle_cmd = "gradlew"
else:
    gradle_cmd = "./gradlew"

    gradlew_path = "./gradlew"
    if not os.access(gradlew_path, os.X_OK):
        print("Making gradlew executable...")
        os.chmod(gradlew_path, os.stat(gradlew_path).st_mode | stat.S_IXUSR)

def run(cmd):
    print(f"> {cmd}")
    os.system(cmd)

def build_all():
    run(f"{gradle_cmd} clean bootJar")

def deploy_all():
    run("docker-compose down")
    run("docker-compose build --no-cache")
    run("docker-compose up -d")

def build_and_deploy_all():
    build_all()
    deploy_all()

def build_service(service):
    run(f"{gradle_cmd} clean :{service}:bootJar --offline")

def deploy_service(service):
    run(f"docker-compose stop {service}")
    run(f"docker-compose rm -f {service}")
    run(f"docker-compose build --no-cache {service}")
    run(f"docker-compose up -d {service}")

def build_and_deploy_service(service):
    build_service(service)
    deploy_service(service)

def rerun_all():
    run("docker-compose up -d")

def rerun_service(service):
    run(f"docker-compose up -d {service}")

def print_action_menu():
    print("=========================================")
    print("       Docker Compose Deployment Menu")
    print("=========================================")
    print("1. Build")
    print("2. Deploy")
    print("3. Build and Deploy")
    print("4. Rerun")
    print("5. Exit")

def print_service_menu():
    print("\n=========================================")
    print("           Select Service")
    print("=========================================")
    print("1. ALL")
    for idx, service in enumerate(services, start=2):
        print(f"{idx}. {service}")

def get_action_choice():
    print_action_menu()
    try:
        choice = int(input("\nChoose an action: "))
        if choice in [1, 2, 3, 4, 5]:
            return choice
        else:
            print("Invalid choice. Please select 1-5.")
            return None
    except ValueError:
        print("Invalid input. Must be a number.")
        return None

def get_service_choice():
    print_service_menu()
    try:
        choice = int(input("\nChoose a service: "))
        if choice == 1:
            return "ALL"
        elif 2 <= choice <= len(services) + 1:
            return services[choice - 2]
        else:
            print(f"Invalid choice. Please select 1-{len(services) + 1}.")
            return None
    except ValueError:
        print("Invalid input. Must be a number.")
        return None

def execute_action(action, service):
    if action == 1:  # Build
        if service == "ALL":
            print("Building all services...")
            build_all()
        else:
            print(f"Building {service}...")
            build_service(service)
    elif action == 2:  # Deploy
        if service == "ALL":
            print("Deploying all services...")
            deploy_all()
        else:
            print(f"Deploying {service}...")
            deploy_service(service)
    elif action == 3:  # Build and Deploy
        if service == "ALL":
            print("Building and deploying all services...")
            build_and_deploy_all()
        else:
            print(f"Building and deploying {service}...")
            build_and_deploy_service(service)
    elif action == 4:  # Rerun
        if service == "ALL":
            print("Rerunning all services...")
            rerun_all()
        else:
            print(f"Rerunning {service}...")
            rerun_service(service)

def main():
    while True:
        action = get_action_choice()
        if action is None:
            continue

        if action == 5:  # Exit
            print("Exiting...")
            sys.exit(0)

        service = get_service_choice()
        if service is None:
            continue

        execute_action(action, service)

        input("\nPress Enter to return to main menu...\n")

if __name__ == "__main__":
    main()