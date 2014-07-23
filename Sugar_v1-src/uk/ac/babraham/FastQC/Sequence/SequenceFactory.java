/**
 * Copyright Copyright 2010-12 Simon Andrews
 *
 *    This file is part of FastQC.
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
package uk.ac.babraham.FastQC.Sequence;

import java.io.File;
import java.io.IOException;

public class SequenceFactory {

	/**
	 * 
	 * This option is used when multiple files are to be treated as a group to produce
	 * a single output.  This is currently used for groups of files generated by casava
	 * 
	 * @param files
	 * @return
	 * @throws SequenceFormatException
	 * @throws IOException
	 */
	public static SequenceFile getSequenceFile (File [] files) throws SequenceFormatException, IOException {
		
		SequenceFile [] sequenceFiles = new SequenceFile[files.length];
		
		for (int f=0;f<files.length;f++) {
			sequenceFiles[f] = getSequenceFile(files[f]);
		}
		
		return new SequenceFileGroup(sequenceFiles);
		
	}
	
	public static SequenceFile getSequenceFile (File file) throws SequenceFormatException, IOException {
		
		if (System.getProperty("fastqc.sequence_format") != null) {
			// We're not autodetecting the format, but taking whatever they said
			
			if (System.getProperty("fastqc.sequence_format").equals("bam") || System.getProperty("fastqc.sequence_format").equals("sam")) {
				return System.getProperty("sugar.samtools","false").equals("true") ? new FastBAMFile(file,false) : new BAMFile(file,false); 				
			}
			else if (System.getProperty("fastqc.sequence_format").equals("bam_mapped") || System.getProperty("fastqc.sequence_format").equals("sam_mapped")) {
				return System.getProperty("sugar.samtools","false").equals("true") ? new FastBAMFile(file,true) : new BAMFile(file,true); 				
			}
			else if (System.getProperty("fastqc.sequence_format").equals("fastq")) {
				return new FastQFile(file);
			}
//			else if (System.getProperty("fastqc.sequence_format").equals("goby")) {
//				return new GobyFile(file);
//			}
			else {
				throw new SequenceFormatException("Didn't understand format name '"+System.getProperty("fastqc.sequence_format")+"'");
			}
			
		}
		
		
		// Otherwise we just use the extension on the end of the file name to try to determine
		// the type
		if (file.getName().toLowerCase().endsWith(".bam") || file.getName().toLowerCase().endsWith(".sam")) {
			// We default to using all reads
			return System.getProperty("sugar.samtools","false").equals("true") ? new FastBAMFile(file,false) : new BAMFile(file,false); 				
//			return new BAMFile(file,false);
		}
//		else if (file.getName().toLowerCase().endsWith(".compact-reads") || file.getName().toLowerCase().endsWith(".compact_reads") || file.getName().toLowerCase().endsWith(".goby")) {
//			return new GobyFile(file);
//		}
		else {
			return new FastQFile(file);
		}

		
	}
	
	
}
