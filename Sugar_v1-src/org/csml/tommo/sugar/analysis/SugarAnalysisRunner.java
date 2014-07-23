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
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.dialogs.FileOptionsPanel;
import org.csml.tommo.sugar.modules.ClearBasesInLowQClusters;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.SugarModule;
import org.csml.tommo.sugar.modules.TileTree;
import org.csml.tommo.sugar.utils.Options;

import uk.ac.babraham.FastQC.Analysis.AnalysisListener;
import uk.ac.babraham.FastQC.Analysis.AnalysisRunner;
import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFactory;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;

/**
 * Class SugarAnalysisRunner extends AnalysisRunner
 * 
 * This class performs the analysis by looping over all modules (first) and then over all sequences.
 * 
 * This means the sequence input file will be parsed read N times, where N is the number of modules.
 * This takes more time than the original AnalysisRunner, which reads the sequence file only once.
 * 
 * This is useful, when one module depends on he results of another module - e.g. QualityHeatMap requires the TileTree
 *
 *
 */
public class SugarAnalysisRunner extends AnalysisRunner {
	
	File[] filesToProcess;
	private long startTime;
	private long timeConsumed;
	
	private boolean useCache;	
	private ClearBasesInLowQClusters clearBasesInLowQClustersModule;
	private int readRate;
	private int matrixSize;
	private int qualityThreshold;
//	private Integer[] qualityThresholdArray;


	public SugarAnalysisRunner(File[] filesToProcess, SequenceFile sequenceFile, int threshold) {
		super(sequenceFile);
		this.filesToProcess = filesToProcess;
		this.qualityThreshold = threshold;
		initOptions();
	}

	protected void initOptions() {
		useCache = !Options.getNoCache();
		readRate = Options.getReadRate();
		matrixSize = Options.getMatrixSize();
	}

	public void initOptions(FileOptionsPanel optionsPanel) {
		useCache = !optionsPanel.getNoCache();
		readRate = optionsPanel.getReadRate();
		matrixSize = optionsPanel.getMatrixSize();
	}

	@Override
	public void run() {
		startTime = System.currentTimeMillis();
		Iterator<AnalysisListener> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisStarted(file);
		}
		
		if (Options.isDebug()) {
			SugarApplication.printMemory();
		}

		// try to get results from cache
		boolean modulesLoadedFromCache = useCache() ? 
				OpenedFileCache.getInstance().readModulesFromCache(file.getFile(), modules, listeners, matrixSize, qualityThreshold) : 
				false;

		if (!modulesLoadedFromCache)
		{
			loadModulesFromFile();			
			
			if (useCache())
				OpenedFileCache.getInstance().writeModulesToCache(file.getFile(), modules, listeners, matrixSize, qualityThreshold);			
		} else {
			percentComplete = (int) getPercentPerLoadFromCache();
		}		
		
		if (Options.isDebug())
		{
			long time = System.currentTimeMillis() - startTime;
			System.out.println("File " + file.getFile().getName() + "  loaded in: " + time + " milliseconds");
			SugarApplication.printMemory();
		}
		
