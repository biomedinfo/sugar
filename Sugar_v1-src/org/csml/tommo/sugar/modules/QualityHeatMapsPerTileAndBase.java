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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.JSONFileSerializable;
import org.csml.tommo.sugar.analysis.JSONSerializationUtils;
import org.csml.tommo.sugar.analysis.OpenedFileCache;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.dialogs.FileOptionsPanel;
import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.heatmap.ETileSelection;
import org.csml.tommo.sugar.modules.heatmap.MeanQualityMatrixMap;
import org.csml.tommo.sugar.modules.heatmap.QualityHeatmapResultPanel;
import org.csml.tommo.sugar.modules.heatmap.ResultsTableModel;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.SequenceCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.csml.tommo.sugar.utils.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;

public class QualityHeatMapsPerTileAndBase implements SugarModule, Serializable, JSONFileSerializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 812269123732433522L;
	private static final String JSON_ATTR_MEAN_QUALITY_MATRIX_MAP_VALUES = "meanQualityMatrixMap.values";
	private static final String JSON_ATTR_MEAN_QUALITY_MATRIX_MAP_KEYS = "meanQualityMatrixMap.keys";
	private static final String JSON_ATTR_MAX_SEQUENCE_LENGTH = "maxSequenceLength";
	private static final String JSON_ATTR_MAX_MATRIX_DENSITY = "maxMatrixDensity";
	private static final Object JSON_ATTR_MATRIX_SIZE = "matrixSize";
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private static final Object JSON_ATTR_QUALITY_THRESHOLD = "qualityThreshold";
	
	public static double MAX_LOWQ_READS_RATIO = 0.7;
	
	public static final String MESSAGE_MIXED_MODE_NOT_SUPPORTED = "Mixed mode is not supported. Top and bottom tiles are not defined for the resolved tile numeration: ";
	public static final String MESSAGE_NO_MIXED_RESULTS = "Mixed results are empty. Corresponding top and bottom tiles were not found.";



	protected TileTree tileTree;

	/**
	 * 
	 * maximum sequence length	 
	 * 
	 * Assumes that different reads have have the same or similar length
	 *  
	 */
	private Integer maxSequenceLength = 0;

	
	/**
	 * 
	 * maximum density at a matrix point
	 * 
	 */
	private Integer maxMatrixDensity = 0;

	protected int matrixSize;
	
	protected int qualityThreshold;	
