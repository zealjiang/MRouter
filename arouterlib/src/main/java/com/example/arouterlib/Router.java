package com.example.arouterlib;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Router {

    private static volatile Router router;
    public static Router getInstance(){
        if(router == null){
            synchronized (Router.class){
                if(router == null){
                    router = new Router();
                }
            }
        }

        return router;
    }

    public static void init(Application application){
        try {
            Set<String> classNames = ClassUtils.getFileNameByPackageName(application,"com.example.routerdemo");
            for (String className : classNames) {
                Class<?> cls = Class.forName(className);
                if(cls.isAssignableFrom((IRouteLoad.class))){
                    IRouteLoad load = (IRouteLoad)cls.newInstance();
                    load.loadInto(routerMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Map<String,Class<? extends Activity>> routerMap = new HashMap<>();

    public void register(String path,Class<? extends Activity> cls){
        Log.d("mtest","register path= "+path+" cls= "+cls);
        routerMap.put(path,cls);
    }

    public void startActivity(Activity activity,String path){
        Class<? extends Activity> cls = routerMap.get(path);
        Log.d("mtest","startActivity path= "+path+" cls= "+cls);
        if(cls != null){
            Intent intent = new Intent(activity,cls);
            activity.startActivity(intent);
        }
    }
}
