import boto3
import json

data = 'receta(filet_minion,tipoY,[filete,ajo,sal,tocineta],[paso1,paso2,paso3],[url1,url2,url3]).'

def main():
	s3_client=boto3.resource('s3')
	objeto = s3_client.Object('recetas-imagenes','prolog/recetas.pl')
	archivo = objeto.get()
	archivo = archivo["Body"]
	archivo = archivo.read().decode()
	print(archivo)
	#archivo = data + "\n" + archivo
	#objeto.put(Body=archivo)
	return


if __name__ == '__main__':
	main()