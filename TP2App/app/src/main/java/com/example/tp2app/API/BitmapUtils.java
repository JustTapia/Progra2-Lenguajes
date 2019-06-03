package com.example.tp2app.API;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//Clase que maneja el archivo de una foto tomada


public class BitmapUtils {

    /**
     * Ajusta la foto a la pantalla para mejor uso de la memoria
     * @param context   Contexto de la aplicacion
     * @param imagePath Path de la foto
     * @return La imagen reajustada
     */
    public static Bitmap resamplePic(Context context, String imagePath) {
        //Informacion de la pantalla del dispositivo
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;
        //Dimensiones del bitmap original
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Cuanto escalar la imagen
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        //Deodifica la imagen en un Bitmap
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * Guarda la imagen temporalmente en la cache
     * @return La imagen temporal
     * @throws IOException Si se produce error al crear el archivo
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }



    /**
     * Borra una imagen dado un path (la imagen del caché)
     * @param context   Contexto de la aplicación
     * @param imagePath Path de la imagen a borrar
     */
    public static boolean deleteImageFile(Context context, String imagePath) {
        File imageFile = new File(imagePath);
        //Borra la imagen
        boolean deleted = imageFile.delete();
        //Toast si ocurre un error borrando la imagen
        if (!deleted) {
            Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }

    /**
     * Agrega la foto en la galería del dispositivo
     * @param imagePath Path de la foto a guardar
     */
    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Helper method for saving the image.
     * @param context The application context.
     * @param image   The image to be saved.
     * @return The path of the saved image.
     */
    public static String saveImage(Context context, Bitmap image) {
        String savedImagePath = null;
        //Crea un nuevo archivo en el storage externo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/MyCamera"); //lo guarda en el album 'My Camera' (lo crea si no existe)
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        //Guarda el Bitmap
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            galleryAddPic(context, savedImagePath);
        }
        return savedImagePath;
    }

}