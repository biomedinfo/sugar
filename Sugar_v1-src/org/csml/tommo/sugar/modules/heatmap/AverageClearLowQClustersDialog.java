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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.SubtileQualityRanking;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.csml.tommo.sugar.utils.FileChooserManager;
import org.csml.tommo.sugar.utils.VerticalTextLabel;

public class AverageClearLowQClustersDialog extends JDialog implements ActionListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 7755493135986780444L;
		
	protected SubtileQualityRanking subtileQualityRanking;
	protected ResultsTable resultsTable;
	protected ResultsPerTileTable averageResultsTable;
	protected JComboBox laneComboBox;
	protected JButton okButton;
	protected JButton cancelButton;
	protected JButton importSelectionButton;
	protected JButton exportSelectionButton;
	protected JButton deselectAllButton;
	protected JButton selectByQualityButton;

	private boolean isCancelled = true;
	
	protected static int BUTTONS_HEIGHT = new JButton("test").getPreferredSize().height;
			
	public AverageClearLowQClustersDialog(SubtileQualityRanking subtileQualityRanking){
		super(SugarApplication.getApplication());
		this.subtileQualityRanking = subtileQualityRanking;
		
		initData();
				
		setLocation(400, 350);
		setSize(800, 600);
	}
	
	public JPanel createEmptyPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Tile Tree is empty. Failed to load Sequences from the input file."), BorderLayout.CENTER);
		return panel;
	}

	protected void initData() {
		setTitle(getPanelTitle());
		this.setLayout(new BorderLayout());
		JPanel centerPanel = new JPanel(new BorderLayout());
		
		List<LaneCoordinates> laneList = new ArrayList<LaneCoordinates>();
		SortedSet<String> flowCells = getQualityHeatmaps().getTileTree().getFlowCells();
		for (String flowCell : flowCells)
		{
			SortedSet<Integer> lanes = getQualityHeatmaps().getTileTree().getLanes(flowCell);
			
			for (Integer lane : lanes)
			{
				LaneCoordinates laneCoordinates = new LaneCoordinates(flowCell, lane);
				laneList.add(laneCoordinates);
			}
		}

		if (laneList.size() == 0)
		{
			this.add(createEmptyPanel(),BorderLayout.CENTER);
			return;
		}
		
		LaneCoordinates laneCoordinates = laneList.get(0);		


		TileNumeration tileNumeration = getQualityHeatmaps().getTileTree().getTileNumeration();

		JPanel resultsPanel = createResultsPanel(laneCoordinates, tileNumeration);
		
		centerPanel.add(resultsPanel,BorderLayout.CENTER);
		
		laneComboBox = new JComboBox(laneList.toArray(new LaneCoordinates[0]));
		laneComboBox.addActionListener(createLaneChangedListener());
		
		centerPanel.add(laneComboBox,BorderLayout.SOUTH);
		
		JPanel buttonsPanel = createButtonsPanel();
		
		
		add(centerPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);
	}

	protected JPanel createButtonsPanel() {
		
		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		textPanel.add(new JLabel("<html>Left click to select, right click to unselect tile.<br/>Left drag to select, right drag to unselect multiple tiles.<br/>CTRL+click to open Tile Details window and select sub-tiles.</html>"));
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 3;
		buttonsPanel.add(textPanel, c);

		
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.X_AXIS));
		selectionPanel.setAlignmentX(RIGHT_ALIGNMENT);
		deselectAllButton = new JButton("Deselect All");
		deselectAllButton.addActionListener(this);
		selectByQualityButton = new JButton("Select Sub-tiles by Quality");
		selectByQualityButton.addActionListener(this);
		Dimension buttonSize = new Dimension(160, BUTTONS_HEIGHT);
		deselectAllButton.setPreferredSize(buttonSize);
		selectByQualityButton.setPreferredSize(buttonSize);

		selectionPanel.add(Box.createHorizontalGlue());		
		selectionPanel.add(deselectAllButton);
		selectionPanel.add(selectByQualityButton);
		
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		buttonsPanel.add(selectionPanel, c);

		JPanel importExportSelectionPanel = new JPanel();
		importExportSelectionPanel.setLayout(new BoxLayout(importExportSelectionPanel, BoxLayout.X_AXIS));
		importExportSelectionPanel.setAlignmentX(RIGHT_ALIGNMENT);
		importSelectionButton = new JButton("Import Selection");
		importSelectionButton.addActionListener(this);
		exportSelectionButton = new JButton("Export Selection");
		exportSelectionButton.addActionListener(this);
		importSelectionButton.setPreferredSize(buttonSize);
		exportSelectionButton.setPreferredSize(buttonSize);

		importExportSelectionPanel.add(Box.createHorizontalGlue());		
		importExportSelectionPanel.add(importSelectionButton);
		importExportSelectionPanel.add(exportSelectionButton);
		
		c.insets = new Insets(0, 0, 5, 15);
		c.gridy = 1;
		buttonsPanel.add(importExportSelectionPanel, c);


		JPanel okPanel = new JPanel();
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
		
		buttonSize = new Dimension(80, BUTTONS_HEIGHT);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		okButton.setPreferredSize(buttonSize);
		cancelButton.setPreferredSize(buttonSize);

		okPanel.add(Box.createHorizontalGlue());		
		okPanel.add(okButton);
		okPanel.add(cancelButton);
		
		c.insets = new Insets(0, 0, 5, 15);
		c.gridy = 2;
		buttonsPanel.add(okPanel, c);
		
		return buttonsPanel;
	}
	
	private JPanel createResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		ExtendedResultsTableModel model = new ExtendedResultsTableModel(getQualityHeatmaps(), laneCoordinates, tileNumeration);
		JPanel resultsPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Tiles"));
		JComponent basePositionLabel = new VerticalTextLabel("Base position");
		resultsPanel.add(northPanel, BorderLayout.NORTH);
		resultsPanel.add(basePositionLabel, BorderLayout.WEST);
		resultsTable = new ResultsTable(getQualityHeatmaps(), model);
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		resultsTable.setCellSelectionEnabled(true);
		int mouseListenersCount = resultsTable.getMouseListeners().length;
		resultsTable.removeMouseListener(resultsTable.getMouseListeners()[mouseListenersCount - 1]);
		resultsTable.setDefaultRenderer(MeanQualityMatrix.class, new SelectableMatrixCellRenderer(EClearLowQClustersMethod.DELETE));
