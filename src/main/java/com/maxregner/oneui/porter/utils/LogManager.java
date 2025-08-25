package com.maxregner.oneui.porter.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages logging for the application
 */
public class LogManager {
    
    private static final String LOG_DIRECTORY = "logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    private static PrintWriter logFileWriter;
    private static List<Consumer<String>> logListeners = new ArrayList<>();
    
    /**
     * Initialize the log manager
     */
    public static void init() {
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Create a new log file with timestamp
            String logFileName = LOG_DIRECTORY + File.separator + "oneui_porter_" + 
                                 FILE_DATE_FORMAT.format(new Date()) + ".log";
            
            logFileWriter = new PrintWriter(new FileWriter(logFileName));
            
            // Log initialization message
            log("INFO", "Log initialized: " + logFileName);
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    /**
     * Add a log listener to receive log messages
     * 
     * @param listener The listener to add
     */
    public static void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }
    
    /**
     * Remove a log listener
     * 
     * @param listener The listener to remove
     */
    public static void removeLogListener(Consumer<String> listener) {
        logListeners.remove(listener);
    }
    
    /**
     * Log an info message
     * 
     * @param message The message to log
     */
    public static void info(String message) {
        log("INFO", message);
    }
    
    /**
     * Log a warning message
     * 
     * @param message The message to log
     */
    public static void warning(String message) {
        log("WARNING", message);
    }
    
    /**
     * Log an error message
     * 
     * @param message The message to log
     */
    public static void error(String message) {
        log("ERROR", message);
    }
    
    /**
     * Log an error message with exception details
     * 
     * @param message The message to log
     * @param e The exception
     */
    public static void error(String message, Exception e) {
        log("ERROR", message + ": " + e.getMessage());
        e.printStackTrace(logFileWriter);
        logFileWriter.flush();
    }
    
    /**
     * Log a debug message
     * 
     * @param message The message to log
     */
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    /**
     * Log a message with the specified level
     * 
     * @param level The log level
     * @param message The message to log
     */
    private static void log(String level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = "[" + timestamp + "] " + level + ": " + message;
        
        // Write to console
        System.out.println(logMessage);
        
        // Write to log file
        if (logFileWriter != null) {
            logFileWriter.println(logMessage);
            logFileWriter.flush();
        }
        
        // Notify listeners
        for (Consumer<String> listener : logListeners) {
            listener.accept(logMessage);
        }
    }
    
    /**
     * Close the log file
     */
    public static void close() {
        if (logFileWriter != null) {
            logFileWriter.close();
        }
    }
}

