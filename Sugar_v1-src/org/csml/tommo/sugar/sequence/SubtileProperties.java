package org.csml.tommo.sugar.sequence;

import java.util.HashMap;
import java.util.Map;

import org.csml.tommo.sugar.modules.heatmap.ESubtileProperty;

public class SubtileProperties {

	private SubtileCoordinates coordinates;
	private Map<ESubtileProperty, Double> valuesMap;
	
	public SubtileProperties(SubtileCoordinates coordinates, double total, double averageQuality, double rateOfReads, double mappingQuality){
		super();
		this.coordinates = coordinates;
		valuesMap = new HashMap<ESubtileProperty, Double>();
		valuesMap.put(ESubtileProperty.READ_DENSITY, total);
		valuesMap.put(ESubtileProperty.AVERAGE_QUALITY, averageQuality);
		valuesMap.put(ESubtileProperty.RATE_OF_LowQ_READS, rateOfReads);
		valuesMap.put(ESubtileProperty.MAPPING_QUALITY, mappingQuality);
	}

	public SubtileCoordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(SubtileCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	public double getPropertyValue(ESubtileProperty property){
		return valuesMap.get(property);
	}

	public void setPropertyValue(ESubtileProperty property, double value){
		valuesMap.put(property, value);
	}
	
	public String toString(){
		double total = getPropertyValue(ESubtileProperty.READ_DENSITY);
		double averageQuality = getPropertyValue(ESubtileProperty.AVERAGE_QUALITY);
		double rateOfReads = getPropertyValue(ESubtileProperty.RATE_OF_LowQ_READS);
		double mappingQuality = getPropertyValue(ESubtileProperty.MAPPING_QUALITY);
		return "total=" + total + ", average quality=" + averageQuality + ", rate of reads=" + rateOfReads + ", mapping quality=" + mappingQuality;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result
				+ ((valuesMap == null) ? 0 : valuesMap.hashCode());
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
		SubtileProperties other = (SubtileProperties) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (valuesMap == null) {
			if (other.valuesMap != null)
				return false;
		} else if (!valuesMap.equals(other.valuesMap))
			return false;
		return true;
	}
}
