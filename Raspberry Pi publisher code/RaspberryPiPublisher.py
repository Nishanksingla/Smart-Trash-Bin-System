

from pi_switch import RCSwitchReceiver

import paho.mqtt.client as mqtt
import json
import time

receiver = RCSwitchReceiver()
receiver.enableReceive(2)

num = 0
prevDist=0

def onConnect(client, userdata, flags, rc):
        print("Connected with result code "+str(rc))


mqttClient = mqtt.Client("c3")
mqttClient.on_connect = onConnect

#mqttClient.connect("54.187.15.61",1883)
mqttClient.connect("54.67.96.20",1883)

message={}

while True:
    if receiver.available():
        received_value = receiver.getReceivedValue()

        if received_value:
            num += 1
            print("Received[%s]:" % num)
            print(received_value)
            print("%s / %s bit" % (received_value, receiver.getReceivedBitlength()))
            print("Protocol: %s" % receiver.getReceivedProtocol())
            print("")
            if prevDist!=received_value and received_value<151:
            	message["id"]=2
            	message["distance"]=received_value
            	mqttClient.publish("Message",json.dumps(message))
            	print("published successfully")
                prevDist=received_value
            mqttClient.loop(2)
        #time.sleep(60)
        receiver.resetAvailable()
