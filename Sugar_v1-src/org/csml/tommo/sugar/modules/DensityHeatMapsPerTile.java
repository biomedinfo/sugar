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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.heatmap.DensityHeatmapResultPanel;
import org.csml.tommo.sugar.modules.heatmap.DensityMatrixCellRenderer;
import org.csml.tommo.sugar.modules.heatmap.DensityResultsTableModel;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.csml.tommo.sugar.utils.Options;

import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;

public class DensityHeatMapsPerTile implements SugarModule {
	

	private QualityHeatMapsPerTileAndBase parentModule;
	

	
	public DensityHeatMapsPerTile(QualityHeatMapsPerTileAndBase module) {
		super();
		parentModule = module;
	}
	
	@Override
	public void processSequence(Sequence sequence) {
		// do nothing 
		// the data was read by the parent already
	}
	
	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new DensityHeatmapResultPanel(parentModule);

		return returnPanel;	
	}
	
	@Override
	public String name() {
		return "Density heatmaps per tile";
	}

	@Override
	public String description() {
		return "Interactive analysis of density heatmaps for each tile";
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

	@Override
	public boolean ignoreFilteredSequences() {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws IOException {	
						
		SortedSet<String> flowCells = getTileTree().getFlowCells();
		TileNumeration tileNumeration = getTileTree().getTileNumeration();
		
		report.htmlDocument().append("Matrix Size: " + parentModule.getMatrixSize() + "<br/>");
		report.htmlDocument().append("Max. Density: " + parentModule.getMaxMatrixDensity() + "<br/>");
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

	private void writeTable2HTML(HTMLReportArchive report, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) throws IOException {
		final DensityResultsTableModel model = new DensityResultsTableModel(parentModule, laneCoordinates, tileNumeration);
		
		String flowCell = laneCoordinates.getFlowCell(); 
		Integer lane = laneCoordinates.getLane();

		ZipOutputStream zip = report.zipFile();
		StringBuffer b = report.htmlDocument();
		StringBuffer d = report.dataDocument();
		
		b.append("<table>\n");
		// Do the headers
		b.append("<tr>\n");
		d.append("#Tiles: ");
		
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
		for (int r=0;r<model.getRowCount();r++) {			
			b.append("<tr>\n");
						
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				TileCoordinates tileCoordinates = tileBPCoordinate.getTileCoordinate();
				b.append("<td>");
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);	
				if (matrix != null) {
					String imgFileName = "matrix_" + flowCell + "_" + lane + "_" + tileCoordinates.getTile() + ".png";
					zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));
					ColorPaintScale paintScale = DensityMatrixCellRenderer.getPaintScale(parentModule.getMaxMatrixDensity()); 
					BufferedImage image = (BufferedImage)matrix.createDensityBufferedImage(paintScale);
					int imgSize = Options.getHeatmapImageSize();
					BufferedImage scaledImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
					Graphics g = scaledImage.getGraphics();
					g.drawImage(image, 0, 0, imgSize, imgSize, null);
					g.dispose();
					ImageIO.write(scaledImage, "png", zip);
					
					b.append("<img src=\"Images/" + imgFileName + "\" alt=\"M[" + tileCoordinates.toString() + "]\">\n");
				}
				else{
					b.append("");
					d.append("Missing matrix for: " + tileCoordinates + "\n");
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
		final DensityResultsTableModel model = new DensityResultsTableModel(parentModule, laneCoordinates, tileNumeration);
		
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
		BufferedImage fullImage = new BufferedImage(width, height + yOffset, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)fullImage.getGraphics();
		Color headerBackground = new Color(0x00, 0x00, 0x80);
		Color headerForeground = Color.WHITE;
		Color color = g2.getColor();
		
		
		// Do the headers
		d.append("#Tiles: ");
		
		d.append("\t");
		g2.setColor(headerBackground);
		g2.fillRect(0, height, width, yOffset);
		g2.setColor(headerForeground);
		g2.setFont(g2.getFont().deriveFont(7f).deriveFont(Font.BOLD));
		
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

		long before = System.currentTimeMillis();
		
		g2.setColor(color);
		for (int r=0;r<model.getRowCount();r++) {	
			
			separator = 0;
			// "header" for base position number
			for (int c=0;c<model.getColumnCount();c++) {
				TileBPCoordinates tileBPCoordinate = model.getCoordinateAt(r, c);
				MeanQualityMatrix matrix = getMeanQualityMatrix(tileBPCoordinate);	
				
				if (matrix != null) {
					ColorPaintScale paintScale = DensityMatrixCellRenderer.getPaintScale(parentModule.getMaxMatrixDensity()); 
					BufferedImage image = (BufferedImage)matrix.createDensityBufferedImage(paintScale);
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
		String imgFileName = "density_matrix_" + laneCoordinates.getFlowCell() + "_" + laneCoordinates.getLane() + ".png";
		zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));					
		ImageIO.write(fullImage, "png", zip);
		b.append("<img src=\"Images/" + imgFileName + "\" alt=\"full image\">\n");

		long after = System.currentTimeMillis();
		d.append("Creating report time: " + (after-before));

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

	public MeanQualityMatrix createMixedQualityMatrix(
			TileBPCoordinates tileCoordinatesTop, TileBPCoordinates tileCoordinatesBottom, 
			IMixOperation mixOperation) {
		return parentModule.createMixedQualityMatrix(tileCoordinatesTop, tileCoordinatesBottom, mixOperation);
	}

		
	public int getHeatMapSize() {
		return parentModule.getHeatMapSize();
	}

}



