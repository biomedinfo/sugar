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

import java.io.File;
import java.io.PrintStream;

import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.utils.Options;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import uk.ac.babraham.FastQC.Sequence.ESequenceFormat;

public class SugarArgs {
	
	@Argument(index=0, metaVar="seqFile1...seqFileN", multiValued=true, required=false, usage="Sequence files to be checked/analyzed. " +
			"If this option is NOT specified Sugar GUI Application is started. Otherwise the console application is executed.")
	private File[] sequenceFiles;
	
	@Option(name="-h", aliases="--help", usage="Print this help message and exit")	
	private boolean help = false;
	
	@Option(name="-debug", aliases="--debug", usage="Debug mode. Print addtional information on the console.")
	private boolean debug = false;
    
	@Option(name="-v", aliases="--version", usage="Print the version of the program and exit")	
	private boolean version = false;

	@Option(name="-o", aliases="--outdir", usage="Create all output files in the specified output directory. " +
												"Please note that this directory must exist as the program will not create it. " +
												"If this option is not set then the output file for each sequence file is created " +
												"in the same directory as the sequence file which was processed.")
	private File outdir = null;
    
	@Option(name="--extract", usage="If set then the zipped output file will be uncompressed in the same directory after it has been created.  By default this option will be set if sugar is run in non-interactive mode.")	    
    private boolean extract = false;
       
	@Option(name="--noextract", usage="Do not uncompress the output file after creating it. You should set this option if you do not wish to uncompress the output when running in non-interactive mode.")	    
    private boolean noExtract = false;
     
   
	@Option(name="-f", aliases="--format", usage="Bypasses the normal sequence file format detection and forces the program to use the specified format. Valid formats are bam,sam,bam_mapped,sam_mapped and fastq")
	private ESequenceFormat format = null;
	
	@Option(name="-t", aliases="--threads", usage="Specifies the number of files which can be processed simultaneously.  Each thread will be allocated 250MB of memory so you shouldn't run more threads than your available memory will cope with, and not more than 6 threads on a 32 bit machine")
	private Integer threads = null;

	@Option(name="-q", aliases="--quiet", usage="Supress all progress messages on stdout and only report errors.")
	private boolean quiet = false;

	@Option(name="--qc_threshold_line", usage="Specifies the quality score that will be marked as a horizontal line in the plot of Per base sequence quality. Default value is 30.")
	private Integer qcThresholdLine = null;
                    
	@Option(name="-matrix_size", aliases="--matrix_size", usage="Specifies the size of mean quality matrix. A square matrix with this size will be created for each tile to calculate the quailty ratio of this region. The higher the size, the better the resolution of the tile analysis. The default value is 10.")
	private Integer matrixSize = null;
      
	@Option(name="--heatmap_quality_threshold", usage="Specifies the quality threshold value as defined in the FASTQ specification. The higher the quality threshold, the more reads will be recognized as lowq and more regions will be colored in red in the quality heatmap. You may specify one number or a set of numbers separated by commas. The analysis time will grow linearly with the size of the set. The default value is 20.")
	private String heatmapQualityThreshold = null;
    
	@Option(name="--heatmap_image_size", usage="Specifies the size of mean quality matrix image in HTML report. The default value is 20.")
	private Integer heatmapImageSize = null;

	@Option(name="-read_rate", aliases="--read_rate", usage="Specifies the frequency of reading data to generate heatmaps. If read_rate is n, then one read per n reads is used to generate heatmaps.")
	private Integer readRate = null;
	
	@Option(name="-nocache", aliases="--nocache", usage="Disable caching of results for loaded files.")
	private boolean noCache = false;

	@Option(name="-samtools", aliases="--samtools", usage="Use samtools instead of picard java library")
	private boolean samTools = false;

	@Option(name="--samtoolspath", usage="path to samtools")
	private File samToolsPath = null;
	
	@Option(name="-clear", aliases="--clear_lowq_clusters", usage="Write a new sequence file with cleared lowq clusters (low quality sub-tiles). Valid clear methods are:\n " +
			"'delete' - removes all lowq sequences from the output file\n" +
			"'change' - changes to N all bases found in a lowq cluster\n" +
			"A base is in a lowq cluster, if it is located in a region colored by red in the heat quality matrix. The output file will have the same format (fastq, bam or sam) as the input file. The output filename will have the additional suffix 'cleared.delete' or 'cleared.change'.")
	private EClearLowQClustersMethod clearLowQlusters = null;
	
