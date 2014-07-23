package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

public class ExtendedXYItemRenderer extends StandardXYItemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1869152607112811289L;
	private static final double MINIMUM_LENGTH = 100;

	private final static NumberFormat PERCENT_NUMBER_FORMAT = createNumberFormat(); 

	/**
	 * Draws a horizontal line across the chart to represent a 'range marker'.
	 *
	 * @param g2  the graphics device.
	 * @param plot  the plot.
	 * @param rangeAxis  the range axis.
	 * @param marker  the marker line.
	 * @param dataArea  the axis data area.
	 */
	@Override
	public void drawRangeMarker(Graphics2D g2,
                                XYPlot plot,
                                ValueAxis rangeAxis,
                                Marker marker,
                                Rectangle2D dataArea) {

        if (marker instanceof ExtendedValueMarker) {
            ExtendedValueMarker vm = (ExtendedValueMarker) marker;   
            
            ValueAxis domainAxis = plot.getDomainAxis();
            
            dataArea = (Rectangle2D) dataArea.clone();
            if (vm.getStart() > 0)
            {
                double start = domainAxis.valueToJava2D(vm.getStart(), dataArea, plot.getDomainAxisEdge());
                
                if (dataArea.getWidth() - start < MINIMUM_LENGTH)
                	start = dataArea.getWidth() - MINIMUM_LENGTH;
                
                double width = dataArea.getWidth() - start + dataArea.getX();
                
            	dataArea.setRect(start, dataArea.getY(), width, dataArea.getHeight());
            	            	
                vm.setLabel("Ratio: " + PERCENT_NUMBER_FORMAT.format(vm.getValue()));

            }
            else if (vm.getEnd() > 0)
            {
                double end = domainAxis.valueToJava2D(vm.getEnd(), dataArea, plot.getDomainAxisEdge()) - dataArea.getX();
                if (end < MINIMUM_LENGTH)
                	end = MINIMUM_LENGTH;
            	dataArea.setRect(dataArea.getX(), dataArea.getY(), end, dataArea.getHeight());

            	vm.setLabel("Value: " + NumberFormat.getInstance().format(vm.getValue()));
            }
            else 
            {
            	// do NOT draw any bars
            	return;
            }
            
        }

        super.drawRangeMarker(g2, plot, rangeAxis, marker, dataArea);
    }

	private static NumberFormat createNumberFormat() {
		NumberFormat result = (NumberFormat) NumberFormat.getPercentInstance().clone();
		result.setMaximumFractionDigits(1);
		
		return result;
	}
	
	
	

}
