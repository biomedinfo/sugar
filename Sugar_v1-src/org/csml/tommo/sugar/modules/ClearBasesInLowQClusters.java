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
package org.csml.tommo.sugar.modules;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.sequence.BAMSequence;
import org.csml.tommo.sugar.sequence.FastBAMSequence;
import org.csml.tommo.sugar.sequence.SequenceCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;

import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.BAMFile;
import uk.ac.babraham.FastQC.Sequence.FastBAMFile;
import uk.ac.babraham.FastQC.Sequence.FastQFile;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;

public class ClearBasesInLowQClusters implements SugarModule {

	static final boolean SUCCESS = true;
	static final boolean FAILED = false;
		
	private QualityHeatMapsPerTileAndBase parentModule;
	private File outputFile;
	private File boutputFile;

	private EClearLowQClustersMethod clearMethod;
	private ELowQClustersSelectionMethdod selectionMethod;
	private File lowQClustersFile;

	private BufferedWriter fastqFileWriter;
	private SAMFileWriter samFileWriter;
	private BufferedWriter fastSamFileWriter;
	
	private BufferedWriter bfastqFileWriter;
	private SAMFileWriter bsamFileWriter;
	private BufferedWriter bfastSamFileWriter;
	
	private long modifiedSeqeunces;
	private long changedBases;
	
	private long totalSeqeunces;
	private long totalBases;
	
	private static SAMFileWriterFactory samFileWriterFactory;	
		
	public ClearBasesInLowQClusters(QualityHeatMapsPerTileAndBase module, SequenceFile sequenceInputFile, 
			EClearLowQClustersMethod clearLowQClustersMethod, ELowQClustersSelectionMethdod clusterSelectionMethdod,
			File lowQClusterSelectionFile, 
			File outFile) throws IOException {
		super();
		parentModule = module;
		clearMethod = clearLowQClustersMethod;
		selectionMethod = clusterSelectionMethdod;
		lowQClustersFile = lowQClusterSelectionFile;
		
		if (outFile != null) {
			outputFile = outFile;
			boutputFile = new File(outFile.getAbsolutePath()+".fail");
		} else {
			String defaultExtention = sequenceInputFile.getDefaultFileExtention();
			outputFile = new File(sequenceInputFile.getFile().getAbsolutePath() + ".cleared." + clearLowQClustersMethod.getShortName() + "." + defaultExtention);
			boutputFile = new File(sequenceInputFile.getFile().getAbsolutePath() + ".fail." + clearLowQClustersMethod.getShortName() + "." + defaultExtention);
		}		
		
		modifiedSeqeunces = 0;
		changedBases = 0;
		totalSeqeunces = 0;
		totalBases = 0;

		if (sequenceInputFile instanceof FastQFile){
			fastqFileWriter = new BufferedWriter(new FileWriter(outputFile));
			bfastqFileWriter = new BufferedWriter(new FileWriter(boutputFile));
		}
		else if (sequenceInputFile instanceof BAMFile)
		{
			BAMFile bamFile = (BAMFile) sequenceInputFile;				
			samFileWriter  = getSAMWriterFactory().makeSAMOrBAMWriter(bamFile.getFileHeader(), true, outputFile);
			bsamFileWriter  = getSAMWriterFactory().makeSAMOrBAMWriter(bamFile.getFileHeader(), true, boutputFile);
		}
		else if (sequenceInputFile instanceof FastBAMFile)
		{
			FastBAMFile bamFile = (FastBAMFile) sequenceInputFile;				
			fastSamFileWriter  = new BufferedWriter(new FileWriter(outputFile));
			fastSamFileWriter.write(bamFile.getSAMHeader());
			
			bfastSamFileWriter  = new BufferedWriter(new FileWriter(boutputFile));
			bfastSamFileWriter.write(bamFile.getSAMHeader());
		}
	}
	
	public ClearBasesInLowQClusters(QualityHeatMapsPerTileAndBase heatMap, SequenceFile sequenceFile,
			EClearLowQClustersMethod clearLowQClustersMethod,
			ELowQClustersSelectionMethdod clusterSelectionMethdod,
			File lowQClusterSelectionFile) throws IOException {
		this(heatMap, sequenceFile, clearLowQClustersMethod, clusterSelectionMethdod, lowQClusterSelectionFile, null);
	}

	public void selectLowQClusters() throws Exception {
		
		if (selectionMethod == ELowQClustersSelectionMethdod.AUTO)
			parentModule.selectRedAreas();
		else if (selectionMethod == ELowQClustersSelectionMethdod.USER)
		{
			// do nothing, already selected
		}
		if (selectionMethod == ELowQClustersSelectionMethdod.FILE) {
			parentModule.importSelectionMatrix(lowQClustersFile);
		}
		
	}
	
	
	
	

	protected SAMFileWriterFactory getSAMWriterFactory() {
		if (samFileWriterFactory == null)
			samFileWriterFactory = new SAMFileWriterFactory();
		return samFileWriterFactory;
	}
	
