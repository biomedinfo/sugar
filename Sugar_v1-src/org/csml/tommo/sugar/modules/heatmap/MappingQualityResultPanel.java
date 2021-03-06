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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.modules.MappingQuality;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.TileTree;
import org.csml.tommo.sugar.sequence.LaneCoordinates;

public class MappingQualityResultPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	protected final MappingQuality mappingQuality;
	
	protected MappingQualityTable resultsTable; 	
	protected MappingQualityMixedTable mixedResultsTable; 	


	public MappingQualityResultPanel(MappingQuality mappingQuality) {
		super();
		this.mappingQuality = mappingQuality;
		initComponents();
	}

	public JPanel createEmptyPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Tile Tree is empty. Failed to load Sequences from the input file."), BorderLayout.CENTER);
		return panel;
	}

	private void initComponents() {
		
		this.setLayout(new BorderLayout());
		this.add(new JLabel(getPanelTitle(),JLabel.CENTER),BorderLayout.NORTH);
		
		TileTree tileTree = mappingQuality.getTileTree();
		
		List<LaneCoordinates> laneList = new ArrayList<LaneCoordinates>();
		SortedSet<String> flowCells = tileTree.getFlowCells();
		for (String flowCell : flowCells)
		{
			SortedSet<Integer> lanes = tileTree.getLanes(flowCell);
			
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

		TileNumeration tileNumeration = tileTree.getTileNumeration();

		JPanel resultsPanel = createResultsPanel(laneCoordinates, tileNumeration);

		JPanel mixedResultsPanel = createMixedResultsPanel(laneCoordinates, tileNumeration);
		
		JTabbedPane resultsTab = new JTabbedPane();
		resultsTab.addTab("Results", resultsPanel);
		resultsTab.addTab("Mixed Results", mixedResultsPanel);

		this.add(resultsTab,BorderLayout.CENTER);
		
		JComboBox laneCombo = new JComboBox(laneList.toArray(new LaneCoordinates[0]));
		laneCombo.addActionListener(createLaneChangedListener());
		
		this.add(laneCombo,BorderLayout.SOUTH);
	}

	protected String getPanelTitle() {
		String s = mappingQuality.name();
		
		s += ". Matrix Size: " + mappingQuality.getMatrixSize();; 
		
		return s;
	}
	
	private JPanel createResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		MappingQualityTableModel model = new MappingQualityTableModel(mappingQuality, laneCoordinates, tileNumeration);
		JPanel resultsPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Tiles"));
		resultsPanel.add(northPanel, BorderLayout.NORTH);
		
		resultsTable = new MappingQualityTable(mappingQuality, model);
		int cycleSize = tileNumeration.getCycleSize();
		MappingQualityNumerationTable numberTable = new MappingQualityNumerationTable(
				cycleSize,
				SugarApplication.getApplication().getTileHeatmapSize());

		JPanel tablePanel = new JPanel(new BorderLayout());

		tablePanel.add(numberTable, BorderLayout.WEST);
		tablePanel.add(resultsTable, BorderLayout.CENTER);

		
//		resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
		resultsPanel.add(new JScrollPane(tablePanel), BorderLayout.CENTER);
		
		return resultsPanel;
	}
	
	private JPanel createMixedResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		if (!tileNumeration.isTopBottomSupported())
		{
			return createMessagePanel(QualityHeatMapsPerTileAndBase.MESSAGE_MIXED_MODE_NOT_SUPPORTED + tileNumeration.getName() );
		}		
	
		MappingQualityMixedTableModel model = new MappingQualityMixedTableModel(mappingQuality, laneCoordinates, tileNumeration);
		
		if (model.getColumnCount() == 0)
		{
			return createMessagePanel(QualityHeatMapsPerTileAndBase.MESSAGE_NO_MIXED_RESULTS);
		}

		JPanel resultsPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Tiles"));
		resultsPanel.add(northPanel, BorderLayout.NORTH);
		mixedResultsTable = new MappingQualityMixedTable(model);
		resultsPanel.add(new JScrollPane(mixedResultsTable), BorderLayout.CENTER);
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
}