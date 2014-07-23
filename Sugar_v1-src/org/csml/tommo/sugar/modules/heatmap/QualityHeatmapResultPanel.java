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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.OpenedFileCache;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.utils.VerticalTextLabel;

public class QualityHeatmapResultPanel extends JPanel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	protected final QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase;
	
	protected ResultsTable resultsTable; 	
	protected MixedResultsTable mixedResultsTable;
	
	protected JComboBox laneCombo;
	protected JComboBox qualityCombo; 


	public QualityHeatmapResultPanel(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase) {
		super();
		
		this.qualityHeatMapsPerTileAndBase = qualityHeatMapsPerTileAndBase;
		
		initComponents();		
	}
	
	public JPanel createEmptyPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Tile Tree is empty. Failed to load Sequences from the input file."), BorderLayout.CENTER);
		return panel;
	}

	private void initComponents() {
		
		this.setLayout(new BorderLayout());
		this.add(getPanelTitle(),BorderLayout.NORTH);
		
		List<LaneCoordinates> laneList = new ArrayList<LaneCoordinates>();
		SortedSet<String> flowCells = qualityHeatMapsPerTileAndBase.getTileTree().getFlowCells();
		for (String flowCell : flowCells)
		{
			SortedSet<Integer> lanes = qualityHeatMapsPerTileAndBase.getTileTree().getLanes(flowCell);
			
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

		TileNumeration tileNumeration = qualityHeatMapsPerTileAndBase.getTileTree().getTileNumeration();

		JPanel resultsPanel = createResultsPanel(laneCoordinates, tileNumeration);

		JPanel mixedResultsPanel = createMixedResultsPanel(laneCoordinates, tileNumeration);
		
		JTabbedPane resultsTab = new JTabbedPane();
		resultsTab.addTab("Results", resultsPanel);
		resultsTab.addTab("Mixed Results", mixedResultsPanel);

		this.add(resultsTab,BorderLayout.CENTER);
		
		laneCombo = new JComboBox(laneList.toArray(new LaneCoordinates[0]));
		laneCombo.addActionListener(createLaneChangedListener());
		
		this.add(laneCombo,BorderLayout.SOUTH);

		
	}

	protected Component getPanelTitle() {
		
		JPanel result = new JPanel();
		
		String s = qualityHeatMapsPerTileAndBase.name();
		
		int matrixSize = qualityHeatMapsPerTileAndBase.getMatrixSize();
		s += ". Matrix Size: " + matrixSize;
//		s += ", Quality Threshold: " + qualityHeatMapsPerTileAndBase.getQualityThreshold();
		s += ", Quality Threshold: " ;
		JLabel label = new JLabel(s,JLabel.CENTER);
		
		Integer[] availableThresholds = OpenedFileCache.getInstance().getAvailableQualityThresholds(
				SugarApplication.getApplication().getSelectedSequenceFile(), matrixSize);

		
		qualityCombo = new JComboBox(availableThresholds);
		qualityCombo.setSelectedItem(qualityHeatMapsPerTileAndBase.getQualityThreshold());
		qualityCombo.addActionListener(qualityHeatMapsPerTileAndBase.createThresholdChangedListener());
		
		result.add(label);
		result.add(qualityCombo);
		
		this.qualityHeatMapsPerTileAndBase.addOualityThresholdPropertyChangeListener(this);
		
		return result;
	}
	
	protected JPanel createResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		ResultsTableModel model = new ResultsTableModel(qualityHeatMapsPerTileAndBase, laneCoordinates, tileNumeration);
		JPanel resultsPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Tiles"));
		JComponent basePositionLabel = new VerticalTextLabel("Base position");
		resultsPanel.add(northPanel, BorderLayout.NORTH);
		resultsPanel.add(basePositionLabel, BorderLayout.WEST);
		resultsTable = new ResultsTable(qualityHeatMapsPerTileAndBase, model);
//		resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

		// create and add "row header"
		int cycleSize = tileNumeration.getCycleSize();
		CycleNumerationTable numberTable = 	new CycleNumerationTable(
				qualityHeatMapsPerTileAndBase.getMaxSequenceLength() * cycleSize,
				cycleSize,
				qualityHeatMapsPerTileAndBase.getHeatMapSize());

		JPanel tablePanel = new JPanel(new BorderLayout());

		tablePanel.add(numberTable, BorderLayout.WEST);
		tablePanel.add(resultsTable, BorderLayout.CENTER);
				
		resultsPanel.add(new JScrollPane(tablePanel), BorderLayout.CENTER);
		
		return resultsPanel;
	}
	
	protected JPanel createMixedResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		if (!tileNumeration.isTopBottomSupported())
		{
			return createMessagePanel(QualityHeatMapsPerTileAndBase.MESSAGE_MIXED_MODE_NOT_SUPPORTED + tileNumeration.getName() );
		}
		
		MixedResultsTableModel model = new MixedResultsTableModel(qualityHeatMapsPerTileAndBase, laneCoordinates, tileNumeration);
		
		if (model.getColumnCount() == 0)
		{
			return createMessagePanel(QualityHeatMapsPerTileAndBase.MESSAGE_NO_MIXED_RESULTS);
		}
		
		JPanel resultsPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Tiles"));
		JComponent basePositionLabel = new VerticalTextLabel("Base position");
		resultsPanel.add(northPanel, BorderLayout.NORTH);
		resultsPanel.add(basePositionLabel, BorderLayout.WEST);
		mixedResultsTable = new MixedResultsTable(qualityHeatMapsPerTileAndBase, model);
//		resultsPanel.add(new JScrollPane(mixedResultsTable), BorderLayout.CENTER);
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(mixedResultsTable, BorderLayout.CENTER);
		int cycleSize = tileNumeration.getCycleSize();
		tablePanel.add(
				new CycleNumerationTable(
						qualityHeatMapsPerTileAndBase.getMaxSequenceLength() * cycleSize, 
						cycleSize, 
						qualityHeatMapsPerTileAndBase.getHeatMapSize()), 
				BorderLayout.WEST);
		resultsPanel.add(new JScrollPane(tablePanel), BorderLayout.CENTER);

		resultsPanel.add(mixedResultsTable.createMixParameterSlider(), BorderLayout.EAST);
		resultsPanel.add(mixedResultsTable.createMixOperationComboBox(), BorderLayout.NORTH);
		return resultsPanel;
	}

	public JPanel createMessagePanel(String message) {
		JPanel resultsPanel = new JPanel(new BorderLayout());
		message = "<html>" + message + "</html>";
		resultsPanel.add(new JLabel(message), BorderLayout.CENTER);
		return resultsPanel;
	}


	private ActionListener createLaneChangedListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
		        JComboBox cb = (JComboBox) event.getSource();
		        LaneCoordinates laneCoordinates = (LaneCoordinates) cb.getSelectedItem();
		        resultsTable.updateModel(laneCoordinates);
		        if (mixedResultsTable != null)
		        	mixedResultsTable.updateModel(laneCoordinates);
			}
		};
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent e) {

		int newThreshold = (Integer) e.getNewValue();
		
		// update combo box
		qualityCombo.setSelectedItem(newThreshold);
		
    	// update tables		        	
        LaneCoordinates laneCoordinates = (LaneCoordinates) laneCombo.getSelectedItem();

        resultsTable.updateModel(laneCoordinates);
        if (mixedResultsTable != null)
        	mixedResultsTable.updateModel(laneCoordinates);

		
	}
}