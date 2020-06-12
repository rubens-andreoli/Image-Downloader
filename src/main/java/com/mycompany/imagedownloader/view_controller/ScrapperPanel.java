package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.ScrapperTask;
import static com.mycompany.imagedownloader.model.ScrapperTask.DEPTH_LIMIT;
import com.mycompany.imagedownloader.model.Task;

public class ScrapperPanel extends BasicTaskPanel {
    private static final long serialVersionUID = 1L;

    public ScrapperPanel() {
        super("Scrapper", 0);
        ((NumberField)txfNumber).setMaxValue(DEPTH_LIMIT);
    }                               

    @Override
    public Task createTask(String url, String dest, int val) {
        ScrapperTask t = null;
        try{
            t = new ScrapperTask(url, dest, val);
            clearUrl();
            appendTaskDescription(url+" ["+val+"] -> "+dest+"\n");
        }catch(Exception e){ //TODO: proper messages
            System.out.println(e.getMessage());
        }
        return t;
    }
}
