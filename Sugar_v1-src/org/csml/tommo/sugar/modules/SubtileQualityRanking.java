package org.csml.tommo.sugar.modules;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.csml.tommo.sugar.heatmap.MappingQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.heatmap.ESubtileProperty;
import org.csml.tommo.sugar.modules.heatmap.SubtileQualityRankingPanel;
import org.csml.tommo.sugar.modules.heatmap.SubtilesMap;
import org.csml.tommo.sugar.sequence.SubtileCoordinates;
import org.csml.tommo.sugar.sequence.SubtileProperties;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.jfree.chart.JFreeChart;

import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;

public class SubtileQualityRanking implements QCModule {

	private QualityHeatMapsPerTileAndBase qualityHeatmapsPerTileAndBase;
	private MappingQuality mappingQuality;
	private SubtilesMap subtilesMap;

	
	public SubtileQualityRanking(QualityHeatMapsPerTileAndBase qualityHeatmapsPerTileAndBase, MappingQuality mappingQuality){
		super();
		this.qualityHeatmapsPerTileAndBase = qualityHeatmapsPerTileAndBase;
		this.mappingQuality = mappingQuality;
		initSubtilesMap();
	}
	
	public void initSubtilesMap() {
		subtilesMap = new SubtilesMap();
		for(TileCoordinates key: qualityHeatmapsPerTileAndBase.meanQualityMatrixMap.keySet()){
			List<MeanQualityMatrix> matrixList = qualityHeatmapsPerTileAndBase.meanQualityMatrixMap.get(key);
			MappingQualityMatrix mappingQualityMatrix = mappingQuality.getMeanQualityMatrix(key);
			if(matrixList != null && !matrixList.isEmpty()){
				int size = matrixList.get(0).getSize();
				for(int x=0; x<size; x++){
					for(int y=0; y<size; y++){
						int counter = 0;
						double total = 0;
						double averageQuality = 0;
						double rateOfReads = 0;
						for(MeanQualityMatrix matrix: matrixList){
							if(matrix != null){
								counter++;
								total += matrix.getTotalValueCounter()[x][y];
								averageQuality += matrix.getAverageQualityMatrix()[x][y];
								rateOfReads += matrix.getMeanValues()[x][y];
							}
						}
						
						double mappingQuality = (mappingQualityMatrix != null) ? mappingQualityMatrix.getMeanValues()[x][y] : 0d;
						
						if (counter > 0) {
							SubtileCoordinates subtile = new SubtileCoordinates(key.getFlowCell(), key.getLane(), key.getTile(), x, y);
							SubtileProperties properties = new SubtileProperties(
									subtile,
									total/counter,
									averageQuality/counter,
									rateOfReads/counter,
									mappingQuality
							);
							subtilesMap.put(subtile, properties);
						}
					}
				}
			}
		}
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new SubtileQualityRankingPanel(this);
		return returnPanel;
	}

	@Override
	public boolean ignoreFilteredSequences() {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws IOException {
		ZipOutputStream zip = report.zipFile();
		StringBuffer b = report.htmlDocument();
		StringBuffer d = report.dataDocument();
		SubtileQualityRankingPanel panel = new SubtileQualityRankingPanel(this);
		
		for(ESubtileProperty property: ESubtileProperty.values()){
			if(ESubtileProperty.MAPPING_QUALITY.equals(property) && !isMappingQualityActive()){
				continue;
			}
			String imgFileName = property.name() + ".png";
			zip.putNextEntry(new ZipEntry(report.folderName()+"/Images/" + imgFileName));
			JFreeChart chart = panel.createChart(property);
			BufferedImage chartImage = chart.createBufferedImage(800, 600);
			ImageIO.write(chartImage, "png", zip);
			b.append("<img src=\"Images/" + imgFileName + "\" alt=\"" + property.name() + "\">\n");
		}
	}

	@Override
	public String name() {
		return "Subtile quality ranking";
	}

	@Override
	public String description() {
		return "Ranking of subtiles";
	}

	@Override
	public void processSequence(Sequence sequence) {
		// do nothing 
		// the data was read by the parent already
	}

	@Override
	public boolean raisesError() {
		return false;
	}

	@Override
	public boolean raisesWarning() {
		return false;
	}

	@Override
	public void reset() {
		subtilesMap.clear();
	}

	public SubtilesMap getSubtilesMap() {
		return subtilesMap;
	}

	public QualityHeatMapsPerTileAndBase getQualityHeatmapsPerTileAndBase() {
		return qualityHeatmapsPerTileAndBase;
	}

	public MappingQuality getMappingQuality() {
		return mappingQuality;
	}
	
	public int getSubtilesCount(){
		return subtilesMap.size();
	}
	
	public boolean isMappingQualityActive(){
		return mappingQuality.isActive();
	}
}
