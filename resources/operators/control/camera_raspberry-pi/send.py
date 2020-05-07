import math
import random, string
import json
import time
from datetime import datetime

from mqttClient import mqttClient

packet_size = 3000

def randomword(length):
 return ''.join(random.choice(string.lowercase) for i in range(length))

def publishEncodedImage(encoded, hostname, port=1883):

    id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
    # --- Create instance of mqtt client
    publisher = mqttClient(hostname, port, id)
    publisher.connect()

    try:  
        # --- Divides enconded image into several packets
        end = packet_size
        start = 0
        length = len(encoded)
        picId = randomword(8)
        pos = 0
        no_of_packets = math.ceil(length/packet_size)

        # --- Loop to send each  
        while start <= len(encoded):
            # --- Creates message to be published
            data = {"data": encoded[start:end], "pic_id":picId, "pos": pos, "size": no_of_packets}
            message = json.JSONEncoder().encode(data)

            # --- Publishes the message
            publisher.sendMessage("image", message)

            # --- Sleep and continues the loop
            time.sleep(0.05)
            end += packet_size
            start += packet_size
            pos = pos +1
        pass
    finally:
        print("sent successfully to broker "+str(hostname))