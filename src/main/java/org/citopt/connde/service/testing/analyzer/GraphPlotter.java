package org.citopt.connde.service.testing.analyzer;

import org.citopt.connde.domain.testing.TestDetails;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The GraphPlotter Component creates a graph of the data generated within a test in the Testing-Tool and saves it as a .gif file.
 */
@Component
public class GraphPlotter {


    public GraphPlotter() throws HeadlessException {
        // required, because some components of the toolkit otherwise trigger a headless exception on headless machines
        if (!GraphicsEnvironment.isHeadless()) {
            System.setProperty("java.awt.headless", "true");
        }
    }

    /**
     * Creates the Test report for a specific test.
     *
     * @param test for which a graph of the generated values of the sensors should be created
     * @throws IOException In case of an I/O issue
     */
    public void createGraph(TestDetails test) throws IOException {
        BufferedImage bufferedImage;
        final XYSeriesCollection dataSetTest = createDataset(test);

        // Creates a line chart based on the generated data set of the specific test values
        JFreeChart chart = ChartFactory.createXYLineChart("Simulation Values", // chart title
                "time", // domain axis label
                "values", // range axis label
                dataSetTest, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
        );

        // Save graph
        bufferedImage = chart.createBufferedImage(400, 400);
        FileOutputStream outputStream = new FileOutputStream(test.getId() + ".gif");
        ImageIO.write(bufferedImage, "gif", outputStream);
        outputStream.close();
    }

    /**
     * Generates the data set for all values generated within a test.
     *
     * @param test for which data should be displayed
     * @return data set of the values generated within a test
     */
    private static XYSeriesCollection createDataset(TestDetails test) {
        XYSeriesCollection dataSet = new XYSeriesCollection();

        // Get the list of generated values within the specific test 
        Map<String, LinkedHashMap<Long, Double>> generatedValues = test.getSimulationList();

        if (generatedValues != null && generatedValues.size() > 0) {
            // Adds sensor name and corresponding data to the data set
            for (Map.Entry<String, LinkedHashMap<Long, Double>> mapEntry : generatedValues.entrySet()) {
                double counter = 0.0;
                // Get sensor name for the legend
                String sensorName = String.valueOf(mapEntry.getKey());
                XYSeries series = new XYSeries(sensorName);

                // Add the generated values to the series to display them
                Map<Long, Double> sensorValues = mapEntry.getValue();
                for (Map.Entry<Long, Double> value : sensorValues.entrySet()) {
                    series.add(counter, value.getValue());
                    counter += 1.0;
                }

                dataSet.addSeries(series);
            }
        }


        return dataSet;
    }

}