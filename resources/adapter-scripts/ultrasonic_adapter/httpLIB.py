import requests 

class Http(object):

    def sendRequest(self, ip, port, api, data):

        URL = "http://"+ip+":"+str(port)+"/"+api

        #data = '{"ip": "'+self.broker_ip+'", "topic": "'+self.topic+'","status": "'+1+'"}"'

        r = requests.post(url = URL, data = data) 

        # extracting response text  
        pastebin_url = r.text 
        print("The pastebin URL is:%s"%pastebin_url) 