import ssl
import requests
import json
import time

from Adafruit_IO import Client

class Pedidos():

    __payload = {}
    __headers = {}
    __apiKey = 'AuSTxmk5pWcBd4ATXWUcvFupmjyMOb3T'
    
    inferiorEsquerdo = "41.540729323010765,-8.403889456544157"
    superiorDireito  = "41.568270930283454, -8.39185576800956"


    def __init__(self):
        pass

    def get_Transito(self, lat, lon):
        ssl._create_default_https_context = ssl._create_unverified_context

        traf = requests.request("GET", "https://api.tomtom.com/traffic/services/5/incidentDetails?key=AuSTxmk5pWcBd4ATXWUcvFupmjyMOb3T&bbox=41.540729323010765,-8.403889456544157,41.568270930283454,-8.39185576800956&fields={incidents{type,geometry{type,coordinates},properties{iconCategory}}}&language=pt-PT&timeValidityFilter=present", headers=self.__headers, data=self.__payload)
        traf_json = json.loads(traf.text)

        return str(traf_json)

class ADAFRUIT_CONECTION():

    def __init__(self, fedd_name):
        self.aio = Client('Dr_Kali', 'aio_pKPK04t5xihR0ZzFpxKbpxt2xfVW')
        self.feed = self.aio.feeds(fedd_name)
    
    def sendData(self, dados):
        self.aio.send_data(self.feed.key, dados)

    def getData(self):
        data = self.aio.receive(self.feed)
        return data

def main():

    p = Pedidos()
    adafruit = ADAFRUIT_CONECTION('trabalho-incidentes')

    while True:
        temp_json = p.get_Transito("41.55933190888603","-8.396057943144688")
        adafruit.sendData(temp_json)

        time.sleep(60*10) # 10 em 10 minutos

if __name__ == "__main__":
    main()