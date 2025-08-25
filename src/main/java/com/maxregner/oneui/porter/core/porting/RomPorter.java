package com.maxregner.oneui.porter.core.porting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.maxregner.oneui.porter.core.model.RomInfo;
import com.maxregner.oneui.porter.utils.LogManager;

/**
 * Class for porting between ROMs
 */
public class RomPorter {
    
    // List of directories to port from source to target
    private static final String[] PORT_SYSTEM_DIRS = {
        "app", "priv-app", "framework", "etc/permissions", "etc/sysconfig"
    };
    
    // List of directories to port from source to target in vendor
    private static final String[] PORT_VENDOR_DIRS = {
        "app", "etc/permissions", "etc/sysconfig", "overlay"
    };
    
    // List of directories to port from source to target in product
    private static final String[] PORT_PRODUCT_DIRS = {
        "app", "priv-app", "overlay", "etc/permissions", "etc/sysconfig"
    };
    
    // List of files to exclude from porting
    private static final String[] EXCLUDE_FILES = {
        "build.prop", "default.prop", "vendor_build.prop", "product_build.prop"
    };
    
    // List of device-specific directories to preserve
    private static final String[] DEVICE_SPECIFIC_DIRS = {
        "vendor/firmware", "vendor/lib", "vendor/lib64", "vendor/bin", 
        "system/lib", "system/lib64", "system/bin"
    };
    
    private RomInfo sourceRom;
    private RomInfo targetRom;
    private File sourceDir;
    private File targetDir;
    private File outputDir;
    private PortingOptions options;
    private List<PortingListener> listeners = new ArrayList<>();
    
