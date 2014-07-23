package org.csml.tommo.sugar.modules.heatmap;


public enum ESubtileProperty {
	
	READ_DENSITY("Number of reads within the subtile"),
	AVERAGE_QUALITY("Average quality value of reads within the subtile"),
	RATE_OF_LowQ_READS("Rate of the reads with QV<"),
	MAPPING_QUALITY("Average MapQ scores");
	
	private ESubtileProperty(String description){
		this.description = description;
	}
	
	private String description;

	public String getDescription() {
		return description;
	}
	
	public boolean hasReversedOrder(){
		return RATE_OF_LowQ_READS.equals(this);
	}
}
