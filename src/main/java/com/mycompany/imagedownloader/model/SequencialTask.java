package com.mycompany.imagedownloader.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

public class SequencialTask implements Task{
	
    public static final int CONNECTION_TIMEOUT = 2000; //ms
//    public static final int FAILED_ATTEMPTS = 5;
    
    private static final String FILENAME_MASK = "%s/%s%s";
    private static final String DUPLICATED_FILENAME_MASK = "%s/%s (%d)%s";
    private static final String URL_MASK = "%s%s%s";
    
    private static final String EXTENSION_REGEX = "^.[a-z]{3,4}$";
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)(.*\\.)([a-z]{3,})$"; //Complete URL: ^http(s|):\/\/(([^\{\}\/]+\/)+)[^\{\}]+(\{\d+\})[^\{\}\/]+\.([a-z]{3,})$
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\{\\}]+"+MARKER_REGEX+"[^\\{\\}\\/]+";
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers '{_}'.";
    private static final String MISSING_DESTINATION_MSG = "Detination folder not found.";
    private static final String INVALID_BOUNDS_MSG = "Marked number in the URL must be smaller than the target upper bound.";
    
    private String path; //with separator at the end
    private String filename; //with %d where number is suposed to be
    private int lowerBound;
    private int upperBound;
    private String extension; //with dot at start
    private String destination;

    public SequencialTask(String url, String dest, int upperBound) throws MalformedURLException, IOException, BoundsException{
        //PARAMETERS TESTS
        File folder = new File(dest);
        if(!folder.exists() || !folder.isDirectory()){
            throw new IOException(MISSING_DESTINATION_MSG);
        }
        if(!url.matches(URL_REGEX)){
            throw new MalformedURLException(INVALID_URL_MSG);
        }
        if(!url.matches(URL_MARKER_REGEX)){
            throw new MalformedURLException(MISSING_MARKERS_MSG);
        }
        
        //URL PATH
        int fileIndex = url.lastIndexOf('/');
        path = url.substring(0, fileIndex+1);
//        System.out.println(path);
        
        //NUMBER POSITION
        String tempFilename = url.substring(fileIndex+1);
        int numberIndex = tempFilename.indexOf(LOWER_MARKER)+1;
        int numberLenght = tempFilename.indexOf(UPPER_MARKER) - numberIndex;
        lowerBound = Integer.parseInt(tempFilename.substring(numberIndex, numberIndex+numberLenght));
        this.upperBound = upperBound;
        String numberMask = "%d";
        if(tempFilename.charAt(numberIndex) == '0'){
            numberMask = "%0"+numberLenght+"d";
        }
        tempFilename = tempFilename.replaceAll(MARKER_REGEX, numberMask);
        if(upperBound <= lowerBound){
            throw new BoundsException(INVALID_BOUNDS_MSG);
        }
        
        //FILENAME AND EXTENSION
        int dotIndex = tempFilename.indexOf(".");
        filename = tempFilename.substring(0, dotIndex);
        extension = tempFilename.substring(dotIndex);
        for(int i=extension.length(); i>4 && !extension.matches(EXTENSION_REGEX); i=extension.length()){  //crop end until it matches or minimal lenght
            extension = extension.substring(0, i-1);
        }
//        System.out.println(filename);
//        System.out.println(extension);
        
        //DESTINATION FOLDER
        destination = dest;
    }

    @Override
    public void perform(){
//        int fails = 0;
        for(int i=lowerBound; i<=upperBound; i++){
            //OUTPUT FILE
            String formatedFilename = String.format(filename, i);
            File file = new File(String.format(FILENAME_MASK, destination,formatedFilename,extension));
            for(int n=1; file.exists(); n++){
                file = new File(String.format(DUPLICATED_FILENAME_MASK, destination,formatedFilename,n,extension));
            }
            
            //DOWNLOAD TO FILE
            String url = String.format(URL_MASK, path,formatedFilename,extension);
            try {
                FileUtils.copyURLToFile(new URL(url), file, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
            } catch (IOException ex) {
                System.err.println("Failed downloading: "+ url);
//                fails++;
            }
//            if(fails>=FAILED_ATTEMPTS) break;
        }
    }

    public String getPath() {return path;}
    public String getFilename() {return filename;}
    public String getExtension() {return extension;}
    public String getDestiantion() {return destination;}

}
