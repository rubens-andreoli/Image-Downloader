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
import rubensandreoli.imagedownloader.support.ProgressLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rubensandreoli.commons.exceptions.checked.BoundsException;
import rubensandreoli.commons.others.Logger;
import rubensandreoli.commons.others.Logger.Level;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.imagedownloader.support.ProgressLog.Tag;
import static rubensandreoli.imagedownloader.tasks.BasicTask.STATUS_LOG_MASK;
import rubensandreoli.imagedownloader.tasks.Searcher.ImageInfo;

public class MoreSubtask implements GoogleSubtask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int PRIORITY = LargerSubtask.PRIORITY+1;
    public static final int DEFAULT_LOWER_MARGIN = 10;
    public static final int DEFAULT_UPPER_MARGIN = 100;
    public static final int DEFAULT_MIN_DIMENSION = 400;
    private static final int OCURRANCE_CONFIRMATION = 2;
    
    private static final String NUMBER_REGEX = "\\d+";
    private static final String CONFIRMED_MARKER = ".";
    private static final String VALID_IMAGE_REGEX = ".*"+NUMBER_REGEX+".*";
    private static final String SEQUENCE_LINK_MASK = "%s/%s{%%s}%s%s"; //parent; filename start; filename end; extension
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
    
    private static final String TITLE_LOG = "MORE";
    private static final String FOUND_SEQUENCE_LOG_MASK = "Found %d potential sequence(s)";
    private static final String SEQUENCE_START_LOG_MASK = "Starting sequence %s [%d:%d]"; //url; start; end
    private static final String SEQUENCE_TOTAL_LOG = "%d sequencial download(s)";
    // </editor-fold>

    private String destination;
    private final Map<String, Set<String>> links = new HashMap<>();
    private int minDimension = DEFAULT_MIN_DIMENSION;
    private int lowerMargin = DEFAULT_LOWER_MARGIN;
    private int upperMargin = DEFAULT_UPPER_MARGIN;
    private SequentialTask currentSubtask;
    
    public MoreSubtask(String folder, String subfolder) throws IOException{
        try{
            destination = FileUtils.createSubfolder(folder, subfolder);
        }catch(IOException ex){
            throw new IOException(String.format("Failed to create subfolder [%s]", destination));
        }
    }   
    
    @Override
    public void processing(GoogleTask task, ImageInfo source, List<ImageInfo> similars) {
        int found = 0;
        for (ImageInfo i : similars) {
            //PREPARE VALUES
            String path = i.path;
            final int index = path.lastIndexOf('?');
            if(index > 0){
                path = path.substring(0, index);
            }
            final String parent = i.getParent();
            final String filename = i.getFilename();
            final String extension = i.getExtension();

            //TEST AND ADD SEQUENCE
            if(filename.matches(VALID_IMAGE_REGEX) && (i.width > minDimension || i.height > minDimension)){
                found += addSequence(parent, filename, extension);
            }
        }
        task.addWorkload(found);
        task.getCurrentLog().appendLine(Tag.INFO, FOUND_SEQUENCE_LOG_MASK, found);
    }
    
    private int addSequence(String parent, String filename, String extension){
        int added = 0, found = 0;
        boolean created = false;
        final Matcher matcher = NUMBER_PATTERN.matcher(filename);
        Set<String> numbers = null;
        while(matcher.find()){
            found++;
            final int start = matcher.start();
            final int end = matcher.end();
            final String maskedLink = String.format(SEQUENCE_LINK_MASK, 
                    parent, 
                    filename.substring(0, start), 
                    filename.substring(end), 
                    extension
            );
            final String number = filename.substring(start, end);

            if((numbers = links.get(maskedLink)) != null){
                numbers.add(number);
                if(numbers.size() == OCURRANCE_CONFIRMATION){ //confirm after X ocurrances
                    numbers.add(CONFIRMED_MARKER);
                    added++;
                }
            }else{
                numbers = new TreeSet<>();
                numbers.add(number);
                links.put(maskedLink, numbers);
                created = true;
            }
        }
        if(numbers != null && found == 1 && created){ //confirm if one number only in the name and new sequence
            numbers.add(CONFIRMED_MARKER);
            added++;
        }
        return added;
    }
    
    @Override
    public void postProcessing(GoogleTask task){
        if (links.isEmpty()) return;
        var log = new ProgressLog(task.getProgress(), task.getWorkload());
        log.appendLine(String.format(STATUS_LOG_MASK, TITLE_LOG));
        task.reportLog(log);
        
        for (var entry : links.entrySet()) {
            if(task.interrupted()) break; //INTERRUPT EXIT POINT
            final Set<String> numbers = entry.getValue();
            if(!numbers.contains(".")) continue; //not confirmed sequence
            
            //PARSE NUMBERS
            final List<Integer> values = new ArrayList<>();
            boolean padding = false;
            int endZero = 0;
            int maxLenght = 0;
            for (String v : numbers) {
                if('0' == v.charAt(0)) padding = true;
                if('0' == v.charAt(v.length()-1)) endZero++;
                if(v.length() > maxLenght) maxLenght = v.length();
                try{
                    values.add(Integer.parseInt(v));
                }catch(NumberFormatException ex){}
            }
            if(endZero == values.size()) continue; //possibly resolutions
            
            //PREPARE TASK VALUES
            int start = values.get(0)-lowerMargin;
            if(start < 1) start = 1;
            final int end = values.get(values.size()-1)+upperMargin;
            String numberMask = "%d";
            if(padding) numberMask = "%0"+maxLenght+"d";
            String link = String.format(entry.getKey(), numberMask);
            link = String.format(link, start);
            
            //START SUB-TASK
            task.report(Tag.INFO, false, SEQUENCE_START_LOG_MASK, link, start, end); //TODO: start -> end too big
            int downloaded = 0;
            try {
                currentSubtask = new SequentialTask(link, end);
                currentSubtask.excludeNumbers(values); //already have similar to found by google
                currentSubtask.setDestination(task.getDestination());
                currentSubtask.setSafeThreshold(values.get(values.size()-1));
                currentSubtask.run();
                downloaded = currentSubtask.getSuccesses();
                task.addSuccesses(downloaded);
            } catch (BoundsException | IOException ex) {
                Logger.log.print(Level.INFO, "failed starting subtask", ex);
            }
            task.report(Tag.INFO, SEQUENCE_TOTAL_LOG, downloaded);
        }
    }

    @Override
    public void interrupt() {
        if(currentSubtask != null) currentSubtask.interrupt();
    }

    public void setMinDimension(int minDimension) {
        this.minDimension = minDimension;
    }

    public void setLowerMargin(int lowerMargin) {
        this.lowerMargin = lowerMargin;
    }

    public void setUpperMargin(int upperMargin) {
        this.upperMargin = upperMargin;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String getDestination() {
        return destination;
    }
    
}
