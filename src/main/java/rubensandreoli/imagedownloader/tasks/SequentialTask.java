package rubensandreoli.imagedownloader.tasks;

import rubensandreoli.commons.utils.Utils;
import java.io.File;
import java.net.MalformedURLException;
import rubensandreoli.commons.utils.Configs;

public class SequentialTask extends DownloadTask{

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
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    public static final int DEFAULT_FAIL_THREASHOLD = 10;
    
    private static final int DOWNLOAD_FAIL_THREASHOLD;
    private static final int MIN_FILESIZE; //bytes
    static{
        DOWNLOAD_FAIL_THREASHOLD = Configs.values.get("sequencial:fail_threashold", DEFAULT_FAIL_THREASHOLD, 0);
        MIN_FILESIZE = Configs.values.get("sequencial:filesize_min", 25600, 0);
    }
    // </editor-fold>
    
    private String path;
    private String maskedFilename; //with %d where number is supposed to be
    private String extension; //with dot at start
    private int lowerBound = -1;
    private int upperBound;

    @Override
    protected int run() {
        setWorkload(upperBound-lowerBound+1); //+1: end inclusive;
        int success = 0;
        int fails = 0;
        for(int i=lowerBound; i<=upperBound; i++){
            if(isInterrupted() || (DOWNLOAD_FAIL_THREASHOLD > 0 && fails >= DOWNLOAD_FAIL_THREASHOLD)) break;

            String formatedFilename = String.format(maskedFilename, i);
            File file = Utils.createValidFile(getDestination(), formatedFilename, extension);
            String url = String.format(URL_MASK, path, formatedFilename, extension);
            
            if(download(url, file, MIN_FILESIZE, null)){
                success++;
                fails = 0;
            }else{
                fails++;
            }
        }
        return success;
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
