
package com.mycompany.imagedownloader.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

public class Utils {
    
    public static final int CONNECTION_TIMEOUT = 2000; //ms
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    public static final int MIN_FILE_EXTENSION_LENGHT = 3; //including '.'
    public static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    public static final String FILENAME_MASK = "%s/%s%s";
    public static final String DUPLICATED_FILENAME_MASK = "%s/%s (%d)%s";
    public static final String FAILED_DOWNLOAD_MSG_MASK = "Failed downloading and saving: %s";
    
    private Utils(){}
    
    public static File generateFile(String folder, String filename, String extension){
        File file = new File(String.format(FILENAME_MASK, folder, filename, extension));
        for(int n=1; file.exists(); n++){
            file = new File(String.format(DUPLICATED_FILENAME_MASK, folder, filename, n, extension));
        }
        return file;
    }
    
    public static String generateFilepath(String folder, String filename, String extension){
        return generateFile(folder, filename, extension).getAbsolutePath();
    }
    
    public static void saveFileFromURL(String url, File file) throws IOException{
        try{
            FileUtils.copyURLToFile(new URL(url), file, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        }catch(IOException ex){
            System.err.println(String.format(FAILED_DOWNLOAD_MSG_MASK, url));
            throw ex;
        }
    }
    
    public static String getExtension(String filename){
        int index = filename.lastIndexOf(".");
        String ext = DEFAULT_IMAGE_EXTENSION;
        if(index != -1 && (filename.length()-index) > MIN_FILE_EXTENSION_LENGHT){
            ext = filename.substring(index);
        }
        return ext;
    }
    
    public static String getFilename(String path){
        int index = path.lastIndexOf("/");
        String filename = path;
        if(index != -1){
            if(index+1 > path.length()){
                filename = path.substring(0, index);
            }else{
                filename = path.substring(index);
            }
        }
        
        int extIndex = filename.lastIndexOf(".");
        if(index != -1){
            filename = filename.substring(0, extIndex);
        }
        return filename;
    }
    
}
