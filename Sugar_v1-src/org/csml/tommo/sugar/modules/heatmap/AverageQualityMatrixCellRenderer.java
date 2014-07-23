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
import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MixOperation;
import org.csml.tommo.sugar.heatmap.TemperaturePaintScale;

public class AverageQualityMatrixCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected JTable table;
	protected int row;
	protected int col;

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		
		this.table = table;
		this.row = row;
		this.col = col;

		setText(null);
		setIcon(null);
		setToolTipText("");
		setBorder(null);

		if (value instanceof MeanQualityMatrix)
		{
			ColorPaintScale paintScale = getPaintScale(table);

			// add separator (border) to group cycles
			if (table instanceof ResultsTable){
				ResultsTable resultsTable = (ResultsTable) table;
				String tooltip = resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);

				MeanQualityMatrix matrix = (MeanQualityMatrix) value;				
				Image image = getImage(paintScale, matrix);
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
			if (table instanceof ResultsTable){
				ResultsTable resultsTable = (ResultsTable) table;
				String tooltip = "No data for tile:" + resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);
			}
		}
		return this;
	}

	protected Image getImage(ColorPaintScale paintScale,
			MeanQualityMatrix matrix) {
		Image image = matrix.createAverageQualityBufferedImage(paintScale);
		return image;
	}

	public static ColorPaintScale getPaintScale(JTable table) {
		ColorPaintScale result = LinearPaintScale.AVERAGE_QUALITY_PAINT_SCALE;
		
		if (table instanceof MixedResultsTable)
		{
			MixedResultsTable mixedTable = (MixedResultsTable) table;
			MixOperation mixOperation = MixOperation.fromMixer(mixedTable.getModel().getMixOperation());
			
			if (mixOperation == mixOperation.DIFF)
			{
				result = TemperaturePaintScale.AVERAGE_QUALITY_DIFF_PAINT_SCALE;
			}
			else if (mixOperation == mixOperation.ABSOLUTE_DIFF)
			{
				result = TemperaturePaintScale.AVERAGE_QUALITY_ABS_DIFF_PAINT_SCALE;				
			}
		}
		
		return result;
	}
	
}
