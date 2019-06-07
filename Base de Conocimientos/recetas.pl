buscarPorIng(Ing,X):-receta(X,_,L,_,_),buscarIng(Ing,X,L).
buscarIng(_,_,[]):-false.
buscarIng(Ing,_,[Ing|_]).
buscarIng(Ing,X,[_|Z]):-buscarIng(Ing,X,Z).
buscarReceta(X,Y,Z,M,N):-receta(X,Y,Z,M,N),!.
buscarPorTipo(Tip,X):-receta(X,Tip,_,_,_).
getRecetas(X):-receta(X,_,_,_,_).
%Base de conocimientos y métodos para navegar recet