	@Option(name="-clear_file", aliases="--clear_lowq_clusters_file", usage="JSON file with the list of tiles and subtiles to be cleared. If this options is specified all sequences that are inside the tiles specified by the JSON file will be cleared. \n" +
			"Note: A JSON file with the list of cleared tiles and subtilees is generated after each clear_lowq_clusters run.")
	private File clearLowQlustersFile = null;
	
	/**
	 * 
	 * Converts arguments to Java System properties (for backward compatibility with FASTQC) 
	 * 
	 * Uses args4j
	 * @return 
	 * 
	 * 
	 */
	public static String[] parseArgs(String[] args) {
		
		SugarArgs sugarArgs = new SugarArgs();
		CmdLineParser parser = new CmdLineParser(sugarArgs);
		
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			sugarArgs.printUsage(System.out, parser, false);
			System.exit(1);
		}
		
		if (sugarArgs.getSequenceFiles() != null && sugarArgs.getSequenceFiles().length > 0) {
			System.setProperty(Options.HEADLESS_ENVIRONMENT, Options.TRUE);
		}
		
		if (sugarArgs.isHelp()) {
			System.setProperty(Options.HEADLESS_ENVIRONMENT, Options.TRUE);
			sugarArgs.printUsage(System.out, parser, true);
			System.exit(0);
		}

		if (sugarArgs.isVersion()) {
			System.setProperty(Options.HEADLESS_ENVIRONMENT, Options.TRUE);
			System.setProperty(Options.SUGAR_SHOW_VERSION, Options.TRUE);
			return new String[0];
		}
		
		if (sugarArgs.isDebug()) {
			System.setProperty(Options.DEBUG, Options.TRUE);
		}
		
