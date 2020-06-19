package com.mycompany.imagedownloader.model;

import com.mycompany.imagedownloader.model.ProgressLog.Status;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class SequencialTask implements Task{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String URL_MASK = "%s/%s%s";
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)([^.]+.)([a-z]{3,})$";
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\{\\}]+(\\{\\d+\\})[^\\{\\}\\/]+$";
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers '"+LOWER_MARKER+"_"+UPPER_MARKER+"'.";
    private static final String MISSING_DESTINATION_MSG = "Destination folder not found.";
    private static final String INVALID_BOUNDS_MSG = "Marked number in the URL must be smaller than the target upper bound.";
    
    private static final String DOWNLOADING_LOG_MASK = "Downloading %s\r\n"; //file
    private static final String DOWNLOADED_LOG = "Download complete\r\n";
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading/saving from %s\r\n"; //url
    // </editor-fold>
    
    private String path;
    private String maskedFilename; //with %d where number is supposed to be
    private int lowerBound;
    private int upperBound;
    private String extension; //with dot at start
    private String destination;
    
    private ProgressListener listener;
    private volatile boolean running;

    public SequencialTask(String url, String dest, int upperBound) 
            throws MalformedURLException, IOException, BoundsException, NullPointerException {
        //PARAMETERS TESTS
        if(!(new File(dest)).isDirectory()){
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
        path = url.substring(0, fileIndex);
        
        //NUMBER POSITION
        this.upperBound = upperBound;
        String file = parseNumber(url.substring(fileIndex+1));
        
        //FILENAME AND EXTENSION
        maskedFilename = Utils.parseFilename(file, false);
        extension = Utils.parseExtension(file);
        
        //DESTINATION FOLDER
        destination = dest;
    }
    
    private String parseNumber(String file) throws BoundsException {
        int numberIndex = file.indexOf(LOWER_MARKER)+1;
        int numberLenght = file.indexOf(UPPER_MARKER) - numberIndex;
        
        lowerBound = Integer.parseInt(file.substring(numberIndex, numberIndex+numberLenght));
        if(upperBound <= lowerBound){
            throw new BoundsException(INVALID_BOUNDS_MSG);
        }
        
        String numberMask = "%d";
        if(file.charAt(numberIndex) == '0'){
            numberMask = "%0"+numberLenght+"d";
        }
        return file.replaceAll(MARKER_REGEX, numberMask);
    }

    @Override
    public void start(){
        running = true;
        for(int i=lowerBound; i<=upperBound; i++){
            if(!running) break;
            //OUTPUT FILE
            String formatedFilename = String.format(maskedFilename, i);
            File file = Utils.createValidFile(destination, formatedFilename, extension);
            
            //DOWNLOAD TO FILE
            String url = String.format(URL_MASK, path, formatedFilename, extension);
            var log = new ProgressLog(i);
            log.appendToLog(String.format(DOWNLOADING_LOG_MASK, file), Status.INFO);
            try {
                Utils.downloadToFile(url, file);
                log.appendToLog(DOWNLOADED_LOG, Status.INFO);
            } catch (IOException ex) {
                log.appendToLog(String.format(DOWNLOAD_FAILED_LOG_MASK, url), Status.ERROR);
            }
            if(listener != null) listener.progressed(log);
        }
        running = false; //not really needed
    }
    
    @Override
    public void stop() {
        running = false;
    }

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return path;
    }

    public String getDestiantion() {
        return destination;
    }
    
    @Override
    public int getProcessesCount() {
        return upperBound-lowerBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 

//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public void setDestination(String destination) throws IOException {
//        if(!(new File(destination)).isDirectory()){
//            throw new IOException(MISSING_DESTINATION_MSG);
//        }
//        this.destination = destination;
//    }
    
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    // </editor-fold>

}
