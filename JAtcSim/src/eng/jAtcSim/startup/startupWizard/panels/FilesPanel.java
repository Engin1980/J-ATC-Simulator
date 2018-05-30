package eng.jAtcSim.startup.startupWizard.panels;

import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.MessageBox;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.SwingFactory;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class FilesPanel extends JStartupPanel {

  private final XmlFileSelectorExtender fleArea;
  private final XmlFileSelectorExtender fleFleet;
  private final XmlFileSelectorExtender fleTypes;

  public FilesPanel() {
    fleArea = new XmlFileSelectorExtender(SwingFactory.FileDialogType.area);
    fleFleet = new XmlFileSelectorExtender(SwingFactory.FileDialogType.fleets);
    fleTypes = new XmlFileSelectorExtender(SwingFactory.FileDialogType.types);

    this.setBorder(new TitledBorder("Source XML files:"));
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(
        LayoutManager.createFormPanel(3, 3,
            new JLabel("area XML file:"),
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
    this.add(btn);
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    this.fleArea.setFileName(settings.files.areaXmlFile);
    this.fleTypes.setFileName(settings.files.planesXmlFile);
    this.fleFleet.setFileName(settings.files.fleetsXmlFile);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.files.areaXmlFile = this.fleArea.getFileName();
    settings.files.planesXmlFile = this.fleTypes.getFileName();
    settings.files.fleetsXmlFile = this.fleFleet.getFileName();
  }

  private void btnLoadXml_click() {
    if (!loadArea()) return;
    if (!loadFleet()) return;
    if (!loadTypes()) return;
  }


  private boolean loadArea() {
    boolean ret;
    String fileName = fleArea.getFileName();
    Area area;
    try {
      area = XmlLoadHelper.loadNewArea(fileName);
      ret = true;
      Sources.setArea(area);
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
      Sources.setFleets(
          XmlLoadHelper.loadFleets(fileName));
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
      Sources.setTypes(
          XmlLoadHelper.loadPlaneTypes(fileName));
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load plane types file " + fileName + ".\n\nReason:\n" + ExceptionUtil.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }
}
