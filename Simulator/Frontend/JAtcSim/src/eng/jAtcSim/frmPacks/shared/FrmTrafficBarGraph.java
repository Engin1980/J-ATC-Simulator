package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.ERandom;
import eng.eSystem.collections.*;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.extenders.ComboBoxExtender;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.app.controls.ImagePanel;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GenericGeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
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

public class FrmTrafficBarGraph extends JFrame {

  private static class DataItem {
    public String domain;
    public String type;
    public double value;

    public DataItem(String domain, double value, String type) {
      this.domain = domain;
      this.value = value;
      this.type = type;
    }
  }

  private static final int IMG_HEIGHT = 500;
  private static final int IMG_WIDTH = 1400;
  private static final int MARGIN = 20;
  private ComboBoxExtender<String> cmbSerie = new ComboBoxExtender<>();
  private IMap<String, IList<DataItem>> ds = new EMap<>();
  private ImagePanel pnlImage = new ImagePanel(IMG_WIDTH, IMG_HEIGHT);
  private String title;

  public FrmTrafficBarGraph() {
    initComponents();
    initLayout();
    this.pack();
  }

  public void init(ITrafficModel traffic, String title) {
    EAssert.Argument.isNotNull(traffic);
    EAssert.Argument.isNotNull(title);

    this.title = title;

    IList<String> keyLst = new EList<>();

    IReadOnlyList<MovementTemplate> scheduledTimes = traffic.generateMovementsForOneDay();

    IList<DataItem> dts = new EList<>();
    DataItem di;
    for (int i = 0; i < 23; i++) {
      String domain = i + ":00 - " + i + ":59";
      int ii = i;
      IReadOnlyList<MovementTemplate> tmp = scheduledTimes.where(q -> q.getAppearanceTime().getHours() == ii);
      int arrivalsCount = tmp.count(q -> q.isArrival());
      int departuresCount = tmp.size() - arrivalsCount;
      di = new DataItem(domain, arrivalsCount, "Arrivals");
      dts.add(di);
      di = new DataItem(domain, departuresCount, "Departures");
      dts.add(di);
    }
    ds.set("Arrivals/Departures", dts);
    keyLst.add("Arrivals/Departures");

    dts = new EList<>();
    for (int i = 0; i < 23; i++) {
      String domain = i + ":00 - " + i + ":59";
      int ii = i;
      IReadOnlyList<MovementTemplate> tmp = scheduledTimes.where(q -> q.getAppearanceTime().getHours() == ii);
      int commercialsCount = tmp.count(q -> q instanceof GenericCommercialMovementTemplate);
      int nonCommercialsCount = tmp.count(q -> q instanceof GenericGeneralAviationMovementTemplate);
      int unknownCount = tmp.count() - commercialsCount - nonCommercialsCount;
      di = new DataItem(domain, commercialsCount, "Commercials");
      dts.add(di);
      di = new DataItem(domain, nonCommercialsCount, "Non-commercials");
      dts.add(di);
      di = new DataItem(domain, unknownCount, "Unknown");
      dts.add(di);
    }
    ds.set("Commercials/non-commercials", dts);
    keyLst.add("Commercials/non-commercials");

    dts = new EList<>();

    Context.getApp().getAppLog().write(ApplicationLog.eType.info,
        "Implementation of type categories overview is not realized.");
//    for (int i = 0; i < 23; i++) {
//      String domain = i + ":00 - " + i + ":59";
//      int ii = i;
//      IReadOnlyList<MovementTemplate> hourMovements = scheduledTimes.where(
//          q -> q.getAppearanceTime().getHours() == ii);
//      IReadOnlyList<MovementTemplate> tmp = scheduledTimes.where(q -> q.getAppearanceTime().getHours() == ii);
//
//      IReadOnlyList<MovementTemplate> tmx = tmp
//          .where(q -> !(q instanceof FlightMovementTemplate));
//
//      int aCount = tmp.count(q -> q.category == 'A');
//      int bCount = tmp.count(q -> q.category == 'B');
//      int cCount = tmp.count(q -> q.category == 'C');
//      int dCount = tmp.count(q -> q.category == 'D');
//      int uCount = tmp.count() - aCount - bCount - cCount - dCount
//      di = new DataItem(domain, aCount, "Type A");
//      dts.add(di);
//      di = new DataItem(domain, bCount, "Type B");
//      dts.add(di);
//      di = new DataItem(domain, cCount, "Type C");
//      dts.add(di);
//      di = new DataItem(domain, dCount, "Type D");
//      dts.add(di);
//    }
    ds.set("Type categories", dts);
    keyLst.add("Type categories");

    String[] keyArr = keyLst.toArray(String.class);
    cmbSerie.addItems(keyArr);
    cmbSerie.setSelectedIndex(0);

    cmbSerie.getOnSelectionChanged().add(q -> this.updateGraph());

    updateGraph();
  }

  private void initComponents() {
    this.setPreferredSize(new Dimension(IMG_WIDTH + MARGIN, IMG_HEIGHT + 4 * MARGIN));
    this.add(pnlImage);
  }

  private void initLayout() {
    JPanel pnlContent =
        LayoutManager.createBoxPanel(pnlImage);
    JPanel pnlBottom = LayoutManager.createFlowPanel(cmbSerie.getControl());

    LayoutManager.fillBorderedPanel(this, null, pnlBottom, null, null, pnlContent);
  }

  private void updateGraph() {
    String key = cmbSerie.getSelectedItem();
    IList<DataItem> ds = this.ds.get(key);

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (DataItem d : ds) {
      dataset.addValue(d.value, d.type, d.domain);
    }

    CategoryAxis domainAxis = new CategoryAxis("Time");
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(
            90d * Math.PI / 180d));
    ValueAxis rangeAxis = new NumberAxis("Movements");
    rangeAxis.setRange(0, 60);
    CategoryItemRenderer renderer = new StackedBarRenderer();

    renderer.setBaseItemLabelGenerator(
        new StandardCategoryItemLabelGenerator());
    for (int i = 0; i < ds.select(q -> q.type).distinct().size(); i++) {
      renderer.setSeriesItemLabelsVisible(i, Boolean.TRUE);
    }

    CategoryPlot plot = new CategoryPlot(
        dataset, domainAxis, rangeAxis, renderer);
    JFreeChart chart = new JFreeChart(key, plot);

    BufferedImage bufferedImage = chart.createBufferedImage(IMG_WIDTH, IMG_HEIGHT);

    pnlImage.setImage(bufferedImage);
    pnlImage.invalidate();
    pnlImage.repaint();
  }
}
