package org.csml.tommo.sugar.modules.heatmap;

import javax.swing.table.TableModel;

public interface ITableModelWithTooltip extends TableModel{

	public String getTooltipAt(int row, int col);
}