	@Override
	public void processSequence(Sequence sequence) {
		
		SequenceCoordinates seqCoord = SequenceCoordinates.createSequenceCoordinates(sequence);		
		List<MeanQualityMatrix> matrixList = parentModule.getMeanQualityMatrixList(seqCoord);
		
		long changesCount = 0;
		boolean goodSequence = true; 
		byte[] outputSequence = sequence.getSequence().getBytes();
		for (int i=0; i < outputSequence.length; i++)
		{				
			if (i < matrixList.size())
			{
				MeanQualityMatrix m = matrixList.get(i);
//				if (m.getMeanQualityValue(seqCoord.getX(), seqCoord.getY()) >= 0.7)					
				if (m.isSelectedRange(seqCoord.getX(), seqCoord.getY()))
				{
					goodSequence = false;
					if (outputSequence[i] != 'N')
					{					
						outputSequence[i] = 'N';
						changesCount++;
					}
				}
			}
			else
			{
				
				// this case should not happen 
				goodSequence = false;
				outputSequence[i] = 'N';
				changesCount++;
			}				
		}
			
		
		totalBases += outputSequence.length;
		totalSeqeunces++;
		
		if (changesCount > 0) {
			changedBases += changesCount;
			modifiedSeqeunces++;			
		}
		
		
		if (clearMethod == EClearLowQClustersMethod.DELETE)
		{
			if (goodSequence){
				writeSequence(sequence,SUCCESS);
			}
			else{
				writeSequence(sequence,FAILED);
			}
		} 
		else if (clearMethod == EClearLowQClustersMethod.CHANGE)
		{
			writeSequence(sequence, outputSequence, SUCCESS);
		}
		
		
	}

	private void writeSequence(Sequence sequence,boolean success) {
		writeSequence(sequence, null, success);
		
	}

	private void writeSequence(Sequence sequence, byte[] outputSequence,boolean success) {
		if(success){
			if (fastqFileWriter != null){
				writeFastQSeqeunce(fastqFileWriter,sequence, outputSequence);
			}
			else if (samFileWriter != null){
				writeSAMSequence(samFileWriter,(BAMSequence) sequence, outputSequence);
			}
			else if (fastSamFileWriter != null){
				writeFastSAMSequence(fastSamFileWriter,(FastBAMSequence) sequence, outputSequence);
			}
		}
		else{
			if (bfastqFileWriter != null){
				writeFastQSeqeunce(bfastqFileWriter,sequence, outputSequence);
			}
			else if (bsamFileWriter != null){
				writeSAMSequence(bsamFileWriter,(BAMSequence) sequence, outputSequence);
			}
			else if (bfastSamFileWriter != null){
				writeFastSAMSequence(bfastSamFileWriter,(FastBAMSequence) sequence, outputSequence);
			}
		}
	}

	private void writeSAMSequence(SAMFileWriter writer,BAMSequence sequence, byte[] outputSequence) {
		
		SAMRecord samRecord = sequence.getSAMRecord();		
		if (outputSequence != null) {
			if (samRecord.getReadNegativeStrandFlag()) {
				outputSequence = BAMSequence.reverseComplement(outputSequence);
			}
			samRecord.setReadBases(outputSequence);
			
		}
		writer.addAlignment(samRecord);	
	}
	
