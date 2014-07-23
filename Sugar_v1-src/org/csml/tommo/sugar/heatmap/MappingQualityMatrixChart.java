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
 *    SUGAR is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SUGAR is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SUGAR; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.heatmap;

import java.awt.Color;
import java.awt.Rectangle;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class MappingQualityMatrixChart extends JFreeChart {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PaintScaleLegend paintScaleLegend;

	public MappingQualityMatrixChart(Plot plot) {
		super(plot);
	}

	public static MappingQualityMatrixChart createNiceChart(MappingQualityMatrixDataset dataset, PaintScale paintScale){
		
		Rectangle range = dataset.getMappingQualityMatrix().getRange();
		String xLabel = "X from " + range.x + " to " + (range.x + range.width);
		String yLabel = "Y from " + range.y + " to " + (range.y + range.height);
		
        NumberAxis xAxis = new NumberAxis(xLabel);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setTickLabelsVisible(true);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        
        NumberAxis yAxis = new NumberAxis(yLabel);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setTickLabelsVisible(true);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);

        MappingQualityMatrixChart chart = createChart(dataset, xAxis, yAxis, paintScale);
        
        // draw paint scale legend
        chart.setPaintScaleLegend(paintScale);
        
        return chart;
	}
	
	public static MappingQualityMatrixChart createSimpleChart(MappingQualityMatrixDataset dataset, PaintScale paintScale){
				
        NumberAxis xAxis = new NumberAxis("");
//        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
//        xAxis.setLowerMargin(0.0);
//        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("");
//        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
//        yAxis.setLowerMargin(0.0);
//        yAxis.setUpperMargin(0.0);
        MappingQualityMatrixChart chart = createChart(dataset, xAxis, yAxis, paintScale);
        
        chart.getPlot().setOutlineVisible(false);
        chart.getPlot().setInsets(new RectangleInsets(0, 0, 0, 0));
        
        return chart;
	}

	private static MappingQualityMatrixChart createChart(
			MappingQualityMatrixDataset dataset, NumberAxis xAxis, NumberAxis yAxis, PaintScale paintScale) {
		XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        MappingQualityMatrixChart chart = new MappingQualityMatrixChart(plot);
        chart.setBackgroundPaint(Color.white);
        
        chart.removeLegend();
		return chart;
	}
	
	public static ChartPanel createChartPanel(
			MappingQualityMatrix matrix,
			PaintScale paintScale) {
		MappingQualityMatrixDataset dataset = new MappingQualityMatrixDataset(matrix);
		MappingQualityMatrixChart chart = MappingQualityMatrixChart.createNiceChart(dataset, paintScale);
		ChartPanel panel = new ChartPanel(chart);
		return panel;
	}
	
	public static ChartPanel createChartPanelByThreshold(int threshold,
			MappingQualityMatrix matrix,
			PaintScale paintScale) {
		MappingQualityMatrixDataset dataset = new MappingQualityMatrixByThresholdDataset(matrix, threshold);
		MappingQualityMatrixChart chart = MappingQualityMatrixChart.createNiceChart(dataset, paintScale);
		ChartPanel panel = new ChartPanel(chart);
		return panel;
	}
	
	public void setPaintScaleLegend(PaintScale paintScale){
		if (paintScaleLegend != null)
			this.removeSubtitle(paintScaleLegend);
		paintScaleLegend = createPaintScaleLegend(paintScale);
		this.addSubtitle(paintScaleLegend);
	}
	
	private static PaintScaleLegend createPaintScaleLegend(PaintScale paintScale) {
		NumberAxis scaleAxis = new NumberAxis("");

        PaintScaleLegend scaleLegend = new PaintScaleLegend(paintScale, scaleAxis);
        scaleLegend.setAxisOffset(2.0);
        scaleLegend.setPosition(RectangleEdge.RIGHT);
        scaleLegend.setMargin(new RectangleInsets(5, 1, 5, 1));
		return scaleLegend;
	}

	public void setPaintScale(PaintScale paintScale) {
		((XYBlockRenderer)getXYPlot().getRenderer()).setPaintScale(paintScale);		
	}

}
