#!/bin/sh

#######################################################################################################################
# This install script provides a fully automated installation of the MBP.                                             #
# It installs all required software components including Java, mosquitto and a MongoDB instance and sets up           #
# a systemd service with which the MBP can be launched and stopped.                                                   #
#######################################################################################################################

# Config
TARGET_DIR="/usr/local/bin/MBP"
SERVICE_NAME="mbp"
SERVICE_DESCRIPTION="Multi-purpose binding and provisioning platform"

# Generated
APP_STARTER_PATH="${TARGET_DIR}/start_mbp"

echo "Writing hostname..."
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
echo "Updating package repositories..."
sudo apt-get -qy update;
sudo apt-get -qy upgrade;

#Installing Java 8
echo "Installing Java 8..."
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy maven;

echo "Installing mosquitto, MongoDB and maven..."

if [ -n "$1" ]
then
    if [ "$1" = "secure" ]
    then
        echo "Installing secured mosquitto with OAuth2 authentication as Docker container..."
        echo "\nbroker_location=LOCAL_SECURE" >> src/main/resources/config.properties
        echo "Building mosquitto with go-auth plugin..."
        cd mosquitto/
        docker build -t mosquitto-go-auth .
        echo "Starting docker container for mosquitto with go-auth plugin..."
        docker run -d --network="host" -p 1883:1883 -p 1884:1884 mosquitto-go-auth
        cd ..
    else
        echo "Invalid command-line argument(s)."
    fi
else
    echo "Installing mosquitto..."
    sudo apt-get install -qy mosquitto;
    sudo systemctl start mosquitto;
fi

# Install and start MongoDB
sudo apt-get -qy install mongodb-server;
sudo systemctl start mongodb;

# Build application
echo "Building MBP application..."
sudo mvn clean package -DskipTests=true

# Check if service is active
IS_ACTIVE=$(sudo systemctl is-active $SERVICE_NAME)
if [ "$IS_ACTIVE" == "active" ]; then
    # Just Restart the service
    echo "MBP service is already running. Restarting it..."
    sudo systemctl restart $SERVICE_NAME
else
    echo "Installing MBP as service..."

    # Create target dir and copy the MBP application to it
    sudo mkdir $TARGET_DIR
    sudo cp target/MBP.jar $TARGET_DIR

    # Create service file
    sudo cat > /etc/systemd/system/${SERVICE_NAME//'"'/}.service << EOF
[Unit]
Description=$SERVICE_DESCRIPTION
After=network.target
[Service]
WorkingDirectory=$TARGET_DIR
ExecStart=$APP_STARTER_PATH
Restart=on-failure
RestartSec=5
TimeoutStopSec=10
[Install]
WantedBy=multi-user.target
EOF

    # Create script for executing the application
    sudo cat > ${APP_STARTER_PATH} << EOF
#!/bin/sh
sudo java -jar MBP.jar
EOF

    # Make starter script executable
    sudo chmod u+x $APP_STARTER_PATH

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
echo "Status of the individual software components:"
echo "> mosquitto: $MOSQUITTO_STATUS"
echo "> MongoDB: $MONGODB_STATUS"
echo "> MBP application: $APP_STATUS"
echo "---------------------------------------------------------"

if [ $MOSQUITTO_STATUS = "active" ] && [ $MONGODB_STATUS = "active" ] && ([ $APP_STATUS = "active" ] || [ $APP_STATUS = "activating" ])
then
  echo "Installation finished SUCCESSFULLY!"
  echo "After some seconds, the web interface of the MBP should become available under http://127.0.0.1:8080/mbp"
else
  echo "Installation INCOMPLETE!"
  echo "At least one of the required components is not available."
fi
echo "---------------------------------------------------------"

exit 0