		// Post-processing
		// fix/clear LowQ clusters if required
		if (isClearingLowQClusters())
		{
			clearLowQClusters();
			appendModules(new QCModule[] {clearBasesInLowQClustersModule});
		}

		
		i = listeners.iterator();
		while (i.hasNext()) {
			i.next().analysisComplete(file,modules);
		}		
	}
	
	private void appendModules(QCModule[] additionalModules) {
		
		int originalLength = modules.length;
		int appendedCount = additionalModules.length;
		
		// create an empty array
		QCModule[] result = new QCModule[modules.length+additionalModules.length];
		
		// add the original modules 
		System.arraycopy(modules, 0, result, 0, originalLength);
		
		// add the additional modules
		System.arraycopy(additionalModules, 0, result, originalLength, appendedCount);
		
		modules = result;
	}

	private boolean useCache() {
		return useCache && readRate == 1;
	}

	public double getPercentPerLoadFromCache() {
		return isClearingLowQClusters() ? 
				5 : 
				95 ;
	}
	
	public double getPercentPerTileTreeModule() {
		return isClearingLowQClusters() ? 
				20 : 
				35 ;
	}

	public double getPercentPerAnalysisModules() {
		return isClearingLowQClusters() ? 
			    40 : 
				65 ;
	}

	protected boolean isClearingLowQClusters() {
		return clearBasesInLowQClustersModule != null;
	}
	
	private void loadModulesFromFile() {		
		
		// 1. Pre-processing
		// first load the tilTree module
		loadTileTreeModule();		

		// 2. Analysis modules
		// loop over all sequences
		runAnalysisModules();
		
	}

	private void runAnalysisModules() {
		
		List<QCModule> moduleList = new ArrayList<QCModule>();
		for (int i = 1; i < modules.length; i++)
			moduleList.add(modules[i]);
			
		runModules(moduleList, percentComplete, getPercentPerAnalysisModules());
		
	}

	private void loadTileTreeModule() {
		TileTree tileTree = (TileTree) modules[0];
		
		List<QCModule> moduleList = new ArrayList<QCModule>();
		moduleList.add(tileTree);
			
		runModules(moduleList, 0, getPercentPerTileTreeModule());

	}

	private void clearLowQClusters() {

		try {
			writeClearedFile();
		} catch (Exception e)
		{
			String message = "Failed to create cleared sequence file";
			SugarApplication.showException(e, message);

		}
	}
	
	public void writeClearedFile() throws Exception {
		
		if (!Options.isQuiet()) {
			System.out.println("Started clearing the LowQ clusters...");
		}
		
		clearBasesInLowQClustersModule.selectLowQClusters();		
		
		List<QCModule> moduleList = new ArrayList<QCModule>();
		moduleList.add(clearBasesInLowQClustersModule);
		
		runModules(moduleList, percentComplete, 100-percentComplete);
		
		clearBasesInLowQClustersModule.closeWriter();
		
		File clustersOutptutFile = new File(file.getFile().getAbsolutePath() + ".cleared.clusters.json");
		clearBasesInLowQClustersModule.writeClearedClusters(clustersOutptutFile);
		
		File outputFile = clearBasesInLowQClustersModule.getOutputFile();
		File boutputFile = clearBasesInLowQClustersModule.getOutputLowQFile();

		if (!Options.isQuiet()) {
			System.out.println("LowQ clusters cleared and written to file: " + outputFile.getAbsolutePath());
			System.out.println("LowQ clusters cleared and written to file: " + boutputFile.getAbsolutePath());
		}
	}


	public void runModules(List<QCModule> moduleList, double percentStart, double percentForRun) {
		Iterator<AnalysisListener> i;
				
		resetSequenceFile();
		int seqCount = 0;
		while (file.hasNext()) {				
			Sequence seq;
			try {
				seq = file.next();
			}
			catch (SequenceFormatException e) {
				i = listeners.iterator();
				while (i.hasNext()) {
					i.next().analysisExceptionReceived(file,e);
				}
				continue;
			}

			// Read every 'readRate' sequence
			if (seqCount++ % readRate != 0)
				continue;

			// loop over all other modules
			for (QCModule m : moduleList) {
				if (seq.isFiltered() && m.ignoreFilteredSequences()) continue;
				m.processSequence(seq);
			}

			int percent = file.getPercentComplete();
			double percentOfFile = (double) percent / (double) 100; 
			double percentTotalInDouble = percentStart + percentForRun*percentOfFile;
			int percentTotal = (int) (percentTotalInDouble);
			long currentTime = System.currentTimeMillis();

			// update time labels after each second
			// display the estimated, if at least 1% of the file was read 
			if(currentTime - startTime - timeConsumed >= 1000 && percent >= 1) {
				timeConsumed = (currentTime - startTime);				
				long timeRemaining = (long)(timeConsumed * (100 - percentTotalInDouble) / percentTotalInDouble);

				i = listeners.iterator();
				while (i.hasNext()) {
					AnalysisListener listener = i.next();
					if(listener instanceof SugarAnalysisListener){
						((SugarAnalysisListener) listener).analysisTimeUpdated(timeConsumed / 1000, timeRemaining / 1000);
					}
				}
			}


			if (percentTotal >= percentComplete+5) {

				percentComplete = percentTotal;

				i = listeners.iterator();
				while (i.hasNext()) {
					AnalysisListener listener = i.next();
					listener.analysisUpdated(file, seqCount, percentComplete);
				}
				try {
					Thread.sleep(10);
				} 
				catch (InterruptedException e) {}
			}
		}
		percentComplete = (int) percentStart + (int) percentForRun;
	}

	private List<QCModule> removeProcessedModules(List<QCModule> moduleList) {
		List<QCModule> result = new ArrayList<QCModule>();
		
		for (QCModule m : moduleList)
		{
			// SPECIAL CASE
			if (m instanceof QualityHeatMapsPerTileAndBase)
				result.add(m);
			
			if (m instanceof SugarModule)
			{
				SugarModule tm = (SugarModule) m;
				if (!tm.isProcessed())
					result.add(tm);
			}
		}
		
		return result;
	}

	private void resetSequenceFile() {
		try {
			file = (filesToProcess.length > 1) ?
				SequenceFactory.getSequenceFile(filesToProcess) : 
				SequenceFactory.getSequenceFile(filesToProcess[0]);
		} catch (SequenceFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ClearBasesInLowQClusters getClearBasesInLowQClustersModule() {
		return clearBasesInLowQClustersModule;
	}

	public void setClearBasesInLowQClustersModule(
			ClearBasesInLowQClusters clearBasesInLowQClustersModule) {
		this.clearBasesInLowQClustersModule = clearBasesInLowQClustersModule;
	}
	
}