	private void writeFastSAMSequence(BufferedWriter writer,FastBAMSequence sequence,byte[] outputSequence) {
		try {
			writer.write(sequence.getSAMRecord());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeFastQSeqeunce(BufferedWriter writer,Sequence sequence, byte[] outputSequence) {
		
		String seq = outputSequence == null ?
			sequence.getSequence() :
			new String(outputSequence);

		try {
			writer.write(sequence.getID());
			writer.newLine();
			writer.write(seq);
			writer.newLine();
			writer.write("+");
			writer.newLine();
			writer.write(sequence.getQualityString());
			writer.newLine();
			
			if (totalSeqeunces % 1000 == 0)
				writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(new JLabel("Clear operation information",JLabel.CENTER),BorderLayout.NORTH);
		
		TableModel model = new ResultsTable();
		returnPanel.add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
		
		return returnPanel;
	}
	
	@Override
	public String name() {
		return "Clear LowQ clusters";
	}

	@Override
	public String description() {
		return "Change the base calls to N in LowQ clusters";
	}

	@Override
	public void reset() {
		modifiedSeqeunces = 0;
		changedBases = 0;
		totalSeqeunces = 0;
		totalBases = 0;
	}

	@Override
	public boolean isProcessed() {
		return totalSeqeunces > 0;
	}

	@Override
	public boolean raisesError() {
		return false;
	}

	@Override
	public boolean raisesWarning() {
		return false;
	}

	@Override
	public boolean ignoreFilteredSequences() {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws IOException {	
						
		ResultsTable table = new ResultsTable();
		
		StringBuffer b = report.htmlDocument();
		StringBuffer d = report.dataDocument();
		
		b.append("<table>\n");
		// Do the headers
		b.append("<tr>\n");
		d.append("#");
		for (int c=0;c<table.getColumnCount();c++) {
			b.append("<th>");
			b.append(table.getColumnName(c));
			d.append(table.getColumnName(c));
			b.append("</th>\n");
			d.append("\t");
		}
		b.append("</tr>\n");
		d.append("\n");
		
		// Do the rows
		for (int r=0;r<table.getRowCount();r++) {
			b.append("<tr>\n");
			for (int c=0;c<table.getColumnCount();c++) {
				b.append("<td>");
				b.append(table.getValueAt(r, c));
				d.append(table.getValueAt(r, c));
				b.append("</td>\n");
				d.append("\t");
			}
			b.append("</tr>\n");
			d.append("\n");
		}
		
		b.append("</table>\n");
				
		
	}
	
	public TileTree getTileTree() {
		return parentModule.getTileTree();
	}
	
	public Integer getMaxSequenceLength() {
		return parentModule.getMaxSequenceLength();
	}

	public MeanQualityMatrix getMeanQualityMatrix(TileBPCoordinates coordinate) {
		
		return parentModule.getMeanQualityMatrix(coordinate);
		
	}

		
	public int getHeatMapSize() {
		return parentModule.getHeatMapSize();
	}	
	
	public EClearLowQClustersMethod getClearMethod() {
		return clearMethod;
	}

	public void setClearMethod(EClearLowQClustersMethod clearMethod) {
		this.clearMethod = clearMethod;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public File getOutputLowQFile(){
		return boutputFile;
	}

//	public void setOutputFile(File outputFile) {
//		this.outputFile = outputFile;
//	}

	public void closeWriter() {
		
		try {
			if (fastqFileWriter != null)
				fastqFileWriter.close();
			if (samFileWriter != null)
				samFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void writeClearedClusters(File outputFile) throws IOException {
		parentModule.exportSelectionMatrix(outputFile);		
	}

	private class ResultsTable extends AbstractTableModel {

		private String [] rowNames = new String [] {
				"Output Filename",
				"Total Sequences",
				"Total Bases",
				"Clear Method",
				"Modified Sequences",
				"Changed Bases",
				"Modified Sequneces Ratio",
				"Changed Bases Ratio"
		};
		
		public ResultsTable() {
			
			// correct row header names
			if (clearMethod == EClearLowQClustersMethod.DELETE)
			{
				for (int i=4; i<rowNames.length; i++)
				{
					rowNames[i] = rowNames[i].replace("Modified", "Deleted").replace("Changed", "Deleted");
				}
			}
			
		}

		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return rowNames.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: return rowNames[rowIndex];
			case 1:
				switch (rowIndex) {
				case 0 : return outputFile.getAbsolutePath();
				case 1 : return totalSeqeunces;
				case 2 : return totalBases;
				case 3 : return clearMethod.getName();				
				case 4 : return modifiedSeqeunces;
				case 5 : return changedBases;
				case 6 :
					double ratio = (double) modifiedSeqeunces / (double) totalSeqeunces;
					return ratio;
				case 7 : 
					double ratioBases = (double) changedBases / (double) totalBases;
					return ratioBases;
				}
			}
			return null;
		}

		public String getColumnName (int columnIndex) {
			switch (columnIndex) {
			case 0: return "Measure";
			case 1: return "Value";
			}
			return null;
		}
//
//		public Class<?> getColumnClass (int columnIndex) {
//			switch (columnIndex) {
//			case 0: return String.class;
//			case 1: return String.class;
//			}
//			return null;
//
//		}
	}
		
	// test cases for checking the write speed

	public static void main (String[] args) throws IOException
	{
		testWrite1();
		testWrite2();

		
	}

	public static void testWrite1() throws IOException {
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File("D:\\test.test")), 1024);
		
		long sT = System.currentTimeMillis();
		
		String s = "1234567890123456789012345678901234567890";
		
		for (int i =0; i<2*1000*1000; i++)
		{
			fileWriter.write(s);
			fileWriter.newLine();
			fileWriter.write(s);
			fileWriter.newLine();
			fileWriter.write("+");
			fileWriter.newLine();
			fileWriter.write(s);
			fileWriter.newLine();
			if (i % 1000 == 0)
				fileWriter.flush();
		}
		
		fileWriter.close();

		long fT = System.currentTimeMillis();
		long t = fT - sT;
		
		System.out.println("Write1 time:" + t);
	}
	
	public static void testWrite2() throws IOException {
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File("D:\\test2.test")));
		
		long sT = System.currentTimeMillis();
		
		String s = "1234567890123456789012345678901234567890";
		
		for (int i =0; i<2*1000*1000; i++)
		{
			StringBuffer sB = new StringBuffer();
			sB.append(s).append("\r\n").append(s).append("\r\n").append("+").append("\r\n").append(s).append("\r\n");		
			fileWriter.write(sB.toString());
		}
		
		fileWriter.close();

		long fT = System.currentTimeMillis();
		long t = fT - sT;
		
		System.out.println("Write2 time:" + t);
	}


	// test cases for checking the write speed

}



