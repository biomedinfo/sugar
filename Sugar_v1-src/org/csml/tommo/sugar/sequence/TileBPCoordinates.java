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



public class TileBPCoordinates extends TileCoordinates {
	
	private static final String JSON_ATTR_BASE_POSITION = "basePosition";

	/**
	 * 
	 */
	private static final long serialVersionUID = 4886189280979070317L;
	
	/**
	 * tile number within the flow cell lane
	 */
	protected Integer basePosition;
	

	/**
	 * @param flowCell
	 * @param lane
	 * @param tile
	 * @param basePosition
	 */
	public TileBPCoordinates(String flowCell, Integer lane, Integer tile,
			Integer basePosition) {
		super(flowCell, lane, tile);
		this.basePosition = basePosition;
	}
	
	public TileBPCoordinates(LaneCoordinates laneCoordinates, Integer tile,
			int basePosition) {
		this(laneCoordinates.getFlowCell(), laneCoordinates.getLane(), tile, basePosition);
	}

	public TileBPCoordinates(TileCoordinates tileCoordinates, int basePosition) {
		this(tileCoordinates.getFlowCell(), tileCoordinates.getLane(), tileCoordinates.getTile(), basePosition);
	}


	public TileBPCoordinates() {
	}

	@Override
	public String toString() {
		return "TileBPCoordinates [flowCell=" + flowCellID + ", lane=" + lane
				+ ", tile=" + tile + ", basePosition=" + basePosition + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((basePosition == null) ? 0 : basePosition.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileBPCoordinates other = (TileBPCoordinates) obj;
		if (basePosition == null) {
			if (other.basePosition != null)
				return false;
		} else if (!basePosition.equals(other.basePosition))
			return false;
		return true;
	}

	public Integer getBasePosition() {
		return basePosition;
	}

	public TileCoordinates getTileCoordinate() {
		return new TileCoordinates(getFlowCell(), getLane(), getTile());
	}
	
	// customized JSON Serialization

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
        obj.put(JSON_ATTR_BASE_POSITION, basePosition);
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {

		super.fromJSONObject(jsonObject);
        basePosition = new Integer(jsonObject.get(JSON_ATTR_BASE_POSITION).toString());
		
	}

	// customized JSON Serialization

}
