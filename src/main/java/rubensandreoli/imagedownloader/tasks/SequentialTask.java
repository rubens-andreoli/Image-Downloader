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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.commons.tools.Configs;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

/**
 * References:
 * https://stackoverflow.com/questions/50359350/can-you-check-whether-a-file-is-really-an-image-in-java
 * http://jubin.tech/articles/2018/12/05/Detect-image-format-using-java.html
 * https://stackoverflow.com/questions/2190161/difference-between-java-lang-runtimeexception-and-java-lang-exception
 */
public class SequentialTask extends DownloadTask{

    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String URL_MASK = "%s/%s%s"; //path, filename, extension
    private static final String URL_REGEX = "^(https*:\\/\\/)(.*\\/)([^.]+.)([a-z]{3,})$"; //file url
    private static final String LOWER_MARKER = "{";
    private static final String UPPER_MARKER = "}";
    private static final String MARKER_REGEX = "(\\"+LOWER_MARKER+"\\d+\\"+UPPER_MARKER+")";
    private static final String URL_MARKER_REGEX = "^[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"]+"+MARKER_REGEX+"[^\\"+LOWER_MARKER+"\\"+UPPER_MARKER+"\\/]+$"; //only one marker
    
    private static final String INVALID_URL_MSG = "Invalid image URL.";
    private static final String MISSING_MARKERS_MSG = "Image URL missing markers "+LOWER_MARKER+"'initial_value'"+UPPER_MARKER+".";
    private static final String INVALID_UPPER_BOUND_MSG = "Upper bound must be greater than or equal to the lower bound.";
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CONFIGURATIONS "> 
    private static final int FAIL_THRESHOLD;
    private static final int MIN_FILESIZE; //bytes
    static{
        FAIL_THRESHOLD = Configs.values.get("sequencial:fails_threshold", 10, 0);
        MIN_FILESIZE = Configs.values.get("sequencial:filesize_min", 25600, 0);
    }
    // </editor-fold>
    
    private final String parent;
    private final String maskedFilename; //with %d where number is supposed to be
    private final String extension; //with dot at start
    private final int lowerBound;
    private int upperBound;
    private Set<Integer> excluding;
    private int safeThreshold = 0; //start counting fails after
    
    public SequentialTask(String url) throws MalformedURLException{
        if(!url.matches(URL_REGEX)) throw new MalformedURLException(INVALID_URL_MSG);
        if(!url.matches(URL_MARKER_REGEX)) throw new MalformedURLException(MISSING_MARKERS_MSG);
        
        //URL PATH AND LEAF
        parent = FileUtils.parseParent(url);
        final String file = FileUtils.parseFile(url);

        //LOWER BOUNDS
        final int numberIndex = file.indexOf(LOWER_MARKER)+1;
        final int numberLenght = file.indexOf(UPPER_MARKER) - numberIndex;
        lowerBound = IntegerUtils.parseInteger(file.substring(numberIndex, numberIndex+numberLenght));
        
        //MASKED FILENAME AND EXTENSION
        String numberMask = "%d";
        if(file.charAt(numberIndex) == '0') numberMask = "%0"+numberLenght+"d";
        maskedFilename = FileUtils.parseFilename(file.replaceAll(MARKER_REGEX, numberMask), false);
        extension = FileUtils.parseExtension(file);
        
        setFailTreashold(FAIL_THRESHOLD);
        setSizeThreashold(MIN_FILESIZE);
    }

    @Override
    protected void run() {
//        if(upperBound < lowerBound) throw new IllegalStateException("upperBound ["+upperBound+"] < lowerBound ["+lowerBound+"]");
//        if(upperBound != 0){
            setWorkload(upperBound-lowerBound+1); //+1: end inclusive;
//        }
        
//        for(int i=lowerBound; (upperBound == 0 || i <= upperBound) && !failed(); i++){
        for(int i=lowerBound; i <= upperBound; i++){
            //CHECKS
            if(interrupted()) break;
            if(i > safeThreshold && failed()) break;
            if(excluding != null && excluding.contains(i)) continue; 
            
            //CREATE URL AND FILE
            final String formatedFilename = String.format(maskedFilename, i);
            final File file = FileUtils.createValidFile(getDestination(), formatedFilename, extension);
            final String imageUrl = String.format(URL_MASK, parent, formatedFilename, extension);
            
            //DOWNLOAD
            if(download(imageUrl, file, null, f -> (FileUtils.readFirstCharacter(f) != '<'))){ //<!DOCTYPE...>
                increaseSuccesses();
                resetFails(); //successive fails
            }else{
                increaseFails();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setUpperBound(int upperBound) throws BoundsException{
        if(/*upperBound != 0 && */upperBound < lowerBound){
            throw new BoundsException(INVALID_UPPER_BOUND_MSG);
        }
        this.upperBound = upperBound;
    }
    
    public void excludeNumbers(Collection<Integer> numbers){
        if(excluding == null) excluding = new HashSet<>();
        excluding.addAll(numbers);
    }
    
    public void setSafeThreshold(int number){
        safeThreshold = number;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getPath() {
        return parent;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
    // </editor-fold>

}
