package com.example.tp2app;

import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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

public class CrearCuenta extends AppCompatActivity {

    private Toolbar toolbar;
    Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*"); //No se permiten caracteres especiales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText userField = findViewById(R.id.editText_email);
                String user = userField.getText().toString();
                EditText contraField = findViewById(R.id.editText_password);
                String contra = contraField.getText().toString();
                Matcher matcher = pattern.matcher(contra);
                if(user.equals("") || contra.equals("")){
                    Toast.makeText(CrearCuenta.this, "Debe ingresar un usuario y contraseña", Toast.LENGTH_SHORT).show();
                }else if(!matcher.matches()){
                    Toast.makeText(CrearCuenta.this, "No se permiten caracteres especiales", Toast.LENGTH_SHORT).show();
                }else {
                    new SignUpOperation().execute(user, contra);
                }

            }
        });
    }

    //Back button
   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Thread para comunicarse con la API y crear una cuenta
     */
    private class SignUpOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            RestClient rc = new RestClient();
            String response = "ERROR";
            try {
                response = rc.signUpUser(params[0], params[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                if (jsonObj.has("message")){
                    String mensaje = jsonObj.getString("message");
                    Toast.makeText(CrearCuenta.this, mensaje, Toast.LENGTH_SHORT).show();
                    if(mensaje.equals("El usuario ha sido registrado")){
                        finish();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
