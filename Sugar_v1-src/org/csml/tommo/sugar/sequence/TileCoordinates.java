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
package org.csml.tommo.sugar.sequence;

import org.json.simple.JSONObject;



public class TileCoordinates extends LaneCoordinates {
	
	private static final String JSON_ATTR_TILE = "tile";

	/**
	 * 
	 */
	private static final long serialVersionUID = -6938504435939486734L;
	
	/**
	 * tile number within the flow cell lane
	 */
	protected Integer tile;

	public TileCoordinates(LaneCoordinates laneCoordinates, Integer tile){
		this(laneCoordinates.getFlowCell(), laneCoordinates.getLane(), tile);
	}
	
	/**
	 * @param lane
	 * @param tile
	 * @param x
	 * @param y
	 */
	public TileCoordinates(String flowCell, Integer lane, Integer tile) {		
		super(flowCell, lane);
		this.tile = tile;
	}

	public TileCoordinates() {
		super();
	}

	@Override
	public String toString() {
		return "TileCoordinates [flowCell=" + flowCellID + ", lane=" + lane
				+ ", tile=" + tile + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + flowCellID;
		result = prime * result + ((lane == null) ? 0 : lane.hashCode());
		result = prime * result + ((tile == null) ? 0 : tile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileCoordinates other = (TileCoordinates) obj;
		if (flowCellID != other.flowCellID)
			return false;
		if (lane == null) {
			if (other.lane != null)
				return false;
		} else if (!lane.equals(other.lane))
			return false;
		if (tile == null) {
			if (other.tile != null)
				return false;
		} else if (!tile.equals(other.tile))
			return false;
		return true;
	}

	public Integer getTile() {
		return tile;
	}
	
	// customized JSON Serialization

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
        obj.put(JSON_ATTR_TILE, tile);
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {

		super.fromJSONObject(jsonObject);
        tile = new Integer(jsonObject.get(JSON_ATTR_TILE).toString());
		
	}

	// customized JSON Serialization


}
