package com.maxregner.oneui.porter.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.maxregner.oneui.porter.core.model.RomInfo;
import com.maxregner.oneui.porter.core.partitions.PartitionExtractor;
import com.maxregner.oneui.porter.core.partitions.PartitionManager;
import com.maxregner.oneui.porter.core.porting.RomPorter;
import com.maxregner.oneui.porter.core.porting.RomPorter.PortingOptions;
import com.maxregner.oneui.porter.utils.ExternalToolManager;
import com.maxregner.oneui.porter.utils.LogManager;

/**
 * Main manager class for the porting process
 */
public class PortingManager {
    
    private PartitionManager partitionManager;
    private PartitionExtractor partitionExtractor;
    private ExternalToolManager toolManager;
    private RomInfo sourceRomInfo;
    private RomInfo targetRomInfo;
    private List<PortingListener> listeners;
    
    /**
     * Constructor
     */
    public PortingManager() {
        toolManager = new ExternalToolManager();
        partitionManager = new PartitionManager();
        partitionExtractor = new PartitionExtractor(toolManager);
        listeners = new ArrayList<>();
        LogManager.info("PortingManager initialized");
    }
    
    /**
     * Add a porting listener
     * 
     * @param listener The listener to add
     */
    public void addPortingListener(PortingListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a porting listener
     * 
     * @param listener The listener to remove
     */
    public void removePortingListener(PortingListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Analyze a ROM file and extract information
     * 
     * @param romFile The ROM file to analyze
     * @return Information about the ROM
     */
    public RomInfo analyzeRom(File romFile) {
        LogManager.info("Analyzing ROM: " + romFile.getAbsolutePath());
        
        // Check if the file exists
        if (!romFile.exists()) {
            LogManager.error("ROM file does not exist: " + romFile.getAbsolutePath());
            return null;
        }
        
        // Check the file extension
        String fileName = romFile.getName().toLowerCase();
        RomInfo romInfo = new RomInfo();
        romInfo.setFilePath(romFile.getAbsolutePath());
        
        try {
            if (fileName.endsWith(".zip")) {
                // Handle ZIP file (OTA package)
                analyzeZipRom(romFile, romInfo);
            } else if (fileName.endsWith(".tar") || fileName.endsWith(".tar.md5")) {
                // Handle TAR file (Samsung firmware)
                analyzeTarRom(romFile, romInfo);
            } else {
                LogManager.error("Unsupported ROM file format: " + fileName);
                return null;
            }
            
            // Notify listeners
            for (PortingListener listener : listeners) {
                listener.onRomAnalyzed(romInfo);
            }
            
            return romInfo;
        } catch (Exception e) {
            LogManager.error("Error analyzing ROM", e);
            return null;
        }
    }
    
    /**
     * Set the source ROM
     * 
     * @param romInfo The source ROM information
     */
    public void setSourceRom(RomInfo romInfo) {
        this.sourceRomInfo = romInfo;
        LogManager.info("Source ROM set: " + romInfo.getFilePath());
    }
    
    /**
     * Set the target ROM
     * 
     * @param romInfo The target ROM information
     */
    public void setTargetRom(RomInfo romInfo) {
        this.targetRomInfo = romInfo;
        LogManager.info("Target ROM set: " + romInfo.getFilePath());
    }
    
    /**
     * Start the porting process
     * 
     * @param outputDir The output directory
     * @param options Porting options
     * @return True if successful, false otherwise
     */
    public boolean startPorting(File outputDir, PortingOptions options) {
        LogManager.info("Starting porting process");
        
        // Check if source and target ROMs are set
        if (sourceRomInfo == null || targetRomInfo == null) {
            LogManager.error("Source or target ROM not set");
            return false;
        }
        
        // Check if output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        try {
            // Notify listeners that porting has started
            for (PortingListener listener : listeners) {
                listener.onPortingStarted();
            }
            
            // Extract ROMs
            LogManager.info("Extracting source ROM");
            File sourceExtractDir = new File(outputDir, "source_rom");
            extractRom(sourceRomInfo, sourceExtractDir);
            
            LogManager.info("Extracting target ROM");
            File targetExtractDir = new File(outputDir, "target_rom");
            extractRom(targetRomInfo, targetExtractDir);
            
            // Perform porting operations
            LogManager.info("Performing porting operations");
            RomPorter romPorter = new RomPorter(
                sourceRomInfo, targetRomInfo, sourceExtractDir, targetExtractDir, 
                new File(outputDir, "output_rom"), options
            );
            
            // Add porting listener
            romPorter.addPortingListener(new RomPorter.PortingListener() {
                @Override
                public void onPortingStarted() {
                    LogManager.info("ROM porting started");
                }
                
                @Override
                public void onPortingProgress(int progress, String message) {
                    LogManager.info("ROM porting progress: " + progress + "% - " + message);
                    
                    // Notify listeners
                    for (PortingListener listener : listeners) {
                        listener.onPortingProgress(progress, message);
                    }
                }
                
                @Override
                public void onPortingCompleted() {
                    LogManager.info("ROM porting completed");
                }
                
                @Override
                public void onPortingFailed(String errorMessage) {
                    LogManager.error("ROM porting failed: " + errorMessage);
                }
            });
            
            // Start porting
            boolean portingResult = romPorter.startPorting();
            
            if (!portingResult) {
                LogManager.error("Porting failed");
                
                // Notify listeners that porting has failed
                for (PortingListener listener : listeners) {
                    listener.onPortingFailed("Porting failed");
                }
                
                return false;
            }
            
            // Create output ROM
            LogManager.info("Creating output ROM");
            File outputRomFile = createOutputRom(outputDir);
            
            // Notify listeners that porting has completed
            for (PortingListener listener : listeners) {
                listener.onPortingCompleted(outputRomFile);
            }
            
            return true;
        } catch (Exception e) {
            LogManager.error("Error during porting process", e);
            
            // Notify listeners that porting has failed
            for (PortingListener listener : listeners) {
                listener.onPortingFailed(e.getMessage());
            }
            
            return false;
        }
    }
    
    /**
     * Analyze a ZIP ROM file
     * 
     * @param romFile The ROM file
     * @param romInfo The ROM information to populate
     */
    private void analyzeZipRom(File romFile, RomInfo romInfo) {
        LogManager.info("Analyzing ZIP ROM file");
        
        // Set ROM type
        romInfo.setRomType(RomInfo.RomType.OTA_PACKAGE);
        
        try {
            // Extract build.prop from the ZIP file to get ROM information
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "oneui_porter_temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // Use unzip command to extract build.prop
            ProcessBuilder pb = new ProcessBuilder(
                "unzip", "-j", romFile.getAbsolutePath(), "system/build.prop", "-d", tempDir.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Check if build.prop was extracted
            File buildPropFile = new File(tempDir, "build.prop");
            if (buildPropFile.exists()) {
                // Parse build.prop to get ROM information
                parseBuildProp(buildPropFile, romInfo);
            } else {
                // Set default values
                romInfo.setAndroidVersion("15");
                romInfo.setOneUIVersion("7.0");
            }
            
            // Clean up
            buildPropFile.delete();
            tempDir.delete();
        } catch (Exception e) {
            LogManager.error("Error analyzing ZIP ROM file", e);
            
            // Set default values
            romInfo.setAndroidVersion("15");
            romInfo.setOneUIVersion("7.0");
        }
    }
    
    /**
     * Analyze a TAR ROM file
     * 
     * @param romFile The ROM file
     * @param romInfo The ROM information to populate
     */
    private void analyzeTarRom(File romFile, RomInfo romInfo) {
        LogManager.info("Analyzing TAR ROM file");
        
        // Set ROM type
        romInfo.setRomType(RomInfo.RomType.SAMSUNG_FIRMWARE);
        
        try {
            // Extract build.prop from the TAR file to get ROM information
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "oneui_porter_temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // Use tar command to extract build.prop
            ProcessBuilder pb = new ProcessBuilder(
                "tar", "-xf", romFile.getAbsolutePath(), "-C", tempDir.getAbsolutePath(), "system/build.prop"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Check if build.prop was extracted
            File buildPropFile = new File(tempDir, "system/build.prop");
            if (buildPropFile.exists()) {
                // Parse build.prop to get ROM information
                parseBuildProp(buildPropFile, romInfo);
            } else {
                // Set default values
                romInfo.setAndroidVersion("15");
                romInfo.setOneUIVersion("7.0");
            }
            
            // Clean up
            buildPropFile.delete();
            new File(tempDir, "system").delete();
            tempDir.delete();
        } catch (Exception e) {
            LogManager.error("Error analyzing TAR ROM file", e);
            
            // Set default values
            romInfo.setAndroidVersion("15");
            romInfo.setOneUIVersion("7.0");
        }
    }
    
    /**
     * Parse a build.prop file to get ROM information
     * 
     * @param buildPropFile The build.prop file
     * @param romInfo The ROM information to populate
     */
    private void parseBuildProp(File buildPropFile, RomInfo romInfo) {
        LogManager.info("Parsing build.prop: " + buildPropFile.getAbsolutePath());
        
        try {
            // Load build.prop
            java.util.Properties props = new java.util.Properties();
            props.load(new java.io.FileReader(buildPropFile));
            
            // Get ROM information
            String androidVersion = props.getProperty("ro.build.version.release");
            String securityPatch = props.getProperty("ro.build.version.security_patch");
            String buildId = props.getProperty("ro.build.id");
            String deviceModel = props.getProperty("ro.product.model");
            
            // Set ROM information
            if (androidVersion != null) {
                romInfo.setAndroidVersion(androidVersion);
            } else {
                romInfo.setAndroidVersion("15");
            }
            
            // Try to determine OneUI version
            String oneUIVersion = determineOneUIVersion(props);
            romInfo.setOneUIVersion(oneUIVersion);
            
            if (securityPatch != null) {
                romInfo.setSecurityPatchLevel(securityPatch);
            }
            
            if (buildId != null) {
                romInfo.setBuildId(buildId);
            }
            
            if (deviceModel != null) {
                romInfo.setDeviceModel(deviceModel);
            }
            
            // Add build properties
            for (String propName : props.stringPropertyNames()) {
                romInfo.addBuildProp(propName, props.getProperty(propName));
            }
        } catch (Exception e) {
            LogManager.error("Error parsing build.prop", e);
        }
    }
    
    /**
     * Determine OneUI version from build properties
     * 
     * @param props The build properties
     * @return The OneUI version
     */
    private String determineOneUIVersion(java.util.Properties props) {
        // Try to find OneUI version from build properties
        String buildDisplay = props.getProperty("ro.build.display.id");
        if (buildDisplay != null && buildDisplay.contains("OneUI")) {
            // Extract OneUI version from build display
            int index = buildDisplay.indexOf("OneUI");
            if (index >= 0 && index + 5 < buildDisplay.length()) {
                String version = buildDisplay.substring(index + 5).trim();
                if (!version.isEmpty()) {
                    return version;
                }
            }
        }
        
        // Try to determine OneUI version from Android version
        String androidVersion = props.getProperty("ro.build.version.release");
        if (androidVersion != null) {
            switch (androidVersion) {
                case "15":
                    return "7.0";
                case "14":
                    return "6.0";
                case "13":
                    return "5.0";
                case "12":
                    return "4.0";
                case "11":
                    return "3.0";
                case "10":
                    return "2.0";
                case "9":
                    return "1.0";
                default:
                    return "7.0"; // Default to OneUI 7.0 for unknown Android versions
            }
        }
        
        // Default to OneUI 7.0
        return "7.0";
    }
    
    /**
     * Extract a ROM file
     * 
     * @param romInfo The ROM information
     * @param extractDir The directory to extract to
     * @throws IOException If an error occurs
     */
    private void extractRom(RomInfo romInfo, File extractDir) throws IOException {
        LogManager.info("Extracting ROM: " + romInfo.getFilePath());
        
        // Create the extraction directory
        if (!extractDir.exists()) {
            extractDir.mkdirs();
        }
        
        // Extract based on ROM type
        if (romInfo.getRomType() == RomInfo.RomType.OTA_PACKAGE) {
            extractOtaPackage(romInfo, extractDir);
        } else if (romInfo.getRomType() == RomInfo.RomType.SAMSUNG_FIRMWARE) {
            extractSamsungFirmware(romInfo, extractDir);
        }
    }
    
    /**
     * Extract an OTA package
     * 
     * @param romInfo The ROM information
     * @param extractDir The directory to extract to
     * @throws IOException If an error occurs
     */
    private void extractOtaPackage(RomInfo romInfo, File extractDir) throws IOException {
        LogManager.info("Extracting OTA package");
        
        File romFile = new File(romInfo.getFilePath());
        
        // Extract partitions from OTA package
        List<File> partitionFiles = partitionExtractor.extractFromOtaPackage(romFile, extractDir);
        
        // Extract files from partitions
        for (File partitionFile : partitionFiles) {
            String partitionName = partitionFile.getName().replace(".img", "");
            File partitionDir = new File(extractDir, partitionName);
            
            LogManager.info("Extracting files from partition: " + partitionName);
            partitionExtractor.extractFilesFromPartition(partitionFile, partitionDir);
        }
    }
    
    /**
     * Extract a Samsung firmware
     * 
     * @param romInfo The ROM information
     * @param extractDir The directory to extract to
     * @throws IOException If an error occurs
     */
    private void extractSamsungFirmware(RomInfo romInfo, File extractDir) throws IOException {
        LogManager.info("Extracting Samsung firmware");
        
        File romFile = new File(romInfo.getFilePath());
        
        // Extract partitions from Samsung firmware
        List<File> partitionFiles = partitionExtractor.extractFromSamsungFirmware(romFile, extractDir);
        
        // Extract files from partitions
        for (File partitionFile : partitionFiles) {
            String partitionName = partitionFile.getName().replace(".img", "");
            File partitionDir = new File(extractDir, partitionName);
            
            LogManager.info("Extracting files from partition: " + partitionName);
            partitionExtractor.extractFilesFromPartition(partitionFile, partitionDir);
        }
    }
    
    /**
     * Create the output ROM file
     * 
     * @param outputDir The output directory
     * @return The output ROM file
     * @throws IOException If an error occurs
     */
    private File createOutputRom(File outputDir) throws IOException {
        LogManager.info("Creating output ROM file");
        
        File outputRomDir = new File(outputDir, "output_rom");
        File outputRomFile = new File(outputDir, "ported_rom.zip");
        
        try {
            // Create a ZIP file from the output ROM directory
            ProcessBuilder pb = new ProcessBuilder(
                "zip", "-r", outputRomFile.getAbsolutePath(), "."
            );
            pb.directory(outputRomDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            LogManager.info("Zip process exit code: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Failed to create output ROM file");
            }
            
            return outputRomFile;
        } catch (InterruptedException e) {
            throw new IOException("Zip process interrupted", e);
        }
    }
    
    /**
     * Interface for porting listeners
     */
    public interface PortingListener {
        void onRomAnalyzed(RomInfo romInfo);
        void onPortingStarted();
        void onPortingProgress(int progress, String message);
        void onPortingCompleted(File outputRomFile);
        void onPortingFailed(String errorMessage);
    }
}

