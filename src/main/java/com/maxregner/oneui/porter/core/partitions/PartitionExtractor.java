package com.maxregner.oneui.porter.core.partitions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.maxregner.oneui.porter.utils.LogManager;
import com.maxregner.oneui.porter.utils.ExternalToolManager;

/**
 * Class for extracting partitions from ROM files
 */
public class PartitionExtractor {
    
    private ExternalToolManager toolManager;
    
    /**
     * Constructor
     * 
     * @param toolManager The external tool manager
     */
    public PartitionExtractor(ExternalToolManager toolManager) {
        this.toolManager = toolManager;
        LogManager.info("PartitionExtractor initialized");
    }
    
    /**
     * Extract partitions from an OTA package
     * 
     * @param otaFile The OTA package file
     * @param outputDir The output directory
     * @return List of extracted partition files
     * @throws IOException If an error occurs
     */
    public List<File> extractFromOtaPackage(File otaFile, File outputDir) throws IOException {
        LogManager.info("Extracting partitions from OTA package: " + otaFile.getAbsolutePath());
        
        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Extract system, vendor, and product folders directly
        extractFoldersFromZip(otaFile, outputDir);
        
        // Create placeholder partition files for compatibility
        List<File> partitionFiles = new ArrayList<>();
        for (String partitionName : new String[]{"system", "vendor", "product"}) {
            File partitionDir = new File(outputDir, partitionName);
            if (partitionDir.exists() && partitionDir.isDirectory()) {
                File partitionFile = new File(outputDir, partitionName + ".img");
                // Create an empty file as a placeholder
                partitionFile.createNewFile();
                partitionFiles.add(partitionFile);
            }
        }
        
        return partitionFiles;
    }
    
    /**
     * Extract partitions from a Samsung firmware file
     * 
     * @param firmwareFile The firmware file
     * @param outputDir The output directory
     * @return List of extracted partition files
     * @throws IOException If an error occurs
     */
    public List<File> extractFromSamsungFirmware(File firmwareFile, File outputDir) throws IOException {
        LogManager.info("Extracting partitions from Samsung firmware: " + firmwareFile.getAbsolutePath());
        
        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Extract system, vendor, and product folders directly
        extractFoldersFromTarMd5(firmwareFile, outputDir);
        
        // Create placeholder partition files for compatibility
        List<File> partitionFiles = new ArrayList<>();
        for (String partitionName : new String[]{"system", "vendor", "product"}) {
            File partitionDir = new File(outputDir, partitionName);
            if (partitionDir.exists() && partitionDir.isDirectory()) {
                File partitionFile = new File(outputDir, partitionName + ".img");
                // Create an empty file as a placeholder
                partitionFile.createNewFile();
                partitionFiles.add(partitionFile);
            }
        }
        
        return partitionFiles;
    }
    
    /**
     * Extract system, vendor, and product folders from a ZIP file
     * 
     * @param zipFile The ZIP file
     * @param outputDir The output directory
     * @throws IOException If an error occurs
     */
    private void extractFoldersFromZip(File zipFile, File outputDir) throws IOException {
        LogManager.info("Extracting system, vendor, and product folders from ZIP file: " + zipFile.getAbsolutePath());
        
        try {
            // Check if 7z.exe exists in the current directory
            File sevenZipExe = new File("7z.exe");
            String sevenZipPath;
            
            if (sevenZipExe.exists()) {
                sevenZipPath = sevenZipExe.getAbsolutePath();
                LogManager.info("Using 7z.exe from current directory: " + sevenZipPath);
            } else {
                // Try to find 7z in the system path
                sevenZipPath = "7z";
                LogManager.info("7z.exe not found in current directory, using system 7z");
            }
            
            // Extract system folder
            extractFolderWith7z(sevenZipPath, zipFile, "system", outputDir);
            
            // Extract vendor folder
            extractFolderWith7z(sevenZipPath, zipFile, "vendor", outputDir);
            
            // Extract product folder
            extractFolderWith7z(sevenZipPath, zipFile, "product", outputDir);
            
        } catch (Exception e) {
            throw new IOException("Error extracting folders from ZIP file", e);
        }
    }
    
    /**
     * Extract system, vendor, and product folders from a tar.md5 file
     * 
     * @param tarMd5File The tar.md5 file
     * @param outputDir The output directory
     * @throws IOException If an error occurs
     */
    private void extractFoldersFromTarMd5(File tarMd5File, File outputDir) throws IOException {
        LogManager.info("Extracting system, vendor, and product folders from tar.md5 file: " + tarMd5File.getAbsolutePath());
        
        try {
            // Check if 7z.exe exists in the current directory
            File sevenZipExe = new File("7z.exe");
            String sevenZipPath;
            
            if (sevenZipExe.exists()) {
                sevenZipPath = sevenZipExe.getAbsolutePath();
                LogManager.info("Using 7z.exe from current directory: " + sevenZipPath);
            } else {
                // Try to find 7z in the system path
                sevenZipPath = "7z";
                LogManager.info("7z.exe not found in current directory, using system 7z");
            }
            
            // Extract system folder
            extractFolderWith7z(sevenZipPath, tarMd5File, "system", outputDir);
            
            // Extract vendor folder
            extractFolderWith7z(sevenZipPath, tarMd5File, "vendor", outputDir);
            
            // Extract product folder
            extractFolderWith7z(sevenZipPath, tarMd5File, "product", outputDir);
            
        } catch (Exception e) {
            throw new IOException("Error extracting folders from tar.md5 file", e);
        }
    }
    
