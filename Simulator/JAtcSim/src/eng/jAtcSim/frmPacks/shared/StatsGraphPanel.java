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

  private static final int IMG_WIDTH = 1500;
  private static final int IMG_HEIGHT = 900;
  private static IReadOnlyList<Measure> measures;
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
//    lines.add(new MeasureLine("Total", q -> q.getRunwayMovementsPerHour().getTotal()));
    measure = new Measure(
        "Runway movements per hour", lines, new StackedBarRenderer());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Departures in sim", q->q.getPlanesInSim().getDepartures().getMaximum()));
    lines.add(new MeasureLine("Arrivals in sim", q->q.getPlanesInSim().getArrivals().getMaximum()));
    lines.add(new MeasureLine("Total in sim", q->q.getPlanesInSim().getTotal().getMaximum()));
    lines.add(new MeasureLine("Departures under APP", q->q.getPlanesUnderApp().getDepartures().getMaximum()));
    lines.add(new MeasureLine("Arrivals under APP", q->q.getPlanesUnderApp().getArrivals().getMaximum()));
    lines.add(new MeasureLine("Total under APP", q->q.getPlanesUnderApp().getTotal().getMaximum()));

    measure = new Measure(
        "Maximum number of planes", lines, new LineRenderer3D());
    measures.add(measure);

    lines = new EList<>();
    lines.add(new MeasureLine("Departures in sim", q->q.getPlanesInSim().getDepartures().getMean()));
    lines.add(new MeasureLine("Arrivals in sim", q->q.getPlanesInSim().getArrivals().getMean()));
    lines.add(new MeasureLine("Total in sim", q->q.getPlanesInSim().getTotal().getMean()));
    lines.add(new MeasureLine("Departures under APP", q->q.getPlanesUnderApp().getDepartures().getMean()));
    lines.add(new MeasureLine("Arrivals under APP", q->q.getPlanesUnderApp().getArrivals().getMean()));
    lines.add(new MeasureLine("Total under APP", q->q.getPlanesUnderApp().getTotal().getMean()));
    measure = new Measure(
        "Average number of planes", lines, new LineRenderer3D());
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
        dataset.addValue(item.values[i], item.lineTitle, gds.xAxisTitles[i]);
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
