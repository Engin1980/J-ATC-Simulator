package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.*;
import eng.jAtcSim.app.controls.ImagePanel;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.lib.stats.read.StatsView;
import eng.jAtcSim.lib.stats.read.shared.DataView;
import eng.jAtcSim.lib.stats.read.shared.MeanView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;
import eng.jAtcSim.lib.stats.read.specific.PlanesSubStats;
import eng.jAtcSim.shared.LayoutManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class StatsGraphPanel extends JPanel {

  private static class GraphMeasureView {
    public Function<StatsView, DataView> dataViewSelector;
    public Function<DataView, Double> valueSelector;

    public GraphMeasureView(Function<StatsView, DataView> dataViewSelector, Function<DataView, Double> valueSelector) {
      this.dataViewSelector = dataViewSelector;
      this.valueSelector = valueSelector;
    }
  }

  private static final int IMG_WIDTH = 8000;
  private static final int IMG_HEIGHT = 800;
  private static IMap<String, IMap<String, GraphMeasureView>> graphMeasures = new EMap();
  private static IMap<String, CategoryItemRenderer> renderers = new EMap<>();
  private XComboBoxExtender<String> cmbMeasure;
  private ImagePanel pnlImage;
  private IReadOnlyList<StatsView> statsViews;

  static {
    IMap<String, GraphMeasureView> g;
    String key;

    // calc time
    key = "Calculation time (s)";
    g = buildSimpleDisplaySet(q -> q.getSecondStats().getDuration());
    graphMeasures.set(key, g);
    renderers.set(key, new LineAndShapeRenderer());

//    key = "Movements / hour (together)";
//    g = new EMap<>();
//    g.set("Movements / hour",
//        new GraphMeasureView(
//            q->q.getPlanes().getFinishedPlanes().getTogether(),
//            q->((MinMaxMeanCountCurrentView)q).getSum() / ));
//    graphMeasures.set(key, g);
//    renderers.set(key, new BarRenderer());

    // max planes in sim (together)
    key = "Planes in sim (together)";
    g = new EMap<>();
    g.set("Number of planes",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesInSim().getTogether(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    graphMeasures.set(key, g);
    renderers.set(key, new BarRenderer());

    // max planes in sim (arrs / deps)
    key = "Planes in sim (arrivals / departures)";
    g = new EMap<>();
    g.set("Number of arrivals",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesInSim().getArrivals(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    g.set("Number of departures",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesInSim().getDepartures(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    graphMeasures.set(key, g);
    renderers.set(key, new LineRenderer3D());

    key = "Planes under APP (together)";
    g = new EMap<>();
    g.set("Number of planes",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesUnderApp().getTogether(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    graphMeasures.set(key, g);
    renderers.set(key, new BarRenderer());

    key = "Planes under APP (arrivals / departures)";
    g = new EMap<>();
    g.set("Number of arrivals",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesUnderApp().getArrivals(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    g.set("Number of departures",
        new GraphMeasureView(
            q -> q.getPlanes().getPlanesUnderApp().getDepartures(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    graphMeasures.set(key, g);
    renderers.set(key, new StackedBarRenderer());

//    g = buildPlanesMaxiumumDisplaySet(q -> q.getPlanes().getPlanesUnderApp());
//    graphMeasures.set("Planes under APP", g);
//    renderers.set("Planes under APP", new BarRenderer());
//    g = buildPlanesDisplaySet(q -> q.getPlanes().getFinishedPlanes());
//    graphMeasures.set("Finished planes", g);
//    g = buildPlanesDisplaySet(q -> q.getPlanes().getDelay());
//    graphMeasures.set("Delay", g);
  }

  private static EMap<String, GraphMeasureView> buildSimpleDisplaySet(Function<StatsView, DataView> selector) {
    EMap<String, GraphMeasureView> g = new EMap<>();
    g.set("",
        new GraphMeasureView(
            q -> selector.apply(q),
            q -> ((MeanView) q).getMean()));
    return g;
  }

  private static <T extends DataView> EMap<String, GraphMeasureView> buildPlanesMaxiumumDisplaySet(Function<StatsView, PlanesSubStats<T>> selector) {
    EMap<String, GraphMeasureView> g = new EMap<>();
    g.set("Arrivals (max)",
        new GraphMeasureView(
            q -> selector.apply(q).getArrivals(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    g.set("Departures (max)",
        new GraphMeasureView(
            q -> selector.apply(q).getDepartures(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    g.set("Together (max)",
        new GraphMeasureView(
            q -> selector.apply(q).getTogether(),
            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
    return g;
  }

  public StatsGraphPanel() {
    initComponents();
    layoutComponents();
  }

  public void init(Statistics stats) {
    this.statsViews = stats.createViews();
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
    CategoryItemRenderer renderer = renderers.get(key);
    showGraphDataSet(gds, renderer);
  }

  private void showGraphDataSet(GraphDataSet gds, CategoryItemRenderer renderer) {
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

    renderer.setBaseItemLabelGenerator(
        new StandardCategoryItemLabelGenerator());
    for (int i = 0; i < gds.xs.length; i++) {
      renderer.setSeriesItemLabelsVisible(i, Boolean.TRUE);
    }

    CategoryPlot plot = new CategoryPlot(
        dataset, domainAxis, rangeAxis, renderer);
    JFreeChart chart = new JFreeChart(gds.title, plot);

    Dimension d = new Dimension(IMG_WIDTH, IMG_HEIGHT);
    BufferedImage bufferedImage = chart.createBufferedImage(d.width, d.height);

    pnlImage.setPreferredSize(d);
    pnlImage.setMinimumSize(d);
    pnlImage.setMaximumSize(d);
    pnlImage.setImage(bufferedImage);
    pnlImage.invalidate();
    pnlImage.repaint();
  }

  private GraphDataSet buildGraphDataSet(String key) {
    GraphDataSet gds = new GraphDataSet();
    IMap<String, GraphMeasureView> g = graphMeasures.get(key);

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
        DataView dv = g.get(keys.get(i)).dataViewSelector.apply(statsView);
        double val = g.get(keys.get(i)).valueSelector.apply(dv);
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
