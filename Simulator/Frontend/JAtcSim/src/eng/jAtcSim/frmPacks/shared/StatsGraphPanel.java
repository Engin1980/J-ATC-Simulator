package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.app.controls.ImagePanel;
import eng.jAtcSim.newLib.area.newStats.Snapshot;
import eng.jAtcSim.newLib.area.newStats.StatsManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class StatsGraphPanel extends JPanel {

  private static class Measure {
    public final IReadOnlyList<MeasureLine> lines;
    public final String title;
    public final String yLabel;
    public final CategoryItemRenderer renderer;

    public Measure(String title, String yLabel, IList<MeasureLine> lines, CategoryItemRenderer renderer) {
      this.title = title;
      this.yLabel = yLabel;
      this.lines = lines;
      this.renderer = renderer;
    }
  }

  private static class MeasureLine {
    public final String title;
    public final Function<Snapshot, Double> valueSelector;
    public final Color color;

    public MeasureLine(String title, Function<Snapshot, Double> valueSelector) {
      this (title, valueSelector, null);
    }
    public MeasureLine(String title, Function<Snapshot, Double> valueSelector, Color color) {
      this.title = title;
      this.valueSelector = valueSelector;
      this.color = color;
    }
  }

  private static final int IMG_WIDTH = 1500;
  private static final int IMG_HEIGHT = 900;
  private static final Color COLOR_ARRIVAL = Color.orange;
  private static final Color COLOR_DEPARTURE = Color.blue;
  private static final Color COLOR_TOTAL = Color.black;
  private static final Color COLOR_ARRIVAL_APP = Color.yellow;
  private static final Color COLOR_DEPARTURE_APP = new Color(150, 150, 255);
  private static final Color COLOR_TOTAL_APP = new Color(178,178,178);
  private static IReadOnlyList<Measure> measures;
  private ComboBoxExtender<String> cmbMeasures;
  private ImagePanel pnlImage;
  private StatsManager statsManager;

  static {
    EList<Measure> measures = new EList<>();
    Measure measure;
    IList<MeasureLine> lines;

    lines = new EList<>();
    lines.add(new MeasureLine("Departures", q -> q.getRunwayMovementsPerHour().getArrivals(), COLOR_DEPARTURE));
    lines.add(new MeasureLine("Arrivals", q -> q.getRunwayMovementsPerHour().getDepartures(), COLOR_ARRIVAL));
    measure = new Measure(
        "Runway movements per hour", "Number of planes", lines, new StackedBarRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Departures in sim", q -> q.getPlanesInSim().getDepartures().getMaximum(), COLOR_DEPARTURE));
    lines.add(new MeasureLine("Arrivals in sim", q -> q.getPlanesInSim().getArrivals().getMaximum(), COLOR_ARRIVAL));
    lines.add(new MeasureLine("Total in sim", q -> q.getPlanesInSim().getTotal().getMaximum(), COLOR_TOTAL));
    lines.add(new MeasureLine("Departures under APP", q -> q.getPlanesUnderApp().getDepartures().getMaximum(), COLOR_DEPARTURE_APP));
    lines.add(new MeasureLine("Arrivals under APP", q -> q.getPlanesUnderApp().getArrivals().getMaximum(), COLOR_ARRIVAL_APP));
    lines.add(new MeasureLine("Total under APP", q -> q.getPlanesUnderApp().getTotal().getMaximum(), COLOR_TOTAL_APP));

    measure = new Measure(
        "Maximum number of planes", "Number of planes", lines, new LineAndShapeRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Departures in sim", q -> q.getPlanesInSim().getDepartures().getMean(), COLOR_DEPARTURE));
    lines.add(new MeasureLine("Arrivals in sim", q -> q.getPlanesInSim().getArrivals().getMean(), COLOR_ARRIVAL));
    lines.add(new MeasureLine("Total in sim", q -> q.getPlanesInSim().getTotal().getMean(), COLOR_TOTAL));
    lines.add(new MeasureLine("Departures under APP", q -> q.getPlanesUnderApp().getDepartures().getMean(), COLOR_DEPARTURE_APP));
    lines.add(new MeasureLine("Arrivals under APP", q -> q.getPlanesUnderApp().getArrivals().getMean(), COLOR_ARRIVAL_APP));
    lines.add(new MeasureLine("Total under APP", q -> q.getPlanesUnderApp().getTotal().getMean(), COLOR_TOTAL_APP));
    measure = new Measure(
        "Average number of planes", "Number of planes",lines, new LineAndShapeRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("MRVA violation", q -> (double) q.getMrvaErrorsCount(), Color.magenta));
    lines.add(new MeasureLine("Airprox violation", q -> (double) q.getAirproxErrorsCount(), Color.red));
    measure = new Measure(
        "Errors", "Incidents per second", lines, new LineAndShapeRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Average of mood rating", q -> q.getFinishedPlanesMoods().getTotal().getMean(), Color.blue));
    lines.add(new MeasureLine("Best mood rating", q -> q.getFinishedPlanesMoods().getTotal().getMaximum(), Color.green));
    lines.add(new MeasureLine("Worst mood rating", q -> q.getFinishedPlanesMoods().getTotal().getMinimum(), Color.red));
    measure = new Measure(
        "Mood rating (total)", "Achieved points", lines, new LineAndShapeRenderer());
    measures.add(measure);

    StatsGraphPanel.measures = measures;
  }

  public StatsGraphPanel() {
    initComponents();
    layoutComponents();
  }

  public void init(StatsManager stats) {
    this.statsManager = stats;
  }

  private void layoutComponents() {
    JPanel pnlTop = LayoutManager.createBorderedPanel(4, cmbMeasures.getControl());

    pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);
    JScrollPane pnlScr = new JScrollPane(pnlImage);
    JPanel pnlContent = LayoutManager.createBorderedPanel(4, pnlScr);

    LayoutManager.fillBorderedPanel(this, pnlTop, null, null, null, pnlContent);
  }

  private void initComponents() {
    cmbMeasures = new ComboBoxExtender<>();
    cmbMeasures.addItems(StatsGraphPanel.measures.select(q -> q.title).toArray(String.class));
    cmbMeasures.getOnSelectionChanged().add(this::cmbMeasure_selectionChanged);
  }

  private void cmbMeasure_selectionChanged(ComboBoxExtender<String> source) {
    String key = source.getSelectedItem();
    Measure m = measures.getFirst(q -> q.title.equals(key));
    GraphDataSet gds = buildGraphDataSet(m);
    showGraphDataSet(gds, m.renderer);
  }

  private void showGraphDataSet(GraphDataSet gds, CategoryItemRenderer renderer) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (GraphDataLine item : gds.lines) {
      for (int i = 0; i < item.values.length; i++) {
        dataset.addValue(item.values[i], item.lineTitle, gds.xAxisTitles[i]);
      }
    }

    CategoryAxis domainAxis = new CategoryAxis(gds.title);
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(
            90d * Math.PI / 180d));
    ValueAxis rangeAxis = new NumberAxis(gds.yAxisLabel);

    renderer.setBaseItemLabelGenerator(
        new StandardCategoryItemLabelGenerator());
    for (int i = 0; i < gds.xAxisTitles.length; i++) {
      renderer.setSeriesItemLabelsVisible(i, Boolean.TRUE);
    }

    CategoryPlot plot = new CategoryPlot(
        dataset, domainAxis, rangeAxis, renderer);

    CategoryItemRenderer cir = plot.getRenderer();
    for (int i = 0; i < gds.lines.size(); i++) {
      if (gds.lines.get(i).color != null)
        cir.setSeriesPaint(i, gds.lines.get(i).color);
    }

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

    gds.title = measure.title;
    gds.yAxisLabel = measure.yLabel;
    gds.xAxisTitles = snapshots.select(q -> q.getTime().toString()).toArray(String.class);

    for (MeasureLine measureLine : measure.lines) {
      GraphDataLine gdl = new GraphDataLine();
      gdl.lineTitle = measureLine.title;
      gdl.color = measureLine.color;
      gdl.values = ArrayUtils.toPrimitive(
          snapshots.select(q -> measureLine.valueSelector.apply(q)).toArray(Double.class));
      gds.lines.add(gdl);
    }

    return gds;
  }
}

class GraphDataSet {
  public String title;
  public String yAxisLabel;
  public String[] xAxisTitles;
  public EList<GraphDataLine> lines = new EList();
}

class GraphDataLine {
  public String lineTitle;
  public double[] values;
  public Paint color;
}
