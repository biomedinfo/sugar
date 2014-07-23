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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;

public class ClearLowQClustersDetailsDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8367381777752879844L;
	
	protected JButton prevButton;
	protected JButton nextButton;
	protected JButton animationButton;
	protected JPanel centerPanel;
	protected int row;
	protected int col;
	protected ResultsTable table;
	protected Timer timer;
	
	public ClearLowQClustersDetailsDialog(JDialog parent, int row, int col, ResultsTable table){
		super(parent);
		initData(row, col, table);
		timer = new Timer(1000, this);
				
		centerPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = createButtonsPanel();
		setLayout(new BorderLayout());
		rebuild();
		add(centerPanel, BorderLayout.CENTER);
//		add(buttonsPanel, BorderLayout.SOUTH);
		setLocation(400, 350);
//		setSize(250, 250);
		pack();
	}

	protected void initData(int row, int col, ResultsTable table) {
		this.row = row;
		this.col = col;
		this.table = table;
	}

	protected JPanel createButtonsPanel() {
		JPanel bottomPanel = new JPanel(new FlowLayout());
		prevButton = new JButton("Previous");
		prevButton.addActionListener(this);
		nextButton = new JButton("Next");
		nextButton.addActionListener(this);
		animationButton = new JButton("Start Animation");
		animationButton.addActionListener(this);
		bottomPanel.add(prevButton);
		bottomPanel.add(animationButton);
		bottomPanel.add(nextButton);
		return bottomPanel;
	}

	protected void rebuild(){
		centerPanel.removeAll();
		TileBPCoordinates tc = table.getModel().getCoordinateAt(row, col);
		QualityHeatMapsPerTileAndBase heatmap = table.getModel().getQualityHeatMapsPerTileAndBase();
		MeanQualityMatrix matrix = heatmap.getMeanQualityMatrix(tc);

		if (matrix != null) {
			TileQualityTableModel model = new TileQualityTableModel(matrix);
			TileQualityTable table = new TileQualityTable(model);
			centerPanel.add(table, BorderLayout.CENTER);
		}
		else{
			centerPanel.add(new JLabel("Missing data for tile=" + tc.getTile() + " , bp=" + tc.getBasePosition()), BorderLayout.CENTER);
		}
		
		String windowTitle = "Mean Quality for tile=" + tc.getTile() + " , bp=" + tc.getBasePosition() + " and neighbors";
		setTitle(windowTitle);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == prevButton){
			prevCycle();
		} 
		if(e.getSource() == nextButton){
			nextCycle();
		} 
		else if(e.getSource() == animationButton){
			if(timer.isRunning()){
				timer.stop();
				animationButton.setText("Start Animation");
			}
			else{
				timer.start();
				animationButton.setText("Stop Animation");
			}
		}
		else if(e.getSource() == timer){
			nextCycle();
		}
	}

	protected void nextCycle() {
		int cycleSize = table.getModel().getTileNumeration().getCycleSize();
		row = (row + cycleSize) % table.getRowCount();
		rebuild();
	}
	
	protected void prevCycle() {
		int cycleSize = table.getModel().getTileNumeration().getCycleSize();
		row = (table.getRowCount() + row - cycleSize) % table.getRowCount();
		rebuild();
	}

}
