package rubensandreoli.imagedownloader.tasks;

import rubensandreoli.commons.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import rubensandreoli.commons.utils.Configs;

public class SequentialTask extends BasicTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String URL_MASK = "%s/%s%s"; //path, filename, extension
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)([^.]+.)([a-z]{3,})$"; //image url
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"]+"+MARKER_REGEX+"[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"\\/]+$"; //only one marker
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers '"+LOWER_MARKER+"_"+UPPER_MARKER+"'.";
    private static final String INVALID_LOWER_BOUND_MSG = "Marked number in the URL must be smaller than the target upper bound.";
    private static final String INVALID_UPPER_BOUND_MSG = "Upper bound must be greater than 0, and then the lower bound.";
    
    private static final String DOWNLOAD_LOG_MASK = "Downloaded image to %s"; //file
    private static final String DOWNLOAD_FAILED_LOG_MASK = "Failed downloading/saving from %s"; //url
    private static final String DOWNLOAD_TOTAL_LOG_MASK = "%s image(s) downloaded"; //success count
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    public static final int FAIL_THREASHOLD;
    static{
        FAIL_THREASHOLD = Configs.values.get("sequencial:fail_threashold", 10, 0);
    }
    // </editor-fold>
    
    //SOURCE
    private String path;
    private String maskedFilename; //with %d where number is supposed to be
    private String extension; //with dot at start
    private int lowerBound = -1;
    
    private int upperBound;

    @Override
    protected void run() {
        setWorkload(upperBound-lowerBound+1); //+1: end inclusive;
        int success = 0;
        int fails = 0;
        for(int i=lowerBound; i<=upperBound; i++){
            if(isInterrupted() || (FAIL_THREASHOLD > 0 && fails >= FAIL_THREASHOLD)) break; //INTERRUPT PROCCESS
            //OUTPUT FILE
            String formatedFilename = String.format(maskedFilename, i);
            File file = Utils.createValidFile(getDestination(), formatedFilename, extension);
            
            //DOWNLOAD TO FILE
            String url = String.format(URL_MASK, path, formatedFilename, extension);
            try {
                Utils.downloadToFile(url, file);
                report(ProgressLog.INFO, DOWNLOAD_LOG_MASK, file);
                success++;
                fails = 0;
                Utils.sleepRandom(CONNECTION_MIN_TIMEOUT, CONNECTION_MAX_TIMEOUT);
            } catch (IOException ex) {
                report(ProgressLog.ERROR, DOWNLOAD_FAILED_LOG_MASK, url);
                fails++;
            }
        }
        report(ProgressLog.INFO, false, DOWNLOAD_TOTAL_LOG_MASK, success);
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSource(String url) throws MalformedURLException, BoundsException{
        if(!url.matches(URL_REGEX)) throw new MalformedURLException(INVALID_URL_MSG);
        if(!url.matches(URL_MARKER_REGEX)) throw new MalformedURLException(MISSING_MARKERS_MSG);
        
        //URL PATH AND LEAF
        int fileIndex = url.lastIndexOf('/');
        path = url.substring(0, fileIndex);
        String file = url.substring(fileIndex+1);

        //LOWER BOUND
        int numberIndex = file.indexOf(LOWER_MARKER)+1;
        int numberLenght = file.indexOf(UPPER_MARKER) - numberIndex;
        lowerBound = Utils.parseInteger(file.substring(numberIndex, numberIndex+numberLenght));
        if(upperBound > 0 && upperBound <= lowerBound){ //if upper bound is set
            throw new BoundsException(INVALID_LOWER_BOUND_MSG);
        }
        
        //MASKED FILENAME AND EXTENSION
        String numberMask = "%d";
        if(file.charAt(numberIndex) == '0'){
            numberMask = "%0"+numberLenght+"d";
        }
        maskedFilename = Utils.parseFilename(file.replaceAll(MARKER_REGEX, numberMask), false);
        extension = Utils.parseExtension(file);
    }
    
    public void setUpperBound(int upperBound) throws BoundsException{
        if(upperBound <= 0 || (lowerBound != -1 && upperBound <= lowerBound)){
            throw new BoundsException(INVALID_UPPER_BOUND_MSG);
        }
        this.upperBound = upperBound;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return path;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
    // </editor-fold>

}
