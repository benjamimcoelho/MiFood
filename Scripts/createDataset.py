from encodings import utf_8
import sys
import json
from datetime import date, datetime, timedelta
from Row import Row
from Adafruit_IO import Client
import re
from datetime import timedelta



#global variables

feriados = [date(2022, 4, 18),date(2022, 4, 25),date(2022, 5, 1)]

#interval in minutes to sum probes
timestamp_difference = 5

conteudo_ementa_by_date = {}
conteudo_ementa_noite_by_date = {}

def isFeriado(date_time):
    diasemana = date_time.weekday()
    data = date_time.date()
    if diasemana == 5 or diasemana==6 or data in feriados:
        return True
    else : return False


def getEmentas():
    ementa = open('../dados/ementa.csv',encoding='utf_8')
    ementa_noite = open('../dados/ementa_noite.csv',encoding='utf_8')

    cabecalho_ementa = ementa.readline()
    cabecalho_ementa_noite = ementa_noite.readline()

    for line in ementa.readlines():
        line = line.strip()
        line_split = re.split(',', line)
        data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
        conteudo_ementa_by_date[data] = line

    for line in ementa_noite.readlines():
        line = line.strip()
        line_split = re.split(',', line)
        data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
        conteudo_ementa_noite_by_date[data] = line

def getNearestEmenta(date_time):
    data_sem_time = date_time.strftime("%Y-%m-%d")
    data_sem_time = datetime.strptime(data_sem_time, "%Y-%m-%d")

    date_divisor_almoco_superior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 11)
    date_divisor_almoco_inferior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 15)

    date_divisor_jantar_superior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 18)
    date_divisor_jantar_inferior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 21,30)

    if date_time > date_divisor_almoco_superior and date_time < date_divisor_almoco_inferior:
        resultado = re.split(',',conteudo_ementa_by_date[data_sem_time])
        return (resultado[1],resultado[2],resultado[13])
    elif date_time > date_divisor_jantar_superior and date_time < date_divisor_jantar_inferior:
        resultado = re.split(',',conteudo_ementa_noite_by_date[data_sem_time])
        return (resultado[1],resultado[2],resultado[13])

    return ('NULL','NULL','NULL')


#returns the nearest weather to the time given
def getNearestWeather(date_time, clima):
    nearest_clima=None
    last_difference= timedelta(days=999999999)
    for x, data in clima.items():
        val = abs(date_time - x) 
        if (last_difference > val) :
            last_difference = val
            nearest_clima = data
    descricao = re.search('description\'\:\s\'([a-z\s]+)\'',nearest_clima).group(1)
    temperatura = re.search('feels_like\'\:\s([\d\.]+)',nearest_clima).group(1)
    sunset = datetime.fromtimestamp(int(re.search('sunrise\'\:\s(\d+)',nearest_clima).group(1)))



    return (descricao,temperatura,sunset)

def main():
    getEmentas()
    #Adafruit Connection
    aio = Client('Dr_Kali','aio_pKPK04t5xihR0ZzFpxKbpxt2xfVW')

    clima_dados = aio.data('trabalho', max_results=None)


    clima={}

    for d in clima_dados:
        clima[datetime.fromtimestamp(d.created_epoch)]=d.value

    #load json with probing data
    f = open('../dados/arduino-286cf-default-rtdb-export.json')

    data = json.load(f)

    #open .csv file to write results
    result = open('../dados/probing.csv','w',encoding='utf_8')


    #auxiliar variables
    last_date = None
    dataset = []
    dataset_size = 0


    #process the data
    for sample in data['Teste-Arduino']:
            tempo = int(data['Teste-Arduino'][sample]['timestamp'])
            date = datetime.fromtimestamp(tempo/1000)

            date_from = datetime(2022, 4, 18)
            if date >= date_from :

                if(last_date!=None):
                    difference_in_minutes = divmod((date-last_date).total_seconds(), 60)[0]
                    if (difference_in_minutes>timestamp_difference):
                        last_date=date
                        clima_tempo = getNearestWeather(date,clima)
                        ementa = getNearestEmenta(date)
                        feriado = isFeriado(date)
                        hora_sunset = clima_tempo[2]
                        is_day=True
                        if(date>hora_sunset) : is_day=False
                        dataset.insert(dataset_size, Row(date,clima_tempo[0],clima_tempo[1],is_day, ementa[0],ementa[1],ementa[2],feriado))
                        dataset_size+=1
                    
                else:
                    last_date=date
                    clima_tempo = getNearestWeather(date,clima)
                    ementa = getNearestEmenta(date)
                    feriado = isFeriado(date)
                    hora_sunset = clima_tempo[2]
                    is_day=True
                    if(date>hora_sunset) : is_day=False

                    dataset.insert(dataset_size, Row(date,clima_tempo[0],clima_tempo[1], is_day, ementa[0],ementa[1],ementa[2],feriado))
                    dataset_size+=1

                #add probe number to this date

                #se existirem probes
                if(len(data['Teste-Arduino'][sample])>3):
                    dataset[dataset_size-1].addProbes(len(data['Teste-Arduino'][sample]['probes']))
                


    #write data to csv file

    result.write('Indice,Mes,Dia,Dia Semana,Hora,Minuto,Clima,Temperatura,IsDay,Sopa,Prato,IsMeat,isFeriado,Probes\n')
    i=0
    for row in dataset:
        result.write(str(i)+','+str(row.getMonth())+','+str(row.getDay())+','+str(row.getWeekDay())+','+str(row.getHour())+','+str(row.getMinute())+','+str(row.getWeather())+','+str(row.getTemperature())+','+str(row.getIsDay())+','+str(row.getSopa())+','+str(row.getPrato())+','+str(row.getCarne())+','+str(row.getIsFeriado())+','+str(row.getProbes())+'\n')
        i+=1

        
        
    #print(.strftime('%Y-%m-%d %H:%M:%S'))


    #close files
    f.close()
    result.close()


main()
print("Sucesso!")