#import pywapi
import paho.mqtt.client as mqtt
import json
import urllib2, urllib,time



logfile = open("logfile.txt","w")


def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("Message")
    print "Subscription successful"

def on_message(client, userdata, msg):
    print "got mesasge"
    print(msg.topic+" "+str(msg.payload))
    jsonmsg = json.loads(msg.payload)
    logfile.write(str(jsonmsg["id"]) + "," + str(jsonmsg["distance"]) +"\n")
    logfile.flush()

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("54.67.96.20", 1883)

client.loop_forever()
logfile.close()
