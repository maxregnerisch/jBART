package com.maxregner.oneui.porter;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.maxregner.oneui.porter.gui.MainWindow;
import com.maxregner.oneui.porter.utils.LogManager;

/**
 * Main class for the MaxRegner OneUI Porter application
 */
public class Main {
    
    private static final String APP_NAME = "MaxRegner OneUI Porter";
    private static final String APP_VERSION = "7.1";
    
    /**
     * Main method
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Initialize logging
        LogManager.init();
        LogManager.info("Starting " + APP_NAME + " v" + APP_VERSION);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LogManager.info("Set system look and feel");
        } catch (Exception e) {
            LogManager.error("Failed to set system look and feel", e);
        }
        
        // Create and show the main window
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow(APP_NAME, APP_VERSION);
            mainWindow.setVisible(true);
            LogManager.info("Main window created and shown");
        });
    }
}