    /**
     * Constructor
     * 
     * @param sourceRom The source ROM information
     * @param targetRom The target ROM information
     * @param sourceDir The source ROM directory
     * @param targetDir The target ROM directory
     * @param outputDir The output directory
     * @param options The porting options
     */
    public RomPorter(RomInfo sourceRom, RomInfo targetRom, File sourceDir, File targetDir, File outputDir, PortingOptions options) {
        this.sourceRom = sourceRom;
        this.targetRom = targetRom;
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.outputDir = outputDir;
        this.options = options;
        
        LogManager.info("RomPorter initialized");
        LogManager.info("Source ROM: " + sourceRom.getFilePath());
        LogManager.info("Target ROM: " + targetRom.getFilePath());
        LogManager.info("Source directory: " + sourceDir.getAbsolutePath());
        LogManager.info("Target directory: " + targetDir.getAbsolutePath());
        LogManager.info("Output directory: " + outputDir.getAbsolutePath());
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
     * Start the porting process
     * 
     * @return True if successful, false otherwise
     */
    public boolean startPorting() {
        LogManager.info("Starting porting process");
        
        try {
            // Notify listeners that porting has started
            for (PortingListener listener : listeners) {
                listener.onPortingStarted();
            }
            
            // Create output directory if it doesn't exist
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Copy target ROM to output directory as base
            copyDirectory(targetDir, outputDir);
            updateProgress(10, "Copied target ROM to output directory");
            
            // Port system apps if enabled
            if (options.isPortSystemApps()) {
                portSystemApps();
                updateProgress(30, "Ported system apps");
            }
            
            // Port vendor blobs if enabled
            if (options.isPortVendorBlobs()) {
                portVendorBlobs();
                updateProgress(50, "Ported vendor blobs");
            }
            
            // Port product configuration if enabled
            if (options.isPortProductConfig()) {
                portProductConfig();
                updateProgress(70, "Ported product configuration");
            }
            
            // Update build properties if enabled
            if (options.isUpdateBuildProps()) {
                updateBuildProps();
                updateProgress(90, "Updated build properties");
            }
            
            // Preserve device drivers if enabled
            if (options.isPreserveDeviceDrivers()) {
                preserveDeviceDrivers();
                updateProgress(95, "Preserved device drivers");
            }
            
            // Optimize for performance if enabled
            if (options.isOptimizePerformance()) {
                optimizePerformance();
                updateProgress(98, "Optimized for performance");
            }
            
            updateProgress(100, "Porting completed successfully");
            
            // Notify listeners that porting has completed
            for (PortingListener listener : listeners) {
                listener.onPortingCompleted();
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
     * Port system apps from source to target
     * 
     * @throws IOException If an error occurs
     */
    private void portSystemApps() throws IOException {
        LogManager.info("Porting system apps");
        
        File sourceSystemDir = new File(sourceDir, "system");
        File outputSystemDir = new File(outputDir, "system");
        
        // Port system directories
        for (String dirName : PORT_SYSTEM_DIRS) {
            File sourceSubDir = new File(sourceSystemDir, dirName);
            File outputSubDir = new File(outputSystemDir, dirName);
            
            if (sourceSubDir.exists() && sourceSubDir.isDirectory()) {
                LogManager.info("Porting system directory: " + dirName);
                copyDirectory(sourceSubDir, outputSubDir);
            }
        }
    }
    
    /**
     * Port vendor blobs from source to target
     * 
     * @throws IOException If an error occurs
     */
    private void portVendorBlobs() throws IOException {
        LogManager.info("Porting vendor blobs");
        
        File sourceVendorDir = new File(sourceDir, "vendor");
        File outputVendorDir = new File(outputDir, "vendor");
        
        // Port vendor directories
        for (String dirName : PORT_VENDOR_DIRS) {
            File sourceSubDir = new File(sourceVendorDir, dirName);
            File outputSubDir = new File(outputVendorDir, dirName);
            
            if (sourceSubDir.exists() && sourceSubDir.isDirectory()) {
                LogManager.info("Porting vendor directory: " + dirName);
                copyDirectory(sourceSubDir, outputSubDir);
            }
        }
    }
    
    /**
     * Port product configuration from source to target
     * 
     * @throws IOException If an error occurs
     */
    private void portProductConfig() throws IOException {
        LogManager.info("Porting product configuration");
        
        File sourceProductDir = new File(sourceDir, "product");
        File outputProductDir = new File(outputDir, "product");
        
        // Port product directories
        for (String dirName : PORT_PRODUCT_DIRS) {
            File sourceSubDir = new File(sourceProductDir, dirName);
            File outputSubDir = new File(outputProductDir, dirName);
            
            if (sourceSubDir.exists() && sourceSubDir.isDirectory()) {
                LogManager.info("Porting product directory: " + dirName);
                copyDirectory(sourceSubDir, outputSubDir);
            }
        }
    }
    
    /**
     * Update build properties
     * 
     * @throws IOException If an error occurs
     */
    private void updateBuildProps() throws IOException {
        LogManager.info("Updating build properties");
        
        // Update system build.prop
        updateBuildProp(
            new File(sourceDir, "system/build.prop"),
            new File(targetDir, "system/build.prop"),
            new File(outputDir, "system/build.prop")
        );
        
        // Update vendor build.prop
        updateBuildProp(
            new File(sourceDir, "vendor/build.prop"),
            new File(targetDir, "vendor/build.prop"),
            new File(outputDir, "vendor/build.prop")
        );
        
        // Update product build.prop
        updateBuildProp(
            new File(sourceDir, "product/build.prop"),
            new File(targetDir, "product/build.prop"),
            new File(outputDir, "product/build.prop")
        );
    }
    
    /**
     * Update a build.prop file
     * 
     * @param sourcePropFile The source build.prop file
     * @param targetPropFile The target build.prop file
     * @param outputPropFile The output build.prop file
     * @throws IOException If an error occurs
     */
    private void updateBuildProp(File sourcePropFile, File targetPropFile, File outputPropFile) throws IOException {
        if (!sourcePropFile.exists() || !targetPropFile.exists()) {
            return;
        }
        
        LogManager.info("Updating build.prop: " + outputPropFile.getAbsolutePath());
        
        // Load properties
        Properties sourceProps = loadProperties(sourcePropFile);
        Properties targetProps = loadProperties(targetPropFile);
        
        // Properties to copy from source to target
        String[] propsToCopy = {
            "ro.build.version.release",
            "ro.build.version.sdk",
            "ro.build.version.security_patch",
            "ro.build.description",
            "ro.build.display.id",
            "ro.build.id",
            "ro.build.version.incremental",
            "ro.product.first_api_level",
            "ro.config.notification_sound",
            "ro.config.alarm_alert",
            "ro.config.ringtone"
        };
        
        // Properties to preserve from target
        String[] propsToPreserve = {
            "ro.product.model",
            "ro.product.brand",
            "ro.product.name",
            "ro.product.device",
            "ro.product.board",
            "ro.product.manufacturer",
            "ro.bootimage.build.fingerprint",
            "ro.build.fingerprint",
            "ro.build.product",
            "ro.product.system.device",
            "ro.product.system.model",
            "ro.product.system.name"
        };
        
        // Copy properties from source to target
        for (String prop : propsToCopy) {
            if (sourceProps.containsKey(prop)) {
                targetProps.setProperty(prop, sourceProps.getProperty(prop));
            }
        }
        
        // Save properties
        saveProperties(targetProps, outputPropFile);
    }
    
    /**
     * Load properties from a file
     * 
     * @param file The properties file
     * @return The properties
     * @throws IOException If an error occurs
     */
    private Properties loadProperties(File file) throws IOException {
        Properties props = new Properties();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            props.load(reader);
        }
        
        return props;
    }
    
    /**
     * Save properties to a file
     * 
     * @param props The properties
     * @param file The properties file
     * @throws IOException If an error occurs
     */
    private void saveProperties(Properties props, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            props.store(writer, "Updated by MaxRegner OneUI Porter");
        }
    }
    
    /**
     * Preserve device drivers
     * 
     * @throws IOException If an error occurs
     */
    private void preserveDeviceDrivers() throws IOException {
        LogManager.info("Preserving device drivers");
        
        // Preserve device-specific directories
        for (String dirName : DEVICE_SPECIFIC_DIRS) {
            File targetSubDir = new File(targetDir, dirName);
            File outputSubDir = new File(outputDir, dirName);
            
            if (targetSubDir.exists() && targetSubDir.isDirectory()) {
                LogManager.info("Preserving device-specific directory: " + dirName);
                copyDirectory(targetSubDir, outputSubDir);
            }
        }
    }
    
    /**
     * Optimize for performance
     * 
     * @throws IOException If an error occurs
     */
    private void optimizePerformance() throws IOException {
        LogManager.info("Optimizing for performance");
        
        // TODO: Implement performance optimizations
        // This could include:
        // - Removing unnecessary apps
        // - Optimizing system settings
        // - Applying performance tweaks
    }
    
    /**
     * Copy a directory recursively
     * 
     * @param sourceDir The source directory
     * @param targetDir The target directory
     * @throws IOException If an error occurs
     */
    private void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return;
        }
        
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        for (File sourceFile : sourceDir.listFiles()) {
            // Skip excluded files
            if (isExcludedFile(sourceFile)) {
                continue;
            }
            
            File targetFile = new File(targetDir, sourceFile.getName());
            
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, targetFile);
            } else {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    /**
     * Check if a file should be excluded from porting
     * 
     * @param file The file to check
     * @return True if the file should be excluded, false otherwise
     */
    private boolean isExcludedFile(File file) {
        for (String excludeFile : EXCLUDE_FILES) {
            if (file.getName().equals(excludeFile)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Update the porting progress
     * 
     * @param progress The progress percentage
     * @param message The progress message
     */
    private void updateProgress(int progress, String message) {
        LogManager.info("Porting progress: " + progress + "% - " + message);
        
        // Notify listeners
        for (PortingListener listener : listeners) {
            listener.onPortingProgress(progress, message);
        }
    }
    
    /**
     * Interface for porting listeners
     */
    public interface PortingListener {
        void onPortingStarted();
        void onPortingProgress(int progress, String message);
        void onPortingCompleted();
        void onPortingFailed(String errorMessage);
    }
    
    /**
     * Class for porting options
     */
    public static class PortingOptions {
        private boolean portSystemApps = true;
        private boolean portVendorBlobs = true;
        private boolean portProductConfig = true;
        private boolean updateBuildProps = true;
        private boolean preserveDeviceDrivers = true;
        private boolean optimizePerformance = false;
        
        public boolean isPortSystemApps() {
            return portSystemApps;
        }
        
        public void setPortSystemApps(boolean portSystemApps) {
            this.portSystemApps = portSystemApps;
        }
        
        public boolean isPortVendorBlobs() {
            return portVendorBlobs;
        }
        
        public void setPortVendorBlobs(boolean portVendorBlobs) {
            this.portVendorBlobs = portVendorBlobs;
        }
        
        public boolean isPortProductConfig() {
            return portProductConfig;
        }
        
        public void setPortProductConfig(boolean portProductConfig) {
            this.portProductConfig = portProductConfig;
        }
        
        public boolean isUpdateBuildProps() {
            return updateBuildProps;
        }
        
        public void setUpdateBuildProps(boolean updateBuildProps) {
            this.updateBuildProps = updateBuildProps;
        }
        
        public boolean isPreserveDeviceDrivers() {
            return preserveDeviceDrivers;
        }
        
        public void setPreserveDeviceDrivers(boolean preserveDeviceDrivers) {
            this.preserveDeviceDrivers = preserveDeviceDrivers;
        }
        
        public boolean isOptimizePerformance() {
            return optimizePerformance;
        }
        
        public void setOptimizePerformance(boolean optimizePerformance) {
            this.optimizePerformance = optimizePerformance;
        }
    }
}

