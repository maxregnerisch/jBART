package com.maxregner.oneui.porter.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to store information about a ROM
 */
public class RomInfo {
    
    /**
     * Enum for ROM types
     */
    public enum RomType {
        OTA_PACKAGE,
        SAMSUNG_FIRMWARE,
        UNKNOWN
    }
    
    private String filePath;
    private RomType romType = RomType.UNKNOWN;
    private String androidVersion;
    private String oneUIVersion;
    private String deviceModel;
    private String buildId;
    private String securityPatchLevel;
    private List<String> availablePartitions = new ArrayList<>();
    private Map<String, String> buildProps = new HashMap<>();
    
    /**
     * Get the file path
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Set the file path
     * 
     * @param filePath The file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Get the ROM type
     * 
     * @return The ROM type
     */
    public RomType getRomType() {
        return romType;
    }
    
    /**
     * Set the ROM type
     * 
     * @param romType The ROM type
     */
    public void setRomType(RomType romType) {
        this.romType = romType;
    }
    
    /**
     * Get the Android version
     * 
     * @return The Android version
     */
    public String getAndroidVersion() {
        return androidVersion;
    }
    
    /**
     * Set the Android version
     * 
     * @param androidVersion The Android version
     */
    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }
    
    /**
     * Get the OneUI version
     * 
     * @return The OneUI version
     */
    public String getOneUIVersion() {
        return oneUIVersion;
    }
    
    /**
     * Set the OneUI version
     * 
     * @param oneUIVersion The OneUI version
     */
    public void setOneUIVersion(String oneUIVersion) {
        this.oneUIVersion = oneUIVersion;
    }
    
    /**
     * Get the device model
     * 
     * @return The device model
     */
    public String getDeviceModel() {
        return deviceModel;
    }
    
    /**
     * Set the device model
     * 
     * @param deviceModel The device model
     */
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
    
    /**
     * Get the build ID
     * 
     * @return The build ID
     */
    public String getBuildId() {
        return buildId;
    }
    
    /**
     * Set the build ID
     * 
     * @param buildId The build ID
     */
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }
    
    /**
     * Get the security patch level
     * 
     * @return The security patch level
     */
    public String getSecurityPatchLevel() {
        return securityPatchLevel;
    }
    
    /**
     * Set the security patch level
     * 
     * @param securityPatchLevel The security patch level
     */
    public void setSecurityPatchLevel(String securityPatchLevel) {
        this.securityPatchLevel = securityPatchLevel;
    }
    
    /**
     * Get the available partitions
     * 
     * @return The available partitions
     */
    public List<String> getAvailablePartitions() {
        return availablePartitions;
    }
    
    /**
     * Add an available partition
     * 
     * @param partition The partition to add
     */
    public void addAvailablePartition(String partition) {
        availablePartitions.add(partition);
    }
    
    /**
     * Get the build properties
     * 
     * @return The build properties
     */
    public Map<String, String> getBuildProps() {
        return buildProps;
    }
    
    /**
     * Add a build property
     * 
     * @param key The property key
     * @param value The property value
     */
    public void addBuildProp(String key, String value) {
        buildProps.put(key, value);
    }
    
    /**
     * Get a build property
     * 
     * @param key The property key
     * @return The property value
     */
    public String getBuildProp(String key) {
        return buildProps.get(key);
    }
    
    /**
     * Get a string representation of the ROM info
     * 
     * @return A string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ROM Info:\n");
        sb.append("  File: ").append(filePath).append("\n");
        sb.append("  Type: ").append(romType).append("\n");
        sb.append("  Android Version: ").append(androidVersion).append("\n");
        sb.append("  OneUI Version: ").append(oneUIVersion).append("\n");
        sb.append("  Device Model: ").append(deviceModel).append("\n");
        sb.append("  Build ID: ").append(buildId).append("\n");
        sb.append("  Security Patch Level: ").append(securityPatchLevel).append("\n");
        sb.append("  Available Partitions: ").append(availablePartitions).append("\n");
        return sb.toString();
    }
}

