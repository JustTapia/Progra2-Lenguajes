from pyswip import Prolog
import sys	

def main(busqueda,script):
	prolog = Prolog()
	script = script.split(".")
	script = script[:-1]
	for linea in script:
		prolog.assertz(linea)
	prueba = list(prolog.query("buscarPorIng("+busqueda+",Nombre)"))

	i = 0
	while i < len(prueba):
		prueba[i] = prueba[i]["Nombre"]
		i = i+1
	print (prueba)

if __name__ == '__main__':
	main(sys.argv[1], sys.argv[2])