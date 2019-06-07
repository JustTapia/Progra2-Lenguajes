CREATE TABLE CUENTAS(
	correo text NOT NULL,
	contrasena text NOT NULL,
	PRIMARY KEY (correo));

CREATE OR REPLACE FUNCTION InsertarUsuarios(IN newNombre varchar(50), IN newContrasena text) 
	RETURNS void AS $$

BEGIN
  INSERT INTO cuentas VALUES (newNombre, newContrasena);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION BuscarUsuario(IN correoUsuario varchar(50)) 
	RETURNS TABLE(Correo text, Contrasena text) AS $$

BEGIN
  RETURN QUERY SELECT * into res FROM CUENTAS WHERE CUENTAS.correo = correoUsuario;	
END
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetUsuarios() 
	RETURNS TABLE(Correo text AS $$

BEGIN
	RETURN QUERY SELECT CUENTAS.correo into res FROM CUENTAS;
END
$$ LANGUAGE plpgsql;