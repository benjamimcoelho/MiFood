from Adafruit_IO import Client
import re
import json
from datetime import datetime, timedelta

#returns the nearest weather to the time given
def getNearestWeather(date_time, clima):
    nearest_clima=None
    last_difference= timedelta(days=999999999)

    temp = None

    for x, data in clima.items():
        val = abs(date_time - x) 
        if (last_difference > val) :
            last_difference = val
            nearest_clima = data
            temp = x

    nearest_clima = re.sub("'",'"', nearest_clima)
    nearest_clima = json.loads(nearest_clima)
    
    main = nearest_clima["weather"][0]["main"]
    description = nearest_clima["weather"][0]["description"]
    temp = nearest_clima["main"]["temp"]
    feels_like = nearest_clima["main"]["feels_like"]
    temp_min = nearest_clima["main"]["temp_min"]
    temp_max = nearest_clima["main"]["temp_max"]
    pressure = nearest_clima["main"]["pressure"]
    humidity = nearest_clima["main"]["humidity"]
    sea_level = nearest_clima["main"]["sea_level"]
    grnd_level = nearest_clima["main"]["grnd_level"]
    visibility = nearest_clima["visibility"]
    wind_speed = str(int(nearest_clima["wind"]["speed"]))
    wind_deg = str(int(nearest_clima["wind"]["deg"]))
    wind_gust = str(int(nearest_clima["wind"]["gust"]))
    clouds = nearest_clima["clouds"]["all"]
    sunrise = datetime.fromtimestamp(int(nearest_clima["sys"]["sunrise"])).strftime("%H:%M:%S")
    sunset = datetime.fromtimestamp(int(nearest_clima["sys"]["sunset"])).strftime("%H:%M:%S")

    return (main,description,temp,feels_like,temp_min,temp_max,pressure,humidity,sea_level,grnd_level,visibility,wind_speed,wind_deg,wind_gust,clouds,sunrise,sunset)

aio = Client('Dr_Kali','aio_pKPK04t5xihR0ZzFpxKbpxt2xfVW')
clima_dados = aio.data('trabalho', max_results=None)
clima={}
for d in clima_dados:
    clima[datetime.fromtimestamp(d.created_epoch)]= d.value

f = open('dados/arduino-286cf-default-rtdb-export.json')
data = json.load(f)

result = open('dados/tempoForPowerBI.csv','w')
result.write('indice_tempo,data_tempo,main_tempo,description_tempo,temp_tempo,feels_like_tempo,temp_min_tempo,temp_max_tempo,pressure_tempo,humidity_tempo,sea_level_tempo,grnd_level_tempo,visibility_tempo,wind speed_tempo,wind deg_tempo,wind gust_tempo,clouds_tempo,sunrise_tempo,sunset_tempo\n')

i = 0
for sample in data['Teste-Arduino']:
    dados = data['Teste-Arduino'][sample]
    date_time = datetime.fromtimestamp(int(dados['timestamp'])/1000)
    
    date_from = datetime(2022, 4, 16)

    if date_time >= date_from :
        ct = getNearestWeather(date_time, clima)
        result.write(f'{i},{date_time.strftime("%Y-%m-%d %H:%M:%S")},{ct[0]},{ct[1]},{ct[2]},{ct[3]},{ct[4]},{ct[5]},{ct[6]},{ct[7]},{ct[8]},{ct[9]},{ct[10]},{ct[11]},{ct[12]},{ct[13]},{ct[14]},{ct[15]},{ct[16]}\n')
        i += 1

print(f'Done -> {i}')

result.close()
f.close()