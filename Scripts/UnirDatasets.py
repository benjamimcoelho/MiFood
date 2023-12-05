import re
from datetime import datetime

f1 = open('dados/probingForPowerBI.csv')
f2 = open('dados/tempoForPowerBI.csv')
f3 = open('dados/ementa.csv')
f4 = open('dados/ementa_noite.csv')
f5 = open('dados/ementa_vegan.csv')
f6 = open('dados/ementa_noite_vegan.csv')

result = open('dados/tudo_junto.csv','w')

cabecalho_f1 = f1.readline()
cabecalho_f2 = f2.readline()
cabecalho_f3 = f3.readline()
cabecalho_f4 = f4.readline()
cabecalho_f5 = f5.readline()
cabecalho_f6 = f6.readline()

conteudo_f2_by_date = {}
conteudo_f3_by_date = {}
conteudo_f4_by_date = {}
conteudo_f5_by_date = {}
conteudo_f6_by_date = {}

for line in f2.readlines():
    line = line.strip()
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[1].strip(), "%Y-%m-%d %H:%M:%S")
    conteudo_f2_by_date[data] = line

for line in f3.readlines():
    line = line.strip()
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
    conteudo_f3_by_date[data] = line

for line in f4.readlines():
    line = line.strip()
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
    conteudo_f4_by_date[data] = line

for line in f5.readlines():
    line = line.strip()
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
    conteudo_f5_by_date[data] = line

for line in f6.readlines():
    line = line.strip()
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[0].strip(), "%Y-%m-%d")
    conteudo_f6_by_date[data] = line

result.write(f'{cabecalho_f1.strip()},{cabecalho_f2.strip()},{cabecalho_f3.strip()},{cabecalho_f5.strip()}\n')

for line in f1.readlines():
    line_split = re.split(',', line)
    data = datetime.strptime(line_split[2].strip(), "%Y-%m-%d %H:%M:%S")

    data_sem_time = data.strftime("%Y-%m-%d")
    data_sem_time = datetime.strptime(data_sem_time, "%Y-%m-%d")

    secound_part = conteudo_f2_by_date[data]
    final_part = f'{data.strftime("%Y-%m-%d")},NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL'
    final_part_vegan = f'{data.strftime("%Y-%m-%d")},NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL'
    
    date_divisor_almoco_superior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 12)
    date_divisor_almoco_inferior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 14)

    date_divisor_jantar_superior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 19)
    date_divisor_jantar_inferior = datetime(data_sem_time.year, data_sem_time.month, data_sem_time.day, 20,30)

    if data > date_divisor_almoco_superior and data < date_divisor_almoco_inferior:
        final_part = conteudo_f3_by_date[data_sem_time]
        final_part_vegan = conteudo_f5_by_date[data_sem_time]
    elif data > date_divisor_jantar_superior and data < date_divisor_jantar_inferior:
        final_part = conteudo_f4_by_date[data_sem_time]
        final_part_vegan = conteudo_f6_by_date[data_sem_time]

    result.write(f'{line.strip()},{secound_part},{final_part},{final_part_vegan}\n')

f1.close()
f2.close()
f3.close()
f4.close()

print('Concluido')