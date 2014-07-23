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

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.AverageOperation;
import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;

public class MixedResultsTableModel extends ResultsTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		List<Integer> topBottomTiles;
		private IMixOperation mixOperation = new AverageOperation();
		
		public MixedResultsTableModel(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
			super(qualityHeatMapsPerTileAndBase, laneCoordinates, tileNumeration);
			
			topBottomTiles = new ArrayList<Integer>();
			SortedSet<Integer> tileSet = this.qualityHeatMapsPerTileAndBase.getTileTree().getTiles(laneCoordinates);		
			for (Integer tile : tiles)
			{
				if ( tileNumeration.isTop(tile) &&
					tileSet.contains(tileNumeration.getBottomTile(tile)) )
				{
					topBottomTiles.add(tile);
				}									
			}
		}
		
		@Override		
		public MeanQualityMatrix getValueAt(int rowIndex, int columnIndex) {
			return this.qualityHeatMapsPerTileAndBase.createMixedQualityMatrix(
					getTopCoordinateAt(rowIndex, columnIndex), 
					getBottomCoordinateAt(rowIndex, columnIndex),
					mixOperation);  
		}

		@Override		
		public int getColumnCount() {
			return topBottomTiles.size();
		}
	
		@Override		
		public Integer getTile(int columnIndex) {
			return topBottomTiles.get(columnIndex);
		}				
		
		public TileBPCoordinates getTopCoordinateAt(int rowIndex, int columnIndex) {
//			return new TileBPCoordinates(laneCoordinates, getTile(columnIndex), rowIndex+1);
			return getCoordinateAt(rowIndex, columnIndex);
		}		

		public TileBPCoordinates getBottomCoordinateAt(int rowIndex, int columnIndex) {
			TileBPCoordinates coordinate = getCoordinateAt(rowIndex, columnIndex);
			Integer bottomTile = tileNumeration.getBottomTile(coordinate.getTile());
			
			return new TileBPCoordinates(laneCoordinates, bottomTile, coordinate.getBasePosition());
//			return new TileBPCoordinates(laneCoordinates, tileNumeration.getBottomTile(getTile(columnIndex)), rowIndex+1);  
		}

		public void setMixParameter(double param) {
			mixOperation.setParameter(param);		
		}
		
		public double getMixParameter() {
			return mixOperation.getParameter();
		}

		public IMixOperation getMixOperation(){
			return mixOperation;
		}

		public void setMixOperation(IMixOperation mixOperation){
			this.mixOperation = mixOperation;
		}

		@Override
		protected void setTopBottomSeparator() {
			
			// there is NO top-bottom separator in Mixed Results table
			// do nothing
		}
	}