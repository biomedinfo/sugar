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
package org.csml.tommo.sugar.analysis;

import java.util.SortedSet;

public abstract class AbstractTileNumeration implements TileNumeration {

	
	@Override
	public int getTileInCycle(int tile, int index) {
		return tile + index*100;
	}

	@Override
	public boolean isFirstInCycle(int tile) {
		tile = tile % 1000;
		return tile >= 100 && tile < 200;
	}

	@Override
	public boolean isTopBottomSupported() {
		return true;
	}
	
	@Override
	public boolean isTop(int tile) {
		return tile >= 1000 && tile < 2000;
	}

	@Override
	public int getBottomTile(int tile) {
		return tile + 1000;
	}

	@Override
	public boolean isCompatible(SortedSet<Integer> tileSet) {
		
		boolean isCompatible = !tileSet.isEmpty();
		for (Integer tile : tileSet)
		{
			if (!isAcceptableTileID(tile))
			{
				isCompatible = false;
				break;
			}
		}
		
		return isCompatible;
	}

	abstract public boolean isAcceptableTileID(Integer tile);

}
