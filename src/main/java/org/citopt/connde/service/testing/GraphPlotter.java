package org.citopt.connde.service.testing;

import org.citopt.connde.domain.testing.TestDetails;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The GraphPlotter component is used to create graphs of the simulated sensor values used for testing applications within the testing tool
 */
@Component
public class GraphPlotter extends JFrame {

    public void createGraphReport(TestDetails test) throws IOException {
        XYDataset dataset = createDataset(test);

        JFreeChart chart = ChartFactory.createXYLineChart("Simulation Values", // chart title
                "time", // domain axis label
                "values", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
        );


        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);
        chartPanel.setVisible(true);
        final File file = new File(test.getId() + ".png");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
        ChartUtils.saveChartAsPNG(file, chart, 500, 300);

    }

    /**
     * Get all values from the test to create the dataset for the chart
     *
     * @param test
     * @return
     */
    private static XYSeriesCollection createDataset(TestDetails test) {
        XYSeriesCollection dataSet = new XYSeriesCollection();

        Map<String, java.util.List<Double>> simulationLists = test.getSimulationList();
        Iterator it = simulationLists.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String object = String.valueOf(pair.getKey());
            XYSeries series = new XYSeries(object);
            java.util.List<Double> simulationVal = (List<Double>) pair.getValue();
            for (int j = 0; j < simulationVal.size(); j++) {
                series.add(Double.valueOf(j), simulationVal.get(j));
            }
            dataSet.addSeries(series);
        }

        return dataSet;
    }

}

    
