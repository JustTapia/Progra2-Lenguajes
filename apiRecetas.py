from flask import Flask, jsonify, request, make_response
import jwt
import datetime
from functools import wraps

app = Flask(__name__)
app.config['SECRET_KEY'] = "Prueba12345"

def token_required(f):
	@wraps(f)
	def decorated(*args, **kwargs):
		token = request.args.get('token')

		if not token:
			return jsonify({'mesage' : 'No hay Token'}),403

		try:
			data = jwt.decode(token, app.config['SECRET_KEY'])
		except:
			return jsonify({'mesage' : 'Token invalido'}),403


		return f(*args, **kwargs)
	return decorated

@app.route('/get_recetas')
@token_required
def protected():
	return jsonify({'mesage' : 'Contenido bloqueado detras de Token'})


@app.route('/login')
def login():
	auth= request.authorization

	if auth and auth.password == "password":
		token = jwt.encode({'user': auth.username, 'exp' : datetime.datetime.utcnow() + datetime.timedelta(minutes=30)},app.config['SECRET_KEY'])

		return jsonify({'token': token.decode('UTF-8')})
	return make_response('Could not verify!', 401, {'WWW-Authenticate' : 'Basic realm="Login Requerido"'})

if __name__ == '__main__':
    app.run()