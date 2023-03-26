package com.example.libtsmerge;

import com.example.libtsmerge.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.security.auth.login.CredentialNotFoundException;


public class M4sMerge {
    final static String DIR_GROUPS = "/Users/zealjiang/android_source/test/Android_ts/";
    final static String DIR = "/Users/zealjiang/android_source/test/50355776/";//"/Users/zealjiang/Desktop/array_m4s/";//"/Users/zealjiang/Desktop/bilibli视频/Android_ts/884746024/";//"/Users/zealjiang/Desktop/array_m4s/";
    public static void main(String[] args) {
        final M4sMerge m4sMerge = new M4sMerge();
/*        m4sMerge.findFileId(DIR+"c_315122287");
        m4sMerge.findFileIdByJsonParse(DIR+"c_315122287");*/
        m4sMerge.mergeM4sVideoAudioArrays();
    }


    private void mergeM4sVideoAudioArrays(){
        try{
            System.out.println("main start ----------");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //doMergeM4s(DIR);
                    doMergeM4sAllGroups();
                }
            });
            t.start();
            t.join();
            System.out.println("main end --------_-");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doMergeM4sAllGroups(){
        try{
            File dirGroups = new File(DIR_GROUPS);
            File[] groupArray = dirGroups.listFiles();
            if(groupArray == null || groupArray.length <= 0){
                System.out.println("doMergeM4sAllGroups groupArray is empty dir ="+dirGroups);
                return;
            }

            for (File file : groupArray) {
                if(file == null){
                    continue;
                }
                System.out.println("group ="+file.getAbsolutePath());
                if (file.getName().endsWith(".DS_Store")) {
                    continue;
                }
                doMergeM4s(file.getAbsolutePath());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doMergeM4s(String groupDir){
        try{
            System.out.println("doMergeM4s groupDir ="+groupDir);
            if (groupDir == null || groupDir.length() <= 0) {
                groupDir = DIR;
            }
            File dir = new File(groupDir);
            File[] fileArray = dir.listFiles();
            if(fileArray == null || fileArray.length <= 0){
                System.out.println("fileArray is empty dir ="+dir);
                return;
            }

            for (File file : fileArray) {
                if(file == null){
                    continue;
                }
                System.out.println("file ="+file.getAbsolutePath());
                if (file.getName().endsWith(".DS_Store")) {
                    continue;
                }
                NameEntry nameEntry = findEntrNames(file.getAbsolutePath());
                System.out.println("nameEntry ="+nameEntry);
                if(nameEntry == null){
                    System.out.println("can't get file id and name file ="+file.getAbsolutePath());
                    continue;
                }

                //文件夹中不能有空格
                String videoGroupName = nameEntry.videoGroupName;//【博毅创为】【layaAir开发教程】【游戏开发】由浅入深，菜鸟也能做游戏【layaAir】
                if (videoGroupName.contains(" ")) {
                    videoGroupName = videoGroupName.replaceAll(" ", "");
                }
                if (videoGroupName.contains("【")) {
                    videoGroupName = videoGroupName.replaceAll("【", "");
                }
                if (videoGroupName.contains("】")) {
                    videoGroupName = videoGroupName.replaceAll("】", "-");
                }
                System.out.println("去掉空格的 videoGroupName ="+videoGroupName);
                if(!new File(DIR_GROUPS,videoGroupName).exists()){
                    new File(DIR_GROUPS,videoGroupName).mkdirs();
                }
                String videoName = nameEntry.videoName;
                String videoId = nameEntry.videoId;
                String m4sDirName = nameEntry.m4sDirName;
                System.out.println("nameEntry videoGroupName ="+videoGroupName+"  videoName ="+videoName+"  videoId ="+videoId+" m4sDirName ="+m4sDirName);
                mergeByFfmpeg(file.getAbsolutePath(), videoName, videoGroupName, m4sDirName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void mergeByFfmpeg(String fileDir,String name,String outDirName, String m4sDirName){
        System.out.println("mergeByFfmpeg fileDir ="+fileDir+" name ="+name+"  outDirName ="+outDirName);
        //String cmd = "ffmpeg -i /Users/zealjiang/Desktop/array_m4s/c_315122287/80/video.m4s -i /Users/zealjiang/Desktop/array_m4s/c_315122287/80/audio.m4s -codec copy /Users/zealjiang/Desktop/array_m4s/c_315122287/80/xxx1.mp4";
        String cmd = "ffmpeg -i "+fileDir+"/"+m4sDirName+"/video.m4s -i "+fileDir+"/"+m4sDirName+"/audio.m4s -codec copy "+DIR+outDirName+"/"+name+".mp4";
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

    /**
     * 使用从文件中查找id和name的方式执行的时间在2ms左右，而使用gson的方式解析却花费了200ms左右，
     * 明显文件查找的文件要快，所以选择了使用文件中查找关键字的方式获取文件的id和name
     * @param dir
     * @return
     */
    private NameEntry findEntrNames(String dir){
        long startT = System.currentTimeMillis();
        System.out.println("findEntrNames dir ="+dir);
        NameEntry nameEntry = new NameEntry();
        if(dir == null || dir.length() <= 0){
            System.out.println("findEntrNames error dir is null");
        }
        File readFile = new File(dir,"entry.json");
        String data = FileUtil.readFile(readFile);
        if(data != null && data.length() > 0){
            //获取title做为文件夹的名称
            int start = data.indexOf("\"title\"");
            if(start == -1){
                System.out.println("findFile title start fail");
                return null;
            }
            int end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFile title end fail");
                return null;
            }

            String dirName = data.substring(start+"\"title\"".length()+2,end-1);
            //文件名称中不能有空格
            dirName = dirName.replaceAll(" ","");
            System.out.println("findFile 去掉空格的dirName ="+dirName);
            nameEntry.videoGroupName = dirName;

            //获取m4s文件所在的文件夹名称
            String typeTag = findM4sDirName(data, start);
            nameEntry.m4sDirName = typeTag;

            start = data.indexOf("\"page\"",end);
            if(start == -1){
                System.out.println("findFileId page start fail");
                return null;
            }
            end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFileId page end fail");
                return null;
            }

            String id = data.substring(start+"\"page\"".length()+1,end);
            System.out.println("id ="+id);
            System.out.println("use time ="+(System.currentTimeMillis() - startT));
            nameEntry.videoId = id;

            String title = findFileTitle(data,end);
            //文件名称中不能有空格
            title = title.replaceAll(" ","");
            System.out.println("去掉空格的title ="+title);
            System.out.println("title.charAt(0) ="+(title.charAt(0)) +" 0="+('0')+"  9 ="+('9'));
            nameEntry.videoName = title;
            return nameEntry;
        }
        return null;
    }

    private String findM4sDirName(String data,int fromIndex) {
        if(data != null && data.length() > 0){
            int start = data.indexOf("\"type_tag\"",fromIndex);
            if(start == -1){
                System.out.println("findFileId page start fail");
                return "";
            }
            int end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFileId page end fail");
                return "";
            }

            String typeTag = data.substring(start+"\"type_tag\"".length()+2,end-1);
            System.out.println("typeTag ="+typeTag);
            return typeTag;
        }
        return "";
    }

    private String findFileTitle(String data,int fromIndex){
        long startT = System.currentTimeMillis();
        if(data != null && data.length() > 0){
            int start = data.indexOf("\"part\"",fromIndex);
            if(start == -1){
                System.out.println("findFileId page start fail");
                return "";
            }
            int end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFileId page end fail");
                return "";
            }

            String id = data.substring(start+"\"part\"".length()+2,end-1);
            System.out.println("title ="+id);
            System.out.println("time_title ="+(System.currentTimeMillis() - startT));
            return id;
        }
        return "";
    }


    private String findFileIdByJsonParse(String dir){
        long startT = System.currentTimeMillis();
        if(dir == null || dir.length() <= 0){
            System.out.println("findFileId error dir is null");
        }
        File readFile = new File(dir,"entry.json");
        String data = FileUtil.readFile(readFile);
        if(data != null && data.length() > 0){
            Gson gson = new Gson();
            Entry entry = gson.fromJson(data,Entry.class);
            if(entry != null && entry.pageBean != null){
                System.out.println("page ="+entry.pageBean.page);
                System.out.println("name ="+entry.pageBean.name);
                System.out.println("time_json ="+(System.currentTimeMillis() - startT));
            }
        }
        return "";
    }

    class Entry{
        @SerializedName("page_data")
        PageBean pageBean;
        public class PageBean{
            public String page;
            @SerializedName("part")
            public String name;
        }
    }

    class NameEntry{
        public String videoId;
        public String videoGroupName;
        public String videoName;
        public String m4sDirName;
    }
}
