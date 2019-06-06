package com.example.tp2app;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.tp2app.API.BitmapUtils;
import com.example.tp2app.API.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgregarRecetas extends AppCompatActivity {

    private Toolbar toolbar;
    ArrayList<String> imagesList = new ArrayList<>();
    ArrayList<String> imagesURL = new ArrayList<>();
    ArrayList<String> ingred = new ArrayList<>();
    ArrayList<String> pasos = new ArrayList<>();
    private static int RESULT_LOAD_IMAGE = 1;
    private final static int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;
    LinearLayout linearLayout;
    private String token;
    AmazonS3 s3Client = null;
    Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]*"); //no se permiten caracteres especiales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_recetas);

        Bundle b = getIntent().getExtras();
        if(b != null)
            token = b.getString("token");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        linearLayout = findViewById(R.id.layout_foto);

        final EditText name = findViewById(R.id.editText5);
        final EditText type = findViewById(R.id.editText6);
        final EditText editText_Ing = findViewById(R.id.textArea_information1);
        final EditText editText_Inst = findViewById(R.id.textArea_information2);

        Button buttonImagen = (Button) findViewById(R.id.button_imagen); //Agregar imagen
        buttonImagen.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check permissions
                    ActivityCompat.requestPermissions(AgregarRecetas.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    Intent i = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });

        Button buttonAgregar= findViewById(R.id.button2); //Agrega la receta
        buttonAgregar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String nombre = name.getText().toString();
                nombre = Character.toLowerCase(nombre.charAt(0)) + nombre.substring(1); //Cambia la primera letra a minuscula (por Prolog)
                String tipo = type.getText().toString();
                tipo = Character.toLowerCase(tipo.charAt(0)) + tipo.substring(1);
                Matcher matcher = pattern.matcher(nombre);
                Matcher matcherT = pattern.matcher(tipo);
                if(nombre.equals("") || tipo.equals("")){
                    Toast.makeText(AgregarRecetas.this, "Nombre/Tipo inválido", Toast.LENGTH_SHORT).show();
                }else if(!matcher.matches() || !matcherT.matches()){
                    Toast.makeText(AgregarRecetas.this, "No se permiten caracteres especiales", Toast.LENGTH_SHORT).show();
                }else {
                    nombre = nombre.replace(' ', '_'); //Reemplaza espacios por underscores
                    tipo = tipo.replace(' ', '_');
                    String ingredientes = editText_Ing.getText().toString();
                    String instrucciones = editText_Inst.getText().toString();
                    crearLista(ingredientes, ingred);
                    crearLista(instrucciones, pasos);
                    checkPermissions();
                    for (int i = 0; i < imagesList.size(); i++) {
                        final String path = imagesList.get(i);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    uploadImageToAWS(path);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();
                        try {
                            thread.join(); //espera a que el thread termine
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("THREAD: ", "Hilo terminado");
                    new AgregarRecetaOperation().execute(nombre, tipo);
                }
            }
        });


        Button buttonFoto= (Button) findViewById(R.id.button_foto);
        buttonFoto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { //Tomar foto
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //Check permissions
                    ActivityCompat.requestPermissions(AgregarRecetas.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    launchCamera();
                }
            }
        });

        editText_Ing.setVerticalScrollBarEnabled(true);
        editText_Ing.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        editText_Ing.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        editText_Ing.setMovementMethod(ScrollingMovementMethod.getInstance());

        //Hace que el editText de ingredientes y de instrucciones sea scrollable
        editText_Ing.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0)
                {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });

        editText_Inst.setVerticalScrollBarEnabled(true);
        editText_Inst.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        editText_Inst.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        editText_Inst.setMovementMethod(ScrollingMovementMethod.getInstance());
        editText_Inst.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0)
                {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }


    //Back button
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
     * Crea una lista con los ingredientes/instrucciones
     * Cada uno esta separado por '\n'
     * @param info String con todos los ingredientes/instrucciones
     * @param lista vacía al principio, va agregando los ing/inst
     */
    private void crearLista(String info, ArrayList<String> lista){
        String str = "";
        for (int i=0; i < info.length(); i++){
            char letra = info.charAt(i);
            if(letra == '\n' || i == info.length()-1){
                str +=letra;
                str = str.replace("\n", "");
                str = str.replace(' ', '_');
                str = Character.toLowerCase(str.charAt(0)) + str.substring(1);
                lista.add(str);
                str = "";
            }
            str += letra;
        }
    }

    /**
     * Abre la camara y guarda la imagen temporalmente
     */
    private void launchCamera() {
        //Intent para abrir la camara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Guarda la imagen en cache
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                //Path del archivo temporal
                mTempPhotoPath = photoFile.getAbsolutePath();
                //URI content
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);
                //Agrega URI para que se pueda guardar la imagen
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //Abre la camara
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //imagen tomada con camara
            mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
            BitmapUtils.deleteImageFile(this, mTempPhotoPath); //elimina la copia cache
            BitmapUtils.saveImage(this, mResultsBitmap); //guarda la imagen en la galeria
            Toast.makeText(this,"Imagen Guardada",Toast.LENGTH_LONG).show();
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) { //imagen elegida de la galeria
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imagesList.add(picturePath); //agrega el path de la imagen para luego subirla a AWS
            //Muestra la imagen en un ImageView
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(myBitmap);
            linearLayout.addView(imageView);
        }
    }

    /**
     * Sube una imagen a AWS S3
     * @param selectedImagePath Path de la imagen a subir
     */

    private void uploadImageToAWS(String selectedImagePath) {
        if (selectedImagePath == null) {
            Toast.makeText(this, "No se pudo encontrar el path de la imagen", Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(selectedImagePath);
        if (s3Client == null) {
            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.HTTP);
            clientConfig.setMaxErrorRetry(0);
            clientConfig.setSocketTimeout(60000);
            BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAI3UO7DGUPJMWET7A", "lHC4ywRvQn7/50wLZYMteoH6N43rZNVuzSkil1mx");
            s3Client = new AmazonS3Client(credentials, clientConfig);
            s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            Log.d("message", "converting to bytes");
            objectMetadata.setContentLength(file.length());
            String[] s = selectedImagePath.split("\\.");
            String extension = s[s.length - 1];
            String fileName = UUID.randomUUID().toString(); //Crea un UUID como nombre de la imagen
            PutObjectRequest putObjectRequest = new PutObjectRequest("recetas-imagenes", "new/" + fileName + "." + extension, stream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            String url = ((AmazonS3Client) s3Client).getResourceUrl("recetas-imagenes", "new/" + fileName + "." + extension); //guarda la imagen en el bucket 'recetas-imagenes' en la carpeta 'new'
            imagesURL.add(url); //guarda el URL de la imagen en una lista
            if (result == null) {
                Log.e("RESULT", "NULL");
            } else {
                Log.e("RESULT", result.toString());
            }
        } catch (Exception e) {
            Log.d("ERROR", " " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thread para agregar una receta en la API
     */
    private class AgregarRecetaOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            RestClient rc = new RestClient();
            String response = "ERROR";
            try {
                response = rc.crearReceta(token, params[0], params[1], ingred, pasos, imagesURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPostExecute(String result) {
            if(result.equals("401")){
                Toast.makeText(AgregarRecetas.this, "Token no autorizado", Toast.LENGTH_SHORT).show();
            }else {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    if (jsonObj.has("message")) {
                        String mensaje = jsonObj.getString("message");
                        Toast.makeText(AgregarRecetas.this, mensaje, Toast.LENGTH_SHORT).show();
                        if (mensaje.equals("Receta anadida")) {
                            finish(); //Terminar actividad si la receta fue añadida
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Revisa si el usuario dio permisos de acceso al storage externo
     * Si no tiene permisos, realiza un request
     */
    private void checkPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1052);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 1052: {
                //Si el request es cancelado, el array es vacio
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                }else {
                    Toast.makeText(this, "ERROR: Permisos denegados", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

}
