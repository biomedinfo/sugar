package org.csml.tommo.sugar.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.heatmap.AverageQualityHeatmapResultPanel;
import org.csml.tommo.sugar.modules.heatmap.ResultsTableModel;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.utils.Options;

import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;

public class AverageQualityHeatMapsPerTileAndBase implements SugarModule {

	private QualityHeatMapsPerTileAndBase parentModule;
	
	public AverageQualityHeatMapsPerTileAndBase(QualityHeatMapsPerTileAndBase module){
		super();
		this.parentModule = module;
	}
	
	@Override
	public void processSequence(Sequence sequence) {
		// do nothing 
		// the data was read by the parent already
	}

	@Override
	public String name() {
		return "Average quality heatmaps per tile and base position";
	}

	@Override
	public String description() {
		return "View average quality heatmaps for specified tile and base position";
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new AverageQualityHeatmapResultPanel(parentModule);
		return returnPanel;	
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

	@Override
	public void reset() {
		parentModule.reset();
	}
	
	@Override
	public boolean isProcessed() {
		return parentModule != null ? parentModule.isProcessed() : false;
	}

	@Override
	public boolean raisesError() {
		return false;
	}

	@Override
	public boolean raisesWarning() {
		return false;
	}
	
	public TileTree getTileTree(){
		return parentModule.getTileTree();
	}
	
	public int getMatrixSize(){
		return parentModule.getMatrixSize();
	}
	
	public int getQualityThreshold(){
		return parentModule.getQualityThreshold();
	}
	
	public MeanQualityMatrix getMeanQualityMatrix(TileBPCoordinates coordinate) {
		return parentModule.getMeanQualityMatrix(coordinate);
	}

	private void writeImage2HTML(HTMLReportArchive report, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) throws IOException {
		final ResultsTableModel model = new ResultsTableModel(parentModule, laneCoordinates, tileNumeration);
		
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
		parentModule.drawColumnHeader(g2, model, imgSize, topBottomSeparator, height, d);

		g2.setFont(g2.getFont().deriveFont(9f).deriveFont(Font.BOLD));
		parentModule.drawRowHeader(g2, model, imgSize, width, xOffset);		

		long before = System.currentTimeMillis();
		
		for (int r=0;r<model.getRowCount();r++) {	
			
			int separator = 0;
			// "header" for base position number
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);	
				
				if (matrix != null) {
					BufferedImage image = (BufferedImage)matrix.createAverageQualityBufferedImage(LinearPaintScale.AVERAGE_QUALITY_PAINT_SCALE);
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
		
		String imgFileName = "average_quality_matrix_" + laneCoordinates.getFlowCell() + "_" + laneCoordinates.getLane() + ".png";
		zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));					
		ImageIO.write(fullImage, "png", zip);
		b.append("<img src=\"Images/" + imgFileName + "\" alt=\"full image\">\n");
		
		// #38: Save each mean value of tile as a matrix file e.g. CSV file format
//		writeCSVFile(report, laneCoordinates, tileNumeration, model);

		long after = System.currentTimeMillis();
		d.append("Creating report time: " + (after-before));
	}

}
