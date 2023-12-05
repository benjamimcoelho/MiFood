from flask import Flask, request, jsonify, render_template
from datetime import datetime
from Modelo import Modelo_Inteligencia_Artificial
from Ementas import EmentasManager

app = Flask(__name__)

app.config['JSON_AS_ASCII'] = False

ementas = EmentasManager()
modelo = Modelo_Inteligencia_Artificial(ementas)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/data.json', methods=['GET']) 
def data():
    data = request.args.get('target')
    date = datetime.strptime(data, "%Y-%m-%d %H:%M:%S")
    res = dict(
        estado = modelo.get_resultado(date),
        data = date.strftime("%Y-%m-%d %H:%M:%S")
    )
    return jsonify(res)

@app.route('/ementa.json', methods=['GET']) 
def data_ementa():
    data = request.args.get('target')
    date = datetime.strptime(data, "%Y-%m-%d %H:%M:%S")
    res = ementas.get_ementa_dia(date)
    return jsonify(res)

@app.route('/ementa_vegan.json', methods=['GET']) 
def data_ementa_vegan():
    data = request.args.get('target')
    date = datetime.strptime(data, "%Y-%m-%d %H:%M:%S")
    res = ementas.get_ementa_dia_vegan(date)
    return jsonify(res)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)