package com.maxregner.oneui.porter.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for logging
 */
public class LogManager {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final List<LogListener> listeners = new ArrayList<>();
    private static File logFile;
    private static PrintWriter logWriter;
    
    /**
     * Initialize the log manager
     */
    public static void init() {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }
            
            // Create log file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            logFile = new File(logsDir, "oneui_porter_" + timestamp + ".log");
            logWriter = new PrintWriter(new FileWriter(logFile));
            
            info("Log manager initialized");
            info("Log file: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to initialize log manager: " + e.getMessage());
        }
    }
    
    /**
     * Add a log listener
     * 
     * @param listener The listener to add
     */
    public static void addLogListener(LogListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a log listener
     * 
     * @param listener The listener to remove
     */
    public static void removeLogListener(LogListener listener) {
        listeners.remove(listener);
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
     * Log an error message with an exception
     * 
     * @param message The message to log
     * @param e The exception to log
     */
    public static void error(String message, Throwable e) {
        log("ERROR", message + ": " + e.getMessage());
        e.printStackTrace(System.err);
        
        if (logWriter != null) {
            e.printStackTrace(logWriter);
            logWriter.flush();
        }
    }
    
    /**
     * Log a message with a specific level
     * 
     * @param level The log level
     * @param message The message to log
     */
    private static void log(String level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = timestamp + " [" + level + "] " + message;
        
        // Print to console
        if (level.equals("ERROR")) {
            System.err.println(logMessage);
        } else {
            System.out.println(logMessage);
        }
        
        // Write to log file
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
        
        // Notify listeners
        for (LogListener listener : listeners) {
            listener.onLogMessage(logMessage);
        }
    }
    
    /**
     * Close the log manager
     */
    public static void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
    
    /**
     * Interface for log listeners
     */
    public interface LogListener {
        void onLogMessage(String message);
    }
}

