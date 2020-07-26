package rubensandreoli.imagedownloader;

import javax.swing.SwingUtilities;
import rubensandreoli.imagedownloader.gui.AttackPanel;
import rubensandreoli.imagedownloader.gui.GooglePanel;
import rubensandreoli.imagedownloader.gui.ImageDownloader;
import rubensandreoli.imagedownloader.gui.NukePanel;
import rubensandreoli.imagedownloader.gui.ScraperPanel;
import rubensandreoli.imagedownloader.gui.SequentialPanel;

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
        view.addTaskPanel(new SequentialPanel());
        view.addTaskPanel(new ScraperPanel());
        view.addTaskPanel(new GooglePanel());
        view.addTaskPanel(new AttackPanel());
        view.addTaskPanel(new NukePanel());
        
        SwingUtilities.invokeLater(() -> view.setVisible(true));
    }

}
