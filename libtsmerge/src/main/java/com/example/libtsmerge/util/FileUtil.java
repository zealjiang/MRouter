package com.example.libtsmerge.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zealjiang on 2018/1/7.
 */

public class FileUtil {


    public static String getFileName(String path){

        int start=path.lastIndexOf("/");
        int end=path.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return path.substring(start+1,end);
        }else{
            return null;
        }

    }

    public static String readFile(File file) {

        String out_str = "";
        StringBuilder sb = new StringBuilder();

        try {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int n;
            try {
                while ((n = in.read(buffer)) != -1) {
                    out_str = new String(buffer, 0, n);
                    sb.append(out_str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    public static String convertCodeAndGetText(String str_filepath) {// 转码

        File file = new File(str_filepath);
        BufferedReader reader;
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte)0xEF  && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8

                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {

                reader = new BufferedReader(
                        new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16le"));
            } else {

                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();

            while (str != null) {
                text = text + str + System.getProperty("line.separator");
                str = reader.readLine();

            }
            reader.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

}
