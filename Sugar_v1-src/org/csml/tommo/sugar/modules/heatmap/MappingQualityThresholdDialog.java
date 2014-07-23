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
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.heatmap.MappingQualityMatrix;
import org.csml.tommo.sugar.heatmap.MappingQualityMatrixChart;
import org.csml.tommo.sugar.modules.MappingQuality;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.PaintScale;

public class MappingQualityThresholdDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2386802641770435788L;
	
	protected JPanel centerPanel;
	protected int row;
	protected int col;
	protected int threshold;
	protected MappingQualityTable table;
	
	public MappingQualityThresholdDialog(int row, int col, MappingQualityTable table, int threshold){
		super(SugarApplication.getApplication());
		initData(row, col, table, threshold);
				
		List<TileCoordinates> tileCoordinateList = table.getTileCoordinateList(row, col);		
		int colSpan = col == table.getColumnCount() - 1 ? 1 : 2;
		int rowSpan = tileCoordinateList.size() / colSpan;
		centerPanel = new JPanel(new GridLayout(rowSpan, colSpan));
		setLayout(new BorderLayout());
		rebuild();
		add(centerPanel, BorderLayout.CENTER);
		setLocation(400, 350);
		setSize(250 * colSpan, 250 * rowSpan);
	}

	protected void initData(int row, int col, MappingQualityTable table, int threshold) {
		this.row = row;
		this.col = col;
		this.table = table;
		this.threshold = threshold;
	}

	protected void rebuild(){
		centerPanel.removeAll();
		List<TileCoordinates> tileCoordinateList = table.getTileCoordinateList(row, col);
		MappingQuality mappingQuality = table.getModel().getMappingQuality();
		PaintScale paintScale = MappingQualityCellRenderer.getThresholdPaintScale();
		for(int i=0; i<tileCoordinateList.size(); i++){
			TileCoordinates tc = tileCoordinateList.get(i);
			MappingQualityMatrix matrix = mappingQuality.getMeanQualityMatrix(tc);
			
			if (matrix != null) {
				ChartPanel panel = MappingQualityMatrixChart.createChartPanelByThreshold(threshold, matrix, paintScale);
				panel.setToolTipText("tile=" + tc.getTile());
				centerPanel.add(panel);
			}
			else{
				centerPanel.add(new JLabel("Missing data for tile=" + tc.getTile()));
			}
		}
		
		TileCoordinates first = tileCoordinateList.get(0);
		String windowTitle = "MAPQ > " + threshold + " for tile=" + first.getTile() + " and neighbors";
		setTitle(windowTitle);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}	
}
