#!/bin/sh

#########################################################################################################################
# This script automates the process of executing the program after changes in code.

# It only works on Linux
# To run it, make sure you are in the project's main folder and 
# $ sh ./update.sh

#########################################################################################################################

# Creates a new MBP.war package in project's taget folder
echo "Building .war file..."   
sudo mvn clean install

# Deploys .war file by moving it to tomcat's webapps folder
echo "Deploying .war file..." 
sudo mv target/MBP-0.1.war /var/lib/tomcat8/webapps/MBP.war

# Restarts tomcat8
echo "Restarting tomcat..." 
sudo service tomcat8 restart

echo "Update finished. Good luck! ;)" 
# You now can find your modified application in localhost:8080/MBP
# I hope it works! 
# If if doesn't, you can always change your code and rerun this script. :)



