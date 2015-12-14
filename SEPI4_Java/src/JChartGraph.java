import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

@SuppressWarnings("serial")
public class JChartGraph extends JFrame {

	public JChartGraph(double[][] trackData, int[] raceData) {
		super("ScalexTric Gyroscope Readings");
		
		JPanel chartPanel = createChartPanel(trackData,raceData);
		add(chartPanel, BorderLayout.CENTER);
		
		setSize(800, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	private JPanel createChartPanel(double[][] trackData, int[] raceData) {
		String chartTitle = "Gyroscope Z-Axis readings";
		String xAxisLabel = "Tacho count";
		String yAxisLabel = "°/sec";
		
		XYDataset dataset = createDataset(trackData);
		
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, 
				xAxisLabel, yAxisLabel, dataset);
		
		customizeChart(chart, raceData);
		
		/* Save the chart as an image files */
		File imageFile = new File("GyroZWithPlan.png");
		int width = 1200;
		int height = 600;
		
		try {
			ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
		} catch (IOException ex) {
			System.err.println(ex);
		}
		
		return new ChartPanel(chart);
	}

	private XYDataset createDataset(double[][] trackData) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series1 = new XYSeries("Gyro Z-axis");
		double[] cumulTacho = new double[trackData.length];
		cumulTacho[0] = 0;
		
		for (int j = 1; j < trackData.length; j++) {
			cumulTacho[j] = cumulTacho[j-1] + trackData[j][2];
		}
		for (int i = 0; i < trackData.length; i++) {
			series1.add(cumulTacho[i],trackData[i][1]);
		}		
		
		dataset.addSeries(series1);

		return dataset;
	}
	
	private void customizeChart(JFreeChart chart, int[] raceData) {
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		renderer.setSeriesPaint(0, Color.CYAN);
		
		for (int i = 0; i < raceData.length-1; i+=2) {
			if(raceData[i+1] == 70)
			{
				((XYPlot) chart.getPlot()).addAnnotation(new XYShapeAnnotation(new Rectangle2D.Double(raceData[i]-2, 50, 4, 1),new BasicStroke(3.0f),Color.YELLOW));
			}
			if(raceData[i+1] == 85)
			{
				((XYPlot) chart.getPlot()).addAnnotation(new XYShapeAnnotation(new Rectangle2D.Double(raceData[i]-2, 100, 4, 1),new BasicStroke(3.0f),Color.GREEN));
			}
			if(raceData[i+1] == 200)
			{
				((XYPlot) chart.getPlot()).addAnnotation(new XYShapeAnnotation(new Rectangle2D.Double(raceData[i]-2, 0, 4, 1),new BasicStroke(3.0f),Color.RED));

			}
		}
		
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, ShapeUtilities.createDiamond(1));

		plot.setOutlinePaint(Color.BLUE);
		plot.setOutlineStroke(new BasicStroke(2.0f));
		
		plot.setRenderer(renderer);

		plot.setBackgroundPaint(Color.DARK_GRAY);
		
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);
		
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);
		
		/* Custom legend creation */
		LegendItemCollection chartLegend = new LegendItemCollection();
		Shape shape = new Rectangle(30, 3);
		chartLegend.add(new LegendItem("GyroZ", null, null, null, shape, Color.CYAN));
		chartLegend.add(new LegendItem("Break", null, null, null, shape, Color.RED));
		chartLegend.add(new LegendItem("Motor 70%", null, null, null, shape, Color.YELLOW));
		chartLegend.add(new LegendItem("Motor 85%", null, null, null, shape, Color.GREEN));
		plot.setFixedLegendItems(chartLegend);
	}
}