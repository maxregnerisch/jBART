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
        
        // Extract the payload.bin file from the OTA package
        File payloadFile = extractPayloadFromZip(otaFile, outputDir);
        
        // Extract partitions from the payload.bin file
        return extractFromPayload(payloadFile, outputDir);
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
        
        // Extract the firmware file
        File extractedDir = extractTarMd5(firmwareFile, outputDir);
        
        // Find and return partition files
        return findPartitionFiles(extractedDir);
    }
    
    /**
     * Extract payload.bin from a ZIP file
     * 
     * @param zipFile The ZIP file
     * @param outputDir The output directory
     * @return The extracted payload.bin file
     * @throws IOException If an error occurs
     */
    private File extractPayloadFromZip(File zipFile, File outputDir) throws IOException {
        LogManager.info("Extracting payload.bin from ZIP file: " + zipFile.getAbsolutePath());
        
        File payloadFile = new File(outputDir, "payload.bin");
        
        try {
            // Use unzip command to extract payload.bin
            ProcessBuilder pb = new ProcessBuilder(
                "unzip", "-j", zipFile.getAbsolutePath(), "payload.bin", "-d", outputDir.getAbsolutePath()
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
            LogManager.info("Unzip process exit code: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Failed to extract payload.bin from ZIP file");
            }
            
            if (!payloadFile.exists()) {
                throw new IOException("payload.bin not found in ZIP file");
            }
            
            return payloadFile;
        } catch (InterruptedException e) {
            throw new IOException("Unzip process interrupted", e);
        }
    }
    
    /**
     * Extract partitions from a payload.bin file
     * 
     * @param payloadFile The payload.bin file
     * @param outputDir The output directory
     * @return List of extracted partition files
     * @throws IOException If an error occurs
     */
    private List<File> extractFromPayload(File payloadFile, File outputDir) throws IOException {
        LogManager.info("Extracting partitions from payload.bin: " + payloadFile.getAbsolutePath());
        
        List<File> partitionFiles = new ArrayList<>();
        
        // Check if payload_dumper_go is available
        if (toolManager.isToolAvailable("payload_dumper_go")) {
            // Use payload_dumper_go to extract partitions
            try {
                int exitCode = toolManager.runTool("payload_dumper_go", payloadFile.getAbsolutePath(), "-o", outputDir.getAbsolutePath());
                
                if (exitCode != 0) {
                    throw new IOException("Failed to extract partitions from payload.bin");
                }
            } catch (InterruptedException e) {
                throw new IOException("payload_dumper_go process interrupted", e);
            }
        } else {
            // Use Python payload_dumper if available
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "python3", "-m", "payload_dumper", payloadFile.getAbsolutePath(), outputDir.getAbsolutePath()
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
                LogManager.info("payload_dumper process exit code: " + exitCode);
                
                if (exitCode != 0) {
                    throw new IOException("Failed to extract partitions from payload.bin");
                }
            } catch (InterruptedException e) {
                throw new IOException("payload_dumper process interrupted", e);
            }
        }
        
        // Find and return partition files
        return findPartitionFiles(outputDir);
    }
    
    /**
     * Extract a tar.md5 file
     * 
     * @param tarMd5File The tar.md5 file
     * @param outputDir The output directory
     * @return The directory containing extracted files
     * @throws IOException If an error occurs
     */
    private File extractTarMd5(File tarMd5File, File outputDir) throws IOException {
        LogManager.info("Extracting tar.md5 file: " + tarMd5File.getAbsolutePath());
        
        File extractedDir = new File(outputDir, "extracted");
        if (!extractedDir.exists()) {
            extractedDir.mkdirs();
        }
        
        try {
            // Use tar command to extract the tar.md5 file
            ProcessBuilder pb = new ProcessBuilder(
                "tar", "-xf", tarMd5File.getAbsolutePath(), "-C", extractedDir.getAbsolutePath()
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
            LogManager.info("Tar process exit code: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Failed to extract tar.md5 file");
            }
            
            return extractedDir;
        } catch (InterruptedException e) {
            throw new IOException("Tar process interrupted", e);
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
        
        // Create a temporary mount point
        File mountPoint = new File(outputDir.getParentFile(), "mount_" + partitionFile.getName());
        if (!mountPoint.exists()) {
            mountPoint.mkdirs();
        }
        
        try {
            // Mount the partition
            mountPartition(partitionFile, mountPoint);
            
            // Copy files from mount point to output directory
            copyDirectory(mountPoint, outputDir);
            
            // Unmount the partition
            unmountPartition(mountPoint);
        } finally {
            // Clean up
            mountPoint.delete();
        }
    }
    
    /**
     * Mount a partition image
     * 
     * @param partitionFile The partition image file
     * @param mountPoint The mount point
     * @throws IOException If an error occurs
     */
    private void mountPartition(File partitionFile, File mountPoint) throws IOException {
        LogManager.info("Mounting partition: " + partitionFile.getAbsolutePath() + " to " + mountPoint.getAbsolutePath());
        
        try {
            // Use mount command to mount the partition
            ProcessBuilder pb = new ProcessBuilder(
                "mount", "-o", "loop,ro", partitionFile.getAbsolutePath(), mountPoint.getAbsolutePath()
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
            LogManager.info("Mount process exit code: " + exitCode);
            
            if (exitCode != 0) {
                // Try alternative method using 7z
                LogManager.info("Mount failed, trying to extract using 7z");
                extractUsing7z(partitionFile, mountPoint);
            }
        } catch (InterruptedException e) {
            throw new IOException("Mount process interrupted", e);
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
            // Use 7z command to extract the partition
            ProcessBuilder pb = new ProcessBuilder(
                "7z", "x", partitionFile.getAbsolutePath(), "-o" + outputDir.getAbsolutePath()
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
     * Unmount a partition
     * 
     * @param mountPoint The mount point
     * @throws IOException If an error occurs
     */
    private void unmountPartition(File mountPoint) throws IOException {
        LogManager.info("Unmounting partition: " + mountPoint.getAbsolutePath());
        
        try {
            // Use umount command to unmount the partition
            ProcessBuilder pb = new ProcessBuilder(
                "umount", mountPoint.getAbsolutePath()
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
            
            // Wait for the process to complete with timeout
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroy();
                LogManager.warning("Unmount process timed out, forcing termination");
            }
            
            int exitCode = process.exitValue();
            LogManager.info("Unmount process exit code: " + exitCode);
        } catch (InterruptedException e) {
            throw new IOException("Unmount process interrupted", e);
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

