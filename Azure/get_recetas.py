from pyswip import Prolog
import sys	

def main(script):
	prolog = Prolog()
	script = script.split(".\n")
	script = script[:-1]
	for linea in script:
		prolog.assertz(linea)
	prueba = list(prolog.query("getRecetas(Nombre)"))

	i = 0
	while i < len(prueba):
		prueba[i] = prueba[i]["Nombre"]
		i = i+1
	print (prueba)

if __name__ == '__main__':
	main(sys.argv[1])