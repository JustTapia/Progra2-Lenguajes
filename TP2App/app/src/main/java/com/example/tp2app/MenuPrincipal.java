package com.example.tp2app;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

public class MenuPrincipal extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Bundle b = getIntent().getExtras();
        if(b != null)
            token = b.getString("token");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu);
        actionbar.setDisplayShowTitleEnabled(false);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view); // Menu
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        Intent intent;
                        Bundle b;
                        switch (item.getItemId()){
                            case R.id.nav_cerrar_sesion:
                                Toast.makeText(MenuPrincipal.this, "Sesión Cerrada", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case R.id.nav_ver_rec:
                                intent = new Intent(MenuPrincipal.this, VerRecetas.class );
                                b = new Bundle();
                                b.putString("token", token);
                                intent.putExtras(b);
                                startActivity(intent);
                                break;
                            case R.id.nav_agregar_rec:
                                intent = new Intent(MenuPrincipal.this, AgregarRecetas.class );
                                b = new Bundle();
                                b.putString("token", token);
                                intent.putExtras(b);
                                startActivity(intent);
                                break;
                        }
                        return true;
                    }
                });
    }

    //Menu (Navigation View)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Back button
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
