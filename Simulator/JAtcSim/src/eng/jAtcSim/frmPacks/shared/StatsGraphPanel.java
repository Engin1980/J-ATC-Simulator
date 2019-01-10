package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.*;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.app.controls.ImagePanel;
import eng.jAtcSim.app.extenders.XComboBoxExtender;
import eng.jAtcSim.lib.newStats.Snapshot;
import eng.jAtcSim.lib.newStats.StatsManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class StatsGraphPanel extends JPanel {

  private static class Measure {
    public final IReadOnlyList<MeasureLine> lines;
    public final String title;
    public final CategoryItemRenderer renderer;

    public Measure(String title, IList<MeasureLine> lines, CategoryItemRenderer renderer) {
      this.title = title;
      this.lines = lines;
      this.renderer = renderer;
    }
  }

  private static class MeasureLine {
    public final String title;
    public final Function<Snapshot, Double> valueSelector;

    public MeasureLine(String title, Function<Snapshot, Double> valueSelector) {
      this.title = title;
      this.valueSelector = valueSelector;
    }
  }

//  private static class GraphMeasureView {
//    public Function<StatsView, DataView> dataViewSelector;
//    public Function<DataView, Double> valueSelector;
//
//    public GraphMeasureView(Function<StatsView, DataView> dataViewSelector, Function<DataView, Double> valueSelector) {
//      this.dataViewSelector = dataViewSelector;
//      this.valueSelector = valueSelector;
//    }
//  }

  private static final int IMG_WIDTH = 2000;
  private static final int IMG_HEIGHT = 800;
  //  private IReadOnlyList<StatsView> statsViews;
  private static IReadOnlyList<Measure> measures;
  //  private static IMap<String, IMap<String, GraphMeasureView>> graphMeasures = new EMap();
//  private static IMap<String, CategoryItemRenderer> renderers = new EMap<>();
  private XComboBoxExtender<String> cmbMeasures;
  private ImagePanel pnlImage;
  private StatsManager statsManager;

  static {
    EList<Measure> measures = new EList<>();
    Measure measure;
    IList<MeasureLine> lines;

    lines = new EList<>();
    lines.add(new MeasureLine("Departures", q -> q.getRunwayMovementsPerHour().getArrivals()));
    lines.add(new MeasureLine("Arrivals", q -> q.getRunwayMovementsPerHour().getDepartures()));
    lines.add(new MeasureLine("Total", q -> q.getRunwayMovementsPerHour().getTotal()));
    measure = new Measure(
        "Runway movements per hour", lines, new BarRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Departures (max)", q->q.getPlanesInSim().getDepartures().getMaximum()));
    lines.add(new MeasureLine("Arrivals (max)", q->q.getPlanesInSim().getArrivals().getMaximum()));
    lines.add(new MeasureLine("Total (max)", q->q.getPlanesInSim().getTotal().getMaximum()));
    lines.add(new MeasureLine("Departures (mean)", q->q.getPlanesInSim().getDepartures().getMean()));
    lines.add(new MeasureLine("Arrivals (mean)", q->q.getPlanesInSim().getArrivals().getMean()));
    lines.add(new MeasureLine("Total (mean)", q->q.getPlanesInSim().getTotal().getMean()));
    measure = new Measure(
        "Planes in sim", lines, new LineRenderer3D());
    measures.add(measure);

    StatsGraphPanel.measures = measures;
  }
//    IMap<String, GraphMeasureView> g;
//    String key;
//
//    // calc time
//    key = "Calculation time (s)";
//    g = buildSimpleDisplaySet(q -> q.getSecondStats().getDuration());
//    graphMeasures.set(key, g);
//    renderers.set(key, new LineAndShapeRenderer());
//
////    key = "Movements / hour (together)";
////    g = new EMap<>();
////    g.set("Movements / hour",
////        new GraphMeasureView(
////            q->q.getPlanes().getFinishedPlanes().getTogether(),
////            q->((MinMaxMeanCountCurrentView)q).getSum() / ));
////    graphMeasures.set(key, g);
////    renderers.set(key, new BarRenderer());
//
//    // max planes in sim (together)
//    key = "Planes in sim (together)";
//    g = new EMap<>();
//    g.set("Number of planes",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesInSim().getTogether(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    graphMeasures.set(key, g);
//    renderers.set(key, new BarRenderer());
//
//    // max planes in sim (arrs / deps)
//    key = "Planes in sim (arrivals / departures)";
//    g = new EMap<>();
//    g.set("Number of arrivals",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesInSim().getArrivals(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    g.set("Number of departures",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesInSim().getDepartures(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    graphMeasures.set(key, g);
//    renderers.set(key, new LineRenderer3D());
//
//    key = "Planes under APP (together)";
//    g = new EMap<>();
//    g.set("Number of planes",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesUnderApp().getTogether(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    graphMeasures.set(key, g);
//    renderers.set(key, new BarRenderer());
//
//    key = "Planes under APP (arrivals / departures)";
//    g = new EMap<>();
//    g.set("Number of arrivals",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesUnderApp().getArrivals(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    g.set("Number of departures",
//        new GraphMeasureView(
//            q -> q.getPlanes().getPlanesUnderApp().getDepartures(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    graphMeasures.set(key, g);
//    renderers.set(key, new StackedBarRenderer());
//
////    g = buildPlanesMaxiumumDisplaySet(q -> q.getPlanes().getPlanesUnderApp());
////    graphMeasures.set("Planes under APP", g);
////    renderers.set("Planes under APP", new BarRenderer());
////    g = buildPlanesDisplaySet(q -> q.getPlanes().getFinishedPlanes());
////    graphMeasures.set("Finished planes", g);
////    g = buildPlanesDisplaySet(q -> q.getPlanes().getDelay());
////    graphMeasures.set("Delay", g);
//  }

//  private static EMap<String, GraphMeasureView> buildSimpleDisplaySet(Function<StatsView, DataView> selector) {
//    EMap<String, GraphMeasureView> g = new EMap<>();
//    g.set("",
//        new GraphMeasureView(
//            q -> selector.apply(q),
//            q -> ((MeanView) q).getMean()));
//    return g;
//  }
//
//  private static <T extends DataView> EMap<String, GraphMeasureView> buildPlanesMaxiumumDisplaySet(Function<StatsView, PlanesSubStats<T>> selector) {
//    EMap<String, GraphMeasureView> g = new EMap<>();
//    g.set("Arrivals (max)",
//        new GraphMeasureView(
//            q -> selector.apply(q).getArrivals(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    g.set("Departures (max)",
//        new GraphMeasureView(
//            q -> selector.apply(q).getDepartures(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    g.set("Together (max)",
//        new GraphMeasureView(
//            q -> selector.apply(q).getTogether(),
//            q -> ((MinMaxMeanCountCurrentView) q).getMaximum()));
//    return g;
//  }

  public StatsGraphPanel() {
    initComponents();
    layoutComponents();
  }

  public void init(StatsManager stats) {
    this.statsManager = stats;
//    this.statsViews = stats.createViews();
  }

  private void layoutComponents() {
    JPanel pnlTop = LayoutManager.createBorderedPanel(4, cmbMeasures.getControl());

    pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);
    JScrollPane pnlScr = new JScrollPane(pnlImage);
    JPanel pnlContent = LayoutManager.createBorderedPanel(4, pnlScr);

    LayoutManager.fillBorderedPanel(this, pnlTop, null, null, null, pnlContent);
  }

  private void initComponents() {
    cmbMeasures = new XComboBoxExtender<>();
    cmbMeasures.setModel(StatsGraphPanel.measures.select(q -> q.title).toArray(String.class));
    cmbMeasures.getOnSelectedItemChanged().add(this::cmbMeasure_selectionChanged);
  }

  private void cmbMeasure_selectionChanged(XComboBoxExtender<String> source) {
    String key = source.getSelectedItem();
    Measure m = measures.getFirst(q->q.title.equals(key));
    GraphDataSet gds = buildGraphDataSet(m);
    showGraphDataSet(gds, m.renderer);
  }

  private void showGraphDataSet(GraphDataSet gds, CategoryItemRenderer renderer) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (GraphDataLine item : gds.lines) {
      for (int i = 0; i < item.values.length; i++) {
        dataset.addValue(item.values[i], gds.xAxisTitles[i], item.lineTitle);
      }
    }

    CategoryAxis domainAxis = new CategoryAxis(gds.title);
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(
            90d * Math.PI / 180d));
    ValueAxis rangeAxis = new NumberAxis("Mean");

    renderer.setBaseItemLabelGenerator(
        new StandardCategoryItemLabelGenerator());
    for (int i = 0; i < gds.xAxisTitles.length; i++) {
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

  private GraphDataSet buildGraphDataSet(Measure measure) {
    int step = 1;
    GraphDataSet gds = new GraphDataSet();
    IReadOnlyList<Snapshot> snapshots = statsManager.getSnapshots(step);

    IList<String> lineNames = measure.lines.select(q -> q.title);

    gds.title = measure.title;
    gds.xAxisTitles = snapshots.select(q -> q.getTime().toString()).toArray(String.class);

    for (MeasureLine measureLine : measure.lines) {
      GraphDataLine gdl = new GraphDataLine();
      gdl.lineTitle = measureLine.title;
      gdl.values = ArrayUtils.toPrimitive(
          snapshots.select(q -> measureLine.valueSelector.apply(q)).toArray(Double.class));
      gds.lines.add(gdl);
    }

    return gds;
  }
}

class GraphDataSet {
  public String title;
  public String[] xAxisTitles;
  public EList<GraphDataLine> lines = new EList();
}

class GraphDataLine {
  public String lineTitle;
  public double[] values;
}
