package com.example.tp2app;


import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tp2app.API.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerRecetas extends AppCompatActivity {

    private Toolbar toolbar;
    private String token;
    RestClient rc = new RestClient();
    Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*"); //No se permiten caracteres especiales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_recetas);

        Bundle b = getIntent().getExtras();
        if(b != null)
            token = b.getString("token");

        final Spinner busqueda = findViewById(R.id.spinner_rec);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);

        Button buttonBuscar = findViewById(R.id.button_buscar);
        buttonBuscar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                int criterio  = busqueda.getSelectedItemPosition();
                EditText busqField = findViewById(R.id.editText_busc);
                String busq = busqField.getText().toString();
                String num = Integer.toString(criterio);
                if(criterio == 0){
                    new GetRecetasOperation().execute();
                }else{
                    Matcher matcher = pattern.matcher(busq);
                    if(busq.equals("")) {
                        Toast.makeText(VerRecetas.this, "Debe indicar una búsqueda", Toast.LENGTH_SHORT).show();
                    }else if(!matcher.matches()){
                        Toast.makeText(VerRecetas.this, "No se permiten caracteres especiales", Toast.LENGTH_SHORT).show();
                    }else {
                        busq = busq.replace(' ', '_');
                        busq = Character.toLowerCase(busq.charAt(0)) + busq.substring(1);
                        new BuscarRecetasOperation().execute(num, busq);
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Spinner spinner = findViewById(R.id.spinner_rec);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.wos, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);
        return true;
    }

    //Tabla con los nombres de las recetas
    public void init(final ArrayList<String> recetasNombres) {
        TableLayout stk = findViewById(R.id.table_main);
        stk.removeAllViews();
        TableRow tbrow0 = new TableRow(this);;
        TextView tv1 = new TextView(this);
        tv1.setText("Recetas");
        tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);
        stk.addView(tbrow0);
        for (int i = 0; i < recetasNombres.size(); i++) {
            final String nombre = recetasNombres.get(i);
            TableRow tbrow = new TableRow(this);
            tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            tbrow.setClickable(true);
            tbrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InfoRecetaOperation().execute(nombre);
            }
            });
            tbrow.setBackgroundResource (android.R.drawable.edit_text);
            TextView t2v = new TextView(this);
            String nombreDisplay = nombre.replace('_', ' ');
            nombreDisplay = Character.toUpperCase(nombreDisplay.charAt(0)) + nombreDisplay.substring(1);
            t2v.setText(nombreDisplay);
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            tbrow.addView(t2v);
            stk.addView(tbrow);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * Thread para obtener todas las recetas de la API
     */
    private class GetRecetasOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String response = "ERROR";
            try {
                response = rc.getTodasRecetas(token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("401")) {
                Toast.makeText(VerRecetas.this, "Token no autorizado", Toast.LENGTH_SHORT).show();
            }else {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray myJSONArray = jsonObj.getJSONArray("Nombres");
                    ArrayList<String> arrayListNombres = new ArrayList<>(myJSONArray.length());
                    for (int i = 0; i < myJSONArray.length(); i++) {
                        arrayListNombres.add(myJSONArray.getString(i));
                    }
                    init(arrayListNombres);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Thread para buscar recetas según un criterio y obtenerlas de la API
     */
    private class BuscarRecetasOperation extends AsyncTask<String, Void, String> {

        String crit = "";
        @Override
        protected String doInBackground(String... params) {
            String response = "ERROR";
            String criterio = "";
            switch(params[0]){
                case "1":
                    criterio = "Nombre";
                    break;
                case "2":
                    criterio = "Tipo";
                    break;
                case "3":
                    criterio = "Ing";
                    break;
            }
            try {
                crit = criterio;
                response = rc.buscarRecetas(criterio, params[1], token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("401")) {
                Toast.makeText(VerRecetas.this, "Token no autorizado", Toast.LENGTH_SHORT).show();
            }else {
                try {

                    JSONObject jsonObj = new JSONObject(result);
                    if (jsonObj.has("message")) {
                        String mensaje = jsonObj.getString("message");
                        Toast.makeText(VerRecetas.this, mensaje, Toast.LENGTH_SHORT).show();
                    } else {
                        JSONArray myJSONArray;
                        if (crit.equals("Nombre")) {
                            String nom = jsonObj.getString("Nombre");
                            ArrayList<String> arrayListNombres = new ArrayList<>(jsonObj.length());
                            arrayListNombres.add(nom);
                            init(arrayListNombres);
                        } else {
                            myJSONArray = jsonObj.getJSONArray("Nombres");
                            ArrayList<String> arrayListNombres = new ArrayList<>(myJSONArray.length());
                            for (int i = 0; i < myJSONArray.length(); i++) {
                                arrayListNombres.add(myJSONArray.getString(i));
                            }
                            init(arrayListNombres);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Thread para obtener la informacion de una receta de la API y pasarla a la siguiente activity
     */
    private class InfoRecetaOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String response = "ERROR";
            try {
                response = rc.buscarRecetas("Nombre", params[0], token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("401")) {
                Toast.makeText(VerRecetas.this, "Token no autorizado", Toast.LENGTH_SHORT).show();
            }
            try {

                JSONObject jsonObj = new JSONObject(result);
                if(jsonObj.has("message")){
                    String mensaje = jsonObj.getString("message");
                    Toast.makeText(VerRecetas.this, mensaje, Toast.LENGTH_SHORT).show();
                }else {
                    String nom = jsonObj.getString("Nombre");
                    String tip = jsonObj.getString("Tipo");
                    JSONArray pasos = jsonObj.getJSONArray("Pasos");
                    JSONArray ing = jsonObj.getJSONArray("Ingredientes");
                    JSONArray fotos = jsonObj.getJSONArray("Fotos");
                    ArrayList<String> arrayListPasos = new ArrayList<>(pasos.length());
                    ArrayList<String> arrayListFotos = new ArrayList<>(ing.length());
                    ArrayList<String> arrayListIng = new ArrayList<>(fotos.length());
                    for (int i = 0; i < pasos.length(); i++) {
                        arrayListPasos.add(pasos.getString(i));
                    }for (int i = 0; i < ing.length(); i++) {
                        arrayListIng.add(ing.getString(i));
                    }for (int i = 0; i < fotos.length(); i++) {
                        arrayListFotos.add(fotos.getString(i));
                    }
                    Intent intent = new Intent(VerRecetas.this, InfoReceta.class);
                    Bundle b = new Bundle();
                    b.putString("tipo", tip);
                    b.putString("nombre", nom);
                    b.putStringArrayList("ingredientes", arrayListIng);
                    b.putStringArrayList("pasos", arrayListPasos);
                    b.putStringArrayList("fotos", arrayListFotos);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
