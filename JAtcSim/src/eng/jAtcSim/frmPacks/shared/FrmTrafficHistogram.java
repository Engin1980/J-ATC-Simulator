package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.Traffic;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class FrmTrafficHistogram extends JFrame {
  private static final int IMG_WIDTH = 700;
  private static final int IMG_HEIGHT = 500;
  private static final int MARGIN = 32;
  private int bankCount = 23;
  private ImagePanel pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);

  public FrmTrafficHistogram() {
    this.setPreferredSize(new Dimension(IMG_WIDTH + MARGIN, IMG_HEIGHT + MARGIN));
    this.add(pnlImage);
    this.pack();
  }

  public void init(Traffic traffic, String title) {

    Validator.isNotNull(traffic);
    IReadOnlyList<ETime> scheduledTimes = traffic.getExpectedTimesForDay();
    Double[] tmp = scheduledTimes.select(q -> q.getTotalHours()).toArray(Double.class);
    double[] vals = Arrays.stream(tmp).mapToDouble(Double::doubleValue).toArray();

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", vals, bankCount);
    String plotTitle = title;
    String xaxis = "Hour";
    String yaxis = "Movements";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean showLegend = false;
    boolean showTooltips = false;
    boolean showUrls = false;
    JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis,
        dataset, orientation, showLegend, showTooltips, showUrls);

    XYPlot plot = chart.getXYPlot();
    NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
    domainAxis.setRange(0, 24);
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setRange(0, 60);
    BufferedImage bufferedImage = chart.createBufferedImage(IMG_WIDTH, IMG_HEIGHT);
    pnlImage.setImage(bufferedImage);
    pnlImage.invalidate();
    pnlImage.repaint();

//    chart.
//    try {
//      ChartUtilities.saveChartAsPNG(new File("r:\\histogram.PNG"), chart, width, height);
//    } catch (IOException e) {
//    }
  }
}

class ImagePanel extends JPanel {

  private BufferedImage image;
  private final int width;
  private final int height;

  public ImagePanel(int width, int height) {
    this.width = width;
    this.height = height;
    Dimension d = new Dimension(width, height);
    this.setPreferredSize(d);
    this.setMinimumSize(d);
    this.setMaximumSize(d);
  }

  public synchronized void setImage(BufferedImage image) {
    this.image = image;
  }

  public synchronized void paint(Graphics g) {
    super.paint(g);
    if (this.image != null)
      g.drawImage(image, 0, 0, width, height, null);
  }
}
