package com.maxregner.oneui.porter.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.maxregner.oneui.porter.core.PortingManager;
import com.maxregner.oneui.porter.core.model.RomInfo;
import com.maxregner.oneui.porter.core.porting.RomPorter.PortingOptions;
import com.maxregner.oneui.porter.utils.LogManager;

/**
 * Main GUI window for the MaxRegner OneUI Porter application
 */
public class MainWindow extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JPanel sourcePanel;
    private JPanel targetPanel;
    private JPanel portingPanel;
    private JPanel logPanel;
    
    private JTextField sourceRomPath;
    private JTextField targetRomPath;
    private JButton sourceRomButton;
    private JButton targetRomButton;
    private JButton analyzeSourceButton;
    private JButton analyzeTargetButton;
    private JButton startPortingButton;
    
    private JTextArea sourceRomInfo;
    private JTextArea targetRomInfo;
    private JTextArea logTextArea;
    private JScrollPane logScrollPane;
    
    private JTextField outputDirField;
    private JButton outputDirButton;
    
    private JCheckBox portSystemAppsCheckBox;
    private JCheckBox portVendorBlobsCheckBox;
    private JCheckBox portProductConfigCheckBox;
    private JCheckBox updateBuildPropsCheckBox;
    private JCheckBox preserveDeviceDriversCheckBox;
    private JCheckBox optimizePerformanceCheckBox;
    
    private JProgressBar portingProgressBar;
    private JLabel portingStatusLabel;
    
    private PortingManager portingManager;
    private RomInfo sourceRom;
    private RomInfo targetRom;
    
    public MainWindow(String appName, String appVersion) {
        super(appName + " v" + appVersion);
        
        // Initialize the porting manager
        portingManager = new PortingManager();
        
        // Set up the main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Create the tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create the panels
        createSourcePanel();
        createTargetPanel();
        createPortingPanel();
        createLogPanel();
        
        // Add the panels to the tabbed pane
        tabbedPane.addTab("Source ROM", sourcePanel);
        tabbedPane.addTab("Target ROM", targetPanel);
        tabbedPane.addTab("Porting", portingPanel);
        tabbedPane.addTab("Logs", logPanel);
        
        // Add the tabbed pane to the frame
        add(tabbedPane);
        
        // Set up the menu bar
        createMenuBar();
        
        // Register log listener
        LogManager.addLogListener(message -> {
            SwingUtilities.invokeLater(() -> {
                logTextArea.append(message + "\n");
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            });
        });
        
        // Register porting listener
        portingManager.addPortingListener(new PortingManager.PortingListener() {
            @Override
            public void onRomAnalyzed(RomInfo romInfo) {
                SwingUtilities.invokeLater(() -> {
                    if (romInfo.getFilePath().equals(sourceRomPath.getText())) {
                        sourceRom = romInfo;
                        sourceRomInfo.setText(romInfo.toString());
                    } else if (romInfo.getFilePath().equals(targetRomPath.getText())) {
                        targetRom = romInfo;
                        targetRomInfo.setText(romInfo.toString());
                    }
                });
            }
            
            @Override
            public void onPortingStarted() {
                SwingUtilities.invokeLater(() -> {
                    portingProgressBar.setValue(0);
                    portingStatusLabel.setText("Porting started...");
                    startPortingButton.setEnabled(false);
                });
            }
            
            @Override
            public void onPortingProgress(int progress, String message) {
                SwingUtilities.invokeLater(() -> {
                    portingProgressBar.setValue(progress);
                    portingStatusLabel.setText(message);
                });
            }
            
            @Override
            public void onPortingCompleted(File outputRomFile) {
                SwingUtilities.invokeLater(() -> {
                    portingProgressBar.setValue(100);
                    portingStatusLabel.setText("Porting completed successfully!");
                    startPortingButton.setEnabled(true);
                    
                    JOptionPane.showMessageDialog(
                        MainWindow.this,
                        "Porting completed successfully!\nOutput ROM: " + outputRomFile.getAbsolutePath(),
                        "Porting Completed",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }
            
            @Override
            public void onPortingFailed(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    portingStatusLabel.setText("Porting failed: " + errorMessage);
                    startPortingButton.setEnabled(true);
                    
                    JOptionPane.showMessageDialog(
                        MainWindow.this,
                        "Porting failed: " + errorMessage,
                        "Porting Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        });
        
        LogManager.info("GUI initialized successfully");
    }
    
    private void createSourcePanel() {
        sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // ROM file selection
        JLabel sourceRomLabel = new JLabel("Source ROM File:");
        sourceRomPath = new JTextField(30);
        sourceRomButton = new JButton("Browse...");
        sourceRomButton.addActionListener(e -> browseForRom(sourceRomPath));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(sourceRomLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(sourceRomPath, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(sourceRomButton, gbc);
        
        // ROM info
        sourceRomInfo = new JTextArea(10, 40);
        sourceRomInfo.setEditable(false);
        JScrollPane sourceRomInfoScrollPane = new JScrollPane(sourceRomInfo);
        
        // Analyze button
        analyzeSourceButton = new JButton("Analyze Source ROM");
        analyzeSourceButton.addActionListener(e -> analyzeSourceRom());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(analyzeSourceButton);
        
        sourcePanel.add(inputPanel, BorderLayout.NORTH);
        sourcePanel.add(sourceRomInfoScrollPane, BorderLayout.CENTER);
        sourcePanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createTargetPanel() {
        targetPanel = new JPanel(new BorderLayout());
        targetPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // ROM file selection
        JLabel targetRomLabel = new JLabel("Target ROM File:");
        targetRomPath = new JTextField(30);
        targetRomButton = new JButton("Browse...");
        targetRomButton.addActionListener(e -> browseForRom(targetRomPath));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(targetRomLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(targetRomPath, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(targetRomButton, gbc);
        
        // ROM info
        targetRomInfo = new JTextArea(10, 40);
        targetRomInfo.setEditable(false);
        JScrollPane targetRomInfoScrollPane = new JScrollPane(targetRomInfo);
        
        // Analyze button
        analyzeTargetButton = new JButton("Analyze Target ROM");
        analyzeTargetButton.addActionListener(e -> analyzeTargetRom());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(analyzeTargetButton);
        
        targetPanel.add(inputPanel, BorderLayout.NORTH);
        targetPanel.add(targetRomInfoScrollPane, BorderLayout.CENTER);
        targetPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createPortingPanel() {
        portingPanel = new JPanel(new BorderLayout());
        portingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Options panel
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Porting Options"));
        
        // Add checkboxes for different porting options
        portSystemAppsCheckBox = new JCheckBox("Port System Apps", true);
        portVendorBlobsCheckBox = new JCheckBox("Port Vendor Blobs", true);
        portProductConfigCheckBox = new JCheckBox("Port Product Configuration", true);
        updateBuildPropsCheckBox = new JCheckBox("Update Build Properties", true);
        preserveDeviceDriversCheckBox = new JCheckBox("Preserve Device Drivers", true);
        optimizePerformanceCheckBox = new JCheckBox("Optimize for Performance", false);
        
        optionsPanel.add(portSystemAppsCheckBox);
        optionsPanel.add(portVendorBlobsCheckBox);
        optionsPanel.add(portProductConfigCheckBox);
        optionsPanel.add(updateBuildPropsCheckBox);
        optionsPanel.add(preserveDeviceDriversCheckBox);
        optionsPanel.add(optimizePerformanceCheckBox);
        
        // Output options
        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Options"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel outputDirLabel = new JLabel("Output Directory:");
        outputDirField = new JTextField(30);
        outputDirButton = new JButton("Browse...");
        outputDirButton.addActionListener(e -> browseForOutputDir());
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        outputPanel.add(outputDirLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        outputPanel.add(outputDirField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        outputPanel.add(outputDirButton, gbc);
        
        // Progress panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder("Porting Progress"));
        
        portingProgressBar = new JProgressBar(0, 100);
        portingProgressBar.setStringPainted(true);
        portingStatusLabel = new JLabel("Ready to start porting");
        
        progressPanel.add(portingProgressBar, BorderLayout.CENTER);
        progressPanel.add(portingStatusLabel, BorderLayout.SOUTH);
        
        // Start porting button
        startPortingButton = new JButton("Start Porting Process");
        startPortingButton.addActionListener(e -> startPorting());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(startPortingButton);
        
        // Add all panels to the porting panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(optionsPanel, BorderLayout.NORTH);
        centerPanel.add(outputPanel, BorderLayout.CENTER);
        centerPanel.add(progressPanel, BorderLayout.SOUTH);
        
        portingPanel.add(centerPanel, BorderLayout.CENTER);
        portingPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createLogPanel() {
        logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> logTextArea.setText(""));
        JButton saveButton = new JButton("Save Logs");
        saveButton.addActionListener(e -> saveLogs());
        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem extractItem = new JMenuItem("Extract ROM");
        extractItem.addActionListener(e -> extractRom());
        JMenuItem analyzeItem = new JMenuItem("Analyze ROM");
        analyzeItem.addActionListener(e -> analyzeRom());
        JMenuItem compareItem = new JMenuItem("Compare ROMs");
        compareItem.addActionListener(e -> compareRoms());
        toolsMenu.add(extractItem);
        toolsMenu.add(analyzeItem);
        toolsMenu.add(compareItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        JMenuItem helpItem = new JMenuItem("Help");
        helpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void browseForRom(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select ROM File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
            LogManager.info("Selected ROM file: " + selectedFile.getAbsolutePath());
        }
    }
    
    private void browseForOutputDir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Output Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            outputDirField.setText(selectedDir.getAbsolutePath());
            LogManager.info("Selected output directory: " + selectedDir.getAbsolutePath());
        }
    }
    
    private void analyzeSourceRom() {
        String romPath = sourceRomPath.getText();
        if (romPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a source ROM file first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LogManager.info("Analyzing source ROM: " + romPath);
        
        // Run analysis in a background thread
        new Thread(() -> {
            try {
                RomInfo romInfo = portingManager.analyzeRom(new File(romPath));
                if (romInfo != null) {
                    sourceRom = romInfo;
                    SwingUtilities.invokeLater(() -> {
                        sourceRomInfo.setText(romInfo.toString());
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Failed to analyze source ROM.", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                LogManager.error("Error analyzing source ROM", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error analyzing source ROM: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void analyzeTargetRom() {
        String romPath = targetRomPath.getText();
        if (romPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a target ROM file first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LogManager.info("Analyzing target ROM: " + romPath);
        
        // Run analysis in a background thread
        new Thread(() -> {
            try {
                RomInfo romInfo = portingManager.analyzeRom(new File(romPath));
                if (romInfo != null) {
                    targetRom = romInfo;
                    SwingUtilities.invokeLater(() -> {
                        targetRomInfo.setText(romInfo.toString());
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Failed to analyze target ROM.", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                LogManager.error("Error analyzing target ROM", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error analyzing target ROM: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void startPorting() {
        // Check if source and target ROMs are set
        if (sourceRom == null) {
            JOptionPane.showMessageDialog(this, "Please analyze the source ROM first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (targetRom == null) {
            JOptionPane.showMessageDialog(this, "Please analyze the target ROM first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if output directory is set
        String outputDirPath = outputDirField.getText();
        if (outputDirPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an output directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Set source and target ROMs
        portingManager.setSourceRom(sourceRom);
        portingManager.setTargetRom(targetRom);
        
        // Create porting options
        PortingOptions options = new PortingOptions();
        options.setPortSystemApps(portSystemAppsCheckBox.isSelected());
        options.setPortVendorBlobs(portVendorBlobsCheckBox.isSelected());
        options.setPortProductConfig(portProductConfigCheckBox.isSelected());
        options.setUpdateBuildProps(updateBuildPropsCheckBox.isSelected());
        options.setPreserveDeviceDrivers(preserveDeviceDriversCheckBox.isSelected());
        options.setOptimizePerformance(optimizePerformanceCheckBox.isSelected());
        
        // Start porting in a background thread
        new Thread(() -> {
            try {
                boolean result = portingManager.startPorting(new File(outputDirPath), options);
                if (!result) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Porting failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                LogManager.error("Error during porting process", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error during porting process: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void extractRom() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select ROM File to Extract");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Select output directory
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Select Output Directory");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            result = dirChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputDir = dirChooser.getSelectedFile();
                
                // Run extraction in a background thread
                new Thread(() -> {
                    try {
                        LogManager.info("Extracting ROM: " + selectedFile.getAbsolutePath());
                        LogManager.info("Output directory: " + outputDir.getAbsolutePath());
                        
                        // TODO: Implement ROM extraction
                        
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "ROM extraction not yet implemented.", "Information", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } catch (Exception e) {
                        LogManager.error("Error extracting ROM", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Error extracting ROM: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        }
    }
    
    private void analyzeRom() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select ROM File to Analyze");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Run analysis in a background thread
            new Thread(() -> {
                try {
                    LogManager.info("Analyzing ROM: " + selectedFile.getAbsolutePath());
                    
                    RomInfo romInfo = portingManager.analyzeRom(selectedFile);
                    if (romInfo != null) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, romInfo.toString(), "ROM Analysis", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Failed to analyze ROM.", "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (Exception e) {
                    LogManager.error("Error analyzing ROM", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Error analyzing ROM: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }
    
    private void compareRoms() {
        // TODO: Implement ROM comparison
        JOptionPane.showMessageDialog(this, "ROM comparison not yet implemented.", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void saveLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Logs");
        fileChooser.setSelectedFile(new File("oneui_porter_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                java.io.FileWriter writer = new java.io.FileWriter(selectedFile);
                writer.write(logTextArea.getText());
                writer.close();
                
                JOptionPane.showMessageDialog(this, "Logs saved to: " + selectedFile.getAbsolutePath(), "Logs Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                LogManager.error("Error saving logs", e);
                JOptionPane.showMessageDialog(this, "Error saving logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "MaxRegner OneUI Porter v7.0\n\n" +
            "A tool for porting between Samsung OneUI 7 ROMs\n\n" +
            "Created by MaxRegner\n" +
            "Copyright © 2025 MaxRegner\n\n" +
            "This application is provided as-is without any warranty.",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(
            this,
            "MaxRegner OneUI Porter v7.0 Help\n\n" +
            "1. Select and analyze the source ROM (the ROM you want to port from)\n" +
            "2. Select and analyze the target ROM (the ROM you want to port to)\n" +
            "3. Configure porting options\n" +
            "4. Select an output directory\n" +
            "5. Click 'Start Porting Process'\n\n" +
            "For more information, visit: https://github.com/maxregnerisch/oneui-porter",
            "Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}

