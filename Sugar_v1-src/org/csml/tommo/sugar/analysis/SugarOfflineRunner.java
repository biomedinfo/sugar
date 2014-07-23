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
import java.util.Vector;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.modules.ClearBasesInLowQClusters;
import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.modules.ELowQClustersSelectionMethdod;
import org.csml.tommo.sugar.modules.MappingQuality;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.report.SugarHTMLReportArchive;
import org.csml.tommo.sugar.utils.Options;
import org.csml.tommo.sugar.utils.StringUtils;

import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Sequence.SequenceFactory;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;
import uk.ac.babraham.FastQC.Utilities.CasavaBasename;

public class SugarOfflineRunner implements SugarAnalysisListener {
	
	private int filesRemaining;
	private boolean showUpdates = true;
	private boolean showDebugMesages = true;

	// for progress display 
	private boolean showTimeEstimation = true;
	private long lastEstimation = 0;
	
	public SugarOfflineRunner (String [] filenames) {
		
		// See if we need to show updates
		if (Options.isQuiet()) {
			showUpdates = false;
		}
		
		// See if we need to show debug messages
		showDebugMesages = Options.isDebug() && showUpdates;
		
		Vector<File> files = new Vector<File>();
		
		for (int f=0;f<filenames.length;f++) {
			File file = new File(filenames[f]);
			if (!file.exists() || ! file.canRead()) {
				System.err.println("Skipping '"+filenames[f]+"' which didn't exist, or couldn't be read");
				continue;
			}
			files.add(file);
		}
		
		File [][] fileGroups;
		
		// See if we need to group together files from a casava group
		if (Options.TRUE.equals(System.getProperty("fastqc.casava"))) {
			fileGroups = CasavaBasename.getCasavaGroups(files.toArray(new File[0]));
		}
		else {
			fileGroups = new File [files.size()][1];
			for (int f=0;f<files.size();f++) {
				fileGroups[f][0] = files.elementAt(f);
			}
		}
		
		Integer[] qualityArray = Options.getHeatmapQualityThresholdArray();
		
		
		filesRemaining = fileGroups.length * qualityArray.length;
		
		for (int i=0;i<fileGroups.length;i++) {

			for (Integer quality : qualityArray)
			{
				
				try {
					processFile(fileGroups[i], quality);
				}
				catch (Exception e) {
					System.err.println("Failed to process "+fileGroups[i][0]);
					e.printStackTrace();
					--filesRemaining;
				}
			}
		}
		
		// We need to hold this class open as otherwise the main method
		// exits when it's finished.
		while (filesRemaining > 0) {
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {}
		}
		System.exit(0);
		
	}
	
	public void processFile (File [] files, int qualityThreshold) throws Exception {
		for (int f=0;f<files.length;f++) {
			if (!files[f].exists()) {
				throw new IOException(files[f].getName()+" doesn't exist");
			}
		}
		SequenceFile sequenceFile;
		if (files.length == 1) {
			sequenceFile = SequenceFactory.getSequenceFile(files[0]);
		}
		else {
			sequenceFile = SequenceFactory.getSequenceFile(files);			
		}
				
		SugarAnalysisRunner runner = new SugarAnalysisRunner(files, sequenceFile, qualityThreshold);
		runner.addAnalysisListener(this);

		QualityHeatMapsPerTileAndBase heatMap = new QualityHeatMapsPerTileAndBase(qualityThreshold);
		MappingQuality mappingQuality = new MappingQuality(heatMap.getTileTree(), sequenceFile);
		QCModule [] module_list = new QCModule [] {
				heatMap.getTileTree(),
				heatMap,
				mappingQuality
		};

		EClearLowQClustersMethod clearMethod = Options.getClearLowQClustersMethod();
		if (clearMethod != null && clearMethod != EClearLowQClustersMethod.NONE)
		{
			File selectionFile = Options.getClearLowQClustersFile();
			ELowQClustersSelectionMethdod selectionMethod = selectionFile != null ? 
					ELowQClustersSelectionMethdod.FILE : 
						ELowQClustersSelectionMethdod.AUTO;
			try {
				ClearBasesInLowQClusters clearModule = new ClearBasesInLowQClusters(
						heatMap, sequenceFile, clearMethod,
						selectionMethod, selectionFile);
				runner.setClearBasesInLowQClustersModule(clearModule);
			} catch (Exception e) {
				String message = "Failed to initialize clear LowQ clusters module"; 
				SugarApplication.showException(e, message);

				e.printStackTrace();
			}
		}

		runner.startAnalysis(module_list);

	}	
	
