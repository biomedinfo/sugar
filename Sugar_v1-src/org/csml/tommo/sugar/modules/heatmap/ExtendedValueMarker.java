package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.chart.plot.ValueMarker;

public class ExtendedValueMarker extends ValueMarker {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7849627577035059908L;

	
	private int start;
	private int end;
	

	public ExtendedValueMarker(double value, Paint paint, Stroke stroke) {
		super(value, paint, stroke);
		start=0;
		end=0;
		
	}	
	
	public int getStart() {
		return start;
	}

	public void setStart(int from) {
		this.start = from;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int to) {
		this.end = to;
	}



}
