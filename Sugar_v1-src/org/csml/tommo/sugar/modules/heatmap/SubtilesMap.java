package org.csml.tommo.sugar.modules.heatmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.csml.tommo.sugar.sequence.SubtileCoordinates;
import org.csml.tommo.sugar.sequence.SubtileProperties;

public class SubtilesMap extends HashMap<SubtileCoordinates, SubtileProperties> {

	public SubtilesMap(){
		super();
	}
	
	public List<SubtileProperties> getSortedValuesByProperty(final ESubtileProperty property){
		List<SubtileProperties> result = new ArrayList<SubtileProperties>();
		
		for(SubtileProperties properties: values()){
			result.add(properties);
		}

		Comparator<SubtileProperties> comparator = new Comparator<SubtileProperties>(){
			@Override
			public int compare(SubtileProperties o1, SubtileProperties o2) {
				double v1 = o1.getPropertyValue(property);
				double v2 = o2.getPropertyValue(property);
				return (int)Math.signum(v1 - v2);
			}
		};
		
		if(property.hasReversedOrder()){
			comparator = Collections.reverseOrder(comparator);
		}
		
		Collections.sort(result, comparator);
		return result;
	}
	
	public double getMaxPropertyValue(ESubtileProperty property){
		double max = Double.MIN_VALUE;
		for(SubtileProperties properties: values()){
			if(properties.getPropertyValue(property) > max){
				max = properties.getPropertyValue(property);
			}
		}
		return max;
	}
	
	public double getMinPropertyValue(ESubtileProperty property){
		double min = Double.MAX_VALUE;
		for(SubtileProperties properties: values()){
			if(properties.getPropertyValue(property) < min){
				min = properties.getPropertyValue(property);
			}
		}
		return min;
	}
}
