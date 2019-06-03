package com.example.tp2app.API;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//Clase que se conecta con la API, utilizando OKHTTP

public class RestClient {
    OkHttpClient client;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String urlBase = "https://prueba-api-recetas.herokuapp.com/"; //URL de la API

    public RestClient(){
        client = new OkHttpClient(); // crea un nuevo cliente
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(40, TimeUnit.SECONDS) //Tiempo maximo de 40 seg para que la API responda
                .writeTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS);

        client = builder.build();
    }

    /**
     * Realiza un POST Request en la API
     * @param url
     * @param json El String del JSON para realizar el POST
     * @return La respuesta de la API (JSON)
     * @throws IOException
     */

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code() == 401){ //Token no autenticado
                return "401";
            }
            return response.body().string();
        }
    }


    /**
     * Realiza un GET Request en la API
     * @param url
     * @return La respuesta de la API (JSON)
     * @throws IOException
     */
    String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if(response.code() == 401){ //Token no autenticado
                return "401";
            }
            return response.body().string();
        }
    }

    /**
     * Crea un JSON para crear cuenta
     * @param usuario
     * @param contrasena
     * @return el String del JSON creado
     */
    String crearCuentaJson(String usuario, String contrasena) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("correo", usuario);
            jsonObject.put("contrasena", contrasena);
        } catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * Crea un JSON para agregar una receta
     * @param nombre
     * @param tipo
     * @param ingredientes lista
     * @param instrucciones lista
     * @param url_fotos lista
     * @return el String del JSON creado
     */

    String agregarRecetaJson(String nombre, String tipo, ArrayList<String> ingredientes, ArrayList<String> instrucciones, ArrayList<String> url_fotos) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsArrayIng = new JSONArray(ingredientes);
        JSONArray jsArrayInst = new JSONArray(instrucciones);
        JSONArray jsArrayFot = new JSONArray(url_fotos);
        try {
            jsonObject.put("nombre", nombre);
            jsonObject.put("tipo", tipo);
            jsonObject.put("ingredientes", jsArrayIng);
            jsonObject.put("pasos", jsArrayInst);
            jsonObject.put("fotos", jsArrayFot);
        } catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * Crea una nueva receta (POST)
     * URLBase + create_receta?token=nadno
     * @param token
     * @param nombre
     * @param tipo
     * @param ingredientes
     * @param instrucciones
     * @param url_fotos
     * @return la respuesta de la API
     * @throws IOException
     */
    public String crearReceta(String token, String nombre, String tipo, ArrayList<String> ingredientes, ArrayList<String> instrucciones, ArrayList<String> url_fotos) throws IOException {
        String url = urlBase+ "create_receta?token="+token;
        String json = agregarRecetaJson(nombre, tipo, ingredientes, instrucciones, url_fotos);
        Log.d("JSON", json);
        String response = post(url, json);
        return response;
    }

    /**
     * Obtiene el nombre de todas las recetas (GET)
     * URLBase + get_recetas?token=blalblasadfa
     * @param token
     * @return la respuesta de la API
     * @throws IOException
     */
    public String getTodasRecetas(String token) throws IOException {
        String url = urlBase+ "get_recetas?token="+token;
        String response = get(url);
        return response;
    }

    /**
     * Busca una receta según el criterio (GET)
     * Si se busca la receta por nombre, devuelve toda la informacion de la receta
     * Si se busca por Tipo o Ingrediente, devuelve los nombres de las recetas con ese tipo/ing
     * URLBase + buscar_recetas?cBusqueda=Nombre&strBusqueda=Pollo&token=blalblasadfa
     * @param criterio 'Nombre', 'Tipo' o 'Ing'
     * @param busqueda
     * @param token
     * @return La respuesta de la API
     * @throws IOException
     */
    public String buscarRecetas(String criterio, String busqueda, String token) throws IOException {
        String url = urlBase+ "buscar_recetas?cBusqueda="+criterio+"&strBusqueda="+busqueda+"&token="+token;
        String response = get(url);
        return response;
    }

    /**
     * Crea un nuevo usuario (POST)
     * URLBase + signUp
     * @param correo
     * @param contrasena
     * @return la respuesta de la API
     * @throws IOException
     */

    public String signUpUser(String correo, String contrasena) throws IOException {
        String url = urlBase+ "signUp";
        String json = crearCuentaJson(correo, contrasena);
        String response = post(url, json);
        return response;
    }

    /**
     * 'Inicia Sesion' con un usuario
     * URLBase + login?correo=correofalso@gmail.com&contrasena=contrasena
     * @param correo
     * @param contrasena
     * @return la respuesta de la API (si los datos de entrada son correctos, devuelve el token)
     * @throws IOException
     */
    public String logInUser(String correo, String contrasena) throws IOException {
        String url = urlBase+ "login?correo=" +correo+"&contrasena="+contrasena;
        String response = get(url);
        return response;
    }

}
