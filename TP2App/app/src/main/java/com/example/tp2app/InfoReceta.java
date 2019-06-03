package com.example.tp2app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class InfoReceta extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout linearLayout;
    private String tipo;
    private String nombreRec;
    private ArrayList<String> ing = new ArrayList<>();
    private ArrayList<String> pasos = new ArrayList<>();
    private ArrayList<String> fotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_receta);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            tipo = b.getString("tipo");
            nombreRec = b.getString("nombre");
            ing = b.getStringArrayList("ingredientes");
            pasos = b.getStringArrayList("pasos");
            fotos = b.getStringArrayList("fotos");
        }

        linearLayout = findViewById(R.id.layout_imagen);
        TextView nombre = findViewById(R.id.textView17);
        nombreRec = nombreRec.replace('_', ' ');
        nombreRec = Character.toUpperCase(nombreRec.charAt(0)) + nombreRec.substring(1);
        nombre.setText(nombreRec);
        TextView tip = findViewById(R.id.textView16);
        tipo = tipo.replace('_', ' ');
        tipo = Character.toUpperCase(tipo.charAt(0)) + tipo.substring(1);
        tip.setText(tipo);
        TableLayout tabIng = findViewById(R.id.tableLayoutIng);
        TableLayout tabInst = findViewById(R.id.tableLayoutInst);

        iniciarTablas(ing, tabIng);
        iniciarTablas(pasos, tabInst);

        for(int i = 0; i<fotos.size(); i++){
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            String url = fotos.get(i);
            new DownloadImageFromInternet(imageView)
                    .execute(url);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);

    }

    //Display de los ingredientes/instrucciones en una tabla
    public void iniciarTablas(ArrayList<String> info, TableLayout stk){
        for (int i = 0; i < info.size(); i++) {
            final String nombre = info.get(i);
            TableRow tbrow = new TableRow(this);
            tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
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

    //Back Button
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
     * Thread para descargar las imagenes guardadas en AWS y las coloca en un imageView
     */
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            Toast.makeText(getApplicationContext(), "Por favor espere...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
            linearLayout.addView(imageView);
        }
    }

}
