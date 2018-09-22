package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.shared.LayoutManager;
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

public class FrmTrafficHistogram extends JFrame {
  private static final int IMG_WIDTH = 700;
  private static final int IMG_HEIGHT = 500;
  private static final int MARGIN = 20;
  private int bankCount = 23;
  private IMap<String, double[]> series = new EMap<>();
  private XComboBoxExtender<String> cmbSerie = new XComboBoxExtender<>();
  private String title;

  private ImagePanel pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);

  public FrmTrafficHistogram() {
    initComponents();
    initLayout();
    this.pack();
  }

  private void initLayout() {
    JPanel pnlContent =
        LayoutManager.createBoxPanel(pnlImage);
    JPanel pnlBottom = LayoutManager.createFlowPanel(cmbSerie.getControl());

    LayoutManager.fillBorderedPanel(this, null, pnlBottom, null, null, pnlContent);
  }

  private void initComponents() {
    this.setPreferredSize(new Dimension(IMG_WIDTH + MARGIN, IMG_HEIGHT + 4 * MARGIN));
    this.add(pnlImage);
  }

  public void init(Traffic traffic, String title) {
    Validator.isNotNull(traffic);
    Validator.isNotNull(title);

    this.title = title;

    Double[] tmpWrapped;
    double[] tmpPrimitive;
    IList<String> keyLst = new EList<>();
    String key;
    IReadOnlyList<Traffic.ExpectedMovement> scheduledTimes = traffic.getExpectedTimesForDay();

    key = "All movements";
    tmpWrapped = scheduledTimes.select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Arrivals";
    tmpWrapped = scheduledTimes.where(q -> q.isArrival).select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Departures";
    tmpWrapped = scheduledTimes.where(q -> !q.isArrival).select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Commercials";
    tmpWrapped = scheduledTimes.where(q -> q.isCommercial).select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Non-commericals";
    tmpWrapped = scheduledTimes.where(q -> !q.isCommercial).select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Type A";
    tmpWrapped = scheduledTimes.where(q -> q.category == 'A').select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Type B";
    tmpWrapped = scheduledTimes.where(q -> q.category == 'B').select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Type C";
    tmpWrapped = scheduledTimes.where(q -> q.category == 'C').select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    key = "Type D";
    tmpWrapped = scheduledTimes.where(q -> q.category == 'D').select(q -> q.time.getTotalHours()).toArray(Double.class);
    tmpPrimitive = ArrayUtils.toPrimitive(tmpWrapped);
    if (tmpPrimitive.length != 0) {
      this.series.set(key, tmpPrimitive);
      keyLst.add(key);
    }

    String [] keyArr = keyLst.toArray(String.class);
    cmbSerie.setModel(keyArr);
    cmbSerie.setSelectedIndex(0);

    cmbSerie.getSelectedItemChanged().add(q -> this.updateHistogram());

    updateHistogram();
  }

  private void updateHistogram() {
    String key = cmbSerie.getSelectedItem();
    double[] values = this.series.get(key);

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries(key, values, bankCount);
    double[] revs = new double[values.length];
    for (int i = 0; i < revs.length; i++) {
      revs[i] = values[revs.length - i - 1];
    }
    String plotTitle = this.title + " - " + key;
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
