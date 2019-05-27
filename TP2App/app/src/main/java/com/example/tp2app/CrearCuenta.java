package com.example.tp2app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
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

public class CrearCuenta extends AppCompatActivity {

    private Toolbar toolbar;

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
                EditText userField = findViewById(R.id.editText_usuario);
                String user = userField.getText().toString();
                EditText contraField = findViewById(R.id.editText_contrasena);
                String contra = contraField.getText().toString();
                if(user.equals("") || contra.equals("")){
                    Toast.makeText(CrearCuenta.this, "Debe ingresar un usuario y contraseña", Toast.LENGTH_SHORT).show();
                }else {
                    new SignUpOperation().execute(user, contra);
                    //CrearCuenta.this.finish();
                }

            }
        });
    }

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
            Toast.makeText(CrearCuenta.this, result, Toast.LENGTH_SHORT).show();
            /*try {
                JSONObject jsonObj = new JSONObject(result);
                if(jsonObj.has("token")) {
                    String token = jsonObj.getString("token");
                }else if (jsonObj.has("message")){
                    String mensaje = jsonObj.getString("message");
                    Toast.makeText(CrearCuenta.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
    }

}
