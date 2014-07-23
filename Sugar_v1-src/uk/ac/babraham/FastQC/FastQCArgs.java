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
package uk.ac.babraham.FastQC;

import java.io.File;
import java.io.PrintStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import uk.ac.babraham.FastQC.Sequence.ESequenceFormat;

public class FastQCArgs {
	
	@Argument(index=0, metaVar="seqFile1...seqFileN", multiValued=true, required=false, usage="Sequence files to be checked/analyzed. " +
			"If this option is NOT specified GUI Application is started. Otherwise the console application is executed.")
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
    
	@Option(name="--extract", usage="If set then the zipped output file will be uncompressed in the same directory after it has been created.  By default this option will be set if fastqc-ext is run in non-interactive mode.")	    
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
	
	@Option(name="-nogroup", aliases="--nogroup", usage="Disable grouping of bases for reads >50bp. All reports will show data for every base in the read.  WARNING: Using this option will cause fastqc to crash and burn if you use it on really long reads, and your plots may end up a ridiculous size.                    You have been warned!.")
	private boolean nogroup = false;
	
	@Option(name="-kmers", aliases="--kmers", usage="Specifies the length of Kmer to look for in the Kmer content module. Specified Kmer length must be between 2 and 10. Default length is 5 if not specified.")
	private Integer kmers = null;
	
	@Option(name="-c", aliases="--contaminants", usage="Specifies a non-default file which contains the list of contaminants to screen overrepresented sequences against. The file must contain sets of named contaminants in the form name[tab]sequence.  Lines prefixed with a hash will be ignored.")
	private File contaminants = null;
                    
	
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
		
		FastQCArgs fastqcArgs = new FastQCArgs();
		CmdLineParser parser = new CmdLineParser(fastqcArgs);
		
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			fastqcArgs.printUsage(System.out, parser, false);
			System.exit(1);
		}
		
		if (fastqcArgs.getSequenceFiles() != null && fastqcArgs.getSequenceFiles().length > 0) {
			System.setProperty("java.awt.headless", "true");
		}
		
		if (fastqcArgs.isHelp()) {
			System.setProperty("java.awt.headless", "true");
			fastqcArgs.printUsage(System.out, parser, true);
			System.exit(0);
		}

		if (fastqcArgs.isVersion()) {
			System.setProperty("java.awt.headless", "true");
			System.setProperty("fastqc.show_version", "true");
			return new String[0];
		}
		
		if (fastqcArgs.isDebug()) {
			System.setProperty("fastqc.debug", "true");
		}
		
		if (fastqcArgs.getOutdir() != null) {
			File outdir = fastqcArgs.getOutdir(); 
			if (!outdir.isDirectory())
			{
				System.err.println("Specified output directory $outdir does not exist");
//				fastqcArgs.printUsage(System.out, parser);
				System.exit(1);				
			}	
			System.setProperty("fastqc.output_dir", outdir.toString());
		}
					
		if (fastqcArgs.isExtract()) {
			System.setProperty("fastqc.unzip", "true");
		}
		
		if (fastqcArgs.isNoExtract()) {
			System.setProperty("fastqc.unzip", "false");
		}
		
		if (fastqcArgs.getFormat() != null) {
			System.setProperty("fastqc.sequence_format", fastqcArgs.getFormat().toString());
		}
		
		if (fastqcArgs.getThreads() != null) {
			if (fastqcArgs.getThreads() < 1)
			{
				System.err.println("Number of $threads must be a positive integer");
//				fastqcArgs.printUsage(System.out, parser);
				System.exit(1);								
			}
			System.setProperty("fastqc.threads", String.valueOf(fastqcArgs.getThreads()));
		}

		if (fastqcArgs.isQuiet()) {
			System.setProperty("fastqc.quiet", "true");
		}
		
		if (fastqcArgs.isNogroup()) {
			System.setProperty("fastqc.nogroup", "true");			
		}
		
		if (fastqcArgs.getKmers() != null) {
			
			if (fastqcArgs.getKmers() < 2 || fastqcArgs.getKmers() > 10)
			{
				System.err.println("Kmer size must be in the range 2-10");
//				fastqcArgs.printUsage(System.out, parser);
				System.exit(1);								
			}
			System.setProperty("fastqc.kmer_size", String.valueOf(fastqcArgs.getKmers()));
			
		}
		
		if (fastqcArgs.getContaminants() != null) {
			File contaminantsFile = fastqcArgs.getContaminants(); 
			if (!contaminantsFile.isFile())
			{
				System.err.println("Specified contaminants file does not exist: " + contaminantsFile.toString());
//				sugarArgs.printUsage(System.out, parser);
				System.exit(1);				
			}	
			System.setProperty("fastqc.contaminant_file", contaminantsFile.toString());			
		}	
				
		if (fastqcArgs.getQcThresholdLine() != null) {
			if (fastqcArgs.getQcThresholdLine() < 1)
			{
				System.err.println("$qc_threshold_line - must be a positive integer");
//				fastqcArgs.printUsage(System.out, parser);
				System.exit(1);												
			}
			System.setProperty("fastqc.qc_threshold_line", String.valueOf(fastqcArgs.getQcThresholdLine()));
		}
		
						
		args = fastqcArgs.copyFileArgs();
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
		printStream.println("FastQC-Ext" + " version " + FastQCApplication.VERSION);
		printStream.println();		
		
		if (printDetails) 
		{
			printStream.println("Description:");				
			printStream.println("This is an an extension of FastQC software. It reads a set of sequence file");
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
		printStream.println(" start GUI: java -jar fastqc-ext.jar [options]");
		printStream.println(" run Console: java -jar fastqc-ext.jar [options] seqfile1 seqfile2 ... seqfileN");
		printStream.println();
		printStream.println("Usage:");
		printStream.println(" java -jar fastqc-ext.jar [-o output dir] [--(no)extract] [-f fastq|bam|sam] [-c contaminant file] [seqfile1] [seqfile2] ... [seqfileN]");
		printStream.println();
		
		if (printDetails)
		{
			printStream.println("Options:");
			parser.printUsage(printStream);
			printStream.println();
		}
		else {
			printStream.println("To get more help, type the command:");
			printStream.println(" java -jar fastqc-ext.jar -h");			
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


	public boolean isNogroup() {
		return nogroup;
	}


	public Integer getKmers() {
		return kmers;
	}


	public File getContaminants() {
		return contaminants;
	}


}
