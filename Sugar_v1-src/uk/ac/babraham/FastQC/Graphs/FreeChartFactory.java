package uk.ac.babraham.FastQC.Graphs;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class FreeChartFactory {
	
	public static ChartPanel createDuplicationLevelGraph(double[] counts, String[] labels, String title) {
		String categoryAxisLabel = "Sequence Duplication Level";
		String valueAxisLabel = "%Duplicate relative to unique";
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
        for(int i=0 ; i<labels.length ; i++){
        	if("1".equals(labels[i])) continue;
        	dataset.addValue(counts[i], categoryAxisLabel, labels[i]);
        }
        
        CategoryAxis categoryAxis = new CategoryAxis("");
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setRange(0d, 30d);
        valueAxis.setFixedDimension(30d);
//        valueAxis.setAutoRangeMinimumSize(30d);
//        valueAxis.setAutoRangeStickyZero(true);
        valueAxis.setAutoRange(false);
        CategoryItemRenderer renderer = new LineAndShapeRenderer();
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        JFreeChart chart = new JFreeChart(title, plot);
        chart.getTitle().setFont(chart.getTitle().getFont().deriveFont(10f));
		ChartPanel newPanel = new ChartPanel(chart);
		newPanel.setDomainZoomable(true);
		return newPanel;
	}
	
	public static ChartPanel createSequenceQualityScoresGraph(int[] xCategories, double[] qualityDistribution) {
		String title = "Quality score distribution over all sequences";
		String categoryAxisLabel = "Mean Sequence Quality (Phred Score)";
		String valueAxisLabel = "Average Quality per read";
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
        for(int i=0 ; i<xCategories.length ; i++){
        	dataset.addValue(qualityDistribution[i], categoryAxisLabel, String.valueOf(xCategories[i]));
        }        
        
        
        CategoryAxis categoryAxis = new CategoryAxis("");
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);    
        BarRenderer renderer = new BarRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        JFreeChart chart = new JFreeChart(title, plot);
        chart.getTitle().setFont(chart.getTitle().getFont().deriveFont(10f));
		ChartPanel newPanel = new ChartPanel(chart);
		return newPanel;
	}

	
}
