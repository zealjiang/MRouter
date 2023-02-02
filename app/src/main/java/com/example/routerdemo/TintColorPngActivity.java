package com.example.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TintColorPngActivity extends AppCompatActivity {

    private int pos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tint_color_png);

        final int[] colors = new int[]{Color.parseColor("#ff0000"),Color.parseColor("#00ff00"),Color.parseColor("#0000ff")};

        final ImageView iv = findViewById(R.id.iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.setColorFilter(colors[pos]);
                //iv.setColorFilter(colors[pos], PorterDuff.Mode.SRC_IN);
                pos++;
                if(pos >= colors.length){
                    pos = 0;
                }
            }
        });
    }
}