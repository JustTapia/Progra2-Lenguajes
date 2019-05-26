from pyswip import Prolog
import sys	

def main(busqueda,script):
	prolog = Prolog()
	script = script[0].split(".")
	script = scri
	print (script)
	for linea in script:
		prolog.assertz(linea)
	prueba = list(prolog.query("buscarReceta("+busqueda+",Tipo,Ing,Pasos,Fotos)"))
	res = {}

	i = 0
	while i < len(prueba[0]["Pasos"]):
		temp = str(prueba[0]["Pasos"][i])
		prueba[0]["Pasos"][i] = temp
		i = i+1

	i = 0
	while i < len(prueba[0]["Ing"]):
		temp = str(prueba[0]["Ing"][i])
		prueba[0]["Ing"][i] = temp
		i = i+1

	i = 0
	while i < len(prueba[0]["Fotos"]):
		temp = str(prueba[0]["Fotos"][i])
		prueba[0]["Fotos"][i] = temp
		i = i+1

	res["Nombre"] = busqueda
	res["Tipo"] = prueba[0]["Tipo"]
	res["Pasos"] = prueba[0]["Pasos"]
	res["Ingredientes"] = prueba[0]["Ing"]
	res["Fotos"] = prueba[0]["Fotos"]
	print (res)

if __name__ == '__main__':
	main(sys.argv[1], sys.argv[2])