from pyswip import Prolog
import sys	

def main(busqueda,script):
	prolog = Prolog()
	script = script.split(".\n")
	script = script[:-1]
	for linea in script:
		prolog.assertz(linea)
	prueba = bool(list(prolog.query("receta(" + busqueda + ",_,_,_,_)")))
	print (prueba)
	
if __name__ == '__main__':
	main(sys.argv[1], sys.argv[2])