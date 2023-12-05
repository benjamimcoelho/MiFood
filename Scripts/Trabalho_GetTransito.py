import ssl
import requests
import json
import time

from Adafruit_IO import Client

class Pedidos():

    __payload = {}
    __headers = {}
    __apiKey = 'AuSTxmk5pWcBd4ATXWUcvFupmjyMOb3T'

    def __init__(self):
        pass

    def get_Transito(self, lat, lon):
        ssl._create_default_https_context = ssl._create_unverified_context

        traf = requests.request("GET", f"https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key={self.__apiKey}&point={lat},{lon}&unit=kmph&thickness=10", headers=self.__headers, data=self.__payload)
        traf_json = json.loads(traf.text)

        traf_json['flowSegmentData']['coordinates'] = []

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
    adafruit = ADAFRUIT_CONECTION('trabalho-transito')

    while True:
        temp_json = p.get_Transito("41.55933190888603","-8.396057943144688")
        adafruit.sendData(temp_json)

        temp_json = p.get_Transito("41.554937056346624","-8.401401987632545")
        adafruit.sendData(temp_json)

        temp_json = p.get_Transito("41.547532789641856","-8.40517968073649")
        adafruit.sendData(temp_json)

        temp_json = p.get_Transito("41.562680345476764","-8.39332903458672")
        adafruit.sendData(temp_json)

        time.sleep(60*10) # 10 em 10 minutos

if __name__ == "__main__":
    main()