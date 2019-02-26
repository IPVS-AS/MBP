/*
  RFBLASTER - part of the TCL IOT project
  Tim Waizenegger (c) 2016
  receive RS232 commands, send 433MHz signals for RC plug sockets

*/

#define RFPIN 4
#define RF_indicator_PIN 3
#define RDYPIN 2

#define sigOn 0
#define sigOff 1

const long interval = 2000; // millis
unsigned long previousMillis = 0;
String incomingString; 

/////////////////////////////////////////////////////////////////
// help
int to_int(char const *s)
{
  if ( s == NULL || *s == '\0' )
    return -1;

  bool negate = (s[0] == '-');
  if ( *s == '+' || *s == '-' )
    ++s;

  if ( *s == '\0')
    return -1;

  int result = 0;
  while (*s)
  {
    if ( *s >= '0' && *s <= '9' )
    {
      result = result * 10  - (*s - '0');  //assume negative number
    }
    else
      return -1;
    ++s;
  }
  return negate ? result : -result; //-result is positive!
}

/////////////////////////////////////////////////////////////////
// RF CODES WE KNOW

#define codesForType1Length 12
#define codesForType1Count 16
const char codesForType1[][codesForType1Length + 1] = {
  "022202222222", // 1-1-on
  "022202222220", // 1-1-off
  "022220222222", // 1-2-on
  "022220222220", // 1-2-off
  "022222022222", // 1-3-on
  "022222022220", // 1-3-off
  "022222202222", // 1-4-on
  "022222202220", // 1-4-off
  "222002222222", // 4-1-on
  "222002222220", // 4-1-off
  "222020222222", // 4-2-on
  "222020222220", // 4-2-off
  "222022022222", // 4-3-on
  "222022022220", // 4-3-off
  "222022202222", // 4-4-on
  "222022202220"  // 4-4-off
};

#define codesForType2Length 33
#define codesForType2Count 6
const char codesForType2[][codesForType2Length + 1] = {
  "213222123221322122232123132132221", // 1-on
  "213222123221322122232123132222221", // 1-off
  "213222123221322122232123132132212", // 2-on
  "213222123221322122232123132222212", // 2-off
  "213222123221322122232123132132131", // 3-on
  "213222123221322122232123132222131" // 3-off
};


#define codesForType3Length 51
#define codesForType3Count 2
const char codesForType3[][codesForType3Length + 1] = {
  "0000101101111110101100000s0000001100110110001100000", // 1-on
  "0000110101100101100100000s0000011110110010101000000" // 1-off
};

/////////////////////////////////////////////////////////////////
// RF CODE STUFF

void setSigOn() {
  digitalWrite(RFPIN, sigOn);
  digitalWrite(RF_indicator_PIN, 1);
}
void setSigOff() {
  digitalWrite(RFPIN, sigOff);
  digitalWrite(RF_indicator_PIN, 0);
}

// this code has 1 long packet, followed by 3 short packets
// the long packet has a header, followed by 3 short suffixes
// the 3 suffixes and the 3 short packets are the same (except the last bit of each packet is a longer tailing pulse).
// the delay between the packets is 14c
// the delay between the suffixes in the first packet is 4c
void sendCodeType3(const char code[])  {
  const int cycleLength = 500;
  
  Serial.println(code);

  for (char i = 0; i < 25; i++) {
     switch (code[i]) {
      case '0':
        setSigOn();
        delayMicroseconds(cycleLength);
        setSigOff();
        delayMicroseconds(2 * cycleLength);
        break;
      case '1':
        setSigOn();
        delayMicroseconds(2*cycleLength);
        setSigOff();
        delayMicroseconds(cycleLength);      
        break;
     }
  }
  delayMicroseconds(3 * cycleLength); // pause is 4c, 1c was already spent in the header so we wait 3c here
  for (char i = (0+25+1); i < (25+25+1); i++) { // skip the separator and continue with chars 26..EOL
    switch (code[i]) {
      case '0':
        setSigOn();
        delayMicroseconds(cycleLength);
        setSigOff();
        delayMicroseconds(2 * cycleLength);
        break;
      case '1':
        setSigOn();
        delayMicroseconds(2*cycleLength);
        setSigOff();
        delayMicroseconds(cycleLength);      
        break;
     }
  }
  for (char i = (0+25+1); i < (25+25+1); i++) { // skip the separator and continue with chars 26..EOL
    switch (code[i]) {
      case '0':
        setSigOn();
        delayMicroseconds(cycleLength);
        setSigOff();
        delayMicroseconds(2 * cycleLength);
        break;
      case '1':
        setSigOn();
        delayMicroseconds(2*cycleLength);
        setSigOff();
        delayMicroseconds(cycleLength);      
        break;
     }
  }
  for (char i = (0+25+1); i < (24+25+1); i++) { // skip the separator and continue with chars 26..EOL
    switch (code[i]) {
      case '0':
        setSigOn();
        delayMicroseconds(cycleLength);
        setSigOff();
        delayMicroseconds(2 * cycleLength);
        break;
      case '1':
        setSigOn();
        delayMicroseconds(2*cycleLength);
        setSigOff();
        delayMicroseconds(cycleLength);      
        break;
     }
  }
  // send the tailing pulse
  setSigOn();
  delayMicroseconds(6*cycleLength);
  setSigOff();
  delayMicroseconds(14*cycleLength);   // delay to next short packet   
  // looks like we don't need to send any more data... receivers already react at this point!
  // the protocol would have (a minimum of) 3 additional packets of repeat-codes here...
}

