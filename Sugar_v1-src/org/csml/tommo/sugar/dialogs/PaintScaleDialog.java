package org.csml.tommo.sugar.dialogs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class PaintScaleDialog extends JDialog{

	private PaintScaleLegend legend;
	
	public PaintScaleDialog(PaintScaleLegend legend){
		super();
		this.legend = legend;
		init();
	}

	private void init() {
		LegendComponent component = new LegendComponent();
		getContentPane().add(component);
		pack();
	}	
	
	private class LegendComponent extends JComponent{
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			legend.draw((Graphics2D)g, new Rectangle(0, 0, getWidth(), getHeight()));
		}
	}
	
	public static void main(String[] args){
        NumberAxis yAxis = new NumberAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        PaintScale scale = new LinearPaintScale(0, 0.7, Color.YELLOW.brighter(), Color.RED.darker());
        yAxis.setRange(0, 0.7);
        yAxis.setMinorTickCount(10);
        yAxis.setMinorTickMarksVisible(true);

        // draw paint scale legend
		PaintScaleLegend legend = new PaintScaleLegend(scale, yAxis);
        legend.setAxisOffset(1.0);
        legend.setMargin(new RectangleInsets(5, 1, 5, 1));
        legend.setPosition(RectangleEdge.LEFT);
        legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
//        legend.setSubdivisionCount(10);
		PaintScaleDialog dialog = new PaintScaleDialog(legend);
		dialog.setSize(20, 100);
		dialog.setLocation(500, 400);
		dialog.setVisible(true);
	}
}
