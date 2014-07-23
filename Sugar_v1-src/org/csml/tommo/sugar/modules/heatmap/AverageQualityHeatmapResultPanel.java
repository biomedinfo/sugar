package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;

public class AverageQualityHeatmapResultPanel extends QualityHeatmapResultPanel {

	public AverageQualityHeatmapResultPanel(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase) {
		super(qualityHeatMapsPerTileAndBase);
	}

	@Override
	protected JPanel createResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		JPanel result = super.createResultsPanel(laneCoordinates, tileNumeration);
		resultsTable.setDefaultRenderer(MeanQualityMatrix.class, new AverageQualityMatrixCellRenderer());
		int mouseListenersCount = resultsTable.getMouseListeners().length;
		resultsTable.removeMouseListener(resultsTable.getMouseListeners()[mouseListenersCount - 1]);
		resultsTable.addMouseListener(new PupupHandler());
		return result;
	}

	@Override
	protected JPanel createMixedResultsPanel(LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		JPanel result = super.createMixedResultsPanel(laneCoordinates, tileNumeration);
		
		if (mixedResultsTable != null)
		{
			mixedResultsTable.setDefaultRenderer(MeanQualityMatrix.class, new AverageQualityMatrixCellRenderer());
			int mouseListenersCount = mixedResultsTable.getMouseListeners().length;
			mixedResultsTable.removeMouseListener(mixedResultsTable.getMouseListeners()[mouseListenersCount - 1]);
			mixedResultsTable.addMouseListener(new PupupHandler());
		}
		
		return result;
	}
	
	private class PupupHandler extends MouseAdapter{

		@Override
		public void mousePressed(MouseEvent e) {

			ResultsTable table = (ResultsTable) e.getSource();
			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			
			if (row < 0 || col < 0)
				return;
			
			if(table instanceof MixedResultsTable){
				createMixedPopupHeatMapWindow(row, col);
			}
			else{
				createPopupHeatMapWindow(row, col);				
			}
		}
		
		protected JDialog createPopupHeatMapWindow(int row, int col) {
			JDialog result = null;
				result = new AverageQualityInteractiveHeatmapDialog(row, col, resultsTable);
				result.setVisible(true);
//			}
			return result;
		}
		
		protected JDialog createMixedPopupHeatMapWindow(int row, int col) {
			JDialog result = null;

			result = new AverageQualityMixedHeatmapDialog(row, col, mixedResultsTable);
			result.setVisible(true);
			return result;
		}

	}
	
	protected Component getPanelTitle() {
				
		String s = qualityHeatMapsPerTileAndBase.name();
		
		int matrixSize = qualityHeatMapsPerTileAndBase.getMatrixSize();
		s += ". Matrix Size: " + matrixSize;
//		s += ", Quality Threshold: " + qualityHeatMapsPerTileAndBase.getQualityThreshold();
		JLabel label = new JLabel(s,JLabel.CENTER);
				
		return label;
	}
}
