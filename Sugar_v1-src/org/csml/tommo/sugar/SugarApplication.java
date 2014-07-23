/**
 *    Copyright Masao Nagasaki
 *    Nagasaki Lab
 *    Laboratory of Biomedical Information Analysis,
 *    Department of Integrative Genomics,
 *    Tohoku Medical Megabank Organization, Tohoku University 
 *    @since 2013
 *
 *    This file is part of SUGAR (Subtile-based GUI-Assisted Refiner).
 *    SUGAR is an extension of FastQC (copyright 2010-12 Simon Andrews)
 *
 *    SUGAR is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SUGAR is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SUGAR; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.csml.tommo.sugar.analysis.SugarAnalysisRunner;
import org.csml.tommo.sugar.analysis.SugarOfflineRunner;
import org.csml.tommo.sugar.dialogs.FileOptionsPanel;
import org.csml.tommo.sugar.dialogs.SaveClearedFileOptionsPanel;
import org.csml.tommo.sugar.dialogs.SugarWelcomePanel;
import org.csml.tommo.sugar.modules.AverageQualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.ClearBasesInLowQClusters;
import org.csml.tommo.sugar.modules.DensityHeatMapsPerTile;
import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.modules.ELowQClustersSelectionMethdod;
import org.csml.tommo.sugar.modules.MappingQuality;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.SubtileQualityRanking;
import org.csml.tommo.sugar.modules.heatmap.AverageClearLowQClustersDialog;
import org.csml.tommo.sugar.modules.heatmap.ClearLowQClustersDialog;
import org.csml.tommo.sugar.report.SugarHTMLReportArchive;
import org.csml.tommo.sugar.utils.FileChooserManager;
import org.csml.tommo.sugar.utils.Options;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Results.ResultsPanel;
import uk.ac.babraham.FastQC.Sequence.SequenceFactory;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;
import uk.ac.babraham.FastQC.Utilities.CasavaBasename;

public class SugarApplication extends JFrame {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String APP_NAME = "SUGAR";
	public static final String VERSION = "1.0.0";

	protected static File APP_HOME_DIR = new File(System.getProperty("user.home"), ".sugar"); 
	protected static File APP_PROPERTIES_FILE = new File(APP_HOME_DIR, "sugar.properties"); 
	
	private static SugarApplication application;  
	
	private JTabbedPane fileTabs;
	private SugarWelcomePanel welcomePanel;
	
	private int tileHeatmapSize;
	
	public SugarApplication () {
			setTitle(APP_NAME);
			
			List<Image> imageList = new ArrayList<Image>();
			imageList.add(new ImageIcon(ClassLoader.getSystemResource("org/csml/tommo/sugar/resources/sugar_icon.png")).getImage());
			imageList.add(new ImageIcon(ClassLoader.getSystemResource("org/csml/tommo/sugar/resources/sugar_icon_16.png")).getImage());
			imageList.add(new ImageIcon(ClassLoader.getSystemResource("org/csml/tommo/sugar/resources/sugar_icon_48.png")).getImage());			
			setIconImages(imageList);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			setSize(1280, 720);
			setSize(800,600);
			setLocationRelativeTo(null);
			
			welcomePanel = new SugarWelcomePanel();
			
			fileTabs = new JTabbedPane(JTabbedPane.TOP);
			setContentPane(welcomePanel);
			
			tileHeatmapSize = Options.DEFAULT_HEATMAP_SIZE;
			
			setJMenuBar(new SugarMenuBar(this));
			
		}

	public void close () {
		if (fileTabs.getSelectedIndex() >=0) {
			fileTabs.remove(fileTabs.getSelectedIndex());
		}
		if (fileTabs.getTabCount() == 0) {
			setContentPane(welcomePanel);
			validate();
			repaint();
		}
	}
	
	public void closeAll () {
		fileTabs.removeAll();
		setContentPane(welcomePanel);
		validate();
		repaint();
	}
	
	public void openFile () {		
		File [] files = FileChooserManager.getSequenceFiles();
		if(files != null && files.length > 0){
			// If we're still showing the welcome panel switch this out for
			// the file tabs panel
			if (fileTabs.getTabCount() == 0) {
				setContentPane(fileTabs);
				validate();
				repaint();
			}
			
			File [][] fileGroups;
			
			// See if we need to group together files from a casava group
			if (Options.TRUE.equals(System.getProperty("fastqc.casava"))) {
				fileGroups = CasavaBasename.getCasavaGroups(files);
			}
			else {
				fileGroups = new File [files.length][1];
				for (int f=0;f<files.length;f++) {
					fileGroups[f][0] = files[f];
				}
			}

		
			for (int i=0;i<fileGroups.length;i++) {
				File [] filesToProcess = fileGroups[i];
				SequenceFile sequenceFile;
				
				
				try {
					if (filesToProcess.length > 1) {
						sequenceFile = SequenceFactory.getSequenceFile(filesToProcess);
					}
					else {
						sequenceFile = SequenceFactory.getSequenceFile(filesToProcess[0]);
					}
				}
				catch (SequenceFormatException e) {
					JPanel errorPanel = new JPanel();
					errorPanel.setLayout(new BorderLayout());
					errorPanel.add(new JLabel("File format error: "+e.getLocalizedMessage(), JLabel.CENTER),BorderLayout.CENTER);
					fileTabs.addTab(filesToProcess[0].getName(), errorPanel);
					e.printStackTrace();
					continue;
				}
				catch (IOException e) {
					System.err.println("File broken");
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Couldn't read file:"+e.getLocalizedMessage(), "Error reading file", JOptionPane.ERROR_MESSAGE);
					continue;
				}
						
				FileOptionsPanel optionsPanel = FileOptionsPanel.getInstance();			
				
				Integer[] qualityArray = optionsPanel.getHeatmapQualityThresholdArray();
				
				for (Integer quality : qualityArray)
				{				
					SugarAnalysisRunner runner = new SugarAnalysisRunner(filesToProcess, sequenceFile, quality);
					runner.initOptions(optionsPanel);
					ResultsPanel rp = new ResultsPanel(sequenceFile);
					runner.addAnalysisListener(rp);
					// #52: Add progress information as the name of tabbed pane
					String titleLabel = sequenceFile.name() + " - Waiting...";
					fileTabs.addTab(titleLabel , rp);
					
					QualityHeatMapsPerTileAndBase heatMap = new QualityHeatMapsPerTileAndBase(quality);
					heatMap.initOptions(optionsPanel);
					
					MappingQuality mappingQuality = new MappingQuality(heatMap.getTileTree(), sequenceFile);
					mappingQuality.initOptions(optionsPanel);
									
					QCModule [] module_list = new QCModule [] {
						heatMap.getTileTree(),
						heatMap,
						mappingQuality,
					};
					
					EClearLowQClustersMethod clearMethod = optionsPanel.getClearLowQClustersMethod();
					if (clearMethod != null && clearMethod != EClearLowQClustersMethod.NONE)
					{
						try {
							ClearBasesInLowQClusters clearModule = new ClearBasesInLowQClusters(heatMap, sequenceFile, optionsPanel.getClearLowQClustersMethod(), ELowQClustersSelectionMethdod.AUTO, null);
							runner.setClearBasesInLowQClustersModule(clearModule);
						} catch (Exception e) {
							String message = "Failed to initialize clear LowQ clusters module"; 
							showException(e, message);
							
							e.printStackTrace();
						}
					}
									
					runner.startAnalysis(module_list);
				}
			}			
		}
	}

	public void saveReport () {
		if (!isSequenceFileLoaded("Can't save report"))
			return;
		
		File selectedFile = new File(((ResultsPanel)fileTabs.getSelectedComponent()).sequenceFile().getFile().getName().replaceAll(".gz$","").replaceAll(".bz2$","").replaceAll(".txt$","").replaceAll(".fastq$", "").replaceAll(".sam$", "").replaceAll(".bam$", "")+"_sugar.zip");
		File reportFile = FileChooserManager.getReportFile(selectedFile);
		
		if(reportFile != null){
			ResultsPanel selectedPanel = (ResultsPanel)fileTabs.getSelectedComponent();
			
			try {
				new SugarHTMLReportArchive(selectedPanel.sequenceFile(), selectedPanel.modules(), reportFile);
			} 
			catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to create archive: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}			
		}
	}

	protected boolean isSequenceFileLoaded(String message) {
		boolean result = true; 
		if (fileTabs.getSelectedComponent() == null) {
			JOptionPane.showMessageDialog(this, "No seqeuence files are open yet", message, JOptionPane.ERROR_MESSAGE);
			result = false;
		}
		else {
			ResultsPanel resultsPanel = (ResultsPanel) fileTabs.getSelectedComponent();
			if (!resultsPanel.isLoaded())
			{
				JOptionPane.showMessageDialog(this, "The selected file is not loaded yet", message, JOptionPane.ERROR_MESSAGE);
				result = false;
			}
		}
		
		return result;
	}

	public static void main(String[] args) {
		
		args = SugarArgs.parseArgs(args);
		
		// See if we just have to print out the version
		if (Options.showVersion()) {
			System.out.println(APP_NAME + " v"+VERSION);
			System.exit(0);
		}
		
		if (args.length > 0) {
			// Set headless to true so we don't get problems
			// with people working without an X display.
			System.setProperty(Options.HEADLESS_ENVIRONMENT, Options.TRUE);
			
			// The non-interactive default is to uncompress the
			// reports after they have been generated
			if (System.getProperty("fastqc.unzip") == null || ! System.getProperty("fastqc.unzip").equals("false")) {
				System.setProperty("fastqc.unzip", Options.TRUE);
			}
			
			String errorMessage = Options.validateInput();
			if (errorMessage != null)
			{
				System.err.println("Invalid input parameters:");
				System.err.println(errorMessage);
				System.exit(1);				
			}
			
			new SugarOfflineRunner(args);
			System.exit(0);
		}
		
		else {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {}
			ToolTipManager.sharedInstance().setDismissDelay(10000);
	
			// The interactive default is to not uncompress the
			// reports after they have been generated
			if (System.getProperty("fastqc.unzip") == null || !System.getProperty("fastqc.unzip").equals("true")) {
				System.setProperty("fastqc.unzip", "false");
			}
	
			application = new SugarApplication();
	
			application.setVisible(true);
		}
	}

	public static SugarApplication getApplication() {
		return application;
	}

	public static File getAPP_HOME_DIR() {
		if (!APP_HOME_DIR.exists())
			APP_HOME_DIR.mkdirs();
		return APP_HOME_DIR;
	}
	
	public static Properties getApplicationPropeties() {
		Properties properties = new Properties();
		if (APP_PROPERTIES_FILE.isFile())
		{
			try {
				properties.load(new FileInputStream(APP_PROPERTIES_FILE));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}

	
	public static QCModule[] addViewOnlyModules(QCModule[] modules) {
		// SEPCIAL CASE, check for "view only" modules
		// View only module are not read, but have a results panel
		// View only modules reuse data of different modules
		List<QCModule> allModules = new ArrayList<QCModule>();
		QualityHeatMapsPerTileAndBase qualityHeatmapsPerTileAndBase = null;
		for (QCModule m : modules)
		{
			allModules.add(m);
			if (m instanceof QualityHeatMapsPerTileAndBase)
			{
				qualityHeatmapsPerTileAndBase = (QualityHeatMapsPerTileAndBase) m;
				allModules.add(new DensityHeatMapsPerTile(qualityHeatmapsPerTileAndBase));
				allModules.add(new AverageQualityHeatMapsPerTileAndBase(qualityHeatmapsPerTileAndBase));
			}
			
			if(m instanceof MappingQuality){
				allModules.add(new SubtileQualityRanking(qualityHeatmapsPerTileAndBase, (MappingQuality)m));
			}
		}
		if (allModules.size() > modules.length)
		{
			modules = allModules.toArray(new QCModule[0]);
		}

		
		return modules;
	}

	public void saveClearedFile() {
		if (!isSequenceFileLoaded("Can't save cleared file")) {
			return;
		}
		
		// get/set default filename 
		ResultsPanel selectedPanel = (ResultsPanel)fileTabs.getSelectedComponent();
		SequenceFile openedSequenceFile = selectedPanel.sequenceFile();		
		String defaultExtention = openedSequenceFile.getDefaultFileExtention();
		File outputFile = new File(openedSequenceFile.getFile().getName()+".cleared." + defaultExtention);
		
		outputFile = FileChooserManager.getClearedFile(outputFile);
		if (outputFile == null)
			return;			
		
		try {
			
			QCModule[] modules = selectedPanel.modules();
			QualityHeatMapsPerTileAndBase qualityModule = getQualityHeatmapModule(modules);
			
			SugarAnalysisRunner analysisRunner = new SugarAnalysisRunner(
					new File[] {selectedPanel.sequenceFile().getFile()}, 
					selectedPanel.sequenceFile(),
					qualityModule.getQualityThreshold());

			SaveClearedFileOptionsPanel optionsPanel = SaveClearedFileOptionsPanel.getInstance();
			
			ELowQClustersSelectionMethdod selectionMethod =  optionsPanel.getLowQClusterSelectionMethod();
			File selectionFile = null;

			if (selectionMethod == ELowQClustersSelectionMethdod.USER){
				if(optionsPanel.getClearLowQClustersMethod() == EClearLowQClustersMethod.DELETE){
					AverageClearLowQClustersDialog dialog = new AverageClearLowQClustersDialog(getSubtileQualityRanking(modules));
					dialog.setModal(true);
					dialog.setVisible(true);
					
					if (dialog.wasCancelled())
						return;
				}
				else{
					ClearLowQClustersDialog dialog = new ClearLowQClustersDialog(qualityModule);
					dialog.setModal(true);
					dialog.setVisible(true);
					
					if (dialog.wasCancelled())
						return;
				}
					
			}
			else if (selectionMethod == ELowQClustersSelectionMethdod.FILE) {

				selectionFile = FileChooserManager.getLowQClustersSelectionFile();
				if (selectionFile == null)
					return;
			}

			ClearBasesInLowQClusters clearBasesInLowQClustersModule = 
					new ClearBasesInLowQClusters(qualityModule, selectedPanel.sequenceFile(), 
							optionsPanel.getClearLowQClustersMethod(), 
							selectionMethod, selectionFile, outputFile);
			
			
			analysisRunner.setClearBasesInLowQClustersModule(clearBasesInLowQClustersModule);			
			analysisRunner.writeClearedFile();				
		} 
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to create cleared sequence file: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
	}

	static public QualityHeatMapsPerTileAndBase getQualityHeatmapModule(
			QCModule[] modules) {
		QualityHeatMapsPerTileAndBase qualityModule = null;
		for (int i = 0; i < modules.length; i++)
		{
			if (modules[i] instanceof QualityHeatMapsPerTileAndBase){
				qualityModule = (QualityHeatMapsPerTileAndBase) modules[i]; 				
				break;
			}
		}
		return qualityModule;
	}

	static public SubtileQualityRanking getSubtileQualityRanking(
			QCModule[] modules) {
		SubtileQualityRanking module = null;
		for (int i = 0; i < modules.length; i++)
		{
			if (modules[i] instanceof SubtileQualityRanking){
				module = (SubtileQualityRanking) modules[i]; 
				break;
			}
		}
		return module;
	}

	static public void showException(Exception e, String message) {
		showException(null, e, message);
	}

	static public void showException(Component parentWindow, Exception e, String message) {
		message += ": " + e.getMessage();
		if (Options.isHeadless())
			System.err.println(message);
		else
		{
			if (parentWindow == null)
				parentWindow = SugarApplication.getApplication();
			
			JOptionPane.showMessageDialog(parentWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
		if (Options.isDebug())
			e.printStackTrace();
	}
	
	public int getTileHeatmapSize() {
		return tileHeatmapSize;
	}

	public static final String TILE_SIZE_PROPERTY = "Tile_Size_Property";
	
	public void setTileHeatmapSize(int tileHeatmapSize) {
		if (tileHeatmapSize > 0)
		{
			int oldValue = this.tileHeatmapSize; 
			this.tileHeatmapSize = tileHeatmapSize;
			firePropertyChange(TILE_SIZE_PROPERTY, oldValue, tileHeatmapSize);
		}
	}

	public void addTileSizePropertyChangeListener(
			PropertyChangeListener listener) {
		addPropertyChangeListener(TILE_SIZE_PROPERTY, listener);		
	}
	
	public SequenceFile getSelectedSequenceFile() {
		if (fileTabs.getSelectedComponent() == null) {
			JOptionPane.showMessageDialog(this, "No seqeunce files are open yet", "Can't save report", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		// get/set default filename 
		ResultsPanel selectedPanel = (ResultsPanel)fileTabs.getSelectedComponent();
		SequenceFile openedSequenceFile = selectedPanel.sequenceFile();
		
		return openedSequenceFile;

	}
	
	static public void printMemory() {
		DecimalFormat f1 = new DecimalFormat("#,###MB");
		final long MEGABYTE = 1024 * 1024;
		long free = Runtime.getRuntime().freeMemory() / MEGABYTE;
		long total = Runtime.getRuntime().totalMemory() / MEGABYTE;
		long max = Runtime.getRuntime().maxMemory() / MEGABYTE;
		long used = total - free;
		String info = "total memory =" + f1.format(total) 
				+ "\tused memory =" + f1.format(used)  
				+ "\tavailable memory =" + f1.format(max);

		System.err.println(info);
	}


	
}
