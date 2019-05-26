import paramiko

def main():
	datos = "receta(stroganoff,tipoX,[sal,perejil,mostaza,lomito,cebolla,aceite],[inst1,inst2,inst3,inst4,inst5],[url1,url2]).receta(hongos_al_ajillo,tipoY,[hongos,ajo,vinagre,aceite_de_oliva,cebolla],[inst1,inst2,inst3,inst4,inst5],[url1,url2]).receta(lo_que_sea,tipoX,[huevo,queso,cebolla,ajo],[inst1,inst2,inst3,inst4,inst5],[url1,url2]).buscarPorIng(Ing,X):-receta(X,_,L,_,_),buscarIng(Ing,X,L).buscarIng(_,_,[]):-false.buscarIng(Ing,_,[Ing|_]).buscarIng(Ing,X,[_|Z]):-buscarIng(Ing,X,Z).buscarReceta(X,Y,Z,M,N):-receta(X,Y,Z,M,N),!.buscarPorTipo(Tip,X):-receta(X,Tip,_,_,_).getRecetas(X):-receta(X,_,_,_,_)."
	datos = datos.replace("!","\x21")
	datos = 'python3 get_recetas.py "' + datos + '"'
	print (datos)
	ssh = paramiko.SSHClient()
	ssh.load_system_host_keys()
	ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh.connect('40.121.36.19',22,"recetas","Helado123$45")
	entrada,salida,error = ssh.exec_command(datos)
	res = salida.read().decode()
	ssh.close()
	print(res)
	res = res[2:-3]
	res = res.split("', '")
	print(res)
	return 
if __name__ == '__main__':
	main()