	public void analysisComplete(SequenceFile file, QCModule[] results) {
		File reportFile;
		
		if (showUpdates) System.out.println("Analysis complete for "+file.name());

		// get the quality threshold
		QualityHeatMapsPerTileAndBase qualityModule = SugarApplication.getQualityHeatmapModule(results);
		String threshodString = qualityModule != null ? "_q" + qualityModule.getQualityThreshold() : "";
		
		
		if (System.getProperty("fastqc.output_dir") != null) {
			// get input filename
			String fileName = file.getFile().getName();

			// remove extension 
			fileName = fileName.replaceAll(".gz$","").replaceAll(".bz2$","").replaceAll(".txt$","").replaceAll(".fastq$", "").replaceAll(".sam$", "").replaceAll(".bam$", "");
			
			// append: "sugar" + quality info + zip extension 
			fileName += "_sugar_q" + threshodString + ".zip";			

			reportFile = new File(System.getProperty("fastqc.output_dir")+"/"+fileName);						
		}
		else {
			// get input file path
			String filePath = file.getFile().getAbsolutePath();

			// remove extension 
			filePath = filePath.replaceAll(".gz$","").replaceAll(".bz2$","").replaceAll(".txt$","").replaceAll(".fastq$", "").replaceAll(".sam$", "").replaceAll(".bam$", "");
			
			// append: "sugar" + quality info + zip extension 
			reportFile = new File(filePath+"_sugar" + threshodString + ".zip");			
		}
		
		// SPECIAL CASE: "view only" modules
		results = SugarApplication.addViewOnlyModules(results);		
		
		try {
			new SugarHTMLReportArchive(file, results, reportFile);
		}
		catch (Exception e) {
			analysisExceptionReceived(file, e);
			return;
		}
		--filesRemaining;

	}

	public void analysisUpdated(SequenceFile file, int sequencesProcessed, int percentComplete) {
		
//		if (percentComplete % 5 == 0) {
			if (percentComplete == 105) {
				if (showUpdates) System.err.println("It seems our guess for the total number of records wasn't very good.  Sorry about that.");
			}
			if (percentComplete > 100) {
				if (showUpdates) System.err.println("Still going at "+percentComplete+"% complete for "+file.name());
			}
			else {
				if (showUpdates) System.err.println("Approx "+percentComplete+"% complete for "+file.name());
			}
			
			showTimeEstimation = true;
			
			if (showDebugMesages)
				SugarApplication.printMemory();
//		}
	}

	public void analysisExceptionReceived(SequenceFile file, Exception e) {
		System.err.println("Failed to process file "+file.name());
		e.printStackTrace();
		--filesRemaining;
	}

	public void analysisStarted(SequenceFile file) {
		if (showUpdates) System.err.println("Started analysis of "+file.name());
		
		lastEstimation = 0;
		showTimeEstimation = true;
		
	}

	@Override
	public void analysisTimeUpdated(long timeConsumed, long timeRemaining) {
		
		if (timeConsumed - lastEstimation >= 10 && showTimeEstimation )
		{
			String timeElapsed = "Elapsed time: " + StringUtils.formatTime(timeConsumed);
			String timeLeft = timeRemaining > 0 ? "Remaining time (estimated): " + StringUtils.formatTime(timeRemaining) : "";
			
			if (showUpdates) System.err.println(timeElapsed + ". " + timeLeft);
						
			showTimeEstimation = false;
			lastEstimation = timeConsumed;
		}
		
	}

	@Override
	public void cacheFileStarted(QCModule m, long filesize, int operation) {
		
		if (showDebugMesages)
		{
			if (operation == SugarAnalysisListener.READING_FILE)
				System.out.println("Started loading cache file for module "+ m.name() + ". The file size is " + filesize );
			else if (operation == SugarAnalysisListener.WRITING_FILE)
				System.out.println("Started writing cache file for module "+ m.name());
		}

	}

	@Override
	public void cacheFileCompleted(QCModule m, long time, int operation) {

		if (showDebugMesages)
		{
			if (operation == SugarAnalysisListener.READING_FILE)
				System.out.println("Completed loading module "+ m.name() + " from cache in " + time + " milliseconds." );
			else if (operation == SugarAnalysisListener.WRITING_FILE)
				System.out.println("Completed writing module "+ m.name() + " to cache in " + time + " milliseconds." );
		}		
	}
		
}
