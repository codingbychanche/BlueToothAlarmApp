
/*
 * ATINY85 Bluetooth connection.
 *
 * Establishes a serial connection via the hc05 bluetooth module.
 * Shows how to handle input- and outut pins.
 * 
 * ATINY will receieve an send data via a serial connection.
 * When the strings "on"/ "off" are received, a led will turn on/ of
 * When an asociatet pin is set to high rising edge (on) is detected, wehn it is set back
 * to low, the falling edge is detected.
 *
 * Works with:
 * - Arduino Studio 1.8.8
 * - 
 */

// Libraries 
#include <SoftwareSerial.h>
 
#define LED_RECEIEVED 0
#define ALARM_PIN 1
#define LED_ALARM_RECEIVED 2
#define RX 3
#define TX 4

SoftwareSerial mySerial(RX,TX);
int alarmPinState,alarmPinPrevState;

#define MAX_SIZE_OF_SERIAL_DATA 255
char serialData[MAX_SIZE_OF_SERIAL_DATA];
int bytesRead;

//
// Once
//
void setup()
{
  pinMode(RX,INPUT);
  pinMode(TX,OUTPUT);
  pinMode(LED_ALARM_RECEIVED,OUTPUT);
  pinMode(LED_RECEIEVED,OUTPUT);
  pinMode(ALARM_PIN,INPUT_PULLUP);
  mySerial.begin(9600);
}

//
// Loop
//
 
void loop()
{
  // If serial is availalabe, then hc05 has received data 
  if(mySerial.available()){
    while(mySerial.available()){
      serialData[bytesRead]=mySerial.read();
      if (bytesRead<=MAX_SIZE_OF_SERIAL_DATA) 
          bytesRead++;
      }
      serialData[bytesRead]='\0';
      mySerial.print("received:"); // Debug echo input back to sender => Works just fine....
      mySerial.println(serialData); 
      bytesRead=0;
    }
    
  if (strcmp(serialData,"on")){
    digitalWrite(LED_RECEIEVED,LOW);
  } else{
    if (strcmp(serialData,"off")){
      digitalWrite(LED_RECEIEVED,HIGH);
    }
  }

  // Alarm receieved?
  alarmPinState=digitalRead(ALARM_PIN);
  
  // Rising edge=> turn led on and send something via serial....
  // LOW to HIGH is checked because alarm oin is in PULLUP- state.. (so, LOW means actually HIGH)
  if(alarmPinState==HIGH && alarmPinPrevState==LOW){
    mySerial.println("ALARM_RISING_EDGE");
    digitalWrite(LED_ALARM_RECEIVED,HIGH);
  } else{ 
    if (alarmPinState==LOW && alarmPinPrevState==HIGH){
      digitalWrite(LED_ALARM_RECEIVED,LOW);
      mySerial.println("ALARM_FALLING_EDGE");
    }
  }
  alarmPinPrevState=alarmPinState;
  delay(150);
}
