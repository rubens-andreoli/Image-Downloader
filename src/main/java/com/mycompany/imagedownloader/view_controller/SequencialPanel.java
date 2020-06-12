package com.mycompany.imagedownloader.view_controller;

import com.mycompany.imagedownloader.model.SequencialTask;
import com.mycompany.imagedownloader.model.Task;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JOptionPane;

public class SequencialPanel extends BasicTaskPanel {
    private static final long serialVersionUID = 1L;

    public SequencialPanel() {
        super("Sequencial", 50);
    }                               

    @Override
    public Task createTask(String url, String dest, int endNumber) {
        SequencialTask t = null;
        try {
            t = new SequencialTask(url, dest, endNumber);
            clearUrl();
            appendTaskDescription(t.getPath()+" -> "+dest+"\n");
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the link provided is valid.", 
                "Malformed URL", 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the destination folder is valid.", 
                "Invalid Folder", 
                JOptionPane.ERROR_MESSAGE
            );
        } catch (BoundsException ex) {
            JOptionPane.showMessageDialog(
                this, 
                ex.getMessage()+"\nPlease verify if the marked file number and the target set.", 
                "Invalid Numbering Bounds",
                JOptionPane.ERROR_MESSAGE
            );
        }
        return t;
    }
}
