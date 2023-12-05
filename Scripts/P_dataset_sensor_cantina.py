import json
from datetime import datetime


f = open('dados/arduino-286cf-default-rtdb-export.json')
data = json.load(f)
result = open('dados/probingForPowerBI.csv','w')

result.write('indice_SC,deviceId_SC,data_SC,mac_SC,previous_millis_detected_SC,rssi_SC,type_SC\n')

i = 0
for sample in data['Teste-Arduino']:
    dados = data['Teste-Arduino'][sample]
    date_time = datetime.fromtimestamp(int(dados['timestamp'])/1000)
    
    date_from = datetime(2022, 4, 16)

    if date_time >= date_from :
        try:
            lista_probes = list(dados["probes"])
        except:
            pass

        for probe in lista_probes:
            result.write(f'{i},{dados["deviceId"]},{date_time.strftime("%Y-%m-%d %H:%M:%S")},{probe["mac"]},{probe["previousMillisDetected"]},{probe["rssi"]},{dados["type"]}\n')
            i += 1

print(f'Done -> {i}')

result.close()
f.close()