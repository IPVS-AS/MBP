#!/bin/sh

#######################################################################################################################
# This install script provides a fully automated installation of the MBP.                                             #
# It installs all required software components including Java, mosquitto and a MongoDB instance.                      #
# Finally, a systemd service is registered and started for the MBP application.                                       #
#######################################################################################################################

# Config
TARGET_DIR="/usr/local/bin/MBP"
SERVICE_NAME="mbp"
SERVICE_DESCRIPTION="Service for the Multi-purpose binding and provisioning platform."

# Generated
SERVICE_PATH="${TARGET_DIR}/MBP.jar"

echo "write hostname\n"
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
echo "\nupdate package repositories\n"
sudo apt-get -qy update;
sudo apt-get -qy upgrade;

#Installing Java 8
echo "\nInstalling Java 8...\n"
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy maven;

echo "\nInstalling Mosquitto Broker, MongoDB and maven...\n"

if [ -n "$1" ]
then
    if [ "$1" = "secure" ]
    then
        echo "Installing secured Mosquitto with OAuth2 authentication as Docker container..."
        echo "\nbroker_location=LOCAL_SECURE" >> src/main/resources/config.properties
        echo "\nBuilding mosquitto with go-auth plugin...\n"
        cd mosquitto/
        docker build -t mosquitto-go-auth .
        echo "\nStarting docker container for mosquitto with go-auth plugin...\n"
        docker run -d --network="host" -p 1883:1883 -p 1884:1884 mosquitto-go-auth
        cd ..
    else
        echo "Invalid command-line argument(s)."
    fi
else
    echo "Installing normal Mosquitto..."
    sudo apt-get install -qy mosquitto;
    sudo systemctl start mosquitto;
fi

# Install and start MongoDB
sudo apt-get -qy install mongodb-server;
sudo systemctl start mongodb;

# Build application
echo "\nBuilding MBP application...\n"
sudo mvn clean package -DskipTests=true

# Check if service is active
IS_ACTIVE=$(sudo systemctl is-active $SERVICE_NAME)
if [ "$IS_ACTIVE" == "active" ]; then
    # Just Restart the service
    echo "MBP service is already running. Just restarting it."
    sudo systemctl restart $SERVICE_NAME
else
    echo "Installing MBP as service"

    # Create target dir and copy the MBP application to it
    sudo mkdir $TARGET_DIR
    sudo cp target/MBP.jar $TARGET_DIR

    # Create service file
    sudo cat > /etc/systemd/system/${SERVICE_NAME//'"'/}.service << EOF
[Unit]
Description=$SERVICE_DESCRIPTION
After=network.target
[Service]
ExecStart=$SERVICE_PATH
Restart=on-failure
[Install]
WantedBy=multi-user.target
EOF

    # Enable and start service
    sudo systemctl daemon-reload
    sudo systemctl enable ${SERVICE_NAME//'.service'/}
    sudo systemctl start ${SERVICE_NAME//'.service'/}
fi

# Retrieve status of components
MOSQUITTO_STATUS=$(systemctl is-active mosquitto)
MONGODB_STATUS=$(systemctl is-active mongodb)
APP_STATUS=$(sudo systemctl is-active $SERVICE_NAME)

# Output status
echo "---------------------------"
echo "Software components status:"
echo "> Mosquitto: $MOSQUITTO_STATUS"
echo "> MongoDB: $MONGODB_STATUS"
echo "> MBP application: $APP_STATUS"
echo "---------------------------------------------------------"

if [ $MOSQUITTO_STATUS = "active" ] && [ $MONGODB_STATUS = "active" ] && [ $APP_STATUS = "active" ]
then
  echo "Installation finished SUCCESSFULLY!"
  echo "The MBP should now be accessible on http://127.0.0.1:8080/mbp"
else
  echo "Installation INCOMPLETE!"
  echo "At least one of the required components is not active"
fi
echo "---------------------------------------------------------"

exit 0
