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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.table.AbstractTableModel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;

public class ResultsTableModel extends AbstractTableModel implements ITableModelWithTooltip {
		
		/**
		 * 
		 */
		protected final QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected Integer[] tiles;
		protected LaneCoordinates laneCoordinates;		
		
		protected TileNumeration tileNumeration;
		
		protected int topBottomSeparatorColumn = -2; 		
				
		public ResultsTableModel(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
			this.qualityHeatMapsPerTileAndBase = qualityHeatMapsPerTileAndBase;
			this.tileNumeration = tileNumeration;
			this.laneCoordinates = laneCoordinates;
//			tiles = getTileTree().getTiles(laneCoordinates).toArray(new Integer[0]);
			SortedSet<Integer> tileSet = this.qualityHeatMapsPerTileAndBase.getTileTree().getTiles(laneCoordinates);
			
			// tile IDs "without cycles"
			List<Integer> tileList = new ArrayList<Integer>(); 
			
			for (Integer tile : tileSet)
			{
				if (tileNumeration.isFirstInCycle(tile))
					tileList.add(tile);					
			}
			
			tiles = tileList.toArray(new Integer[0]);
			
			setTopBottomSeparator();			
		}

		public int getColumnCount() {
			return tiles.length;
		}
	
		@Override
		public String getColumnName(int col) {
			Integer tile = getTile(col);
			String columnName = tile.toString();
			for (int i = 1; i < tileNumeration.getCycleSize(); i++)
			{
				Integer nextTile = tileNumeration.getTileInCycle(tile, i);
				columnName += "<br>" + nextTile.toString();
			}

			return columnName;
		}

		public Integer[] getTilesForColumn(int col){
			Integer[] result = new Integer[tileNumeration.getCycleSize()];
			Integer tile = getTile(col);
			result[0] = tile;
			for (int i = 1; i < tileNumeration.getCycleSize(); i++)
			{
				Integer nextTile = tileNumeration.getTileInCycle(tile, i);
				result[i] = nextTile;
			}
			return result;
		}
		
		@Override
		public int getRowCount() {
			return this.qualityHeatMapsPerTileAndBase.getMaxSequenceLength() * tileNumeration.getCycleSize();
		}

		public TileBPCoordinates getCoordinateAt(int row, int col) {
			Integer tile = getTile(col);
			Integer bp = getBasePosition(row);
			Integer cycleID = getCycleID(row);
			if (cycleID > 0) 
				tile = tileNumeration.getTileInCycle(tile, cycleID);
			
			return new TileBPCoordinates(laneCoordinates, tile, bp);  
		}

		public int getCycleID(int row) {
			return row % tileNumeration.getCycleSize();
		}

		public int getBasePosition(int row) {
			return row / tileNumeration.getCycleSize()+1;
		}

		public String getTooltipAt(int row, int col) {
			TileBPCoordinates cds = getCoordinateAt(row, col);
			MeanQualityMatrix matrix = getValueAt(row, col);
			String tooltip = cds.getTile() + ", " + cds.getBasePosition();
			if(matrix != null){
				tooltip += "  density: " + matrix.getCounter();
			}
			return tooltip;
		}

		@Override
		public MeanQualityMatrix getValueAt(int row, int col) {
			return this.qualityHeatMapsPerTileAndBase.getMeanQualityMatrix(getCoordinateAt(row, col));  
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return MeanQualityMatrix.class;
		}

		public Integer getTile(int columnIndex) {
			return tiles[columnIndex];
		}
		
		public LaneCoordinates getLaneCoordinates() {
			return laneCoordinates;
		}

		public TileNumeration getTileNumeration() {
			return tileNumeration;
		}		
		
		public int getTopBottomSeparatorColumn() {
			return topBottomSeparatorColumn;
		}
		
		protected void setTopBottomSeparator() {
			// set top-bottom separator column
			for (int i=0; i<getColumnCount()-1; i++) {
				Integer tile = getTile(i);
				Integer nextTile = getTile(i+1);				
				if (tileNumeration.isTop(tile) && !tileNumeration.isTop(nextTile))
				{
					topBottomSeparatorColumn = i;
					break;
				}				
			}
		}

		public QualityHeatMapsPerTileAndBase getQualityHeatMapsPerTileAndBase() {
			return qualityHeatMapsPerTileAndBase;
		}

		/**
		 * 
		 * check if the tile is selected
		 * 
		 * returns three value:
		 * - WHOLE_TILE_SELECTED 
		 * - PART_TILE_SELECTED
		 * - NOTHING_SELECTED  
		 * @param col 
		 * 
		 * @param i
		 * @return
		 */
		public ETileSelection getTileSelection(int row, int col) {
			
			TileCoordinates tile = getCoordinateAt(row, col).getTileCoordinate(); 			
			return qualityHeatMapsPerTileAndBase.getTileSelection(tile);
		}

		public MeanQualityMatrix getTileSelectionMatrix(int row, int col) {
			TileCoordinates tile = getCoordinateAt(row, col).getTileCoordinate(); 			
			return qualityHeatMapsPerTileAndBase.getFirstTileMatrix(tile);
		}
	}