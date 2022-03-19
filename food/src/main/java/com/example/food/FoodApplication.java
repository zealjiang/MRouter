package com.example.food;

import android.app.Application;
import android.os.Debug;
import android.util.Log;

import com.example.arouterlib.Router;

public class FoodApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mtest","-----FoodApplication----");
        Router.getInstance().register("/food/MainActivity",FoodMainActivity.class);
    }
}
