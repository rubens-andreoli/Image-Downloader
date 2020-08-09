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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rubensandreoli.commons.others.Level;
import rubensandreoli.commons.others.Logger;
import rubensandreoli.imagedownloader.tasks.exceptions.BoundsException;
import rubensandreoli.imagedownloader.tasks.support.Downloader;
import rubensandreoli.imagedownloader.tasks.support.ImageInfo;
import rubensandreoli.imagedownloader.tasks.support.TaskJournal;

public class MoreSubtask extends BasicGoogleSubtask{
    
    // <editor-fold defaultstate="collapsed" desc=" STATIC FIELDS ">
    public static final int PRIORITY = LargerSubtask.PRIORITY+1;
    public static final int DEFAULT_LOWER_MARGIN = 20;
    public static final int DEFAULT_UPPER_MARGIN = 200;
    public static final int DEFAULT_MIN_DIMENSION = 400;
    public static final int DEFAULT_SEQUENCE_MAX_LENGHT = 1000;
    private static final int OCURRANCE_CONFIRMATION = 2;
    
    private static final String NUMBER_REGEX = "\\d+";
    private static final String CONFIRMED_MARKER = ".";
    private static final String VALID_IMAGE_REGEX = ".*"+NUMBER_REGEX+".*";
    private static final String SEQUENCE_LINK_MASK = "%s/%s{%%s}%s%s"; //parent; filename start; filename end; extension
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
    
    private static final String TITLE_LOG = "MORE";
    private static final String FOUND_SEQUENCE_LOG_MASK = "Found %d new potential sequence(s)";
    private static final String SEQUENCE_START_LOG_MASK = "Starting sequence %s [%d:%d]"; //url; start; end
    private static final String SEQUENCE_TOTAL_LOG = "%d sequential download(s)";
    // </editor-fold>

    private final Map<String, Set<String>> links = new HashMap<>();
    private int minDimension = DEFAULT_MIN_DIMENSION;
    private int lowerMargin = DEFAULT_LOWER_MARGIN;
    private int upperMargin = DEFAULT_UPPER_MARGIN;
    private int sequenceMaxLenght = DEFAULT_SEQUENCE_MAX_LENGHT;
    
    public MoreSubtask(String subfolder) {
        super(subfolder);
    }
    
    @Override
    public void processing(TaskJournal monitor, Downloader downloader, ImageInfo source, List<ImageInfo> similars)  {
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
        monitor.addWorkload(found);
        monitor.getCurrentLog().appendLine(Level.INFO, FOUND_SEQUENCE_LOG_MASK, found);
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
                numbers = new HashSet<>();
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
    public void postProcessing(TaskJournal journal, Downloader downloader){
        if (links.isEmpty() || journal.isInterrupted()) return;
        journal.reportTitle(TITLE_LOG);
        
        final List<String> more = new ArrayList<>();
        for (var entry : links.entrySet()) {
            if(journal.isInterrupted()) break; //INTERRUPT EXIT POINT
            final Set<String> numbers = entry.getValue();
            if(!numbers.contains(".")) continue; //not confirmed sequence, don't progress here
            
            //PARSE NUMBERS
            final TreeSet<Integer> values = new TreeSet<>();
            boolean padding = false;
            int endZero = 0, maxLenght = 0;
            for (String v : numbers) {
                if('0' == v.charAt(0)) padding = true;
                if('0' == v.charAt(v.length()-1)) endZero++;
                if(v.length() > maxLenght) maxLenght = v.length();
                try{
                    values.add(Integer.parseInt(v));
                }catch(NumberFormatException ex){}
            }
            if(endZero == values.size()){ //possibly resolutions
                journal.increaseProgress();
                continue;
            }
            
            //PREPARE TASK VALUES
            try{ //FIX: temporary solution
            final int start = Math.max(values.first()-lowerMargin, 1); 
            final int end = values.last()+upperMargin;
            String numberMask = "%d";
            if(padding) numberMask = "%0"+maxLenght+"d";
            String link = String.format(entry.getKey(), numberMask); //FIX: exception Conversion = 'L'; Conversion = 'm'
            link = String.format(link, start);
            final int size = end - start;
            if(size > sequenceMaxLenght){
                journal.report(Level.WARNING, "Sequence is to big [%,d values]", size);
                journal.increaseProgress();
                continue;
            }

            //SUB-TASK
            journal.report(Level.INFO, false, SEQUENCE_START_LOG_MASK, link, start, end);
            int downloaded = 0;
            try {
                var subtask = new SequenceTask(link, end);
                subtask.silent(true);
                subtask.excludeNumbers(values); //already have similar to found by google
                subtask.setDestination(subfolder);
                subtask.setSafeThreshold(values.last());
                subtask.run();
                downloaded = subtask.getSuccesses();
                journal.addSuccesses(downloaded);
//                if(downloaded == size) more.add(link); //fully successful
            } catch (BoundsException | IOException ex) {
                journal.report(Level.ERROR, false, "failed starting sequence [%s]", entry.getKey());
            }
            journal.report(Level.INFO, SEQUENCE_TOTAL_LOG, downloaded);
            journal.increaseProgress();
            }catch(Exception ex){
                Logger.log.print(Level.ERROR, String.format("%s %d:%d", entry, values.first(), values.last()), ex);
                journal.increaseProgress();
            }
        }
        
//        if(!more.isEmpty());{
//            journal.reportTitle("ATTENTION");
//            journal.report(Level.INFO, false, "%s may have more values to download:", (more.size()>1? "These links":"This link"));
//            more.forEach(link -> {
//                System.out.println("link: ["+link+"]");
//                journal.report(Level.INFO, false, link);  
//            });
//        }
    }

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setMinDimension(int d) {
        this.minDimension = d;
    }

    public void setLowerMargin(int amount) {
        this.lowerMargin = amount;
    }

    public void setUpperMargin(int amount) {
        this.upperMargin = amount;
    }

    public void setSequenceLimit(int amount) {
        this.sequenceMaxLenght = amount;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    // </editor-fold>

}
