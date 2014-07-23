package org.csml.tommo.sugar.heatmap;

import java.util.List;

import org.csml.tommo.sugar.modules.heatmap.ESubtileProperty;
import org.csml.tommo.sugar.modules.heatmap.SubtilesMap;
import org.csml.tommo.sugar.sequence.SubtileProperties;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class SubtileQualityRankingDataset extends XYSeriesCollection {

	public SubtileQualityRankingDataset(SubtilesMap subtileMap, ESubtileProperty property){
		int counter = 0;		
		XYSeries series = new XYSeries(property.name());
		
		List<SubtileProperties> list = subtileMap.getSortedValuesByProperty(property);
		
		for(SubtileProperties properties: list){
			counter++;
			series.add(counter, properties.getPropertyValue(property));
		}
		addSeries(series);
	}
}
