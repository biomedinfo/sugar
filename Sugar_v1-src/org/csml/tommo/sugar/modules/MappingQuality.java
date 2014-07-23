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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.csml.tommo.sugar.analysis.JSONFileSerializable;
import org.csml.tommo.sugar.analysis.JSONSerializationUtils;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.dialogs.FileOptionsPanel;
import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.MappingQualityMatrix;
import org.csml.tommo.sugar.modules.heatmap.MappingQualityCellRenderer;
import org.csml.tommo.sugar.modules.heatmap.MappingQualityResultPanel;
import org.csml.tommo.sugar.modules.heatmap.MappingQualityTableModel;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.SAMInfo;
import org.csml.tommo.sugar.sequence.SequenceCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.csml.tommo.sugar.utils.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.BAMFile;
import uk.ac.babraham.FastQC.Sequence.FastBAMFile;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;

public class MappingQuality implements SugarModule, Serializable,
		JSONFileSerializable {

	private static final String MAPPING_QUALITY_NOT_FOUND_MESSAGE = "Mapping quality data was not found in the input file. Mapping quality can be found in BAM/SAM files only.";

	/**
	 * 
	 */
	private static final long serialVersionUID = -899746876143478258L;
	
	private static final String JSON_ATTR_MAPPING_QUALITY_MATRIX_MAP_VALUES = "mappingQualityMatrixMap.values";
	private static final String JSON_ATTR_MAPPING_QUALITY_MATRIX_MAP_KEYS = "mappingQualityMatrixMap.keys";
	private static final String JSON_ATTR_MAX_SEQUENCE_LENGTH = "maxSequenceLength";

	private TileTree tileTree;
	private Map<TileCoordinates, MappingQualityMatrix> mappingQualityMatrixMap = new HashMap<TileCoordinates, MappingQualityMatrix>();
	
	private int matrixSize;
	private SequenceFile sequenceFile;

	private Integer maxSequenceLength = 0;

	public MappingQuality(TileTree tileTree, SequenceFile sequenceFile) {
		super();
		this.tileTree = tileTree;
		this.sequenceFile = sequenceFile;
		initOptions();
	}

	@Override
	public String description() {
		return "Analysis of mapping quality heatmaps";
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel result = null;
		if(isActive()){
			result = new MappingQualityResultPanel(this);			
		}
		else{
			result = createEmptyPanel();			
		}
		return result;
	}

	@Override
	public boolean ignoreFilteredSequences() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws IOException {	
					
		if(isActive()){
			SortedSet<String> flowCells = getTileTree().getFlowCells();
			TileNumeration tileNumeration = getTileTree().getTileNumeration();
			
			report.htmlDocument().append("Matrix Size: " + getMatrixSize() + "<br/>");
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
//					writeTable2HTML(report, laneCoordinates, tileNumeration);
					writeImage2HTML(report, laneCoordinates, tileNumeration);
					report.htmlDocument().append("<br/>");
				}
			}			
		}
		else{
			report.htmlDocument().append(MAPPING_QUALITY_NOT_FOUND_MESSAGE);			
		}		
	}
	
	private void writeImage2HTML(HTMLReportArchive report, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) throws IOException {
		final MappingQualityTableModel model = new MappingQualityTableModel(this, laneCoordinates, tileNumeration);
		
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
		int xOffset = 50; // space for row header
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
			int separator=0;
			
			for (int c=0;c<model.getColumnCount();c++) {
				TileCoordinates tileCoordinate = model.getCoordinateAt(r, c);
				MappingQualityMatrix matrix = getMeanQualityMatrix(tileCoordinate);	
				
				if (matrix != null) {
					if(r < MappingQualityMatrix.THRESHOLDS.length){
						ColorPaintScale paintScale = MappingQualityCellRenderer.getThresholdPaintScale();
						BufferedImage image = (BufferedImage)matrix.createBufferedImageForThreshold(MappingQualityMatrix.THRESHOLDS[r], paintScale);
						g2.drawImage(image, 1 + imgSize * c + separator, 1 + imgSize * r, imgSize * (c+1) + separator, imgSize * (r+1), 0, 0, image.getWidth(), image.getHeight(), null);						
					}
					else{
						ColorPaintScale paintScale = MappingQualityCellRenderer.getAveragePaintScale();
						BufferedImage image = (BufferedImage)matrix.createBufferedImage(paintScale);
						g2.drawImage(image, 1 + imgSize * c + separator, 1 + imgSize * r, imgSize * (c+1) + separator, imgSize * (r+1), 0, 0, image.getWidth(), image.getHeight(), null);												
					}

				}
				else{
					d.append("Missing matrix for: " + tileCoordinate + "\n");
				}
				if(c == model.getTopBottomSeparatorColumn()){
					separator = topBottomSeparator;
				}
			}
		}
		
		g2.dispose();
		String imgFileName = "quality_matrix_" + laneCoordinates.getFlowCell() + "_" + laneCoordinates.getLane() + ".png";
		zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));					
		ImageIO.write(fullImage, "png", zip);
		b.append("<img src=\"Images/" + imgFileName + "\" alt=\"full image\">\n");

		long after = System.currentTimeMillis();
		d.append("Creating report time: " + (after-before));
	}

	protected void drawRowHeader(Graphics2D g2,
			final MappingQualityTableModel model, int imgSize, int width,
			int xOffset) {
		for(int r=0;r<model.getRowCount();r++) {
			int i = r / model.getTileNumeration().getCycleSize();
			String s = i<MappingQualityMatrix.THRESHOLDS.length ? 
					"MAPQ<" + MappingQualityMatrix.THRESHOLDS[i] :
					"AVERAGE";
			
			if(model.getCycleID(r) == 0){
				int stringHeight = g2.getFont().getSize(); //g2.getFontMetrics().getHeight();
				int stringWidth = g2.getFontMetrics().stringWidth(s);
				g2.drawString(s, 1 + width + (xOffset - 1 - stringWidth) / 2, imgSize * (r+1) - (imgSize - stringHeight) / 2);
			}
		}
	}

	protected void drawColumnHeader(Graphics2D g2,
			final MappingQualityTableModel model, int imgSize,
			int topBottomSeparator, int height, StringBuffer d) {
		
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

	@Override
	public String name() {
		return "Mapping quality heatmaps per tile";
	}

	@Override
	public void processSequence(Sequence sequence) {
		if (sequence instanceof SAMInfo)
		{
			SequenceCoordinates seqCoord = SequenceCoordinates.createSequenceCoordinates(sequence);
			
			if (sequence.getQualityString().length() > maxSequenceLength){
				maxSequenceLength = sequence.getQualityString().length();
			}
			storeMappingQuality(seqCoord, (SAMInfo) sequence);
		}
	}

	private void storeMappingQuality(SequenceCoordinates seqCoord, SAMInfo seq) {
		TileCoordinates tileCoordinates = new TileCoordinates(seqCoord.getFlowCell(), seqCoord.getLane(), seqCoord.getTile());
		MappingQualityMatrix matrix = mappingQualityMatrixMap.get(tileCoordinates);
		
		Rectangle tileRange = tileTree.getRange(tileCoordinates);
		if (matrix == null)
		{
			matrix = new MappingQualityMatrix(tileRange, matrixSize);
			mappingQualityMatrixMap.put(tileCoordinates, matrix);
		}
		matrix.addQualityValue(seqCoord.getX(), seqCoord.getY(), seq.getMappingQuality());
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
	public void reset() {
		maxSequenceLength = 0;
		mappingQualityMatrixMap.clear();
	}

	@Override
	public boolean isProcessed() {
		return mappingQualityMatrixMap.size() > 0;
	}

	public void initOptions() {
		matrixSize = Options.getMatrixSize();
	}
	
	public void initOptions(FileOptionsPanel optionsPanel) {
		matrixSize = optionsPanel.getMatrixSize();
	}

	public TileTree getTileTree() {
		return tileTree;
	}
	
	public MappingQualityMatrix getMeanQualityMatrix(TileCoordinates coordinate) {
		return mappingQualityMatrixMap.get(coordinate);	
	}

	public int getMatrixSize() {
		return matrixSize;
	}
	
	public MappingQualityMatrix createMixedQualityMatrix(
			TileCoordinates tileCoordinatesTop, TileCoordinates tileCoordinatesBottom, 
			IMixOperation mixOperation) {
		
		MappingQualityMatrix result = null;		
		MappingQualityMatrix matrixTop = getMeanQualityMatrix(tileCoordinatesTop);
		MappingQualityMatrix matrixBottom = getMeanQualityMatrix(tileCoordinatesBottom); 		
		
		if (matrixTop != null && matrixBottom != null) {
			result = MappingQualityMatrix.createMixedMatrix(matrixTop, matrixBottom, mixOperation);
		}
		
		return result;
	}

	public Integer getMaxSequenceLength() {
		return maxSequenceLength;
	}

	public JPanel createEmptyPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		String message = "<html>" + MAPPING_QUALITY_NOT_FOUND_MESSAGE + "</html>";
		panel.add(new JLabel(message), BorderLayout.CENTER);
		return panel;
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
		
		// maxSequenceLength
        obj.put(JSON_ATTR_MAX_SEQUENCE_LENGTH, maxSequenceLength);
        
        // meanQualityMatrixMap
        JSONSerializationUtils.saveMapInJSONObject(obj, mappingQualityMatrixMap, "mappingQualityMatrixMap");

        return obj;

	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {

		// maxSequenceLength
		maxSequenceLength = new Integer(jsonObject.get(JSON_ATTR_MAX_SEQUENCE_LENGTH).toString());

        // tileIDsMap
		JSONArray keyArray = (JSONArray) jsonObject.get(JSON_ATTR_MAPPING_QUALITY_MATRIX_MAP_KEYS);
		JSONArray valueArray = (JSONArray) jsonObject.get(JSON_ATTR_MAPPING_QUALITY_MATRIX_MAP_VALUES);
		
		for (int i = 0 ; i < keyArray.size(); i++)
		{
			JSONObject jsonTileCoordinate = (JSONObject) keyArray.get(i);
			TileCoordinates tc = new TileCoordinates();
			tc.fromJSONObject(jsonTileCoordinate); 
			Rectangle tileRange = tileTree.getRange(tc);			
			
			JSONObject jsonMatrix = (JSONObject) valueArray.get(i);
			MappingQualityMatrix matrix = new MappingQualityMatrix(tileRange, matrixSize);
			matrix.fromJSONObject(jsonMatrix); 
			mappingQualityMatrixMap.put(tc, matrix);
			
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

	public SequenceFile getSequenceFile() {
		return sequenceFile;
	}
	
	// customized JSON Serialization
	
	public boolean isActive(){
		return sequenceFile instanceof BAMFile || sequenceFile instanceof FastBAMFile;
	}
}
