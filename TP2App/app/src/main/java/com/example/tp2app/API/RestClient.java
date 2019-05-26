package com.example.tp2app.API;


/*import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestClient {
    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    String signUpUser(String urlBase, String correo, String contrasena){ //singUp?correo=correofalso@gmail.com&contrasena=helado123
        String url = urlBase+ "singUp?correo=" +correo+"&contrasena="+contrasena;
        return url;
    }

    String logInUser(String urlBase, String correo, String contrasena){ //login?correo=correofalso@gmail.com&contrasena=contrasena
        String url = urlBase+ "login?correo=" +correo+"&contrasena="+contrasena;
        return url;
    }

    public static void main(String[] args) throws IOException {
        RestClient example = new RestClient();
        String response = example.run("https://prueba-api-recetas.herokuapp.com/");
        System.out.println(response);
    }
}*/
