package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;

public class AverageMatrixCellRenderer extends DefaultTableCellRenderer {

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
//		setToolTipText("");
		setBorder(null);

		if (value instanceof MeanQualityMatrix)
		{
			ColorPaintScale paintScale = getPaintScale(table);

			// add separator (border) to group cycles
			if (table instanceof ResultsPerTileTable){
				ResultsPerTileTable resultsTable = (ResultsPerTileTable) table;
//				String tooltip = resultsTable.getModel().getTooltipAt(row, col);
//				setToolTipText(tooltip);

				MeanQualityMatrix matrix = (MeanQualityMatrix) value;				
				Image image = getImage(paintScale, matrix);
				image = image.getScaledInstance(resultsTable.getHeatMapSize(), resultsTable.getHeatMapSize(), Image.SCALE_FAST);
				ImageIcon icon = new ImageIcon(image);
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

				ETileSelection tileSelection = resultsTable.getModel().getParentModel().getTileSelection(row, col);

				// if is dragging check the table selection model
				if (resultsTable.isDragging() && isSelected) {
					// if not partial, set to ALL selection 
					if (!ETileSelection.PART.equals(tileSelection)) {
						tileSelection = ETileSelection.ALL; 
					}
				}
				
				Image selectionImage = tileSelection.getIcon();
				if (selectionImage != null)
					icon.setImage(markImageAsSelected(image, selectionImage));
				
			}
		}
		else {
			if (table instanceof ResultsPerTileTable){
				ResultsPerTileTable resultsTable = (ResultsPerTileTable) table;
//				String tooltip = "No data for tile:" + resultsTable.getModel().getTooltipAt(row, col);
//				setToolTipText(tooltip);
			}
		}
		return this;
	}	

	protected Image getImage(ColorPaintScale paintScale,
			MeanQualityMatrix matrix) {
		Image image = matrix.createBufferedImage(paintScale);
		return image;
	}

	protected ColorPaintScale getPaintScale(JTable table) {
		ColorPaintScale paintScale = LinearPaintScale.PAINT_SCALE;
		if(table instanceof MixedResultsTable){
			MixedResultsTable mixedTable = (MixedResultsTable) table;
			paintScale = (ColorPaintScale)mixedTable.getModel().getMixOperation().getPaintScale();
		}
		return paintScale;
	}
	
	private Image markImageAsSelected(Image image, Image decoration) {
		Image result = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)result.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.drawImage(decoration, 0, 0, image.getWidth(null) / 2,image.getHeight(null) / 2, null);
		g2.dispose();
		return result;
	}
	
	@Override
	public String getToolTipText() {
		String result = "";
		if(table.getModel() instanceof ITableModelWithTooltip){
			ITableModelWithTooltip model = (ITableModelWithTooltip) table.getModel();
			result = model.getTooltipAt(row, col);
		}
		else{
			result = super.getToolTipText();
		}
		return result;
	}
}
