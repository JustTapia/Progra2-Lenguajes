package com.example.tp2app;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AgregarRecetas extends AppCompatActivity {

    private Toolbar toolbar;
    List<String> imagesList = new ArrayList<>();
    private static int RESULT_LOAD_IMAGE = 1;
    private final static int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    EditText editText_Fot;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_recetas);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        linearLayout = findViewById(R.id.layout_foto);

        EditText editText_Ing = findViewById(R.id.textArea_information1);
        EditText editText_Inst = findViewById(R.id.textArea_information2);

        /*LinearLayout linearLayout = findViewById(R.id.layout_imagen);
        imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(imageView);
        setContentView(linearLayout);*/

        Button buttonImagen = (Button) findViewById(R.id.button_imagen);
        buttonImagen.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button buttonAgregar= (Button) findViewById(R.id.button2);
        buttonAgregar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                checkPermissions();
                for(int i = 0; i < imagesList.size(); i++) {
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
                }
            }
        });

        Button buttonFoto= (Button) findViewById(R.id.button_foto);
        buttonFoto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If you do not have permission, request it
                    ActivityCompat.requestPermissions(AgregarRecetas.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    // Launch the camera if the permission exists
                    launchCamera();
                }
            }
        });

        editText_Ing.setVerticalScrollBarEnabled(true);
        editText_Ing.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        editText_Ing.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        editText_Ing.setMovementMethod(ScrollingMovementMethod.getInstance());
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

    private void launchCamera() {
        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();
                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);
                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
            BitmapUtils.saveImage(this, mResultsBitmap);
            Toast.makeText(this,"Image Saved",Toast.LENGTH_LONG).show();
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imagesList.add(picturePath);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(myBitmap);
            linearLayout.addView(imageView);
            //setContentView(linearLayout);
            // String picturePath contains the path of selected Image
        }
    }


    private void uploadImageToAWS(String selectedImagePath) {

        if (selectedImagePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file", Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(selectedImagePath);
        AmazonS3 s3Client = null;
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
            Log.d("messge", "converting to bytes");
            objectMetadata.setContentLength(file.length());
            String[] s = selectedImagePath.split("\\.");
            String extenstion = s[s.length - 1];
            Log.d("messge", "set content length : " + file.length() + "sss" + extenstion);
            String fileName = UUID.randomUUID().toString();
            PutObjectRequest putObjectRequest = new PutObjectRequest("recetas-imagenes", "new/" + fileName + "." + extenstion, stream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            String url = ((AmazonS3Client) s3Client).getResourceUrl("recetas-imagenes", "new/" + fileName + "." + extenstion);
            if (result == null) {
                Log.e("RESULT", "NULL");
            } else {
                Log.e("RESULT", result.toString());
                editText_Fot.append(url);
                editText_Fot.append("\n");
            }
        } catch (Exception e) {
            Log.d("ERRORR", " " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1052: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                }else {
                    Toast.makeText(this, "ERROR: Permisos denegados", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

}
