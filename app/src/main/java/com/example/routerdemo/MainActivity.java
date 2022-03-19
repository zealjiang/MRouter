package com.example.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.arouterlib.Router;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Router.getInstance().startActivity(MainActivity.this,"/food/MainActivity");
                MArrayMap<String,String> arrayMap = new MArrayMap<>();
                arrayMap.put("11111","slot_id_0001");
                arrayMap.remove("11111");
                if(true)return;
                arrayMap.put("22222","slot_id_0002");
                arrayMap.put("33333","slot_id_0003");
                arrayMap.put("44444","slot_id_0004");
                arrayMap.put("55555","slot_id_0004");
                Log.d("mtest","size = "+arrayMap.mSize+"  mHashes.length ="+arrayMap.mHashes.length);
                arrayMap.put("7777","slot_id_7777");
                arrayMap.put("8888","slot_id_8888");
                arrayMap.put("9999","slot_id_9999");
                Log.d("mtest","size = "+arrayMap.mSize+"  mHashes.length ="+arrayMap.mHashes.length);
                arrayMap.put("9991","slot_id_9991");
                arrayMap.remove("11111");
                arrayMap.remove("22222");
                arrayMap.remove("33333");
                arrayMap.remove("44444");
                arrayMap.remove("55555");
                arrayMap.remove("7777");
                arrayMap.remove("8888");

            }
        });
        //if(true)return;
        ArrayMap<String,String> arrayMap = new ArrayMap<>();
        arrayMap.put("11111","slot_id_0001");
        arrayMap.put("22222","slot_id_0002");
        arrayMap.put(null,"slot_id_000null");
        arrayMap.put("3333","slot_id_0003");
        arrayMap.put(null,"slot_id_0001null");

        for (int i = 0; i < arrayMap.size(); i++) {
            Log.d("mtest",i+" ="+arrayMap.keyAt(i)+" value ="+arrayMap.valueAt(i));
        }

        String value = arrayMap.get("22222");
        Log.d("mtest","value-->"+value);
        Log.d("mtest","arrayMap.containsKey(\"11111\") ="+arrayMap.containsKey("11111"));
        arrayMap.remove("22222");
        Log.d("mtest","22222-->"+arrayMap.get("22222"));
        arrayMap.put("11111","slot_id_00000");
        Log.d("mtest","11111-->"+arrayMap.get("11111"));
    }
}
