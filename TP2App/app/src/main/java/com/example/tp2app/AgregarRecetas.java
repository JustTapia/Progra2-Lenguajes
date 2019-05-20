package com.example.tp2app;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class AgregarRecetas extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_recetas);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);

        EditText editText_Ing = (EditText)findViewById(R.id.textArea_information1);
        EditText editText_Inst = (EditText)findViewById(R.id.textArea_information2);
        EditText editText_Fot = (EditText)findViewById(R.id.textArea_information3);

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
        editText_Fot.setVerticalScrollBarEnabled(true);
        editText_Fot.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        editText_Fot.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        editText_Fot.setMovementMethod(ScrollingMovementMethod.getInstance());
        editText_Fot.setOnTouchListener(new View.OnTouchListener() {
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

}
