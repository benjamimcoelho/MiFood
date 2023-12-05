import ssl
import requests
import json
import time

from Adafruit_IO import Client

class Pedidos():

    __payload = {}
    __headers = {}
    __apiKey = '3b7b8d22483fcd71f374ee087a86802d'

    lon = "-8.397764"
    lat = "41.561830"

    def __init__(self):
        pass

    def get_Transito(self):
        ssl._create_default_https_context = ssl._create_unverified_context

        temp = requests.request("GET", f"https://api.openweathermap.org/data/2.5/weather?lat={self.lat}&lon={self.lon}&appid={self.__apiKey}&units=metric", headers=self.__headers, data=self.__payload)
        temp_JSON = json.loads(temp.text)

        return str(temp_JSON)

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
    adafruit = ADAFRUIT_CONECTION('trabalho')

    while True:
        temp_json = p.get_Transito()
        adafruit.sendData(temp_json)

        time.sleep(60*60) # 1 hora

if __name__ == "__main__":
    main()