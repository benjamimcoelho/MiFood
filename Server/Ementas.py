from datetime import datetime
import re

class EmentasManager():

    def __init__(self, path_ementa_dia='ementa.csv', path_ementa_noite='ementa_noite.csv', path_ementa_dia_vegan='ementa_vegan.csv', path_ementa_noite_vegan='ementa_noite_vegan.csv', encoding="utf-8"):
        self.ementas_dia = {}
        self.ementas_noite = {}

        self.ementas_dia_vegan = {}
        self.ementas_noite_vegan = {}

        f = open(path_ementa_dia, "r", encoding=encoding)
        cabecalho = f.readline()
        cabecalho = re.split(',', cabecalho)
        
        for line in f.readlines():
            line_split = re.split(',', line)
            
            data = line_split[0]
            
            temp = {}
            i = 1
            for x in line_split[1:]:
                x = x.strip()
                if( x == 'NULL'):
                    temp[cabecalho[i].strip()] = ''
                else:
                    temp[cabecalho[i].strip()] = x
                i += 1
            
            self.ementas_dia[data] = temp
        
        f.close()
        
        f = open(path_ementa_noite, "r", encoding=encoding)
        cabecalho = f.readline()
        cabecalho = re.split(',', cabecalho)
        
        for line in f.readlines():
            line_split = re.split(',', line)
            
            data = line_split[0]
            
            temp = {}
            i = 1
            for x in line_split[1:]:
                x = x.strip()
                if( x == 'NULL'):
                    temp[cabecalho[i].strip()] = ''
                else:
                    temp[cabecalho[i].strip()] = x
                i += 1
            
            self.ementas_noite[data] = temp
        
        f.close()
        
        # Load Vegan
        f = open(path_ementa_dia_vegan, "r", encoding=encoding)
        cabecalho = f.readline()
        cabecalho = re.split(',', cabecalho)
        
        for line in f.readlines():
            line_split = re.split(',', line)
            
            data = line_split[0]
            
            temp = {}
            i = 1
            for x in line_split[1:]:
                x = x.strip()
                if( x == 'NULL'):
                    temp[cabecalho[i].strip()] = ''
                else:
                    temp[cabecalho[i].strip()] = x
                i += 1
            
            self.ementas_dia_vegan[data] = temp
        
        f.close()

        f = open(path_ementa_noite_vegan, "r", encoding=encoding)
        cabecalho = f.readline()
        cabecalho = re.split(',', cabecalho)
        
        for line in f.readlines():
            line_split = re.split(',', line)
            
            data = line_split[0]
            
            temp = {}
            i = 1
            for x in line_split[1:]:
                x = x.strip()
                if( x == 'NULL'):
                    temp[cabecalho[i].strip()] = ''
                else:
                    temp[cabecalho[i].strip()] = x
                i += 1
            
            self.ementas_noite_vegan[data] = temp
        
        f.close()

    def get_dados_modelo(self, data : datetime):

        date_divisor_almoco_superior = datetime(data.year, data.month, data.day, 12)
        date_divisor_almoco_inferior = datetime(data.year, data.month, data.day, 14)

        date_divisor_jantar_superior = datetime(data.year, data.month, data.day, 19)
        date_divisor_jantar_inferior = datetime(data.year, data.month, data.day, 20,30)

        try:
            if data > date_divisor_almoco_superior and data < date_divisor_almoco_inferior:
                temp = self.ementas_dia[data.strftime("%Y-%m-%d")]
                return ( temp['Sopa'], temp['Prato'], float(temp['Carne']) )
            elif data > date_divisor_jantar_superior and data < date_divisor_jantar_inferior:
                temp = self.ementas_noite[data.strftime("%Y-%m-%d")]
                return ( temp['Sopa'], temp['Prato'], float(temp['Carne']) )
        except:
            pass

        return (-1, -1, -1)

    def get_ementa_dia(self, data : datetime) -> dict :
        
        date_divisor_almoco_superior = datetime(data.year, data.month, data.day, 12)
        date_divisor_almoco_inferior = datetime(data.year, data.month, data.day, 14)

        date_divisor_jantar_superior = datetime(data.year, data.month, data.day, 19)
        date_divisor_jantar_inferior = datetime(data.year, data.month, data.day, 20,30)

        if data > date_divisor_almoco_superior and data < date_divisor_almoco_inferior:
            try:
                return self.ementas_dia[data.strftime("%Y-%m-%d")]
            except:
                return self.ementas_dia[datetime(2022, 4, 15).strftime("%Y-%m-%d")]
        elif data > date_divisor_jantar_superior and data < date_divisor_jantar_inferior:
            try:
                return self.ementas_noite[data.strftime("%Y-%m-%d")]
            except:
                return self.ementas_dia[datetime(2022, 4, 15).strftime("%Y-%m-%d")]
        
        return self.ementas_dia[datetime(2022, 4, 15).strftime("%Y-%m-%d")]

    def get_ementa_dia_vegan(self, data : datetime) -> dict :
        
        date_divisor_almoco_superior = datetime(data.year, data.month, data.day, 12)
        date_divisor_almoco_inferior = datetime(data.year, data.month, data.day, 14)

        date_divisor_jantar_superior = datetime(data.year, data.month, data.day, 19)
        date_divisor_jantar_inferior = datetime(data.year, data.month, data.day, 20,30)

        if data > date_divisor_almoco_superior and data < date_divisor_almoco_inferior:
            try:
                return self.ementas_dia_vegan[data.strftime("%Y-%m-%d")]
            except:
                return self.ementas_dia_vegan[datetime(2022, 4, 15).strftime("%Y-%m-%d")]
        elif data > date_divisor_jantar_superior and data < date_divisor_jantar_inferior:
            try:
                return self.ementas_noite_vegan[data.strftime("%Y-%m-%d")]
            except:
                return self.ementas_dia_vegan[datetime(2022, 4, 15).strftime("%Y-%m-%d")]
        
        return self.ementas_dia_vegan[datetime(2022, 4, 15).strftime("%Y-%m-%d")]