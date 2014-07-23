package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Component;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.MappingQualityMatrix;
import org.csml.tommo.sugar.heatmap.TemperaturePaintScale;

public class MappingQualityCellRenderer extends DefaultTableCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4518146429633655909L;

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {

		setText(null);
		setIcon(null);
		setToolTipText("");
		setBorder(null);

		if (value instanceof MappingQualityMatrix)
		{
			// add separator (border) to group cycles
			if (table instanceof MappingQualityTable){
				MappingQualityTable resultsTable = (MappingQualityTable) table;
				String tooltip = resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);
				

				MappingQualityMatrix matrix = (MappingQualityMatrix) value;
				Image image = null;
				if(row < MappingQualityMatrix.THRESHOLDS.length){
					image = getImageForThreshold(MappingQualityMatrix.THRESHOLDS[row], matrix);
				}
				else{
					image = getImage(matrix);					
				}
				Icon icon = new ImageIcon(image.getScaledInstance(resultsTable.getHeatMapSize(), resultsTable.getHeatMapSize(), Image.SCALE_FAST));
				setHorizontalAlignment(SwingConstants.CENTER);
				setIcon(icon);										

				int cycleSize = resultsTable.getModel().getTileNumeration().getCycleSize();					

				int top = (row % cycleSize == 0) ? 1 : 0; 
				int bottom = (row % cycleSize == cycleSize - 1) ? 1 : 0;
				int left = col == resultsTable.getModel().getTopBottomSeparatorColumn() + 1 ? 1 : 0;
				int right = col == resultsTable.getModel().getTopBottomSeparatorColumn() ? 1 : 0;
				//					Border inBorder = BorderFactory.createMatteBorder(top, left, bottom, right, Color.GRAY);
				Border outBorder = BorderFactory.createEmptyBorder(top * ResultsTable.CELL_SEPARATOR_SIZE, 
						left * ResultsTable.COLUMN_SEPARATOR_SIZE, 
						bottom * ResultsTable.CELL_SEPARATOR_SIZE, 
						right * ResultsTable.COLUMN_SEPARATOR_SIZE);
				//					setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
				setBorder(outBorder);
			}
		}
		else {
			if (table instanceof MappingQualityTable){
				MappingQualityTable resultsTable = (MappingQualityTable) table;
				String tooltip = "No data for tile:" + resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);
			}
		}
		return this;
	}

	protected Image getImage(MappingQualityMatrix matrix) {
		Image image = matrix.createBufferedImage(getAveragePaintScale());
		return image;
	}

	protected Image getImageForThreshold(int threshold, MappingQualityMatrix matrix){
		return matrix.createBufferedImageForThreshold(threshold, getThresholdPaintScale());
	}
		
	public static ColorPaintScale getThresholdPaintScale() {
		return new TemperaturePaintScale(0, 1);
	}

	public static ColorPaintScale getAveragePaintScale() {
		return new TemperaturePaintScale(0, 50);
	}

}
