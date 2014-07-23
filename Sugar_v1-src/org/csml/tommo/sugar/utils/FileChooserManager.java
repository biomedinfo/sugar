package org.csml.tommo.sugar.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.SugarMenuBar;
import org.csml.tommo.sugar.dialogs.FileOptionsPanel;
import org.csml.tommo.sugar.dialogs.SaveClearedFileOptionsPanel;
import org.csml.tommo.sugar.dialogs.SaveHTMLReportOptionsPanel;
import org.csml.tommo.sugar.filefilters.JSONFileFilter;

import uk.ac.babraham.FastQC.Analysis.AnalysisQueue;
import uk.ac.babraham.FastQC.FileFilters.BAMFileFilter;
import uk.ac.babraham.FastQC.FileFilters.CasavaFastQFileFilter;
import uk.ac.babraham.FastQC.FileFilters.FastQFileFilter;
import uk.ac.babraham.FastQC.FileFilters.MappedBAMFileFilter;
import uk.ac.babraham.FastQC.FileFilters.SequenceFileFilter;

public class FileChooserManager {

	private static FileChooserManager INSTANCE;
	
	private static File lastUsedDir = null;

	private FileChooserManager(){
		super();
	}
	
	public static FileChooserManager getInstance(){
		if(INSTANCE == null){
			INSTANCE = new FileChooserManager();
		}
		return INSTANCE;
	}
	
	public static File getLowQClustersSelectionFile() {

		JFileChooser chooser;
				
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new JSONFileFilter());
		int result = chooser.showOpenDialog(SugarApplication.getApplication());
		if (result == JFileChooser.CANCEL_OPTION) return null;
	
		// See if they forced a file format
		File file = chooser.getSelectedFile();
		if(file != null){
			lastUsedDir = file.getParentFile();
		}
		return file;
	}
	
	public static File[] getSequenceFiles(){
		JFileChooser chooser;
		
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		chooser.setMultiSelectionEnabled(true);
		SequenceFileFilter sff = new SequenceFileFilter();
		chooser.addChoosableFileFilter(sff);
		chooser.addChoosableFileFilter(new FastQFileFilter());
		chooser.addChoosableFileFilter(new CasavaFastQFileFilter());
		chooser.addChoosableFileFilter(new BAMFileFilter());
		chooser.addChoosableFileFilter(new MappedBAMFileFilter());
		chooser.setFileFilter(sff);
		FileOptionsPanel optionsPanel = FileOptionsPanel.getInstance();
		chooser.setAccessory(optionsPanel);

		boolean inputParametersOK = true;
		do {
			
			int result = chooser.showOpenDialog(SugarApplication.getApplication());
			if (result == JFileChooser.CANCEL_OPTION) return null;

			// validate user input
			String inputErrors = optionsPanel.validateInput();
			if (inputErrors != null)
			{
				inputParametersOK = false;
				JOptionPane.showMessageDialog(SugarApplication.getApplication(), inputErrors, "Invalid Input Parameter", JOptionPane.ERROR_MESSAGE);
			}
		} while (!inputParametersOK);
	
		// See if they forced a file format
		FileFilter chosenFilter = chooser.getFileFilter();
		if (chosenFilter instanceof FastQFileFilter) {
			System.setProperty("fastqc.sequence_format", "fastq");
		}
		if (chosenFilter instanceof CasavaFastQFileFilter) {
			System.setProperty("fastqc.sequence_format", "fastq");
			System.setProperty("fastqc.casava",Options.TRUE);
		}
		else if (chosenFilter instanceof BAMFileFilter) {
			System.setProperty("fastqc.sequence_format", "bam");
		}
		else if (chosenFilter instanceof MappedBAMFileFilter) {
			System.setProperty("fastqc.sequence_format", "bam_mapped");
		}
		
		AnalysisQueue.getInstance().setAvailableSlots(optionsPanel.getThreads());
		
		File[] files = chooser.getSelectedFiles();
		if(files != null && files.length > 0){
			lastUsedDir = files[0].getParentFile();
		}
		
		return chooser.getSelectedFiles();
	}
	
	public static File getReportFile(File defaultFile){
		JFileChooser chooser;
		
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		
		chooser.setSelectedFile(defaultFile);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {
		
			public String getDescription() {
				return "Zip files";
			}
		
			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().toLowerCase().endsWith(".zip")) {
					return true;
				}
				else {
					return false;
				}
			}
		
		});
		
		SaveHTMLReportOptionsPanel optionsPanel = SaveHTMLReportOptionsPanel.getInstance();
		chooser.setAccessory(optionsPanel);
	
		File reportFile;
		while (true) {
			int result = chooser.showSaveDialog(SugarApplication.getApplication());
			if (result == JFileChooser.CANCEL_OPTION) return null;
			
			reportFile = chooser.getSelectedFile();
			if (! reportFile.getName().toLowerCase().endsWith(".zip")) {
				reportFile = new File(reportFile.getAbsoluteFile()+".zip");
			}
			
			// Check if we're overwriting something
			if (reportFile.exists()) {
				int reply = JOptionPane.showConfirmDialog(SugarApplication.getApplication(), reportFile.getName()+" already exists.  Overwrite?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.NO_OPTION) {
					continue;
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		
		if (reportFile != null)
			Options.setHeatmapImageSize(optionsPanel.getHeatmapImageSize());
		
		return reportFile;
	}
	
	public static File getClearedFile(File defaultFile){
		JFileChooser chooser;

		// get/set initial directory
		if (lastUsedDir == null) {
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(lastUsedDir);
		}
		
		chooser.setDialogTitle(SugarMenuBar.SAVE_CLEARED_SEQUENCE_FILE);
		
		// get/set default method
		SaveClearedFileOptionsPanel optionsPanel = SaveClearedFileOptionsPanel.getInstance();
		chooser.setAccessory(optionsPanel);
		
		chooser.setSelectedFile(defaultFile);

		chooser.setMultiSelectionEnabled(false);
		SequenceFileFilter sff = new SequenceFileFilter();
		chooser.addChoosableFileFilter(sff);		
		chooser.setFileFilter(sff);
	
		File outputFile;
		while (true) {
			int result = chooser.showSaveDialog(SugarApplication.getApplication());
			if (result == JFileChooser.CANCEL_OPTION) return null;
			
			outputFile = chooser.getSelectedFile();
			
			// Check if we're overwriting something
			if (outputFile.exists()) {
				int reply = JOptionPane.showConfirmDialog(SugarApplication.getApplication(), outputFile.getName()+" already exists.  Overwrite?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.NO_OPTION) {
					continue;
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return outputFile;
	}
}