		if (sugarArgs.getOutdir() != null) {
			File outdir = sugarArgs.getOutdir(); 
			if (!outdir.isDirectory())
			{
				System.err.println("Specified output directory $outdir does not exist");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);				
			}	
			System.setProperty("fastqc.output_dir", outdir.toString());
		}
					
		if (sugarArgs.isExtract()) {
			System.setProperty("fastqc.unzip", Options.TRUE);
		}
		
		if (sugarArgs.isNoExtract()) {
			System.setProperty("fastqc.unzip", "false");
		}
		
		if (sugarArgs.getFormat() != null) {
			System.setProperty("fastqc.sequence_format", sugarArgs.getFormat().toString());
		}
		
		if (sugarArgs.getThreads() != null) {
			if (sugarArgs.getThreads() < 1)
			{
				System.err.println("Number of $threads must be a positive integer");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);								
			}
			System.setProperty(Options.THREADS_OPTION, String.valueOf(sugarArgs.getThreads()));
		}

		if (sugarArgs.isQuiet()) {
			System.setProperty(Options.QUIET_OPTION, Options.TRUE);
		}
		
		if (sugarArgs.getQcThresholdLine() != null) {
			if (sugarArgs.getQcThresholdLine() < 1)
			{
				System.err.println("$qc_threshold_line - must be a positive integer");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty("fastqc.qc_threshold_line", String.valueOf(sugarArgs.getQcThresholdLine()));
		}
		
		if (sugarArgs.getMatrixSize() != null) {
			if (sugarArgs.getMatrixSize() < 1)
			{
				System.err.println("$matrix_size must be a positive integer");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty(Options.MATRIX_SIZE_OPTION, String.valueOf(sugarArgs.getMatrixSize()));
		}
		
		if (sugarArgs.getHeatmapQualityThreshold() != null) {
			if (!sugarArgs.getHeatmapQualityThreshold().matches("[\\d\\-\\,]+"))
			{
				System.err.println("$heatmap_quality_threshold must be a list of numbers separated by commas or minus signs");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty(Options.HEATMAP_QUALITY_THRESHOLD_OPTION, String.valueOf(sugarArgs.getHeatmapQualityThreshold()));
		}
			
		if (sugarArgs.getHeatmapImageSize() != null) {
			if (sugarArgs.getHeatmapImageSize() < 1)
			{
				System.err.println("$heatmap_image_size must be a positive integer");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty(Options.HEATMAP_IMAGE_SIZE_OPTION, String.valueOf(sugarArgs.getHeatmapImageSize()));
		}
			
		if (sugarArgs.getReadRate() != null) {
			if (sugarArgs.getReadRate() < 1)
			{
				System.err.println("$read_rate must be a positive integer");
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty(Options.READ_RATE_OPTION, String.valueOf(sugarArgs.getReadRate()));
		}
		
		if (sugarArgs.isNoCache()) {
			System.setProperty(Options.NO_CACHE, Options.TRUE);
		}
		
		if (sugarArgs.isSamTools()) {
			System.setProperty("sugar.samtools", Options.TRUE);
		}
			
		if (sugarArgs.getSamToolsPath() != null) {
			System.setProperty("sugar.samtoolspath", sugarArgs.getSamToolsPath().toString());
		}
		
		if (sugarArgs.getClearLowQlusters() != null) {
			System.setProperty(Options.CLEAR_LOWQ_CLUSTERS, sugarArgs.getClearLowQlusters().getShortName());						
		}
			
		if (sugarArgs.getClearLowQlustersFile() != null) {
			File lowQClustersFile = sugarArgs.getClearLowQlustersFile(); 
			if (!lowQClustersFile.isFile())
			{
				System.err.println("Specified lowQ_clusters_file does not exist: " + lowQClustersFile);
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);				
			}	
			System.setProperty(Options.CLEAR_LOWQ_CLUSTERS_FILE, lowQClustersFile.toString());			
		}	
						
		args = sugarArgs.copyFileArgs();
		return args;
				
	}


	public String[] copyFileArgs() {
		String[] fileArgs = new String[sequenceFiles != null ? sequenceFiles.length : 0];
		
		if (sequenceFiles != null) 
		{
			for (int i = 0; i < sequenceFiles.length; i++)
			{
				if (!sequenceFiles[i].isFile())
					System.err.println("Specified seqeunce file is NOT a file: " + sequenceFiles[i].toString());
				else
					fileArgs[i] = sequenceFiles[i].toString(); 
			}
		}
		
		return fileArgs;
	}
	
	
	public void printUsage(PrintStream printStream, CmdLineParser parser, boolean printDetails) {
		printStream.println();
		printStream.println(SugarApplication.APP_NAME + " version " + SugarApplication.VERSION);
		printStream.println();				
		
		if (printDetails) 
		{
			printStream.println("Description:");				
			printStream.println("Sugar is an extension of FastQC software. It reads a set of sequence file");
			printStream.println(" and produces from each one a quality control report consisting of ");
			printStream.println(" a number of different modules, each one of which will help to identify"); 
			printStream.println(" a different potential type of problem in your data.");
			printStream.println();			
			printStream.println(" If no files to process are specified on the command line then the program");
			printStream.println(" will start as an interactive graphical application.  If files are provided");
			printStream.println(" on the command line then the program will run with no user interaction");
			printStream.println(" required.  In this mode it is suitable for inclusion into a standardised");
			printStream.println(" analysis pipeline.");
			printStream.println();
		}
		printStream.println("Examples: ");
		printStream.println(" start GUI: java -jar Sugar.jar [options]");
		printStream.println(" run Console: java -jar Sugar.jar [options] seqfile1 seqfile2 ... seqfileN");
		printStream.println();
		printStream.println("Usage:");
		printStream.println(" java -jar Sugar.jar [options] [seqfile1] [seqfile2] ... [seqfileN]");
		printStream.println();
		
		if (printDetails) 
		{
			printStream.println("Options:");
			parser.printUsage(printStream);
			printStream.println();
		}
		else {
			printStream.println("To get more help, type the command:");
			printStream.println(" java -jar Sugar.jar -h");			
		}
	}


	public boolean isHelp() {
		return help;
	}


	public boolean isDebug() {
		return debug;
	}


	public boolean isVersion() {
		return version;
	}


	public File[] getSequenceFiles() {
		return sequenceFiles;
	}


	public File getOutdir() {
		return outdir;
	}


	public boolean isExtract() {
		return extract;
	}


	public boolean isNoExtract() {
		return noExtract;
	}


	public ESequenceFormat getFormat() {
		return format;
	}


	public Integer getThreads() {
		return threads;
	}


	public boolean isQuiet() {
		return quiet;
	}


	public Integer getQcThresholdLine() {
		return qcThresholdLine;
	}


	public Integer getMatrixSize() {
		return matrixSize;
	}


	public String getHeatmapQualityThreshold() {
		return heatmapQualityThreshold;
	}


	public Integer getHeatmapImageSize() {
		return heatmapImageSize;
	}


	public Integer getReadRate() {
		return readRate;
	}


	public boolean isNoCache() {
		return noCache;
	}


	public boolean isSamTools() {
		return samTools;
	}


	public EClearLowQClustersMethod getClearLowQlusters() {
		return clearLowQlusters;
	}


	public File getClearLowQlustersFile() {
		return clearLowQlustersFile;
	}


	public File getSamToolsPath() {
		return samToolsPath;
	}
}
