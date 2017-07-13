!/bin/sh
sudo apt-get -qy update;
sudo apt-get -qy upgrade;
#Installing Java8
sudo apt-get install -qy openjdk-8-jdk;

# Install Mosquitto Broker
sudo apt-get install -y mosquitto;

# Install and start MongoDB 
sudo apt-get -y install mongodb-server;
sudo /etc/init.d/mongodb start;
sudo /etc/init.d/mongodb status;

# Install Tomcat 9
sudo apt-get install -y tomcat8 git maven;

# checkout repository
sudo git clone https://github.com/ana-silva/connde.git
cd connde/java-projects/restful-connde
sudo mvn clean install

# deploy war to Tomcat
sudo mv target/restful-connde-1.0-SNAPSHOT.war /var/lib/tomcat8/webapps/MBP.war
echo "Installation finished"