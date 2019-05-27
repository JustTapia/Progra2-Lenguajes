package com.example.tp2app.API;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String urlBase = "https://prueba-api-recetas.herokuapp.com/";
    private String token;

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String crearCuentaJson(String usuario, String contrasena) {
        return "{'correo':'" + usuario + "',"
                + "{'contrasena':'" + contrasena + "'}";
        /*JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("correo", usuario);
            jsonObject.put("contrasena", contrasena);
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject.toString();*/
    }

    String agregarCuentaJson(String nombre, String tipo, ArrayList<String> ingredientes, ArrayList<String> instrucciones, ArrayList<String> url_fotos) {
        String json = "{'nombre':'"+nombre+"'," + "'tipo':'"+tipo+"'," + "'ingredientes':[";
        for(int i = 0; i<ingredientes.size();i++){
            json += "'"+ingredientes.get(i)+"',";
        }
        json+= "]"+",'instrucciones':[";
        for(int i = 0; i<instrucciones.size();i++){
            json += "'"+instrucciones.get(i)+"',";
        }
        json+= "]"+",'fotos':[";
        for(int i = 0; i<url_fotos.size();i++){
            json += "'"+url_fotos.get(i)+"',";
        }
        json+= "]}";
        return json;
    }

    public void getTodasRecetas() throws IOException { //get_recetas?token=blalblasadfa
        String url = urlBase+ "get_recetas?token="+token;
        String response = get(url);
        System.out.println(response);
    }

    public void buscarRecetas(String criterio, String busqueda) throws IOException { //buscar_recetas?cBusqueda=Nombre&strBusqueda=Pollo&token=blalblasadfa
        String url = urlBase+ "buscar_recetas?cBusqueda="+criterio+"&strBusqueda="+busqueda+"&token="+token;
        String response = get(url);
        System.out.println(response);
    }

    public void showReceta(String nombreReceta) throws IOException { //show_receta?nombreReceta=Hola&token=blalblasadfa
        String url = urlBase+ "show_receta?nombreReceta=" +nombreReceta+"&token="+token;
        String response = get(url);
        System.out.println(response);
    }

    public String signUpUser(String correo, String contrasena) throws IOException { //singUp
        String url = urlBase+ "signUp";
        String json = crearCuentaJson(correo, correo);
        String response = post(url, json);
        return response;
    }

    public String logInUser(String correo, String contrasena) throws IOException { //login?correo=correofalso@gmail.com&contrasena=contrasena
        String url = urlBase+ "login?correo=" +correo+"&contrasena="+contrasena;
        String response = get(url);
        return response;
    }

}
