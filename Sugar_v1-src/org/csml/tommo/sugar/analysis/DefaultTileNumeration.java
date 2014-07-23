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

public class DefaultTileNumeration extends AbstractTileNumeration {

	static public DefaultTileNumeration INSTANCE = new DefaultTileNumeration();
	
	@Override
	public boolean isFirstInCycle(int tile) {
		return true;
	}

	@Override
	public boolean isTopBottomSupported() {
		return false;
	}

	@Override
	public boolean isTop(int tile) {
		return true;
	}

	@Override
	public int getBottomTile(int tile) {
		return -1;
	}

	@Override
	public boolean isCompatible(SortedSet<Integer> tileSet) {
		
		return true;
	}

	@Override
	public int getCycleSize() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean isAcceptableTileID(Integer tile) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Default";
	}

}