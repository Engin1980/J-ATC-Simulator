package eng.jAtcSim.startup.startupWizard;

import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.MessageBox;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;
import javafx.scene.layout.HBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

public class FrmTest extends JFrame {

  private JPanel pnlContent;
  private XmlFileSelectorExtender fleArea;
  private XmlFileSelectorExtender fleFleet;
  private XmlFileSelectorExtender fleTypes;

  public FrmTest() throws HeadlessException {
    fleArea = new XmlFileSelectorExtender();
    fleFleet = new XmlFileSelectorExtender();
    fleTypes = new XmlFileSelectorExtender();

    // top
    JPanel pnlTop = createTopPanel();

    // content
    pnlContent = createContentPanel();

    // bottom
    JPanel pnlBottom = createBottomPanel();

    JPanel pnl = LayoutManager.createBorderedPanel(pnlTop, pnlBottom, null, null, pnlContent);

    this.setContentPane(pnl);
    this.pack();

    eng.eSystem.utilites.awt.ComponentUtils.adjustComponentTree(pnlContent, q -> q.setEnabled(false));
  }

  private JPanel createBottomPanel() {
    JPanel ret = new JPanel();

    LayoutManager.fillFlowPanel(ret, LayoutManager.eVerticalAlign.bottom, 4,
        new JButton("Save"),
        new JButton("Load"));

    ret = LayoutManager.createBorderedPanel(4, ret);

    return ret;
  }

  private JPanel createContentPanel() {
    JPanel ret = new JPanel();
    //ret.setPreferredSize(new Dimension(800, 500));

    JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.addTab("Airport & Weather", new PnlWizardAirportTimeAndWeather());
    tabbedPane.addTab("Traffic", new PnlWizardTraffic());
    tabbedPane.addTab("Simulation", new PnlWizardSimulationAndRadar());

    ret.add(tabbedPane);

    return ret;
  }

  private JPanel createTopPanel() {

    JPanel ret = new JPanel();
    ret.setBorder(new TitledBorder("Source XML files:"));
    ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
    ret.add(
        LayoutManager.createFormPanel(3, 3,
            new JLabel("Area XML file:"),
            fleArea.getTextControl(),
            fleArea.getButtonControl(),
            new JLabel("Company fleets XML file:"),
            fleFleet.getTextControl(),
            fleFleet.getButtonControl(),
            new JLabel("Plane types XML file:"),
            fleTypes.getTextControl(),
            fleTypes.getButtonControl()
        ));

    JButton btn = new JButton("Load XML files");
    btn.addActionListener(q -> btnLoadXml_click());
    ret.add(btn);

    return ret;
  }

  private void btnLoadXml_click() {
    eng.eSystem.utilites.awt.ComponentUtils.adjustComponentTree(pnlContent, q -> q.setEnabled(false));
    if (!loadArea()) return;
    if (!loadFleet()) return;
    if (!loadTypes()) return;

    eng.eSystem.utilites.awt.ComponentUtils.adjustComponentTree(pnlContent, q -> q.setEnabled(true));
  }

  private boolean loadArea() {
    boolean ret;
    String fileName = fleArea.getFileName();

    try {
      XmlLoadHelper.loadNewArea(fileName);
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load area file " + fileName + ".\n\nReason:\n" + ExceptionUtil.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }

  private boolean loadFleet() {
    boolean ret;
    String fileName = fleFleet.getFileName();

    try {
      XmlLoadHelper.loadFleets(fileName);
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load company fleets file " + fileName + ".\n\nReason:\n" + ExceptionUtil.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }

  private boolean loadTypes() {
    boolean ret;
    String fileName = fleTypes.getFileName();

    try {
      XmlLoadHelper.loadPlaneTypes(fileName);
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load plane types file " + fileName + ".\n\nReason:\n" + ExceptionUtil.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }
}
