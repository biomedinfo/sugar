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
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrixChart;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.PaintScale;

public class DensityHeatmapDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7755493135986780444L;
	
	
	protected JPanel centerPanel;
	protected int row;
	protected int col;
	protected DensityResultsTable table;
	
	public DensityHeatmapDialog(int row, int col, DensityResultsTable densityResultsTable){
		super(SugarApplication.getApplication());
		initData(row, col, densityResultsTable);
				
		List<TileBPCoordinates> tileBPCoordinateList = densityResultsTable.getTileBPCoordinateList(row, col);		
		int colSpan = col == densityResultsTable.getColumnCount() - 1 ? 1 : 2;
		int rowSpan = tileBPCoordinateList.size() / colSpan;
		centerPanel = new JPanel(new GridLayout(rowSpan, colSpan));
		setLayout(new BorderLayout());
		rebuild();
		add(centerPanel, BorderLayout.CENTER);
		setLocation(400, 350);
		setSize(250 * colSpan, 250 * rowSpan);
	}

	protected void initData(int row, int col, DensityResultsTable table) {
		this.row = row;
		this.col = col;
		this.table = table;
	}

	protected void rebuild(){
		centerPanel.removeAll();
		List<TileBPCoordinates> tileBPCoordinateList = table.getTileBPCoordinateList(row, col);
		QualityHeatMapsPerTileAndBase heatmap = table.getModel().getQualityHeatMapsPerTileAndBase();
		PaintScale paintScale = DensityMatrixCellRenderer.getPaintScale(table.getMaxDensity());
		for(int i=0; i<tileBPCoordinateList.size(); i++){
			TileBPCoordinates tc = tileBPCoordinateList.get(i);
			MeanQualityMatrix matrix = heatmap.getMeanQualityMatrix(tc);
			
			if (matrix != null) {
				ChartPanel panel = MeanQualityMatrixChart.createDensityChartPanel(matrix, paintScale);
				panel.setToolTipText("tile=" + tc.getTile());
				centerPanel.add(panel);
			}
			else{
				centerPanel.add(new JLabel("Missing data for tile=" + tc.getTile()));
			}
		}
		
		TileBPCoordinates first = tileBPCoordinateList.get(0);
		String windowTitle = "Density for tile=" + first.getTile() + " and neighbors";
		setTitle(windowTitle);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}	
}