void sendCodeType2(const char code[]) {
  const int cycleLengthBpulse = 240; 
  const int cycleLengthBwait = 300;
  const int cycleLengthBhold = 1360;
  
  // init pulse
  setSigOn();
  delayMicroseconds(cycleLengthBpulse);
  setSigOff();
  delayMicroseconds(2 * cycleLengthBhold);

  //Serial.println("sending code...");
  for (char i = 0; i < codesForType2Length; i++) {
    //Serial.println(i);
    //Serial.println(code[i]);
    switch (code[i]) {
      case '3':
        setSigOn();
        delayMicroseconds(cycleLengthBpulse);
        setSigOff();
        delayMicroseconds(cycleLengthBwait);
      case '2':
        setSigOn();
        delayMicroseconds(cycleLengthBpulse);
        setSigOff();
        delayMicroseconds(cycleLengthBwait);
      case '1':
        setSigOn();
        delayMicroseconds(cycleLengthBpulse);
        setSigOff();
        delayMicroseconds(cycleLengthBwait);
        break;
    }
    delayMicroseconds(cycleLengthBhold);
  }
}

void sendCodeType1(const char code[]) {
  //Serial.println("sending code...");
  const int cycleLength = 120;
  for (char i = 0; i < codesForType1Length; i++) {
    //Serial.println(i);
    //Serial.println(code[i]);
    switch (code[i]) {
      case '0':
        setSigOn();
        delayMicroseconds(4 * cycleLength);
        setSigOff();
        delayMicroseconds(12 * cycleLength);
        setSigOn();
        delayMicroseconds(4 * cycleLength);
        setSigOff();
        delayMicroseconds(12 * cycleLength);
        break;
      case '1':
        setSigOn();
        delayMicroseconds(12 * cycleLength);
        setSigOff();
        delayMicroseconds(4 * cycleLength);
        setSigOn();
        delayMicroseconds(12 * cycleLength);
        setSigOff();
        delayMicroseconds(4 * cycleLength);
        break;
      case '2':
        setSigOn();
        delayMicroseconds(4 * cycleLength);
        setSigOff();
        delayMicroseconds(12 * cycleLength);
        setSigOn();
        delayMicroseconds(12 * cycleLength);
        setSigOff();
        delayMicroseconds(4 * cycleLength);
        break;
    }
  }
  // sync pulse
  setSigOn();
  delayMicroseconds(4 * cycleLength);
  setSigOff();
}


void sendType1(const char code[]) {
  Serial.println("sending code type 1...");
  for (char i = 0; i < 4; i++) {
    sendCodeType1(code);
    delay(15);
  }
  Serial.println("sending code type 1... done");
}
void sendType2(const char code[]) {
  Serial.println("sending code type 2...");
  for (char i = 0; i < 4; i++) {
    sendCodeType2(code);
    delay(15);
  }
  Serial.println("sending code type 2... done");
}
void sendType3(const char code[]) {
  Serial.println("sending code type 3...");
  for (char i = 0; i < 4; i++) {
    sendCodeType3(code);
    delay(15);
  }
  Serial.println("sending code type 3... done");
}

char currentCode1 = 0;
void sendDemo1() {
  Serial.println("sendDemo1 currentCode");
  Serial.println(int(currentCode1));

  for (char i = 0; i < 2; i++) {
    sendType1(codesForType1[currentCode1]);
    delay(15);
  }
  if (++currentCode1 == codesForType1Count) currentCode1 = 0;
}

char currentCode2 = 0;
void sendDemo2() {
  Serial.println("sendDemo2 currentCode");
  Serial.println(int(currentCode2));

  for (char i = 0; i < 2; i++) {
    sendType2(codesForType2[currentCode2]);
    delay(15);
  }
  if (++currentCode2 == codesForType2Count) currentCode2 = 0;
}

char currentCode3 = 0;
void sendDemo3() {
  Serial.println("sendDemo3 currentCode");
  Serial.println(int(currentCode3));

  for (char i = 0; i < 2; i++) {
    sendType3(codesForType3[currentCode3]);
    delay(15);
  }
  if (++currentCode3 == codesForType3Count) currentCode3 = 0;
}

/////////////////////////////////////////////////////////////////
// ARDUINO API
void setup() {
  Serial.begin(115200);
  pinMode(RFPIN, OUTPUT);
  pinMode(RF_indicator_PIN, OUTPUT);
  pinMode(RDYPIN, OUTPUT);
  setSigOff();
  Serial.println("READY");
  digitalWrite(RDYPIN, 1);
}

// the loop routine runs over and over again forever:
void loop() {

    if (Serial.available () > 0) {
      incomingString = Serial.readString();
      
      Serial.println (incomingString);
      
      if (incomingString == "ON") {
        Serial.println("Command ON!");
         sendType3(codesForType3[0]);
      } else if (incomingString == "OFF") {
        Serial.println("Command OFF!");
        sendType3(codesForType3[1]);
      } else {
         Serial.println("Command not recognized!");
      }
    }
}
