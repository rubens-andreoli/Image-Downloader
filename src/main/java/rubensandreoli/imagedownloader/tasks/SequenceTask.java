/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rubensandreoli.imagedownloader.tasks;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import rubensandreoli.commons.others.CachedFile;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;

/**
 * References:
 https://stackoverflow.com/questions/50359350/can-you-check-whether-a-name-is-really-an-image-in-java
 http://jubin.tech/articles/2018/12/05/Detect-image-format-using-java.html
 https://stackoverflow.com/questions/2190161/difference-between-java-lang-runtimeexception-and-java-lang-exception
 * 
 * @author Rubens A. Andreoli Jr.
 */
public class SequenceTask extends DownloadTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    public static final int DEFAULT_FAIL_THRESHOLD = 10;
    public static final int DEFAULT_MIN_FILESIZE = 25600; //bytes
    private static final int REPEAT_LIMIT = 2; //+1 files same size
    
    private static final String URL_MASK = "%s/%s%s"; //path, name, extension
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)(.{1,})(\\.[a-z]{3,})$"; //file url
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"]+"+MARKER_REGEX+"[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"\\/]+$"; //only one marker
    
    private static final String REAPAT_LOG = "Downloading repeated files. Sequence was interrupted!";
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers "+LOWER_MARKER+"'initial_value'"+UPPER_MARKER+".";
    private static final String INVALID_UPPER_BOUND_MSG = "Upper bound must be greater than or equal to the lower bound.";
    // </editor-fold>
    
    private final String parent;
    private final String maskedFilename; //with '%d' where number is supposed to be
    private final String extension; //with dot at start
    private final int lowerBound, upperBound;
    private Set<Integer> excluding; //'null' won't exclude any values
    private int safeThreshold = 0; //start counting fails after; value '0' never safe

    public SequenceTask(String url, int upperBound) throws MalformedURLException, BoundsException{
        if(!url.matches(URL_REGEX)) throw new MalformedURLException(INVALID_URL_MSG);
        if(!url.matches(URL_MARKER_REGEX)) throw new MalformedURLException(MISSING_MARKERS_MSG);
        
        //URL PARENT AND LEAF
        parent = FileUtils.getParent(url);
        final String name = FileUtils.getName(url);

        //BOUNDS
        final int numberIndex = name.indexOf(LOWER_MARKER)+1;
        final int numberLenght = name.indexOf(UPPER_MARKER) - numberIndex;
        lowerBound = IntegerUtils.parseInteger(name.substring(numberIndex, numberIndex+numberLenght));
        if(upperBound < lowerBound) throw new BoundsException(INVALID_UPPER_BOUND_MSG);
        this.upperBound = upperBound;
        
        //MASKED FILENAME AND EXTENSION
        String numberMask = "%d";
        if(name.charAt(numberIndex) == '0') numberMask = "%0"+numberLenght+"d";
        maskedFilename = FileUtils.getFilename(name.replaceAll(MARKER_REGEX, numberMask), false);
        extension = FileUtils.getExtension(name);
        
        setFailThreshold(DEFAULT_FAIL_THRESHOLD);
        setMinFilesize(DEFAULT_MIN_FILESIZE);
    }

    @Override
    protected void run() {
        monitor.setWorkload(upperBound-lowerBound+1); //+1: end inclusive;
        
        long lastSize = 0;
        int same = 0;
        for(int i=lowerBound; i <= upperBound; i++){
            //CHECKS
            if(interrupted()) break; //INTERRUPT EXIT POINT
            if(i > safeThreshold && failed()) break;
            if(excluding != null && excluding.contains(i)) continue; 
            
            //CREATE URL AND FILE NAME
            final String formattedFilename = String.format(maskedFilename, i);
            final String imageUrl = String.format(URL_MASK, parent, formattedFilename, extension);
            
            //DOWNLOAD
            CachedFile file;
            if((file = downloader.download(imageUrl, getDestination(), formattedFilename, extension)) != null){

                //CHECK IF SAME SIZE
                if(file.length() == lastSize){
                    if(++same == REPEAT_LIMIT){
                        monitor.report(Level.WARNING, REAPAT_LOG);
                        break;
                    }
                }else{
                    lastSize = file.length();
                    same = 0;
                }
                
                monitor.increaseSuccesses();
                monitor.resetFails(); //successive fails
            }else{
                monitor.increaseFails();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void excludeNumbers(Collection<Integer> values){
        if(excluding == null) excluding = new HashSet<>();
        excluding.addAll(values);
    }
    
    public void setSafeThreshold(int value){
        safeThreshold = value;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getMaskedName() {
        return maskedFilename+extension;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
    // </editor-fold>

    @Override
    public void downloadStateChanged(Level level, String description) {
        monitor.report(level, description);
    }

}