//	protected Integer[] qualityThresholdArray;

	protected MeanQualityMatrixMap meanQualityMatrixMap = new MeanQualityMatrixMap();
	
	
	/**
	 * Gathers all quality heatmaps for each tile and base position
	 * for various threshold values
	 * 
	 * "Internal cache" 
	 * 
	 * used for changing thresholds 
	 */
	private Map<Integer, MeanQualityMatrixMap> thresholdQualityMatrixMap = new HashMap<Integer, MeanQualityMatrixMap>();

		
	public QualityHeatMapsPerTileAndBase(int threshold) {
		super();
		tileTree = new TileTree();
		qualityThreshold = threshold;
		initOptions();
	}
	
	@Override
	public void processSequence(Sequence sequence) {
		{
			SequenceCoordinates seqCoord = SequenceCoordinates.createSequenceCoordinates(sequence);
						
			if (sequence.getQualityString().length() > maxSequenceLength)
				maxSequenceLength = sequence.getQualityString().length();
			
			// store the quality in the main map for the selected threshold value
			storeSequenceQuality(seqCoord, sequence, meanQualityMatrixMap, qualityThreshold);
			
			// #40: Quality Heatmaps for multiple QV thersholds, e.g. QV = 1,2,3,....,49,50
			// store the store the quality for various thresholds given by the range
//			for (int threshold : qualityThresholdArray)
//			{
//				// is main threshold value
//				if (threshold == qualityThreshold)
//				{
//					thresholdQualityMatrixMap.put(qualityThreshold, meanQualityMatrixMap);
//					continue;					
//				}
//								
//				MeanQualityMatrixMap matrixMap = thresholdQualityMatrixMap.get(threshold);
//				if (matrixMap == null)
//				{
//					matrixMap = new MeanQualityMatrixMap();
//					thresholdQualityMatrixMap.put(threshold, matrixMap);
//				}
//				storeSequenceQuality(seqCoord, sequence, matrixMap, threshold);
//			}
		}
	}

	private void storeSequenceQuality(SequenceCoordinates seqCoord, Sequence seq, MeanQualityMatrixMap matrixMap, int threshold) {
		TileCoordinates tileCoordinates = new TileCoordinates(seqCoord.getFlowCell(), seqCoord.getLane(), seqCoord.getTile());
		List<MeanQualityMatrix> matrixList = matrixMap.get(tileCoordinates);
		if (matrixList == null)
		{
			matrixList = new ArrayList<MeanQualityMatrix>();
			matrixMap.put(tileCoordinates, matrixList);
		}
		
		char[] chars = seq.getQualityString().toCharArray();
		
		Rectangle tileRange = tileTree.getRange(tileCoordinates);
		for (int i=0; i < chars.length; i++)
		{
			MeanQualityMatrix matrix = null;
			if(i < matrixList.size()){
				matrix = matrixList.get(i);
			}
			if (matrix == null)
			{
				matrix = new MeanQualityMatrix(tileRange, matrixSize, threshold);
				matrixList.add(matrix);
			}
			
			PhredEncoding phredEncoding = tileTree.getPhredEncoding();
			int total = matrix.addQualityValue(seqCoord.getX(), seqCoord.getY(), phredEncoding.char2QualityScore(chars[i]));
			if (total > maxMatrixDensity)
				maxMatrixDensity = total;
		}
				
	}
	
	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new QualityHeatmapResultPanel(this);

		return returnPanel;	
	}
	
	@Override
	public String name() {
		return "Quality heatmaps per tile and base position";
	}

	@Override
	public String description() {
		return "Interactive analysis of quality heatmaps for specified tile and base position";
	}

	@Override
	public void reset() {
		maxSequenceLength = 0;
		maxMatrixDensity = 0;
		meanQualityMatrixMap.clear();
		thresholdQualityMatrixMap.clear();
	}
	
	@Override
	public boolean isProcessed() {
		return meanQualityMatrixMap.size() > 0;
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
						
		SortedSet<String> flowCells = getTileTree().getFlowCells();
		TileNumeration tileNumeration = getTileTree().getTileNumeration();

		report.htmlDocument().append("Matrix Size: " + getMatrixSize() + "<br/>");
		report.htmlDocument().append("Quality Threshold: " + getQualityThreshold() + "<br/>");
		report.htmlDocument().append("Tile Numeration: " + tileNumeration.getName() + "<br/>");		
		
		report.htmlDocument().append("<br/>");
		
		for (String flowCell : flowCells)
		{
			SortedSet<Integer> lanes = getTileTree().getLanes(flowCell);
			
			report.htmlDocument().append("FlowCell: " + flowCell + "<br/>");
			
			for (Integer lane : lanes)
			{
				report.htmlDocument().append("Lane: " + lane + "<br/>");				
				
				LaneCoordinates laneCoordinates = new LaneCoordinates(flowCell, lane);				
//				writeTable2HTML(report, laneCoordinates, tileNumeration);
				writeImage2HTML(report, laneCoordinates, tileNumeration);		
				report.htmlDocument().append("<br/>");
			}
		}
	}

	private void writeCSVFile(HTMLReportArchive report,
			LaneCoordinates laneCoordinates, TileNumeration tileNumeration, ResultsTableModel model) throws IOException {

		String flowCell = laneCoordinates.getFlowCell(); 
		Integer lane = laneCoordinates.getLane();

		ZipOutputStream zip = report.zipFile();
		
		Double[][] result = new Double[model.getRowCount()][model.getColumnCount()];
		
		for (int r=0;r<model.getRowCount();r++) {			
			int bp = model.getBasePosition(r);
			
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);
				
				if (matrix != null) {
					result[r][c] = matrix.getMeanRatio();					
				}
				else{
					result[r][c] = -1.0;
				}
				
			}
		}
		
		String csvFileName = "mean_lowq_reads_ratio_" + flowCell + "_" + lane + ".txt";
		zip.putNextEntry(new ZipEntry(report.folderName() + "/" + csvFileName));

		String LINE_SEPARATOR = System.getProperty("line.separator");
		String COLUMN_SEPARATOR = "\t";

		
		// write column header
		for (int c=0;c<model.getColumnCount();c++) {
			
			if (c==0)
			{
				String s = "Tiles:" + COLUMN_SEPARATOR;
				zip.write(s.getBytes());
			}
				
			String s = model.getTile(c)+COLUMN_SEPARATOR;
			zip.write(s.getBytes());
		}
		
		zip.write(LINE_SEPARATOR.getBytes());

		
		for (int r=0;r<result.length;r++) {			
			String s = model.getBasePosition(r)+COLUMN_SEPARATOR;			
			zip.write(s.getBytes());
			
			for (int c=0;c<model.getColumnCount();c++) {
				s = result[r][c].toString()+COLUMN_SEPARATOR;			
				zip.write(s.getBytes());
			}
			
			zip.write(LINE_SEPARATOR.getBytes());

		}
		
	}

	/*
	 * Old version write an HTML table with one image in each cell 
	 * - writes too many images  
	 */
	@Deprecated
	private void writeTable2HTML(HTMLReportArchive report, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) throws IOException {
		final ResultsTableModel model = new ResultsTableModel(this, laneCoordinates, tileNumeration);
		
		String flowCell = laneCoordinates.getFlowCell(); 
		Integer lane = laneCoordinates.getLane();

		ZipOutputStream zip = report.zipFile();
		StringBuffer b = report.htmlDocument();
		StringBuffer d = report.dataDocument();
		
		b.append("<table>\n");
		// Do the headers
		b.append("<tr>\n");
		d.append("#Tiles: ");
		
		// "row header" for base position number
		b.append("<th></th>\n");
		d.append("\t");
		for (int c=0;c<model.getColumnCount();c++) {
			b.append("<th>");
			b.append(model.getColumnName(c));
			d.append(model.getColumnName(c));
			b.append("</th>\n");
			d.append("\t");
			
			if (c == model.getTopBottomSeparatorColumn())
			{
				b.append("<th>");
				b.append("Top-Bottom");
				d.append("Top-Bottom");
				b.append("</th>\n");
				d.append("\t");
			}				
		}
		b.append("</tr>\n");
		d.append("\n");

		long before = System.currentTimeMillis();
		
		// Do the rows
		d.append("#Base Positions: 1-" + maxSequenceLength);
		d.append("\n");				
		for (int r=0;r<model.getRowCount();r++) {			
			b.append("<tr>\n");
			
			// "header" for base position number
			b.append("<th>");
			int bp = model.getBasePosition(r);
			b.append( model.getCycleID(r)==0 ? 
					bp : "");			
			b.append("</th>\n");			
			
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				TileCoordinates tileCoordinates = tileBPCoordinate.getTileCoordinate();
				b.append("<td>");
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);	

				if (matrix != null) {
					String imgFileName = "matrix_" + flowCell + "_" + lane + "_" + tileCoordinates.getTile() + "_" + tileBPCoordinate.getBasePosition() + ".png";
					zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));					
					BufferedImage image = (BufferedImage)matrix.createBufferedImage(LinearPaintScale.PAINT_SCALE);
					int imgSize = Options.getHeatmapImageSize();
					BufferedImage scaledImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
					Graphics g = scaledImage.getGraphics();
					g.drawImage(image, 0, 0, imgSize, imgSize, null);
					g.dispose();
					ImageIO.write(scaledImage, "png", zip);
					
					b.append("<img src=\"Images/" + imgFileName + "\" alt=\"M[" + tileBPCoordinate.toString() + "]\">\n");
				}
				else{
					b.append("");
					d.append("Missing matrix for: " + tileBPCoordinate + "\n");
				}
				b.append("</td>\n");
				
				if (c == model.getTopBottomSeparatorColumn())
				{
					b.append("<td></td>\n");
				}
			}
			b.append("</tr>\n");
		}
		long after = System.currentTimeMillis();
		d.append("Creating report time: " + (after-before));

		b.append("</table>\n");
	}

	private void writeImage2HTML(HTMLReportArchive report, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) throws IOException {
		final ResultsTableModel model = new ResultsTableModel(this, laneCoordinates, tileNumeration);
		
		ZipOutputStream zip = report.zipFile();
		StringBuffer b = report.htmlDocument();
		StringBuffer d = report.dataDocument();
		
		int imgSize = Options.getHeatmapImageSize() + 1; // add one pixel for internal grid
		int width = imgSize * model.getColumnCount() + 1; // add one pixel for left border
		int topBottomSeparator = 50;
		if(model.getTopBottomSeparatorColumn() > 0){
			width += topBottomSeparator; // add top bottom separator
		}
		int height = imgSize * model.getRowCount() + 1; // add one pixel for top border
		int yOffset = 10 * model.getTileNumeration().getCycleSize(); // space for column header
		int xOffset = 20; // space for row header
		BufferedImage fullImage = new BufferedImage(width + xOffset, height + yOffset, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)fullImage.getGraphics();
		Color headerBackground = new Color(0x00, 0x00, 0x80);
		Color headerForeground = Color.WHITE;		
		
		// Do the headers
		d.append("#Tiles: ");
		
		d.append("\t");
		g2.setColor(headerBackground);
		g2.fillRect(0, height, width + xOffset, yOffset);
		g2.fillRect(width, 0, xOffset, height + yOffset);
		g2.setColor(headerForeground);
		g2.setFont(g2.getFont().deriveFont(7f).deriveFont(Font.BOLD));		
		drawColumnHeader(g2, model, imgSize, topBottomSeparator, height, d);

		g2.setFont(g2.getFont().deriveFont(9f).deriveFont(Font.BOLD));
		drawRowHeader(g2, model, imgSize, width, xOffset);		

		long before = System.currentTimeMillis();
		
		for (int r=0;r<model.getRowCount();r++) {	
			
			int separator = 0;
			// "header" for base position number
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);	
				
				if (matrix != null) {
					BufferedImage image = (BufferedImage)matrix.createBufferedImage(LinearPaintScale.PAINT_SCALE);
					g2.drawImage(image, 1 + imgSize * c + separator, 1 + imgSize * r, imgSize * (c+1) + separator, imgSize * (r+1), 0, 0, image.getWidth(), image.getHeight(), null);
				}
				else{
					d.append("Missing matrix for: " + tileBPCoordinate + "\n");
				}
				if(c == model.getTopBottomSeparatorColumn()){
					separator = topBottomSeparator;
				}
			}
		}
		
	
		g2.dispose();
		
		String imgFileName = "matrix_" + laneCoordinates.getFlowCell() + "_" + laneCoordinates.getLane() + ".png";
		zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));					
		ImageIO.write(fullImage, "png", zip);
		b.append("<img src=\"Images/" + imgFileName + "\" alt=\"full image\">\n");
		
		// #38: Save each mean value of tile as a matrix file e.g. CSV file format
		writeCSVFile(report, laneCoordinates, tileNumeration, model);

		long after = System.currentTimeMillis();
		d.append("Creating report time: " + (after-before));

	}

	void drawRowHeader(Graphics2D g2, final ResultsTableModel model,
			int imgSize, int width, int xOffset) {
		for(int r=0;r<model.getRowCount();r++) {
			int bp = model.getBasePosition(r);
			if(model.getCycleID(r) == 0){
				String s = String.valueOf(bp);
				int stringHeight = g2.getFont().getSize(); //g2.getFontMetrics().getHeight();
				int stringWidth = g2.getFontMetrics().stringWidth(s);
				g2.drawString(s, 1 + width + (xOffset - 1 - stringWidth) / 2, imgSize * (r+1) - (imgSize - stringHeight) / 2);
			}
		}
	}

	void drawColumnHeader(Graphics2D g2,
			final ResultsTableModel model, int imgSize, int topBottomSeparator,
			int height, StringBuffer d) {
		// draw Top-Bottom header
		if(model.getTopBottomSeparatorColumn() > 0){
			String s = "Top-Bottom";
			int stringWidth = g2.getFontMetrics().stringWidth(s);
			g2.drawString(s, 1 + imgSize * (model.getTopBottomSeparatorColumn() + 1) + (topBottomSeparator - 1 - stringWidth) / 2, 1 + height + 10 - 3);			
		}
		
		int separator = 0;
		for(int c=0;c<model.getColumnCount();c++) {
			d.append(model.getColumnName(c));
			d.append("\t");
			
			if (c == model.getTopBottomSeparatorColumn())
			{
				d.append("Top-Bottom");
				d.append("\t");
			}
			Integer[] tiles = model.getTilesForColumn(c);
			for(int i=0; i<tiles.length; i++){
				String s = tiles[i].toString();
				int stringWidth = g2.getFontMetrics().stringWidth(s);
				g2.drawString(s, 1 + imgSize * c + (imgSize - 1 - stringWidth) / 2 + separator, 1 + height + 10 * (i+1) - 3);				
			}
			if(c == model.getTopBottomSeparatorColumn()){
				separator = topBottomSeparator;
			}
		}
		d.append("\n");
	}

	public TileTree getTileTree() {
		return tileTree;
	}
	
	public Integer getMaxSequenceLength() {
		return maxSequenceLength;
	}

	public MeanQualityMatrix getMeanQualityMatrix(TileBPCoordinates coordinate) {
		
		TileCoordinates tileCoordinate = coordinate.getTileCoordinate();
		int index = coordinate.getBasePosition() - 1;
		return meanQualityMatrixMap.containsKey(tileCoordinate) && index < meanQualityMatrixMap.get(tileCoordinate).size() ? 
				meanQualityMatrixMap.get(tileCoordinate).get(index) : 
				null;
	}

	public MeanQualityMatrix createMixedQualityMatrix(
			TileBPCoordinates tileCoordinatesTop, TileBPCoordinates tileCoordinatesBottom, 
			IMixOperation mixOperation) {
		
		MeanQualityMatrix result = null;		
		MeanQualityMatrix matrixTop = getMeanQualityMatrix(tileCoordinatesTop);
		MeanQualityMatrix matrixBottom = getMeanQualityMatrix(tileCoordinatesBottom); 		
		
		if (matrixTop != null && matrixBottom != null) {
			result = MeanQualityMatrix.createMixedMatrix(matrixTop, matrixBottom, mixOperation);
		}
		
		return result;
	}

	// customized JSON Serialization

	@Override
	public String toJSONString() {
        JSONObject obj = toJSONObject();
        return obj.toString();		        
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
        JSONObject obj = toJSONObject();
        JSONValue.writeJSONString(obj, out);		
	}

	@Override
	public JSONObject toJSONObject() {
		
		JSONObject obj = new JSONObject();
		
		// matrixSize
        obj.put(JSON_ATTR_MATRIX_SIZE, matrixSize);

		// qualityThreshold
        obj.put(JSON_ATTR_QUALITY_THRESHOLD, qualityThreshold);
        
		// maxSequenceLength
        obj.put(JSON_ATTR_MAX_SEQUENCE_LENGTH, maxSequenceLength);
                
		// maxMatrixDensity
        obj.put(JSON_ATTR_MAX_MATRIX_DENSITY, maxMatrixDensity);

        // meanQualityMatrixMap
        JSONSerializationUtils.saveMapInJSONObject(obj, meanQualityMatrixMap, "meanQualityMatrixMap");

        return obj;

	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
		// matrixSize
		if (jsonObject.get(JSON_ATTR_MATRIX_SIZE) != null)
			matrixSize = new Integer(jsonObject.get(JSON_ATTR_MATRIX_SIZE).toString());

		// qualityThreshold
		if (jsonObject.get(JSON_ATTR_QUALITY_THRESHOLD) != null)
			qualityThreshold = new Integer(jsonObject.get(JSON_ATTR_QUALITY_THRESHOLD).toString());

		// maxSequenceLength
		maxSequenceLength = new Integer(jsonObject.get(JSON_ATTR_MAX_SEQUENCE_LENGTH).toString());

		// maxMatrixDensity
		maxMatrixDensity = new Integer(jsonObject.get(JSON_ATTR_MAX_MATRIX_DENSITY).toString());

        // tileIDsMap
		JSONArray keyArray = (JSONArray) jsonObject.get(JSON_ATTR_MEAN_QUALITY_MATRIX_MAP_KEYS);
		JSONArray valueArray = (JSONArray) jsonObject.get(JSON_ATTR_MEAN_QUALITY_MATRIX_MAP_VALUES);
		
		for (int i = 0 ; i < keyArray.size(); i++)
		{
			JSONObject jsonTileCoordinate = (JSONObject) keyArray.get(i);
			TileCoordinates tc = new TileCoordinates();
			tc.fromJSONObject(jsonTileCoordinate); 
			Rectangle tileRange = tileTree.getRange(tc);			
			List<MeanQualityMatrix> matrixList = new ArrayList<MeanQualityMatrix>();
			JSONArray array = (JSONArray) valueArray.get(i);
			for (Object o : array)
			{
				MeanQualityMatrix matrix = new MeanQualityMatrix(tileRange, matrixSize, qualityThreshold);
				matrix.fromJSONObject((JSONObject) o); 
				matrixList.add(matrix);
			}
			meanQualityMatrixMap.put(tc, matrixList);
			
		}			

	}

	@Override
	public void toJSONFile(File file) throws IOException {	
		JSONSerializationUtils.writeJSONFile(file, this);
	}

	@Override
	public void fromJSONFile(File file) throws IOException, ParseException {		
		JSONSerializationUtils.fromJSONFile(file, this);		
	}
	
	// customized JSON Serialization
	
	public void initOptions(FileOptionsPanel optionsPanel) {
		matrixSize = optionsPanel.getMatrixSize();		
	}

	public void initOptions() {
		matrixSize = Options.getMatrixSize();
	}
	
	public int getMatrixSize() {
		return matrixSize;
	}

	public int getQualityThreshold() {
		return qualityThreshold;
	}

	public int getHeatMapSize() {
		return SugarApplication.getApplication().getTileHeatmapSize();
	}

	public int getMaxMatrixDensity() {
		return maxMatrixDensity;
	}

	public List<MeanQualityMatrix> getMeanQualityMatrixList(TileCoordinates tile) {
		return meanQualityMatrixMap.get(tile);		
	}

	public List<MeanQualityMatrix> getMeanQualityMatrixList(SequenceCoordinates seqCoord) {
		 
		TileCoordinates tileCoordinates = new TileCoordinates(seqCoord.getFlowCell(), seqCoord.getLane(), seqCoord.getTile());
		return meanQualityMatrixMap.get(tileCoordinates);
		
	}
	
	private static final String JSON_ATTR_SELECTION_MATRIX_MAP = "selectionMatrixMap";
	private static final String JSON_ATTR_SELECTION_MATRIX_SIZE = "selectionMatrixSize";

	private static final String QUALITY_THRESHOLD_PROPERTY = "Quality_Threshold_Property";

	
	public void exportSelectionMatrix(File file) throws IOException {

		JSONObject obj = new JSONObject();
		
		//matrixSize
        obj.put(JSON_ATTR_SELECTION_MATRIX_SIZE, matrixSize);

        // get selection
		Map<TileBPCoordinates, boolean[][]> selectionMatrixMap = new HashMap<TileBPCoordinates, boolean[][]>();
		
		// for all tiles
		for (TileCoordinates tile : meanQualityMatrixMap.keySet())
		{
			List<MeanQualityMatrix> meanQualityMatrixList = meanQualityMatrixMap.get(tile);
			//for all base positions			
			for (int i=0; i<meanQualityMatrixList.size() ; i++)
			{
				MeanQualityMatrix m = meanQualityMatrixList.get(i);				
				if (m != null && m.isAnythingSelected())
				{					
					selectionMatrixMap.put(new TileBPCoordinates(tile, i+1), m.getSelectedEntries());
				}
			}
		}

		// write selection to JSON
		JSONArray keyArray = new JSONArray();
		JSONArray valueArray = new JSONArray();
		
		for(TileBPCoordinates tilebp : selectionMatrixMap.keySet()) {
			keyArray.add(tilebp);
			
			JSONArray selectionArray = JSONSerializationUtils.matrix2json(selectionMatrixMap.get(tilebp)); 
			valueArray.add(selectionArray);
		}		
		
        obj.put(JSON_ATTR_SELECTION_MATRIX_MAP + ".keys", keyArray);
        obj.put(JSON_ATTR_SELECTION_MATRIX_MAP + ".values", valueArray);


		
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));			
	        JSONValue.writeJSONString(obj, writer);		
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}					


	}

	public void importSelectionMatrix(File file) throws Exception {
		
		
		Reader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));			
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(reader);

			//matrixSize
			int size = new Integer(obj.get(JSON_ATTR_SELECTION_MATRIX_SIZE).toString());
			if (size != matrixSize)
				throw new Exception("Different Size of selection matrix: " + size);
			
			// get selection
			Map<TileBPCoordinates, boolean[][]> selectionMatrixMap = new HashMap<TileBPCoordinates, boolean[][]>();

			// write selection to JSON
			JSONArray keyArray = (JSONArray) obj.get(JSON_ATTR_SELECTION_MATRIX_MAP + ".keys");
			JSONArray valueArray = (JSONArray) obj.get(JSON_ATTR_SELECTION_MATRIX_MAP + ".values");
			

			for (int i = 0 ; i < keyArray.size(); i++)
			{
				JSONObject jsonTileCoordinate = (JSONObject) keyArray.get(i);
				TileBPCoordinates tc = new TileBPCoordinates();
				tc.fromJSONObject(jsonTileCoordinate); 
//				Rectangle tileRange = tileTree.getRange(tc);
				
				MeanQualityMatrix matrix = getMeanQualityMatrix(tc);
				
				JSONArray selectionArray = (JSONArray) valueArray.get(i);
				JSONSerializationUtils.json2matrix(selectionArray, matrix.getSelectedEntries());

			}
			

		}

		finally{
			if(reader != null){
				reader.close();
			}
		}					

		
	}

	public void selectRedAreas() {
		
		// for all tiles
		for (TileCoordinates tile : meanQualityMatrixMap.keySet())
		{
			List<MeanQualityMatrix> meanQualityMatrixList = meanQualityMatrixMap.get(tile);
			//for all base positions			
			for (int i=0; i<meanQualityMatrixList.size() ; i++)
			{
				MeanQualityMatrix m = meanQualityMatrixList.get(i);
				m.getMeanValues();
				m.selectRedArea();
			}
		}

		
	}
	
	/**
	 * 
	 * check if the tile is selected
	 * checks only the first matrix in the list!!!
	 * 
	 * returns three value:
	 * - WHOLE_TILE_SELECTED 
	 * - PART_TILE_SELECTED
	 * - NOTHING_SELECTED  
	 * 
	 * @param i
	 * @return
	 */
	public ETileSelection getTileSelection(TileCoordinates tile) {
				
		ETileSelection result = ETileSelection.NONE; 
		MeanQualityMatrix m = getFirstTileMatrix(tile);
		if(m != null){
			
			result = ETileSelection.getTileSelection(m.getSelectedEntries());
		}

		return result;
	
	}
	
	/**
	 * 
	 * get the first matrix for the given tile
	 * 
	 * @param i
	 * @return
	 */
	public MeanQualityMatrix getFirstTileMatrix(TileCoordinates tile) {
		
		MeanQualityMatrix result = null;
		
		List<MeanQualityMatrix> matrixList = meanQualityMatrixMap.get(tile);		
		
		if (matrixList != null)
		{
			for (MeanQualityMatrix m : matrixList){
				if (m != null)
				{
					result = m;
					// first matrix read successfully, so break 
					break;
				}
			}
			
		}
		
		return result;
	}
	
	// update selection of first matrix in tile
	public void expandTileSelection() {
		for (TileCoordinates tile : meanQualityMatrixMap.keySet())
		{
			expandTileSelection(tile);
		}
	}

	// update selection of first matrix in tile
	public void expandTileSelection(TileCoordinates tile) {
		List<MeanQualityMatrix> meanQualityMatrixList = meanQualityMatrixMap.get(tile);

		MeanQualityMatrix firstMatrix = null;
		//for all base positions			
		for (MeanQualityMatrix m : meanQualityMatrixList)
		{				
			if (m == null)
				continue;

			if (firstMatrix == null){
				firstMatrix = m;
			}
			else {
				for (int i =0; i<matrixSize; i++)
				{
					for (int j =0; j<matrixSize; j++)
					{
						if (m.isEntrySelected(i, j))
							firstMatrix.setSelectedEntry(i, j, true);
					}
				}
			}
		}
	}
	
	public ActionListener createThresholdChangedListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
				JComboBox qualityCombo = (JComboBox) event.getSource();
				
		        Integer threshold = (Integer) qualityCombo.getSelectedItem();
		        
		        QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase = QualityHeatMapsPerTileAndBase.this; 
		        		        				
		        if (qualityHeatMapsPerTileAndBase.changeQualityMatrixMap(threshold)) {
		        	
		        	// loaded successfully
		        	// do nothing
		        	// the table will be updated in propertChanged() listener 
		        	
			        			        
		        } else {
		        	// load failed
		        	
		        	// display info
		        	JOptionPane.showMessageDialog(SugarApplication.getApplication(), "Failed to load module from cache for threshold: " + threshold, "Load Error", JOptionPane.ERROR_MESSAGE);

		        	// delete the failed threshold
		        	qualityCombo.removeItem(threshold);
		        	
			        QCModule[] module = new QCModule[] {qualityHeatMapsPerTileAndBase};

			        // revert to old value
					int originalThreshold = qualityHeatMapsPerTileAndBase.getQualityThreshold();
					int matrixSize = qualityHeatMapsPerTileAndBase.getMatrixSize();
					File file = SugarApplication.getApplication().getSelectedSequenceFile().getFile();		        	
		        	
		        	OpenedFileCache.getInstance().readModulesFromCache(file, module, null, matrixSize, originalThreshold);
		        	qualityCombo.setSelectedItem(originalThreshold);		        	

		        }
			}
		};
	}

	public boolean changeQualityMatrixMap(Integer threshold) {
		boolean result = false;
		
		if (threshold == qualityThreshold) {
			return true;			
		}

		int oldThreshold = qualityThreshold;

		// the module is replaced by a new one with a different threshold. Keep the previous one in "local cache".
		this.keepQualityMatrixMap();			

        // first try to load from the internal map		
		if (thresholdQualityMatrixMap.containsKey(threshold))
		{
			meanQualityMatrixMap = thresholdQualityMatrixMap.get(threshold);
			result = true;
		}
		else {
	        
			meanQualityMatrixMap = new MeanQualityMatrixMap();
	        QCModule[] module = new QCModule[] {this};

			int matrixSize = this.getMatrixSize();
			File file = SugarApplication.getApplication().getSelectedSequenceFile().getFile();
	        
	        if (OpenedFileCache.getInstance().readModulesFromCache(file, module, null, matrixSize, threshold)) {
	        	result = true;
	        }
		}
		
		if (result) {
        	// update threshold
			qualityThreshold = threshold;
			fireQualityThresholdPropertyChanged(oldThreshold, qualityThreshold);
		}			
		
		return result;
	}		

	public void keepQualityMatrixMap() {
				
		if (!thresholdQualityMatrixMap.containsKey(qualityThreshold))
		{
			thresholdQualityMatrixMap.put(qualityThreshold, meanQualityMatrixMap);			
		}		
		
	}
	
	public void addOualityThresholdPropertyChangeListener(
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(QUALITY_THRESHOLD_PROPERTY, listener);		
	}
	
	public void fireQualityThresholdPropertyChanged(Integer oldValue, Integer newValue) {
		
		if (oldValue != newValue)
			propertyChangeSupport.firePropertyChange(QUALITY_THRESHOLD_PROPERTY, oldValue, newValue);
	}

	
}



