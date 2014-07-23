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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.csml.tommo.sugar.analysis.JSONSerializable;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class LaneCoordinates implements Serializable, JSONSerializable {
	
	
	private static final String JSON_ATTR_FLOW_CELL = "flowCell";

	private static final String JSON_ATTR_LANE = "lane";

	/**
	 * 
	 */
	private static final long serialVersionUID = 3004529061996513322L;
	
	protected static Map<String, Byte> flowCellIdsMap = new HashMap<String, Byte>();
	protected static Map<Byte, String> flowCellNamesMap = new HashMap<Byte, String>();
	
	/**
	 * flow cell ID
	 */
	protected byte flowCellID;

	/**
	 * flow cell lane
	 */
	protected Integer lane;
		
	/**
	 * @param lane
	 * @param tile
	 * @param x
	 * @param y
	 */
	public LaneCoordinates(String flowCell, Integer lane) {
		
		Byte flowCellID = computeFlowCellID(flowCell);
		
		this.flowCellID = flowCellID;
		this.lane = lane;
	}

	public LaneCoordinates() {
	}

	private Byte computeFlowCellID(String flowCell) {
		Byte flowCellID = flowCellIdsMap.get(flowCell); 
		if (flowCellID == null)
		{
			flowCellID = ((Integer) flowCellIdsMap.size()).byteValue();
			flowCellIdsMap.put(flowCell, flowCellID);
			flowCellNamesMap.put(flowCellID, flowCell);
		}
		return flowCellID;
	}

	@Override
	public String toString() {
		return "LaneCoordinates [flowCell=" + getFlowCell() + ", lane=" + lane + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + flowCellID;
		result = prime * result + ((lane == null) ? 0 : lane.hashCode());
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
		LaneCoordinates other = (LaneCoordinates) obj;
		if (flowCellID != other.flowCellID)
			return false;
		if (lane == null) {
			if (other.lane != null)
				return false;
		} else if (!lane.equals(other.lane))
			return false;
		return true;
	}

	public Integer getLane() {
		return lane;
	}
	
	public Byte getFlowCellID() {
		return flowCellID;
	}

	public String getFlowCell() {
		return flowCellNamesMap.get(flowCellID);
	}

	// customized JSON Serialization

	@Override
	public String toJSONString() {
        JSONObject obj = toJSONObject();
        return obj.toString();		        
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
        JSONObject obj = toJSONObject();
        JSONValue.writeJSONString(obj, out);		
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
        obj.put(JSON_ATTR_LANE, lane);
        obj.put(JSON_ATTR_FLOW_CELL, getFlowCell());
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
        lane = new Integer(jsonObject.get(JSON_ATTR_LANE).toString());
		String flowCell = jsonObject.get(JSON_ATTR_FLOW_CELL).toString();
        flowCellID = computeFlowCellID(flowCell);        
		
	}

	// customized JSON Serialization

}
