package com.maxregner.oneui.porter.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages external tools required by the application
 */
public class ExternalToolManager {
    
    private static final String TOOLS_DIR = "tools";
    
    private Map<String, String> toolPaths = new HashMap<>();
    
    /**
     * Constructor
     */
    public ExternalToolManager() {
        LogManager.info("ExternalToolManager initialized");
        initializeToolsDirectory();
    }
    
    /**
     * Initialize the tools directory
     */
    private void initializeToolsDirectory() {
        File toolsDir = new File(TOOLS_DIR);
        if (!toolsDir.exists()) {
            toolsDir.mkdirs();
            LogManager.info("Created tools directory: " + toolsDir.getAbsolutePath());
        }
    }
    
    /**
     * Check if a tool is available
     * 
     * @param toolName The tool name
     * @return True if the tool is available, false otherwise
     */
    public boolean isToolAvailable(String toolName) {
        if (toolPaths.containsKey(toolName)) {
            return true;
        }
        
        // Check if the tool is in the tools directory
        File toolFile = new File(TOOLS_DIR, getToolFileName(toolName));
        if (toolFile.exists() && toolFile.canExecute()) {
            toolPaths.put(toolName, toolFile.getAbsolutePath());
            return true;
        }
        
        // Check if the tool is in the system PATH
        String systemPath = findToolInPath(toolName);
        if (systemPath != null) {
            toolPaths.put(toolName, systemPath);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the path to a tool
     * 
     * @param toolName The tool name
     * @return The tool path, or null if not available
     */
    public String getToolPath(String toolName) {
        if (isToolAvailable(toolName)) {
            return toolPaths.get(toolName);
        }
        return null;
    }
    
    /**
     * Extract a tool from the resources
     * 
     * @param toolName The tool name
     * @return True if successful, false otherwise
     */
    public boolean extractTool(String toolName) {
        String resourcePath = "/tools/" + getToolFileName(toolName);
        File outputFile = new File(TOOLS_DIR, getToolFileName(toolName));
        
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                LogManager.error("Tool resource not found: " + resourcePath);
                return false;
            }
            
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            outputFile.setExecutable(true);
            
            toolPaths.put(toolName, outputFile.getAbsolutePath());
            LogManager.info("Extracted tool: " + toolName + " to " + outputFile.getAbsolutePath());
            
            return true;
        } catch (IOException e) {
            LogManager.error("Failed to extract tool: " + toolName, e);
            return false;
        }
    }
    
    /**
     * Download a tool from a URL
     * 
     * @param toolName The tool name
     * @param url The download URL
     * @return True if successful, false otherwise
     */
    public boolean downloadTool(String toolName, String url) {
        File outputFile = new File(TOOLS_DIR, getToolFileName(toolName));
        
        try {
            LogManager.info("Downloading tool: " + toolName + " from " + url);
            
            // TODO: Implement tool downloading
            // This would use HTTP client to download the tool
            
            outputFile.setExecutable(true);
            toolPaths.put(toolName, outputFile.getAbsolutePath());
            
            return true;
        } catch (Exception e) {
            LogManager.error("Failed to download tool: " + toolName, e);
            return false;
        }
    }
    
    /**
     * Run a tool with arguments
     * 
     * @param toolName The tool name
     * @param args The arguments
     * @return The process exit code
     * @throws IOException If an error occurs
     * @throws InterruptedException If the process is interrupted
     */
    public int runTool(String toolName, String... args) throws IOException, InterruptedException {
        if (!isToolAvailable(toolName)) {
            throw new IOException("Tool not available: " + toolName);
        }
        
        String toolPath = toolPaths.get(toolName);
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // Build the command
        String[] command = new String[args.length + 1];
        command[0] = toolPath;
        System.arraycopy(args, 0, command, 1, args.length);
        
        processBuilder.command(command);
        processBuilder.redirectErrorStream(true);
        
        LogManager.info("Running tool: " + String.join(" ", command));
        
        Process process = processBuilder.start();
        
        // Read the output
        try (InputStream inputStream = process.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String output = new String(buffer, 0, bytesRead);
                LogManager.info(output);
            }
        }
        
        int exitCode = process.waitFor();
        LogManager.info("Tool exit code: " + exitCode);
        
        return exitCode;
    }
    
    /**
     * Find a tool in the system PATH
     * 
     * @param toolName The tool name
     * @return The tool path, or null if not found
     */
    private String findToolInPath(String toolName) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }
        
        String[] pathDirs = pathEnv.split(File.pathSeparator);
        for (String pathDir : pathDirs) {
            File toolFile = new File(pathDir, getToolFileName(toolName));
            if (toolFile.exists() && toolFile.canExecute()) {
                return toolFile.getAbsolutePath();
            }
        }
        
        return null;
    }
    
    /**
     * Get the tool file name based on the operating system
     * 
     * @param toolName The tool name
     * @return The tool file name
     */
    private String getToolFileName(String toolName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return toolName + ".exe";
        } else {
            return toolName;
        }
    }
}

