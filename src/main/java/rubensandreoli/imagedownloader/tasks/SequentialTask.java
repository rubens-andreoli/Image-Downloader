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
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.commons.utils.Configs;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

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
    private static final String INVALID_UPPER_BOUND_MSG = "Upper bound must be greater than or equal to the lower bound.";
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
    
    private final String path;
    private final String maskedFilename; //with %d where number is supposed to be
    private final String extension; //with dot at start
    private final int lowerBound;
    private int upperBound;
    
    public SequentialTask(String url) throws MalformedURLException{
        if(!url.matches(URL_REGEX)) throw new MalformedURLException(INVALID_URL_MSG);
        if(!url.matches(URL_MARKER_REGEX)) throw new MalformedURLException(MISSING_MARKERS_MSG);
        
        //URL PATH AND LEAF
        int fileIndex = url.lastIndexOf('/');
        path = url.substring(0, fileIndex);
        String file = url.substring(fileIndex+1);

        //LOWER BOUND
        int numberIndex = file.indexOf(LOWER_MARKER)+1;
        int numberLenght = file.indexOf(UPPER_MARKER) - numberIndex;
        upperBound = lowerBound = IntegerUtils.parseInteger(file.substring(numberIndex, numberIndex+numberLenght));
        
        //MASKED FILENAME AND EXTENSION
        String numberMask = "%d";
        if(file.charAt(numberIndex) == '0') {
            numberMask = "%0"+numberLenght+"d";
        }
        maskedFilename = FileUtils.parseFilename(file.replaceAll(MARKER_REGEX, numberMask), false);
        extension = FileUtils.parseExtension(file);
    }

    @Override
    protected int run() {
        setWorkload(upperBound-lowerBound+1); //+1: end inclusive;
        int success = 0;
        int fails = 0;
        for(int i=lowerBound; i<=upperBound; i++){
            if(isInterrupted() || (DOWNLOAD_FAIL_THREASHOLD > 0 && fails >= DOWNLOAD_FAIL_THREASHOLD)) break;

            String formatedFilename = String.format(maskedFilename, i);
            File file = FileUtils.createValidFile(getDestination(), formatedFilename, extension);
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
    public void setUpperBound(int upperBound) throws BoundsException{
        if(upperBound < lowerBound){
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
