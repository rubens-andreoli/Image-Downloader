package com.mycompany.imagedownloader;

import com.mycompany.imagedownloader.view_controller.AttackPanel;
import com.mycompany.imagedownloader.view_controller.GooglePanel;
import com.mycompany.imagedownloader.view_controller.ImageDownloader;
import com.mycompany.imagedownloader.view_controller.ScraperPanel;
import com.mycompany.imagedownloader.view_controller.SequencialPanel;

public class Launcher {
    
    public static void main(String[] args) {
        //<editor-fold defaultstate="collapsed" desc=" Look and Feel ">
	try {
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		//Metal; Nimbus; CDE/Motif; Windows; Windows Classic
                if ("Windows".equals(info.getName())) {
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {}
        //</editor-fold>

        ImageDownloader view = new ImageDownloader();
        view.addTaskPanel(new SequencialPanel());
        view.addTaskPanel(new ScraperPanel());
        view.addTaskPanel(new GooglePanel());
        view.addTaskPanel(new AttackPanel());
	
        java.awt.EventQueue.invokeLater(() -> {
            view.setVisible(true);
        });
    }

}
