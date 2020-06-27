package rubensandreoli.imagedownloader.tasks;

import rubensandreoli.commons.utils.Utils;
import static rubensandreoli.imagedownloader.tasks.ProgressLog.ERROR;
import static rubensandreoli.imagedownloader.tasks.ProgressLog.INFO;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class SequencialTask extends BasicTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String URL_MASK = "%s/%s%s";
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)([^.]+.)([a-z]{3,})$";
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\{\\}]+(\\{\\d+\\})[^\\{\\}\\/]+$";
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers '"+LOWER_MARKER+"_"+UPPER_MARKER+"'.";
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
    
    public SequencialTask(String url, String destination, int upperBound) 
            throws MalformedURLException, IOException, BoundsException, NullPointerException {
        //PARAMETERS TESTS
        setDestination(destination);
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
    protected void run() {
        for(int i=lowerBound; i<=upperBound; i++){
            if(isInterrupted()) break;
            //OUTPUT FILE
            String formatedFilename = String.format(maskedFilename, i);
            File file = Utils.createValidFile(getDestination(), formatedFilename, extension);
            
            //DOWNLOAD TO FILE
            String url = String.format(URL_MASK, path, formatedFilename, extension);
            progress = i-lowerBound;
            var log = new ProgressLog(progress);
            log.appendToLog(String.format(DOWNLOADING_LOG_MASK, file), INFO);
            try {
                Utils.downloadToFile(url, file);
                log.appendToLog(DOWNLOADED_LOG, INFO);
            } catch (IOException ex) {
                log.appendToLog(String.format(DOWNLOAD_FAILED_LOG_MASK, url), ERROR);
            }
            if(listener != null) listener.progressed(log);
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return path;
    }

    @Override
    public int getWorkload() {
        return upperBound-lowerBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
    // </editor-fold>

}
