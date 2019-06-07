from flask import Flask, jsonify, request, make_response, abort
from functools import wraps
from cryptography.fernet import Fernet
import jwt
import datetime
import os
import psycopg2
import json
import boto3
import paramiko

app = Flask(__name__)
app.config['SECRET_KEY'] = "Secret Key" #Llave para los tokens
llave_cifra = b'pRmgMa8T0INjEAfksaq2aafzoZXEuwKI7wDe4c1F8AY=' #Llave de cifrado de contraseñas

DATABASE_URL = os.environ['DATABASE_URL'] #Conexión para la base de datos, con ayuda de heroku
conn = psycopg2.connect(DATABASE_URL, sslmode='require')

def getRecetas_Prolog(): #Obtiene la base de conocimientos de s3
	s3_client=boto3.resource('s3')
	objeto = s3_client.Object('recetas-imagenes','prolog/recetas.pl')
	archivo = objeto.get()
	archivo = archivo["Body"]
	archivo = archivo.read().decode()
	archivo = archivo.replace("!","\x21")#se necesita cambiar los ! por \x21 para poder usarlos en la línea de comandos

	return archivo



def subir_receta(nombre,data):
	archivo = getRecetas_Prolog()

	comando = 'python3 check_receta.py ' + nombre + ' "' + archivo + '"'

	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45") #Se conecta a Azure para usar pyswip

	entrada,salida,error = ssh.exec_command(comando) 
	res = salida.read().decode() # Aqui recupera los datos, pero sólo obtiene prints()

	ssh.close()

	res = res[:-1] #Elimina el \n

	if(res == "True"): #El equivalente a false en pyswip
		return {'message': 'Ya existe una receta con ese nombre'}

	s3_client=boto3.resource('s3')
	objeto = s3_client.Object('recetas-imagenes','prolog/recetas.pl')
	archivo = archivo[:-1]
	archivo = data + "\n" + archivo
	objeto.put(Body=archivo)#Subir el archivo

	return {'message': 'Receta anadida'}


def buscarRecetas_nombre(nombre): #Busca la receta con el nombre dado(No puede haber más de una receta con el mismo nombre)
	archivo = getRecetas_Prolog()

	datos = 'python3 get_receta.py ' + nombre + ' "' + archivo + '"' #crea el comando a ejecutar

	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45") #Se conecta a Azure para usar pyswip

	entrada,salida,error = ssh.exec_command(datos) 
	res = salida.read().decode() # Aqui recupera los datos, pero sólo obtiene prints()

	ssh.close()

	res = res[:-1]
	res = res.replace("'","\"")
	try:
		res = json.loads(res)#Transforma el string en un diccionario
	except:
		return {'message':'No existe ninguna receta con ese nombre'}
	return res #Si no encuentra ninguna receta con ese nombre, le mismo comando de ssh devuelve un json con el mensaje de error
	#Formato enseñado si encuentra rceta: {'Nombre':'nombre','Tipo':'tipoX','Pasos': ['paso1','paso2'],'Ingredientes':['ing','ing2','ing3'],'Fotos':['url','url2','url3']}



def buscarRecetas_tipo(tipo):#Busca en la base de conocimientos las recetas que sean del tipo dado
	archivo = getRecetas_Prolog() 
	
	datos = 'python3 buscar_por_tipo.py ' + tipo + ' "' + archivo + '"' #crea el comando a ejecutar

	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45") #Se conecta a Azure para usar pyswip

	entrada,salida,error = ssh.exec_command(datos) 
	res = salida.read().decode() # Aqui recupera los datos, pero sólo obtiene prints()

	ssh.close()	

	res = res[2:-3]
	res = res.split("', '") #Convierte el string en una lista

	if (res == [""]):
		return {"message": "No existe ninguna receta con ese tipo"}

	recetasJSON = {"Nombres": res}
	return recetasJSON


def buscarRecetas_ing(ing):#Busca en la base de conocimientos las recetas que tengan el ingrediente dado
	archivo = getRecetas_Prolog()
	
	datos = 'python3 buscar_por_ing.py ' + ing + ' "' + archivo + '"' #crea el comando a ejecutar

	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45") #Se conecta a Azure para usar pyswip

	entrada,salida,error = ssh.exec_command(datos) 
	res = salida.read().decode() # Aqui recupera los datos, pero sólo obtiene prints()

	ssh.close()	
	res = res[2:-3]
	res = res.split("', '") #Convierte el string en una lista

	if (res == [""]):
		return {'message': 'No existe ninguna receta con ese ingrediente'}

	recetasJSON = {"Nombres": res}
	return recetasJSON

######################################  Token Required ##################################################################
def token_required(f): #Comprueba que el token sea valido
	@wraps(f)
	def decorated(*args, **kwargs):
		token = request.args.get('token')

		if not token: #revisa que exita el token
			return abort(401)

		try:
			data = jwt.decode(token, app.config['SECRET_KEY']) #Verifica que el token sea válido
		except:
			return abort(401)


		return f(*args, **kwargs)
	return decorated

