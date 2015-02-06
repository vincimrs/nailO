//For flex pcb buttons
#include <Wire.h>
#include <SPI.h>
#include <boards.h>
#include <RBL_nRF8001.h>
#include <services.h> 


uint8_t i2caddr = 0x25;

unsigned char c = 'a'; 
unsigned char sensorValues [15] = {'a','a','a','a','a','a','a','a','a','a','a','a','a','a','a'}; 
unsigned char touchRegisters [4] = {'a','a','a','a'}; 
int counter = 0;
unsigned int touchX = 0; 
unsigned int touchY = 0;

int led = 7;

static byte buf_len = 0;

uint8_t str[] = "ABC";

void setup() { 
  Wire.begin();
  Serial.begin(57600);
  Serial.println("Hello");  
  pinMode(led, OUTPUT); 
  
  
  ble_set_pins(10, 2);
  // Set your BLE Shield name here, max. length 10
  ble_set_name("Nails");
  
  // Init. and start BLE library.
  ble_begin();
  
  for(int i = 0; i<10; i++) { 
    digitalWrite(led , HIGH );    // turn the LED off by making the voltage LOW
    delay(100);
     digitalWrite(led, LOW);   // turn the LED on (HIGH is the voltage level)
     delay(100);
  }
}

void loop() { 
   digitalWrite(led, LOW);   // turn the LED on (HIGH is the voltage level)
  //sendCommand(0x80);
  readSensorValues(); 
  readXYPosition();
  delay(100);
  digitalWrite(led , HIGH );    // turn the LED off by making the voltage LOW
  unsigned int a = c; 
  
  /*
  Serial.print("S,");
  for(int i=0; i<15; i++){
  Serial.print(sensorValues[i]);
  Serial.print(",");
  }
 // Serial.println(" ");
  Serial.print(touchX); 
  Serial.print(",");
  Serial.println(touchY);
  //Serial.println(touchY);
  //Serial.println(" ");
  */ 
  
  //The connected sensors are 0,1,2,7,8,9,10,11,14
  Serial.print("S,"); 
  Serial.print(sensorValues[0]); 
  Serial.print(","); 
  Serial.print(sensorValues[1]); 
  Serial.print(","); 
  Serial.print(sensorValues[2]); 
  Serial.print(","); 
  Serial.print(sensorValues[7]); 
  Serial.print(","); 
  Serial.print(sensorValues[12]); 
  Serial.print(","); 
  Serial.print(sensorValues[9]); 
  Serial.print(",");   
    Serial.print(sensorValues[10]); 
  Serial.print(",");  
    Serial.print(sensorValues[11]); 
  Serial.print(",");  
    Serial.print(sensorValues[14]); 
  Serial.println(" ");  
  
  if ( ble_connected() )
  {
    ble_write('S');
    ble_write(',');
    ble_write(sensorValues[0]);
    ble_write(',');
    ble_write(sensorValues[1]);
    ble_write(',');
    ble_write(sensorValues[2]);
    ble_write(',');
    ble_write(sensorValues[7]);
    ble_write(',');
    ble_write(sensorValues[8]);
    ble_write(',');   
    ble_write(sensorValues[9]);
    ble_write(',');   
    ble_write(sensorValues[10]);
    ble_write(',');
    ble_write(sensorValues[11]);
    ble_write(','); 
    ble_write(sensorValues[14]);
    ble_write(',');   
    //sendCustomData(str, 3);
  }
  ble_do_events();
  
  if ( ble_available() )
  {
    while ( ble_available() )
    {
      Serial.write(ble_read());
    }
    
    Serial.println();
  }
  
  
}

void sendCommand(uint8_t c) { 
  
     //uint8_t control = 0x4A;   // Co = 0, D/C = 0
     
   //  uint8_t i2caddr = 0x4A;
    Wire.beginTransmission(i2caddr);
    Wire.write(0x04);
    Wire.write(0x80);
    Wire.endTransmission();
}

void readTransaction() { 
   Wire.beginTransmission(i2caddr); 
   Wire.write(0x80);
   Wire.endTransmission(false);
  // Wire.write(0x25);
   Wire.requestFrom(0x25,14);  
}

void readSensorValues() { 
  counter = 0;
   Wire.beginTransmission(i2caddr); 
   Wire.write(0x80);
   Wire.endTransmission(false);
  // Wire.write(0x25);
   Wire.requestFrom(0x25,15);  
   while(Wire.available()) { 
     sensorValues[counter]= Wire.read();
     counter++;
     
   }
}

void readXYPosition() { 
  counter = 0;
   Wire.beginTransmission(i2caddr); 
   Wire.write(0x11);
   Wire.endTransmission(false);
  // Wire.write(0x25);
   Wire.requestFrom(0x25,3);  
   while(Wire.available()) { 
     touchRegisters[counter]= Wire.read();
     counter++;
     
   }
   unsigned char ylsb = (touchRegisters[2] & B00001111); 
   unsigned char xlsb = (touchRegisters[2]>>4) & B00001111; 
   touchX = (touchRegisters[0]<<4) + xlsb;
   touchY= (touchRegisters[1]<<4) + ylsb;
}

void ble_write_string(byte *bytes, uint8_t len)
{
  if (buf_len + len > 20)
  {
    for (int j = 0; j < 15000; j++)
      ble_do_events();
    
    buf_len = 0;
  }
  
  for (int j = 0; j < len; j++)
  {
    ble_write(bytes[j]);
    buf_len++;
  }
    
  if (buf_len == 20)
  {
    for (int j = 0; j < 15000; j++)
      ble_do_events();
    
    buf_len = 0;
  }  
}  

void sendCustomData(uint8_t *buf, uint8_t len)
{
  uint8_t data[20] = "Z";
  memcpy(&data[1], buf, len);
  ble_write_string(data, len+1);
}
