from datetime import datetime, date
import requests
import ssl
import json

from tensorflow.keras.models import load_model
import pandas as pd
import numpy as np

class Modelo_Inteligencia_Artificial():
    
    def __init__(self, ementas, path_modelos='modelo'):
        self.model = load_model(path_modelos)
        self.ementas = ementas
        self.model.summary()

        temp_codes_clima = {0: 'broken clouds', 1: 'clear sky', 2: 'few clouds', 3: 'light rain', 4: 'moderate rain', 5: 'overcast clouds', 6: 'scattered clouds'}
        temp_codes_sopa = {0: 'Caldo verde', 1: 'Creme de cenoura', 2: 'Creme de ervilhas', 3: 'Creme de legumes', 4: 'Sopa de abobora', 5: 'Sopa de abobóra', 6: 'Sopa de abóbora', 7: 'Sopa de alho francês', 8: 'Sopa de couve-flor', 9: 'Sopa de curgete', 10: 'Sopa de feijão verde', 11: 'Sopa de feijão vermelho', 12: 'Sopa de grão de bico', 13: 'Sopa de grão de bico com nabiças', 14: 'Sopa de legumes', 15: 'Sopa de nabos', 16: 'Sopa de penca'}
        temp_codes_parto = {0: 'Almondegas estufadas', 1: 'Arroz de frango', 2: 'Atum grelhado', 3: 'Bife de peru grelhado', 4: 'Carne de vaca estufada com cenoura e ervilhas e feijão verde', 5: 'Cação assado', 6: 'Cação estufado', 7: 'Costeleta de porco grelhada', 8: 'Entrecosto de porco assado', 9: 'Frango assado', 10: 'Lasanha de carne', 11: 'Lasanha de carnes', 12: 'Massa bolonhesa', 13: 'Massa com peru e legumes', 14: 'Medalhões de pescada com molho de tomate e legumes', 15: 'Panado de porco frito', 16: 'Pescada com broa', 17: 'Redfish no forno com cebolada', 18: 'Salada de atum e batata e feijão frade'}

        self.codes_clima = {}
        self.codes_sopa = {}
        self.codes_parto = {}

        for x,y in temp_codes_clima.items():
            self.codes_clima[y] = x
        
        for x,y in temp_codes_sopa.items():
            self.codes_sopa[y] = x
        
        for x,y in temp_codes_parto.items():
            self.codes_parto[y] = x
        
    def isFeriado(self, date_time : datetime):
        feriados = [
                    date(date_time.year, 1, 1), 
                    date(date_time.year, 4, 15), 
                    date(2022, 1, 1), 
                    date(2022, 4, 17), 
                    date(date_time.year, 4, 25),
                    date(date_time.year, 5, 1),
                    date(date_time.year, 6, 10),
                    date(date_time.year, 6, 16),
                    date(date_time.year, 8, 15),
                    date(date_time.year, 10, 5),
                    date(date_time.year, 9, 1),
                    date(date_time.year, 12, 1),
                    date(date_time.year, 12, 8),
                    date(date_time.year, 12, 25)
        ]

        diasemana = date_time.weekday()
        data = date_time.date()
        
        if diasemana == 5 or diasemana==6 or data in feriados:
            return 1
        else : return 0

    def _get_predict(self, data : datetime):
        
        mes = int(data.month)
        dia = int(data.day)
        dia_semana = int(data.weekday())
        hora = int(data.hour)
        minuto = int(data.minute)

        is_feriado = self.isFeriado(data)

        temp = self.ementas.get_dados_modelo(data)
        try:
            sopa = temp[0]
            sopa = self.codes_sopa[sopa]
        except:
            prato = -1

        try:
            prato = temp[1]
            prato = self.codes_parto[prato]
        except:
            prato = -1
        
        is_meat = temp[2]

        apiKey = '3b7b8d22483fcd71f374ee087a86802d'
        lon = "-8.397764"
        lat = "41.561830"
        ssl._create_default_https_context = ssl._create_unverified_context
        temp_site = requests.request("GET", f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={apiKey}&units=metric")
        temp_JSON = json.loads(temp_site.text)
        
        clima = -1
        try:
            clima = temp_JSON["weather"][0]["description"]
            clima = self.codes_clima[clima]
        except:
            pass
        
        temperatura = 0.0
        try:
            temperatura = float(temp_JSON["main"]["feels_like"])
        except:
            pass
        
        is_day = 1
        try:
            hora_sunset = datetime.fromtimestamp(int(temp_JSON["sys"]["sunset"]))
            if(data > hora_sunset) : is_day = 0
        except:
            pass

        df = pd.DataFrame({
                            'Mes': mes,
                            'Dia': dia,
                            'Dia Semana': dia_semana,
                            'Hora': hora,
                            'Minuto': minuto,
                            'Clima': clima,
                            'Temperatura': temperatura,
                            'IsDay': is_day,
                            'Sopa': sopa,
                            'Prato': prato,
                            'IsMeat': is_meat,
                            'isFeriado': is_feriado
        }, index=[0])

        print('---------------------------------------------------------------------------------------------------')
        print(df)
        print('---------------------------------------------------------------------------------------------------')

        valor_predict = self.model.predict(df)[0][0]
        
        if valor_predict <= 30:
            return 'Vazia ('+str(valor_predict)+')'
        elif valor_predict > 30 and valor_predict <= 85:
            return 'Normal ('+str(valor_predict)+')'
        elif valor_predict > 85 and valor_predict <= 150:
            return 'Lotada ('+str(valor_predict)+')'
        else:
            return 'Sobrelotada ('+str(valor_predict)+')'


    def get_resultado(self, data : datetime) -> str:
        
        date_divisor_almoco_superior = datetime(data.year, data.month, data.day, 11)
        date_divisor_almoco_inferior = datetime(data.year, data.month, data.day, 15)

        date_divisor_jantar_superior = datetime(data.year, data.month, data.day, 18)
        date_divisor_jantar_inferior = datetime(data.year, data.month, data.day, 21, 30)

        if data > date_divisor_almoco_superior and data < date_divisor_almoco_inferior:
            return self._get_predict(data)
        elif data > date_divisor_jantar_superior and data < date_divisor_jantar_inferior:
            return self._get_predict(data)
        
        return 'Cantina Fechada'