######################################  Get recetas ##################################################################
@app.route('/get_recetas', methods=['GET']) #/get_recetas?token=blalblasadfa
@token_required
def getRecetas():#Obtiene todos los nombres de las recetas
	archivo = getRecetas_Prolog() #Obtiene el archivo de recetas

	datos = 'python3 get_recetas.py "' + archivo + '"' #crea el comando a ejecutar

	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45") #Se conecta a Azure para usar pyswip

	entrada,salida,error = ssh.exec_command(datos) 
	res = salida.read().decode() # Aqui recupera los datos, pero sólo obtiene prints()

	ssh.close()	
	res = res[2:-3]
	res = res.split("', '") #Convierte el string en una lista

	if (res == [""]):
		return jsonify({'message': 'No hay recetas'})

	recetasJSON = {"Nombres" : res}
	return jsonify(recetasJSON)

######################################  Buscar recetas ##################################################################
@app.route('/buscar_recetas', methods=['GET']) #/buscar_recetas?cBusqueda=Nombre&strBusqueda=Pollo&token=blalblasadfa
@token_required
def buscarRecetas():#Busca recetas según el criterio de búsqueda (cBusqueda) y nombre del lo que quiere buscar (strBusqueda)
	cBusqueda = request.args.get('cBusqueda')
	strBusqueda = request.args.get('strBusqueda')
	if(cBusqueda =="Nombre"):#Enseña la receta en Json
		recetas = buscarRecetas_nombre(strBusqueda)
	elif(cBusqueda == "Tipo"):
		recetas = buscarRecetas_tipo(strBusqueda)
	elif(cBusqueda == "Ing"):
		recetas = buscarRecetas_ing(strBusqueda)
	else:
		return jsonify({"message": "Criterio de Busqueda invalido"})

	return jsonify(recetas)



######################################  Create receta ##################################################################
@app.route('/create_receta', methods=['POST','GET']) #/create_receta?token=nadno, no esta listo
@token_required
def createReceta():#Añade la receta a la base de conocimientos, {'nombre': 'nombre', 'tipo': 'tipoX', 'ingredientes': ['ing','ing2'],' pasos': ['paso','paso2'], 'fotos': ['url','url2']}
	datos = request.data.decode()
	parametros = json.loads(datos)
	nombre = parametros["nombre"]
	tipo = parametros["tipo"]
	ing = str(parametros["ingredientes"]).replace(" ","")
	pasos = str(parametros["pasos"]).replace(" ","")
	fotos = str(parametros["fotos"]).replace(" ","")
	data = "receta('" + nombre + "','" + tipo + "'," + ing + "," + pasos + "," + fotos + ")."

	mensaje = subir_receta(nombre,data)
	return jsonify(mensaje)
	

######################################  Auth required ##################################################################
def auth_required(f): #comprueba el login
	@wraps(f)
	def decorated(*args, **kwargs):
		correo = request.args.get('correo')
		cursor = conn.cursor()
		try:
			cursor.execute("SELECT * FROM CUENTAS WHERE CUENTAS.correo = '"+correo+"'") #Pide a la base de datos el usuario que comparta el correo
			res = cursor.fetchone()
			if (res == []):
				return jsonify({'message' : "El usuario o la contrasena son incorrectos"})
			else: 
				cipher_suite = Fernet(llave_cifra)
				contrasena = res[1].encode() #Hay que convertir el string a binario para cifrar o descifrar la contraseña con esta libreria
				contrasena = cipher_suite.decrypt(contrasena) #Descifrar
				contrasena = contrasena.decode()

				if (contrasena != request.args.get('contrasena')):
					return jsonify({'message' : 'El usuario o la contrasena son incorrectos'})
			cursor.close()
			return f(*args, **kwargs)
		except:
			return jsonify({'message' : "El usuario o la contrasena son incorrectos"})
		
	return decorated

######################################  Login ##################################################################
@app.route('/login', methods=['GET']) #/login?correo=correofalso@gmail.com&contrasena=contrasena
@auth_required
def login():#Login
	correo = request.args.get('correo')
	token = jwt.encode({'user': correo, 'exp' : datetime.datetime.utcnow() + datetime.timedelta(minutes=30)},app.config['SECRET_KEY'])
	return jsonify({'token': token.decode('UTF-8')})

######################################  sing Up ##################################################################
@app.route('/signUp',methods=['POST','GET']) # /signUp
def signUp():#Añade cuentas a la base de datos {'correo': 'correo', 'contrasena': 'contrasena'}
	data = request.data.decode()
	parametros = json.loads(data)
	correo = parametros["correo"] #Correo incluido en el json
	contrasena = parametros["contrasena"] #Contrasena incluida en el json

	try:

		cipher_suite = Fernet(llave_cifra)
		contrasena = cipher_suite.encrypt(contrasena.encode())   #Para cifrar, Hay que convertir el string a binario con decode para cifrar o descifrar la contraseña con esta libreria
		contrasena = contrasena.decode()# convertir el binario a texto

		cursor = conn.cursor()
		cursor.execute("INSERT INTO cuentas VALUES (%s, %s)",(correo,contrasena)) #Intenta agregar el nuevo usuario a la base de datos
		conn.commit()
		cursor.close()
		return jsonify({'message': 'El usuario ha sido registrado'})
	except: #Falla si el correo ya existe en la base de datos
		conn.rollback()
		return jsonify({'message': 'El usuario ya esta registrado'})


if __name__ == '__main__':
    app.run()