package com.example.arouterlib;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

public class ClassUtils {

    private static List<String> getSourcePaths(Context context)throws PackageManager.NameNotFoundException{
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),0);
        //applicationInfo = context.getApplicationInfo();
        List<String> sourcePaths = new ArrayList<>();
        //当前应用的apk文件
        sourcePaths.add(applicationInfo.sourceDir);
        //instant run
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(null != applicationInfo.splitSourceDirs){
                sourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            }
        }

        return sourcePaths;
    }

    public static Set<String> getFileNameByPackageName(Application context,final String packageName) throws PackageManager.NameNotFoundException {

        //拿到apk当中的dex地址
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);

        for(final String path : paths){
            DexFile dexFile = null;
            try {
                //加载apk中的dex并遍历获得所有packageName的类
                dexFile = new DexFile(path);
                Enumeration<String> dexEntries = dexFile.entries();
                while (dexEntries.hasMoreElements()){
                    //整个apk中所有的类
                    String className = dexEntries.nextElement();
                    if(className.startsWith(packageName)){
                        classNames.add(className);
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }finally {
                if(null != dexFile){
                    try {
                        dexFile.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        return classNames;
    }

}
