package org.citopt.connde.service.testing;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The GraphPlotter component is used to create graphs of the simulated sensor values used for testing applications within the testing tool
 */
@Component
public class GraphPlotter {
    public GraphPlotter() throws HeadlessException {
        System.setProperty("java.awt.headless", "true");
        boolean headless = GraphicsEnvironment.isHeadless();
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.beep();
    }

    public void createTestReport(TestDetails test) throws IOException {

        BufferedImage bufferedImage;

        final XYSeriesCollection data = createDataset(test);


        JFreeChart chart = ChartFactory.createXYLineChart("Simulation Values", // chart title
                "time", // domain axis label
                "values", // range axis label
                data, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
        );


        bufferedImage = chart.createBufferedImage(400, 400);
        FileOutputStream outputStream = new FileOutputStream(test.getId() + ".gif");
        ImageIO.write(bufferedImage, "gif", outputStream);
        outputStream.close();


    }

    /**
     * Get all values from the test to create the dataset for the chart
     *
     * @param test with values to be plotted
     * @return data set of the values simulated for the test
     */
    private static XYSeriesCollection createDataset(TestDetails test) {
        XYSeriesCollection dataSet = new XYSeriesCollection();

        Map<String, List<Double>> simulationLists = test.getSimulationList();
        Iterator it = simulationLists.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String object = String.valueOf(pair.getKey());
            XYSeries series = new XYSeries(object);
            List<Double> simulationVal = (List<Double>) pair.getValue();
            for (int j = 0; j < simulationVal.size(); j++) {
                series.add(Double.valueOf(j), simulationVal.get(j));
            }
            dataSet.addSeries(series);
        }


        return dataSet;
    }

}

    
