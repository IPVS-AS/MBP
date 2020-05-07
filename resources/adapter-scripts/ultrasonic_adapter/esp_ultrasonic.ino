#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266HTTPClient.h>

#include <ArduinoJson.h>

#define TRIGGERPIN D1
#define ECHOPIN    D2

#include <PubSubClient.h>

//WiFi
const char* ssid = "HotTPL";
const char* password = "mbpheroes";
WiFiClient wifiClient;                       // WEB Client instance

//WEBSERVER
ESP8266WebServer server(80);

//MQTT Server
char SENSOR_STATUS[32];                      // broker URL
char COMPONENT[32];                          // broker URL
char COMPONENT_ID[32];                       // broker URL
char BROKER_MQTT[32];                        // broker URL
//const char* BROKER_MQTT = "129.69.209.78";  
char TOPIC_PUBLISH[32];                      // broker topic
int BROKER_PORT = 1883;                      // MQTT Broker Port
#define ID_MQTT  "esp2"                     // MQTT ID for the conection
PubSubClient MQTT(wifiClient);               // MQTT client instance

//Function declarations
void keepConections();                       // Keep WiFi and MQTT Broker connections alive
void connectWiFi();                          // Connect to WiFi
void connectMQTT();                          // Connect to MQTT
void sendPacket();                          // Connect to MQTT
void handleConfig();                         // Handle post request with broker configuration
void handleStatus();                         // Handle post request with status

//global variables
int configured = 0;
String message;
long distance;

void setup()
{
  // Debug console
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  connectWiFi();
  connectMQTT();
  
  pinMode(TRIGGERPIN, OUTPUT);
  pinMode(ECHOPIN, INPUT);

  server.on("/config", handleConfig);
  server.on("/status", handleStatus);

  server.begin(); //Start the server
  Serial.println("Server listening");
}

void loop()
{
  server.handleClient(); //Handling of incoming requests
  
  keepConections();
  getSensorValue();

  if(checkStatus() == 1)
  {
    sendPacket();  
  }

  delay(1500);

}

void getSensorValue() {
  long duration;
  distance = 0;
  digitalWrite(TRIGGERPIN, LOW);  
  delayMicroseconds(3); 
  
  digitalWrite(TRIGGERPIN, HIGH);
  delayMicroseconds(12); 
  
  digitalWrite(TRIGGERPIN, LOW);
  duration = pulseIn(ECHOPIN, HIGH);
  distance = (duration/2) / 29.1;
  Serial.print(distance);
  Serial.println("Cm");
}

void keepConections() {
  connectWiFi(); // reconnect if there is no connection
  connectMQTT();
}

void connectMQTT() {

  if (MQTT.connected() || !configured || !checkStatus()) {
    return;
  }
  
  while (!MQTT.connected()) 
  {
    Serial.print("Connecting to MQTT Broker: ");
    
    Serial.println(BROKER_MQTT);
    MQTT.setServer(BROKER_MQTT, BROKER_PORT);
    
    if (MQTT.connect(ID_MQTT)) 
    {
      Serial.println("Successfully connected to Broker!");
    }
    else 
    {
      Serial.println("Unable to connect to broker.");
      Serial.println("New attempt in 10s");
      delay(10000);
    }
  }
}

void connectWiFi() {

  if (WiFi.status() == WL_CONNECTED) {
    return;
  }

  Serial.println();

  Serial.print("Connecting to network: ");
  Serial.print(ssid);

  WiFi.begin(ssid, password); // Connect to WiFi
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  
  //server.begin();             // Inicia o servidor WEB

  Serial.println();
  Serial.print("Successfully connected, network: ");
  Serial.print(ssid);
  Serial.print("  IP: ");
  Serial.println(WiFi.localIP());
  Serial.println(WiFi.subnetMask());
  Serial.println(WiFi.gatewayIP());
}



int checkStatus()
{  
  if(String(SENSOR_STATUS).equals("1"))
  {
    return 1;
  }
  return 0;
}

void handleStatus() {
  if (server.hasArg("plain") == false) //Check if body received
  { 
    server.send(200, "text/plain", "Body not received");
    return;
  }

  char json[64];

  String request = server.arg("plain");

  request.toCharArray(json, request.length());
  
  const size_t capacity = JSON_OBJECT_SIZE(1) + 10;
  DynamicJsonDocument doc(capacity);
  
  deserializeJson(doc, json);
  
  const char* status_temp = doc["status"];

  int i = 0;
  for(i = 0; i < String(status_temp).length(); i ++)
  {
    SENSOR_STATUS[i] = String(status_temp).charAt(i);
  }

  Serial.print("Status: ");
  Serial.println(SENSOR_STATUS);

  String message = "Body received:\n";
         message += request;
         message += "\n";

  server.send(200, "text/plain", message);
  Serial.println(message);
}




void handleConfig()
{
  if (server.hasArg("plain")== false) //Check if body received
  { 
    server.send(200, "text/plain", "Body not received");
    return;
  }

  //const size_t capacity = JSON_OBJECT_SIZE(2) + 40;
  const size_t capacity = JSON_OBJECT_SIZE(5) + 140;
  DynamicJsonDocument doc(capacity);
   
  char json[256];

  String request = server.arg("plain");

  request.toCharArray(json, request.length());
  
  deserializeJson(doc, json);
  
  const char* broker_mqtt_temp = doc["ip"]; // "129.69.185.186"
  const char* topic_temp = doc["topic"]; // "SENSOR/123412341234123412341234"
  const char* component_temp = doc["component"]; // "SENSOR"
  const char* componentId_temp = doc["componentId"]; // "123412341234123412341234"
  const char* status_temp = doc["status"];

  int i = 0;
  for(i = 0; i < String(broker_mqtt_temp).length(); i ++)
  {
    BROKER_MQTT[i] = String(broker_mqtt_temp).charAt(i);
  }

  for(i = 0; i < String(topic_temp).length(); i ++)
  {
    TOPIC_PUBLISH[i] = String(topic_temp).charAt(i);
  }

  for(i = 0; i < String(component_temp).length(); i ++)
  {
    COMPONENT[i] = String(component_temp).charAt(i);
  }

  for(i = 0; i < String(componentId_temp).length(); i ++)
  {
    COMPONENT_ID[i] = String(componentId_temp).charAt(i);
  }

  for(i = 0; i < String(status_temp).length(); i ++)
  {
    SENSOR_STATUS[i] = String(status_temp).charAt(i);
  }

  String message = "Body received:\n";
         message += server.arg("plain");
         message += "\n";

  server.send(200, "text/plain", message);
  Serial.println(message);

  configured = 1;

}

void sendPacket() {

  if(!configured) {
    Serial.println("MQTT needs to be configred first!");
    return;
  }

  char myString[16];
  char myMessage[64];

  int i = 0;

  for(i = 0; i < 16; i ++)
  {
    myString[i] = '\0';
  }
  
  for(i = 0; i < String(distance).length(); i ++)
  {
    myString[i] = String((float) distance).charAt(i);
  }

  //Serial.println(String(value).toInt());
  message = "{";
  message = message + "\"component\": \"" + String(COMPONENT) + "\", " + "\"id\": \"" + String(COMPONENT_ID) + "\", \"value\": " + myString +  "}";

  i = 0;
  for(i = 0; i < String(message).length(); i ++)
  {
    myMessage[i] = String(message).charAt(i);
  }
  
  Serial.println(myMessage);
  Serial.println(TOPIC_PUBLISH);

  MQTT.publish(TOPIC_PUBLISH, myMessage);
  Serial.println("Payload sent.");
  
}
