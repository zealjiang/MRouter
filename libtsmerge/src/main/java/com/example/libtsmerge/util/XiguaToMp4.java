package com.example.libtsmerge.util;

import com.example.libtsmerge.TsMerge;

import java.io.File;

public class XiguaToMp4 {
    private final String DIR = "/Users/zealjiang/Desktop/xigua/";

    public static void main(String[] args) {
        System.out.println("-----tsMerge start----");
        XiguaToMp4 xiguaToMp4 = new XiguaToMp4();
        xiguaToMp4.mergeTsArrayDirs();
    }

    private void mergeTsArrayDirs(){
        try{
            File dir = new File(DIR);
            if(dir == null){
                System.out.println("file "+DIR+" don't exist");
                return;
            }
            File[] fileArray = dir.listFiles();
            if(fileArray == null || fileArray.length <= 0){
                System.out.println("fileArray is empty");
                return;
            }
            int i = 0;
            for (File file : fileArray) {
                if(file == null){
                    continue;
                }
                renameFile(file);
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void renameFile(File file){
        if(file == null || !file.exists()) return;
        String path = file.getAbsolutePath();
        if(!path.endsWith("mdlnodeconf")){
            file.renameTo(new File(path+".mp4"));
        }
    }
}
