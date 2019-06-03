package com.example.tp2app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tp2app.API.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniciarSesion extends AppCompatActivity {

    Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*"); //No se permiten caracteres especiales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);

        Button button = findViewById(R.id.button_iniciar_sesion);
        Button buttonCrearCuenta = findViewById(R.id.button_crear_cuenta);
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
                Matcher matcher = pattern.matcher(contra);
                if(user.equals("") || contra.equals("")){
                    Toast.makeText(IniciarSesion.this, "Debe ingresar un usuario y contraseña", Toast.LENGTH_SHORT).show();
                }else if(!matcher.matches()){
                    Toast.makeText(IniciarSesion.this, "No se permiten caracteres especiales", Toast.LENGTH_SHORT).show();
                }else {
                    new LogInOperation().execute(user, contra);
                }

            }
        });
    }

    /**
     * Thread para comunicarse con la API y loggearse
     */

    private class LogInOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            RestClient rc = new RestClient();
            String response = "ERROR";
            try {
                response = rc.logInUser(params[0], params[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                if(jsonObj.has("token")) {
                    String token = jsonObj.getString("token");
                    Intent intent = new Intent(IniciarSesion.this, MenuPrincipal.class);
                    Bundle b = new Bundle(); //manda el token a la siguiente activity
                    b.putString("token", token);
                    intent.putExtras(b);
                    startActivity(intent);
                }else if (jsonObj.has("message")){
                    String mensaje = jsonObj.getString("message");
                    Toast.makeText(IniciarSesion.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Se sale de la aplicacion si selecciona el back button
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}









