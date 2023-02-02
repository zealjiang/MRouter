package com.example.libtsmerge;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class TsMerge {

    private final String PATH_PREFIX = "/Users/zealjiang/Desktop/ts/";
    private final String DIR = "/Users/zealjiang/Desktop/array_ts/";
    //private final String DIR = "/Users/zealjiang/Desktop/ffmpeg/";
    //private final String DIR = "/Users/zealjiang/Downloads/ffmpeg/";
    private final String FILE_NAME_PREFIX = "春风又绿江南岸";

    public static void main(String[] args) {
        System.out.println("-----tsMerge start----");
        System.out.println("cur dir ="+System.getProperty("user.dir"));
        TsMerge tsMerge = new TsMerge();
        //tsMerge.writeToFileWithM3u8();
        //tsMerge.traverseDirAndWriteToFile();
        tsMerge.mergeTsArrayDirs();
    }

    private void mergeTsArrayDirs(){
        try{
            File dir = new File(DIR);
            if(dir == null || !dir.isDirectory()){
                System.out.println("file "+DIR+" don't exist");
                return;
            }
            File[] fileArray = dir.listFiles();
            System.out.println("dir "+dir+"  fileArray ="+fileArray);
            System.out.println("dir.exists() "+dir.exists()+"  fileArray ="+dir.listFiles());
            if(fileArray == null || fileArray.length <= 0){
                System.out.println("fileArray is empty");
                return;
            }
            int i = 0;
            for (File file : fileArray) {
                if(file == null){
                    continue;
                }
                traverseDirAndWriteToFile(file.getAbsolutePath(),i);
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeToFileWithM3u8() {
        File readFile = new File("/Users/zealjiang/Desktop/ts/offline.m3u8");

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            File file = new File(PATH_PREFIX,"file.txt");
            if (file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file);

            fis = new FileInputStream(readFile);
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);

            String s = null;
            while ((s = br.readLine()) != null) {
                if (match(s)) {
                    fw.write("file '" + PATH_PREFIX + splitPartPath(s) + "'\n");
                    fw.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
                br.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean match(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.endsWith(".ts");
    }

    private String splitPartPath(String path) {
        //lastIndexOf()这个函数会从后向前查找
        String part = path.substring(path.lastIndexOf("/", path.lastIndexOf("/") - 1) + 1);
        System.out.println("part =" + part);
        return part;
    }

    private void traverseDirAndWriteToFile(String tsDir,int index) {
        System.out.println("-----traverseDirAndWriteToFile tsDir ="+tsDir);
        if(!tsDir.endsWith(".hls")){
            System.out.println("-----traverseDirAndWriteToFile tsDir is not hls return "+tsDir);
            return;
        }
        File fileDir = new File(tsDir);//"/Users/zealjiang/Desktop/ts");
        File[] dirs = fileDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains(".hls_")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        //排序，对各ts文件的目录排序，按文件从小到大来排，比如：xxx_0_29  xxx_30_59 ...
        Collections.sort(Arrays.asList(dirs), new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String o1Num = o1.getName().substring(o1.getName().lastIndexOf("_") + 1);
                String o2Num = o2.getName().substring(o2.getName().lastIndexOf("_") + 1);
                return Integer.valueOf(o1Num) - Integer.valueOf(o2Num);
            }
        });

        System.out.println("dirs length = " + dirs.length);
        System.out.println("dirs = " + Arrays.toString(dirs));
        //if(true)return;

        FileWriter fw = null;
        File fileTxt = null;
        try {
            fileTxt = new File(tsDir,"file.txt");
            if (fileTxt.exists()) {
                fileTxt.delete();
            }
            if (!fileTxt.exists()) {
                fileTxt.createNewFile();
            }
            fw = new FileWriter(fileTxt);

            for (int i = 0; i < dirs.length; i++) {
                File dir = dirs[i];
                File[] tsFiles = dir.listFiles();
                if (tsFiles.length > 0) {
                    //对tsFiles内的文件以文件名从小到大排序，即0.ts 1.ts ...
                    Collections.sort(Arrays.asList(tsFiles), new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            String o1Num = o1.getName().substring(0,o1.getName().lastIndexOf(".ts"));
                            String o2Num = o2.getName().substring(0,o2.getName().lastIndexOf(".ts"));
                            return Integer.valueOf(o1Num) - Integer.valueOf(o2Num);
                        }
                    });

                    for (int j = 0; j < tsFiles.length; j++) {
                        File tsFile = tsFiles[j];
                        String path = tsFile.getAbsolutePath();
                        //System.out.println("path = " + path);

                        fw.write("file '" + path + "'\n");
                        fw.flush();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(fileTxt == null)return;
            tsConcatToMp4ByFfmpeg(fileTxt.getAbsolutePath(),FILE_NAME_PREFIX+"_"+index);
        }
    }

    private void tsConcatToMp4ByFfmpeg(String fileTxtPath,String name){
        System.out.println("tsConcatToMp4ByFfmpeg fileTxtPath = " + fileTxtPath);
        //String cmd = "ffmpeg -f concat -safe 0 -i D:\新建文件夹\file.txt -c copy D:\新建文件夹\out.mp4";        String cmd = "ffmpeg -i "+fileDir+"/80/video.m4s -i "+fileDir+"/80/audio.m4s -codec copy "+DIR+name+".mp4";
        String cmd = "ffmpeg -f concat -safe 0 -i "+fileTxtPath+" -c copy "+DIR+name+".mp4";
        try{
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedInputStream in = new BufferedInputStream(p.getErrorStream());//ffmpeg输入日志的类型是error，所以这里使用p.getErrorStream()
            BufferedReader  inBr = new BufferedReader(new InputStreamReader(in));
            String lineStr;
            System.out.println("start---");
            while ((lineStr = inBr.readLine()) != null){
                System.out.println(lineStr);
            }
            inBr.close();
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void runCommandTsConcatToMp4(){
        //String cmd = "ffmpeg -f concat -safe 0 -i D:\新建文件夹\file.txt -c copy D:\新建文件夹\out.mp4";
        String cmd = "usr/bin/touch /Users/computer/Desktop/a.md";
        Process process = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            if(process.waitFor() != 0){
                process.destroy();
            }
/*            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}