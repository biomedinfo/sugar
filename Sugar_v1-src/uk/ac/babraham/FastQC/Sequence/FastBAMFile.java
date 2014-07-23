package uk.ac.babraham.FastQC.Sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.csml.tommo.sugar.sequence.FastBAMSequence;

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
public class FastBAMFile implements SequenceFile {

	private File file;
	private boolean onlyMapped;
	private long fileSize = 0;
	private long recordSize = 0;
	private long readBytes = 0;
	
	// We keep the file stream around just so we can see how far through
	// the file we've got.  We don't read from this directly, but it's the
	// only way to access the file pointer.
	private FileInputStream fis;

//	private SAMFileReader br;
	private String name;
	private Sequence nextSequence = null;
//	Iterator<SAMRecord> it;
	
	private String samHeader;
	
	Process p;
	InputStream is;
	BufferedReader reader;
	
	private boolean isBinary;
	
	String samtoolsPath;

	public String getSAMHeader(){
		return samHeader;
	}
	
	protected FastBAMFile (File file, boolean onlyMapped) throws SequenceFormatException, IOException {
		samtoolsPath = System.getProperty("sugar.samtoolspath","samtools");
		this.file = file;
		fileSize = file.length();
		name = file.getName();
		this.onlyMapped = onlyMapped;

//		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);

//		fis = new FileInputStream(file);
		
//		br = new SAMFileReader(fis);
		
		initalize();
		
		//TODO
		//temporal
		isBinary = file.toString().endsWith("bam") ? true : false; 		
//		isBinary = true;//br.isBinary();
				
//		it = br.iterator();
//		readNext();
	}
	
	public void initalize() throws IOException{
		
		System.out.println("Now using samtools based processing");
		
		ProcessBuilder pb = new ProcessBuilder(samtoolsPath,"view","-H",file.getAbsolutePath());
		
		String logFile;
		//TODO
		if(System.getProperty("os.name","unix").startsWith("Windows")){
			logFile = "NUL";
		}
		else{
			logFile = "/dev/null";
		}
		
		pb.redirectError(new File(logFile));
		p = pb.start();
		is = p.getInputStream();
		reader = new BufferedReader(new InputStreamReader(is));
		
		StringBuffer header = new StringBuffer();
		
		try {
			String line;
			while((line = reader.readLine())!=null){
				header.append(line).append("\n");
			}
		} finally {
			reader.close();
			is.close();
		}
		this.samHeader = header.toString();
		
		//http://stackoverflow.com/questions/4131225/invoke-a-unix-shell-from-java-programread-and-write-a-steady-stream-of-data-to
		//http://www.ne.jp/asahi/hishidama/home/tech/java/process.html
		pb = new ProcessBuilder(samtoolsPath,"view",file.getAbsolutePath());
		//TODO might have better solution.
		pb.redirectError(new File(logFile));
		p = pb.start();
		is = p.getInputStream();
		reader = new BufferedReader(new InputStreamReader(is));
		try {
			readNext();
		} catch (SequenceFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String name () {
		return name;
	}
		
	public int getPercentComplete() {
		try {
//			int percent = (int) (((double)fis.getChannel().position()/ fileSize)*100);
			int percent = (int)(100 * readBytes / fileSize); 
			return percent;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean isColorspace () {
		return false;
	}
		
	public boolean hasNext() {
		return nextSequence != null;
	}

	public Sequence next () throws SequenceFormatException {
		Sequence returnSeq = nextSequence;
		readNext();
		return returnSeq;
	}
	
	
	private void readNext() throws SequenceFormatException {
		
	//	SAMRecord record;
		
		String sequence;
		String qualities;
		String readname;
		int mappingquality;
		
		String line;
		
		while (true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				line = null;
			}
			
			if (line == null){
				nextSequence = null;
				return;
			}
		
			String[] content = line.split("\t");
			int flag = Integer.parseInt(content[1]);
			if(onlyMapped && (flag & (int)0x0000004) > 0){
				continue;
			}

			//TODO
			//estimate the compression ratio as 25%.
			// totalbytes/8 * 4 = totalbyte/2
			readBytes += line.length() / 4;

			sequence = content[9];
			qualities = content[10];				
			// BAM/SAM files always show sequence relative to the top strand of
			// the mapped reference so if this sequence maps to the reverse strand
			// we need to reverse complement the sequence and reverse the qualities
			// to get the original orientation of the read.
			if((flag & (int)0x00000010)>0){
				sequence = reverseComplement(sequence);
				qualities = reverse(qualities);
			}
			readname = content[0];
			mappingquality = Integer.parseInt(content[4]);
			break;
		}
		nextSequence = new FastBAMSequence(this, sequence, qualities, readname, mappingquality,line);

	}

	
	private String reverseComplement (String sequence) {
		
		char [] letters = reverse(sequence).toUpperCase().toCharArray();
		char [] rc = new char[letters.length];
		
		for (int i=0;i<letters.length;i++) {
			switch(letters[i]) {
			case 'G': rc[i] = 'C';break;
			case 'A': rc[i] = 'T';break;
			case 'T': rc[i] = 'A';break;
			case 'C': rc[i] = 'G';break;
			default: rc[i] = letters[i];
			}
		}
	
		return new String(rc);

	}
	
	private String reverse (String sequence) {
		char [] starting = sequence.toCharArray();
		char [] reversed = new char[starting.length];
		
		for (int i=0;i<starting.length;i++) {
			reversed[reversed.length-(1+i)] = starting[i];
		}
		
		return new String(reversed);
	}

	public File getFile() {
		return file;
	}
	
	public boolean isBinary() {
		return isBinary;		
	}  	
	
//	public SAMFileHeader getFileHeader() {
//		return br.getFileHeader();
//	}

	@Override
	public String getDefaultFileExtention() {
		return isBinary() ? "bam" : "sam";
	}
}