    /**
     * Extract a specific folder from an archive using 7z
     * 
     * @param sevenZipPath The path to the 7z executable
     * @param archiveFile The archive file
     * @param folderName The folder name to extract
     * @param outputDir The output directory
     * @throws IOException If an error occurs
     * @throws InterruptedException If the process is interrupted
     */
    private void extractFolderWith7z(String sevenZipPath, File archiveFile, String folderName, File outputDir) throws IOException, InterruptedException {
        LogManager.info("Extracting " + folderName + " folder from archive: " + archiveFile.getAbsolutePath());
        
        // Create the output directory for this folder
        File folderOutputDir = new File(outputDir, folderName);
        if (!folderOutputDir.exists()) {
            folderOutputDir.mkdirs();
        }
        
        // Use 7z command to extract the folder
        ProcessBuilder pb = new ProcessBuilder(
            sevenZipPath, "x", archiveFile.getAbsolutePath(), folderName + "/*", "-o" + outputDir.getAbsolutePath(), "-y"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogManager.info(line);
            }
        }
        
        // Wait for the process to complete
        int exitCode = process.waitFor();
        LogManager.info("7z process exit code for " + folderName + ": " + exitCode);
        
        // Check if the folder was extracted
        if (!folderOutputDir.exists() || folderOutputDir.list().length == 0) {
            LogManager.warning(folderName + " folder not found or empty in the archive");
        } else {
            LogManager.info("Successfully extracted " + folderName + " folder");
        }
    }
    
    /**
     * Find partition files in a directory
     * 
     * @param directory The directory to search
     * @return List of partition files
     */
    private List<File> findPartitionFiles(File directory) {
        LogManager.info("Finding partition files in directory: " + directory.getAbsolutePath());
        
        List<File> partitionFiles = new ArrayList<>();
        
        // List of partition names to look for
        String[] partitionNames = {
            "system", "vendor", "product", "odm", "system_ext", "boot", "recovery", "dtbo", "vbmeta"
        };
        
        // Look for partition files
        for (String partitionName : partitionNames) {
            File partitionFile = new File(directory, partitionName + ".img");
            if (partitionFile.exists()) {
                LogManager.info("Found partition file: " + partitionFile.getAbsolutePath());
                partitionFiles.add(partitionFile);
            }
        }
        
        return partitionFiles;
    }
    
    /**
     * Extract files from a partition image
     * 
     * @param partitionFile The partition image file
     * @param outputDir The output directory
     * @throws IOException If an error occurs
     */
    public void extractFilesFromPartition(File partitionFile, File outputDir) throws IOException {
        LogManager.info("Extracting files from partition: " + partitionFile.getAbsolutePath());
        
        // Create output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // For placeholder partition files, the actual content is already in the directory
        String partitionName = partitionFile.getName().replace(".img", "");
        File partitionDir = new File(partitionFile.getParentFile(), partitionName);
        
        if (partitionDir.exists() && partitionDir.isDirectory()) {
            LogManager.info("Using existing " + partitionName + " directory instead of extracting from image");
            copyDirectory(partitionDir, outputDir);
        } else {
            // Try to extract using 7z for real partition images
            extractUsing7z(partitionFile, outputDir);
        }
    }
    
    /**
     * Extract a partition image using 7z
     * 
     * @param partitionFile The partition image file
     * @param outputDir The output directory
     * @throws IOException If an error occurs
     */
    private void extractUsing7z(File partitionFile, File outputDir) throws IOException {
        LogManager.info("Extracting partition using 7z: " + partitionFile.getAbsolutePath());
        
        try {
            // Check if 7z.exe exists in the current directory
            File sevenZipExe = new File("7z.exe");
            String sevenZipPath;
            
            if (sevenZipExe.exists()) {
                sevenZipPath = sevenZipExe.getAbsolutePath();
                LogManager.info("Using 7z.exe from current directory: " + sevenZipPath);
            } else {
                // Try to find 7z in the system path
                sevenZipPath = "7z";
                LogManager.info("7z.exe not found in current directory, using system 7z");
            }
            
            // Use 7z command to extract the partition
            ProcessBuilder pb = new ProcessBuilder(
                sevenZipPath, "x", partitionFile.getAbsolutePath(), "-o" + outputDir.getAbsolutePath(), "-y"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LogManager.info(line);
                }
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            LogManager.info("7z process exit code: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Failed to extract partition using 7z");
            }
        } catch (InterruptedException e) {
            throw new IOException("7z process interrupted", e);
        }
    }
    
    /**
     * Copy a directory recursively
     * 
     * @param sourceDir The source directory
     * @param targetDir The target directory
     * @throws IOException If an error occurs
     */
    private void copyDirectory(File sourceDir, File targetDir) throws IOException {
        LogManager.info("Copying directory: " + sourceDir.getAbsolutePath() + " to " + targetDir.getAbsolutePath());
        
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        for (File sourceFile : sourceDir.listFiles()) {
            File targetFile = new File(targetDir, sourceFile.getName());
            
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, targetFile);
            } else {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}

