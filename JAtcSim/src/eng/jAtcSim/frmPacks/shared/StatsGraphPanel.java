package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.*;
import eng.jAtcSim.app.controls.ImagePanel;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.lib.stats.read.StatsView;
import eng.jAtcSim.lib.stats.read.shared.DataView;
import eng.jAtcSim.lib.stats.read.shared.MeanView;
import eng.jAtcSim.lib.stats.read.specific.PlanesSubStats;
import eng.jAtcSim.shared.LayoutManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class StatsGraphPanel extends JPanel {

  private static final int IMG_WIDTH = 4000;
  private static final int IMG_HEIGHT = 1000;
  private static IMap<String, IMap<String, Function<StatsView, DataView>>> graphMeasures = new EMap();

  static {
    IMap<String, Function<StatsView, DataView>> g;

    g = buildSimpleDisplaySet(q -> q.getSecondStats().getDuration());
    graphMeasures.set("Calculation time (s)", g);

    g = buildPlanesDisplaySet(q -> q.getPlanes().getPlanesInSim());
    graphMeasures.set("Planes in sim", g);
    g = buildPlanesDisplaySet(q -> q.getPlanes().getPlanesUnderApp());
    graphMeasures.set("Planes under APP", g);
    g = buildPlanesDisplaySet(q -> q.getPlanes().getFinishedPlanes());
    graphMeasures.set("Finished planes", g);
    g = buildPlanesDisplaySet(q -> q.getPlanes().getDelay());
    graphMeasures.set("Delay", g);
  }

  private static EMap<String, Function<StatsView, DataView>> buildSimpleDisplaySet(Function<StatsView, DataView> selector) {
    EMap<String, Function<StatsView, DataView>> g = new EMap<>();
    g.set("", q -> selector.apply(q));
    return g;
  }

  private static <T extends DataView> EMap<String, Function<StatsView, DataView>> buildPlanesDisplaySet(Function<StatsView, PlanesSubStats<T>> selector) {
    EMap<String, Function<StatsView, DataView>> g = new EMap<>();
    g.set("Arrivals", q -> selector.apply(q).getArrivals());
    g.set("Departures", q -> selector.apply(q).getDepartures());
    g.set("Together", q -> selector.apply(q).getTogether());
    return g;
  }

  private XComboBoxExtender<String> cmbMeasure;
  private ImagePanel pnlImage;
  private IReadOnlyList<StatsView> statsViews;

  public StatsGraphPanel() {
    initComponents();
    layoutComponents();
  }

  private void layoutComponents() {
    JPanel pnlTop = LayoutManager.createBorderedPanel(4, cmbMeasure.getControl());

    pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);
    JScrollPane pnlScr = new JScrollPane(pnlImage);
    JPanel pnlContent = LayoutManager.createBorderedPanel(4, pnlScr);

    LayoutManager.fillBorderedPanel(this, pnlTop, null, null, null, pnlContent);
  }

  private void initComponents() {
    cmbMeasure = new XComboBoxExtender<>();
    cmbMeasure.setModel(graphMeasures.getKeys().toArray(String.class));
    cmbMeasure.getOnSelectedItemChanged().add(this::cmbMeasure_selectionChanged);
  }

  private void cmbMeasure_selectionChanged(XComboBoxExtender<String> source) {
    String key = source.getSelectedItem();
    GraphDataSet gds = buildGraphDataSet(key);
    showGraphDataSet(gds);
  }

  private void showGraphDataSet(GraphDataSet gds) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (GraphDataItem item : gds.items) {
      for (int i = 0; i < item.ys.length; i++) {
        dataset.addValue(item.ys[i], gds.xs[i], item.x);
      }
    }

    CategoryAxis domainAxis = new CategoryAxis(gds.title);
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(
            90d * Math.PI / 180d));
    ValueAxis rangeAxis = new NumberAxis("Mean");
    CategoryItemRenderer renderer = new StackedBarRenderer();

    renderer.setBaseItemLabelGenerator(
        new StandardCategoryItemLabelGenerator());
    for (int i = 0; i < gds.xs.length; i++) {
      renderer.setSeriesItemLabelsVisible(i, Boolean.TRUE);
    }

    CategoryPlot plot = new CategoryPlot(
        dataset, domainAxis, rangeAxis, renderer);
    JFreeChart chart = new JFreeChart(gds.title, plot);

    Dimension d = new Dimension(IMG_WIDTH , IMG_HEIGHT);
    BufferedImage bufferedImage = chart.createBufferedImage(d.width, d.height);

    pnlImage.setPreferredSize(d);
    pnlImage.setMinimumSize(d);
    pnlImage.setMaximumSize(d);
    pnlImage.setImage(bufferedImage);
    pnlImage.invalidate();
    pnlImage.repaint();
  }

  public void init(Statistics stats) {
    this.statsViews = stats.createViews();
  }

  private GraphDataSet buildGraphDataSet(String key) {
    GraphDataSet gds = new GraphDataSet();
    IMap<String, Function<StatsView, DataView>> g = graphMeasures.get(key);

    IList<String> keys = g.getKeys().toList();

    gds.title = key;
    gds.xs = new String[keys.size()];
    for (int i = 0; i < keys.size(); i++) {
      gds.xs[i] = keys.get(i);
      if (gds.xs[i].equals(""))
        gds.xs[i] = gds.title;
    }

    for (StatsView statsView : statsViews) {
      GraphDataItem gdi = new GraphDataItem();
      gdi.x = statsView.getFromTime().toString();
      gdi.ys = new double[keys.size()];
      for (int i = 0; i < keys.size(); i++) {
        DataView dv = g.get(keys.get(i)).apply(statsView);
        MeanView mv = (MeanView) dv;
        double val = mv.getMean();
        gdi.ys[i] = val;
      }
      gds.items.add(gdi);
    }
    return gds;
  }
}

class GraphDataSet {
  public String title;
  public String[] xs;
  public EList<GraphDataItem> items = new EList();
}

class GraphDataItem {
  public String x;
  public double[] ys;
}
