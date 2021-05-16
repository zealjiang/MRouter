package com.example.arouterlib;

import android.app.Activity;

import java.util.Map;

public interface IRouteLoad {

    void loadInto(Map<String,Class<? extends Activity>> routes);
}
