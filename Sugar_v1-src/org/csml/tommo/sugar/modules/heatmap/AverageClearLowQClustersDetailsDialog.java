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

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.sequence.TileCoordinates;

public class AverageClearLowQClustersDetailsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8367381777752879844L;
	
	protected JPanel centerPanel;
	protected int col;
	protected MeanQualityMatrix matrix;
	TileCoordinates tc;
	
	public AverageClearLowQClustersDetailsDialog(
			JDialog parent,
			MeanQualityMatrix matrix,
			TileCoordinates tile) {
		super(parent);
		initData(tile, matrix);
				
		centerPanel = new JPanel(new BorderLayout());
		setLayout(new BorderLayout());
		rebuild();
		add(centerPanel, BorderLayout.CENTER);
		setLocation(400, 350);
//		setSize(250, 250);
		pack();
	}


	protected void initData(TileCoordinates tile, MeanQualityMatrix matrix) {
		this.tc = tile;
		this.matrix = matrix;
	}

	protected void rebuild(){
		centerPanel.removeAll();

		if (matrix != null) {
			TileQualityTableModel model = new TileQualityTableModel(matrix);
			TileQualityTable table = new TileQualityTable(model);
			centerPanel.add(table, BorderLayout.CENTER);
		}
		else{
			centerPanel.add(new JLabel("Missing data for tile=" + tc.getTile()), BorderLayout.CENTER);
		}
		
		String windowTitle = "Mean Quality for tile=" + tc.getTile() + " and neighbors";
		setTitle(windowTitle);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}	
}