//		resultsTable.getSelectionModel().addListSelectionListener(this);
//		resultsTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
		MouseAdapter mouseAdapter = new ResultsTableMouseAdapter();
		resultsTable.addMouseListener(mouseAdapter);
		resultsTable.addMouseMotionListener(mouseAdapter);
//		resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

		ResultsPerTileTableModel averageModel = new ResultsPerTileTableModel(model);
		averageResultsTable = new ResultsPerTileTable(getQualityHeatmaps(), averageModel);
		averageResultsTable.addMouseListener(mouseAdapter);
		averageResultsTable.addMouseMotionListener(mouseAdapter);
		
		resultsTable.getModel().addTableModelListener(averageResultsTable);
		
		
		// create and add "row header"
		int cycleSize = tileNumeration.getCycleSize();
		CycleNumerationTable numberTable = 	new CycleNumerationTable(
				getQualityHeatmaps().getMaxSequenceLength() * cycleSize,
				cycleSize,
				getQualityHeatmaps().getHeatMapSize());

		JPanel tablePanel = new JPanel(new BorderLayout());

		JPanel mainTablePanel = new JPanel(new BorderLayout());
		mainTablePanel.add(resultsTable, BorderLayout.CENTER);
		mainTablePanel.add(numberTable, BorderLayout.WEST);
		
		JPanel averageTablePanel = new JPanel(new BorderLayout());
		averageTablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		averageTablePanel.add(averageResultsTable, BorderLayout.CENTER);		
		
		JComponent c = new JPanel();
		c.setPreferredSize(new Dimension(CycleNumerationTable.COLUMN_WIDTH, c.getPreferredSize().height));
		averageTablePanel.add(c, BorderLayout.WEST);
		
		JViewport viewPort = new JViewport();
		viewPort.setView(averageTablePanel);
		viewPort.setPreferredSize(averageTablePanel.getPreferredSize());

		JComponent separator = new JPanel();
		separator.setPreferredSize(new Dimension(separator.getPreferredSize().width, 30));

		tablePanel.add(mainTablePanel, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane(tablePanel);
		scrollPane.setColumnHeader(viewPort);
		resultsPanel.add(scrollPane, BorderLayout.CENTER);
		
		return resultsPanel;
	}
	
	private ActionListener createLaneChangedListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
		        JComboBox cb = (JComboBox) event.getSource();
		        LaneCoordinates laneCoordinates = (LaneCoordinates) cb.getSelectedItem();
				updateTableModels(laneCoordinates);

			}
		};
	}
	
	protected String getPanelTitle() {
		return "Select LowQ Clusters";
	}

	private class ResultsTableMouseAdapter extends HeatmapSelectionHandler{

		@Override
		public void mouseReleased(MouseEvent e){
			super.mouseReleased(e);
			resultsTable.setDragging(false);			
			averageResultsTable.setDragging(false);
		}

		protected void markTile(MouseEvent e, Point releasePoint) {
			int minRow = Math.min(anchorPoint.y, releasePoint.y);
			int maxRow = Math.max(anchorPoint.y, releasePoint.y);
			
			int minCol = Math.min(anchorPoint.x, releasePoint.x);
			int maxCol = Math.max(anchorPoint.x, releasePoint.x);
			
//					boolean isSingleSelection = minCol == maxCol && minRow == maxRow;

			boolean isSelecting = !(e.isPopupTrigger() || rightButtonPressed);

			int cycleSize = resultsTable.getModel().getTileNumeration().getCycleSize();
			if (maxRow - minRow > cycleSize)
				maxRow = minRow + cycleSize - 1;


			for(int c=minCol; c<=maxCol; c++){
				if(minRow <= maxRow){
					
					// support  multiple tile in one column
					for(int r=minRow; r<=maxRow; r++){
					
						MeanQualityMatrix matrix = resultsTable.getModel().getTileSelectionMatrix(r, c);
						if(matrix != null){
							if (isSelecting){
								if(!matrix.isAnythingSelected()){
									matrix.selectAll();
								}
							}
							else {
								matrix.deselectAll();					
							}
						}
					}
				}
			}
			resultsTable.getModel().fireTableDataChanged();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			averageResultsTable.setDragging(true);
			resultsTable.setDragging(true);			
		}
		
		public void mouseClicked(MouseEvent e){
			if(e.isControlDown()){
				
				JTable table = (JTable) e.getSource();
				int col = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				
				if (row < 0 || col < 0)
					return;

				TileBPCoordinates tc = resultsTable.getModel().getCoordinateAt(row, col);
				MeanQualityMatrix matrix = null;
				
				TileCoordinates tile = null;
				if(table == resultsTable){
					tile = resultsTable.getModel().getCoordinateAt(row, col);
					matrix = getQualityHeatmaps().getMeanQualityMatrix(tc);
				}
				else{
					tile = averageResultsTable.getModel().getCoordinateAt(row, col);
					matrix = averageResultsTable.getModel().getValueAt(row, col);
				}
				
				MeanQualityMatrix firstInTileMatrix = getQualityHeatmaps().getFirstTileMatrix(tc.getTileCoordinate());
				MeanQualityMatrix.copySelection(firstInTileMatrix, matrix);
				
				JDialog dialog = new AverageClearLowQClustersDetailsDialog(AverageClearLowQClustersDialog.this, matrix, tile);
				dialog.setModal(true);
				dialog.setVisible(true);
				
				// move selection to first matrix in tile
				MeanQualityMatrix.copySelection(matrix, firstInTileMatrix);
				// deselect matrix if that was non first matrix in tile modified
				// since we wanted to modify first matrix in tile, but show mouse indicated matrix
				if(matrix != firstInTileMatrix){
					matrix.deselectAll();					
				}
				
				resultsTable.getModel().fireTableDataChanged();
			}
		}		
	} 

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == okButton){
			isCancelled = false;
			setVisible(false);
		}
		else if(e.getSource() == cancelButton){
			isCancelled = true;
			setVisible(false);
		}
		if(e.getSource() == importSelectionButton){
			
			File file = FileChooserManager.getLowQClustersSelectionFile();
			if(file != null){
				try {
					getQualityHeatmaps().importSelectionMatrix(file);
					
					getQualityHeatmaps().expandTileSelection();
					
				} catch (Exception e1) {
					
					String message = "Failed to import selection file";
					SugarApplication.showException(e1, message);				
				}
				
				resultsTable.getModel().fireTableDataChanged();				
			}
		}
		else if(e.getSource() == exportSelectionButton){
			
			File file = FileChooserManager.getLowQClustersSelectionFile();
			if(file != null){
				try {
					getQualityHeatmaps().expandTileSelection();
					getQualityHeatmaps().exportSelectionMatrix(file);
				} catch (Exception e1) {
					
					String message = "Failed to export selected clusters to file";
					SugarApplication.showException(e1, message);
				}				
			}
		}
		else if(e.getSource() == deselectAllButton){
			
			for(int r=0; r<resultsTable.getRowCount(); r++){
				for(int c=0; c<resultsTable.getColumnCount(); c++){
					MeanQualityMatrix matrix = (MeanQualityMatrix)resultsTable.getValueAt(r, c);
										
					if(matrix != null){
						matrix.deselectAll();							
					}					
				}
			}
			resultsTable.getModel().fireTableDataChanged();
		}
		else if(e.getSource() == selectByQualityButton){
			SelectLowQClustersByQualityDialog dialog = new SelectLowQClustersByQualityDialog(this, subtileQualityRanking);
			dialog.setVisible(true);
			resultsTable.repaint();
			averageResultsTable.repaint();
		}
	}
	
	
	public boolean wasCancelled() {
		return isCancelled;
	}

	private void updateTableModels(LaneCoordinates laneCoordinates) {
		ExtendedResultsTableModel model = new ExtendedResultsTableModel(getQualityHeatmaps(), laneCoordinates, getQualityHeatmaps().getTileTree().getTileNumeration());
		resultsTable.setModel(model);					
		resultsTable.setColumnWidth();

		averageResultsTable.setModel(new ResultsPerTileTableModel(model));	
		averageResultsTable.setColumnWidth();
		model.addTableModelListener(averageResultsTable);

	}
	
	protected QualityHeatMapsPerTileAndBase getQualityHeatmaps(){
		return subtileQualityRanking.getQualityHeatmapsPerTileAndBase();
	}
}
