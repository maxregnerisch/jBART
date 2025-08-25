package com.maxregner.oneui.porter.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import com.maxregner.oneui.porter.core.PortingManager;
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
    private JButton analyzeButton;
    private JButton startPortingButton;
    
    private JTextArea logTextArea;
    private JScrollPane logScrollPane;
    
    private PortingManager portingManager;
    
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
        
        // Analyze button
        analyzeButton = new JButton("Analyze Source ROM");
        analyzeButton.addActionListener(e -> analyzeSourceRom());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(analyzeButton);
        
        sourcePanel.add(inputPanel, BorderLayout.NORTH);
        sourcePanel.add(new JPanel(), BorderLayout.CENTER); // Placeholder for ROM info
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
        
        targetPanel.add(inputPanel, BorderLayout.NORTH);
        targetPanel.add(new JPanel(), BorderLayout.CENTER); // Placeholder for ROM info
    }
    
    private void createPortingPanel() {
        portingPanel = new JPanel(new BorderLayout());
        portingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Options panel
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Porting Options"));
        
        // Add checkboxes for different porting options
        optionsPanel.add(new JCheckBox("Port System Apps"));
        optionsPanel.add(new JCheckBox("Port Vendor Blobs"));
        optionsPanel.add(new JCheckBox("Port Product Configuration"));
        optionsPanel.add(new JCheckBox("Update Build Properties"));
        optionsPanel.add(new JCheckBox("Preserve Device Drivers"));
        optionsPanel.add(new JCheckBox("Optimize for Performance"));
        
        // Output options
        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Options"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel outputDirLabel = new JLabel("Output Directory:");
        JTextField outputDirField = new JTextField(30);
        JButton outputDirButton = new JButton("Browse...");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        outputPanel.add(outputDirLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        outputPanel.add(outputDirField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        outputPanel.add(outputDirButton, gbc);
        
        // Start porting button
        startPortingButton = new JButton("Start Porting Process");
        startPortingButton.addActionListener(e -> startPorting());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(startPortingButton);
        
        // Add all panels to the porting panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(optionsPanel, BorderLayout.NORTH);
        centerPanel.add(outputPanel, BorderLayout.SOUTH);
        
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
        buttonPanel.add(clearButton);
        
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
        JMenuItem analyzeItem = new JMenuItem("Analyze ROM");
        JMenuItem compareItem = new JMenuItem("Compare ROMs");
        toolsMenu.add(extractItem);
        toolsMenu.add(analyzeItem);
        toolsMenu.add(compareItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem helpItem = new JMenuItem("Help");
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
    
    private void analyzeSourceRom() {
        String romPath = sourceRomPath.getText();
        if (romPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a source ROM file first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LogManager.info("Analyzing source ROM: " + romPath);
        // TODO: Implement ROM analysis
        // This would be handled by the PortingManager
        
        // For now, just show a message
        JOptionPane.showMessageDialog(this, "ROM analysis not yet implemented.", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void startPorting() {
        String sourceRom = sourceRomPath.getText();
        String targetRom = targetRomPath.getText();
        
        if (sourceRom.isEmpty() || targetRom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both source and target ROM files.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LogManager.info("Starting porting process");
        LogManager.info("Source ROM: " + sourceRom);
        LogManager.info("Target ROM: " + targetRom);
        
        // TODO: Implement the actual porting process
        // This would be handled by the PortingManager
        
        // For now, just show a message
        JOptionPane.showMessageDialog(this, "Porting process not yet implemented.", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}

