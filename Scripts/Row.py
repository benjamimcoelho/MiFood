from datetime import date


class Row:
    

    def __init__(self, datetime, weather, temperature, is_day, sopa, prato, carne, feriado):
        self.date = datetime
        self.weather=weather
        self.temperature = temperature
        self.probes = 0
        self.is_day = is_day
        self.sopa = sopa
        self.prato = prato
        self.carne = carne
        self.feriado = feriado


    def addProbes(self, n):
        self.probes += n

    def getYear(self):
        return self.date.year

    def getMonth(self):
        return self.date.month

    def getDay(self):
        return self.date.day

    def getHour(self):
        return self.date.hour

    def getMinute(self):
        return self.date.minute
    
    def getWeekDay(self):
        return self.date.weekday()

    def getProbes(self):
        return self.probes
    
    def getWeather(self):
        return self.weather
    
    def getTemperature(self):
        return self.temperature

    def getIsDay(self):
        if self.is_day:
            return 1
        else: return 0

    def getIsFeriado(self):
        if self.feriado:
            return 1
        else: return 0

    def getSopa(self):
        return self.sopa
    
    def getPrato(self):
        return self.prato

    def getCarne(self):
        return self.carne
    