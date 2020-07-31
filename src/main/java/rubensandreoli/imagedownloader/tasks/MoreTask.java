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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rubensandreoli.commons.exceptions.BoundsException;
import rubensandreoli.commons.utils.FileUtils;

public class MoreTask extends GoogleTask {
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS "> 
    private static final String NUMBER_REGEX = "([?^\\d]{0,1}\\d{1,4}[?^\\d]{0,1})";
    private static final String VALID_IMAGE_REGEX = ".*"+NUMBER_REGEX+".*";
    private static final String LINK_MASK = "%s/%s{%%s}%s%s"; //parent; filename start; filename end; extension
    
    private static final String FOUND_SEQUENCE_LOG_MASK = "Found %d potential sequence(s)";
    
    private static final int MARGIN_LOWER = SequentialTask.DOWNLOAD_FAIL_THREASHOLD-1;
    private static final int MARGIN_UPPER = 50;
    private static final Pattern PATTERN = Pattern.compile(NUMBER_REGEX);
    // </editor-fold>

    private final Map<String, Set<String>> links = new HashMap<>();
    private SequentialTask currentSubtask;
    private boolean google;
    
    public MoreTask(String folder) throws IOException {
        super(folder);
    }

    @Override
    protected void run() {
        setRetrySmall(true);
        super.run();
        downloadSequence();
    }
    
    private ImageInfo lastImage;
    @Override
    protected void downloadLargest(List<ImageInfo> googleImages, ImageInfo source) {
        if(!source.equals(lastImage)){ //TODO: temporary solution for repeating when retrying
            lastImage = source;
            int found = 0;
            for (ImageInfo i : googleImages) {
                if(i.getFilename().matches(VALID_IMAGE_REGEX)){
                    if(addSequence(FileUtils.parseParent(i.path), i.getFilename(), i.getExtension())){
                        found++;
                    }
                }
            }
            getCurrentLog().appendLine(ProgressLog.INFO, FOUND_SEQUENCE_LOG_MASK, found);    
        }
        if(google) super.downloadLargest(googleImages, source);
    }
    
    private boolean addSequence(String site, String filename, String extension){
        Matcher matcher = PATTERN.matcher(filename); //TODO: filename until '?'
        int start = 0, end = 0;
        while(matcher.find()){ //last ocurrance
            start = matcher.start();
            end = matcher.end();
        }
        String maskedLink = String.format(LINK_MASK, site, filename.substring(0, start), filename.substring(end), extension);
        String value = filename.substring(start, end);
        
        Set<String> numbers;
        if((numbers = links.get(maskedLink)) != null){
            numbers.add(value);
            if(numbers.size() == 2){ //consider a sequence only if more than one occurance
                increaseWorkload();
                return true;
            }
        }else{
            numbers = new TreeSet<>();
            numbers.add(value);
            links.put(maskedLink, numbers);
        }
        return false;
    }
    
    private void downloadSequence(){
        for (var entry : links.entrySet()) {
            if(isInterrupted()) break; //INTERRUPT EXIT POINT

            final Set<String> values = entry.getValue();
            if(values.size() <= 1) continue; //not confirmed sequence
            
            //PARSE NUMBERS
            final List<Integer> numbers = new ArrayList<>();
            boolean padding = false;
            int endZero = 0;
            int maxLenght = 0;
            for (String v : values) {
                if('0' == v.charAt(0)) padding = true;
                if('0' == v.charAt(v.length()-1)) endZero++;
                if(v.length() > maxLenght) maxLenght = v.length();
                try{
                    numbers.add(Integer.parseInt(v));
                }catch(NumberFormatException ex){}
            }
            if(endZero == numbers.size()) continue; //possibly resolutions
            
            //PREPARE TASK VALUES
            int start = numbers.get(0)-MARGIN_LOWER;
            if(start < 1) start = 1;
            final int end = numbers.get(numbers.size()-1)+MARGIN_UPPER;
            String numberMask = "%d";
            if(padding) numberMask = "%0"+maxLenght+"d";
            String link = String.format(entry.getKey(), numberMask);
            link = String.format(link, start);
            //TODO: if google, don't download found; sequenceTask.exclude(Set<Integer> numbers);
            
            //START SUB-TASK
            report(ProgressLog.INFO, false, "Starting sequence %s [%d:%d]", link, start, end);
            int downloaded = 0;
            try {
                currentSubtask = new SequentialTask(link);
                currentSubtask.setUpperBound(end);
                String destination = super.getDestination();
                if(google){
                    File subfolder = new File(destination, "more");
                    if(!subfolder.isDirectory()) subfolder.mkdirs();
                    destination = subfolder.getAbsolutePath();
                }
                currentSubtask.setDestination(destination);
                currentSubtask.run();
                downloaded = currentSubtask.getSuccesses();
                addSuccesses(downloaded);
            } catch (MalformedURLException ex) {
                System.err.println("ERROR: failed marking image url " + ex.getMessage());
            } catch (BoundsException ex) {
                System.err.println("ERROR: failed setting upper bound " + ex.getMessage());
            } catch (IOException ex) {
                System.err.println("ERROR: failed setting source/destination " + ex.getMessage());
                ex.printStackTrace();
            }
            report(ProgressLog.INFO, "%d downloaded from sequence", downloaded);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(currentSubtask != null) currentSubtask.interrupt();
    }

    public void setLargest(boolean b) {
        this.google = b;
    }
      
}
