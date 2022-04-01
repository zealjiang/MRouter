package com.example.libtsmerge;

import com.example.libtsmerge.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


public class M4sMerge {
    final static String DIR = "/Users/zealjiang/Desktop/array_m4s/";
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
                    doMergeM4s();
                }
            });
            t.start();
            t.join();
            System.out.println("main end --------_-");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doMergeM4s(){
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

            for (File file : fileArray) {
                if(file == null){
                    continue;
                }
                String idAndTitleAndDirName = findFileIdAndTitleAndDirName(file.getAbsolutePath());
                System.out.println("idAndTitleAndDirName ="+idAndTitleAndDirName);
                if(idAndTitleAndDirName == null || idAndTitleAndDirName.length() <= 1){
                    System.out.println("can't get file id and name file ="+file.getAbsolutePath());
                    continue;
                }
                String[] titleAndDir = idAndTitleAndDirName.split("\\|");
                if(!new File(DIR,titleAndDir[1]).exists()){
                    new File(DIR,titleAndDir[1]).mkdir();
                }
                System.out.println("idAndTitleAndDirName ="+idAndTitleAndDirName+"  titleAndDir[0] ="+titleAndDir[0]+"  titleAndDir[1] ="+titleAndDir[1]);
                mergeByFfmpeg(file.getAbsolutePath(),titleAndDir[0],titleAndDir[1]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void mergeByFfmpeg(String fileDir,String name,String outDirName){
        //String cmd = "ffmpeg -i /Users/zealjiang/Desktop/array_m4s/c_315122287/80/video.m4s -i /Users/zealjiang/Desktop/array_m4s/c_315122287/80/audio.m4s -codec copy /Users/zealjiang/Desktop/array_m4s/c_315122287/80/xxx1.mp4";
        String cmd = "ffmpeg -i "+fileDir+"/80/video.m4s -i "+fileDir+"/80/audio.m4s -codec copy "+DIR+outDirName+"/"+name+".mp4";
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
    private String findFileIdAndTitleAndDirName(String dir){
        long startT = System.currentTimeMillis();
        if(dir == null || dir.length() <= 0){
            System.out.println("findFileId error dir is null");
        }
        File readFile = new File(dir,"entry.json");
        String data = FileUtil.readFile(readFile);
        if(data != null && data.length() > 0){
            //获取title做为文件夹的名称
            int start = data.indexOf("\"title\"");
            if(start == -1){
                System.out.println("findFile title start fail");
                return "";
            }
            int end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFile title end fail");
                return "";
            }

            String dirName = data.substring(start+"\"title\"".length()+2,end-1);
            System.out.println("findFile dirName ="+dirName);

            start = data.indexOf("\"page\"",end);
            if(start == -1){
                System.out.println("findFileId page start fail");
                return "";
            }
            end = data.indexOf(",",start);
            if(end == -1){
                System.out.println("findFileId page end fail");
                return "";
            }

            String id = data.substring(start+"\"page\"".length()+1,end);
            System.out.println("id ="+id);
            System.out.println("time_id ="+(System.currentTimeMillis() - startT));

            String title = findFileTitle(data,end);
            //文件名称中不能有空格
            title = title.replace(' ','_');
            System.out.println("title.charAt(0) ="+(title.charAt(0)) +" 0="+('0')+"  9 ="+('9'));
            if(title.charAt(0) < '0' || title.charAt(0) > '9'){
                return id+"_"+title+"|"+dirName;
            }else {
                return title+"|"+dirName;
            }
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
}
