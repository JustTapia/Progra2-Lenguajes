package com.example.tp2app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IniciarSesion extends AppCompatActivity {

    private String usuario;
    private String contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);
        Button button = (Button) findViewById(R.id.button_iniciar_sesion);
        Button buttonCrearCuenta = (Button) findViewById(R.id.button_crear_cuenta);
        buttonCrearCuenta.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(IniciarSesion.this, CrearCuenta.class));
            }
        });
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText userField = findViewById(R.id.editText_usuario);
                String user = userField.getText().toString();
                EditText contraField = findViewById(R.id.editText_contrasena);
                String contra = contraField.getText().toString();
                if(user.equals("") || contra.equals("")){
                    Toast.makeText(IniciarSesion.this, "Debe ingresar un usuario y contraseña", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(IniciarSesion.this, "OK", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(IniciarSesion.this, MenuPrincipal.class));
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}





















