package eng.jAtcSim.startup.startupWizard;

import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.MessageBox;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;

public class PnlWizardAreaAndPlaneTypes extends JWizardPanel {

  private javax.swing.JButton btnAreaXml;
  private javax.swing.JButton btnContinue;
  private javax.swing.JButton btnTypesXml;
  private javax.swing.JButton btnFleetsXml;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JTextField txtAreaXml;
  private javax.swing.JTextField txtTypesXml;
  private javax.swing.JTextField txtFleetsXml;
  private XmlFileSelectorExtender fsAreaFile;
  private XmlFileSelectorExtender fsTypesFile;
  private XmlFileSelectorExtender fsFleetsFile;

  public PnlWizardAreaAndPlaneTypes() {
    super();
    initComponents();
    initExtenders();
  }

  private void initExtenders() {
    fsAreaFile = new XmlFileSelectorExtender(txtAreaXml, btnAreaXml);
    fsTypesFile = new XmlFileSelectorExtender(txtTypesXml, btnTypesXml);
    fsFleetsFile = new XmlFileSelectorExtender(txtFleetsXml, btnFleetsXml);
  }

  private void initComponents() {
    createComponents();
    createLayout();
  }

  private void createLayout() {
    JPanel pnl = LayoutManager.createFormPanel(3, 3,
        jLabel1, txtAreaXml, btnAreaXml,
        jLabel3, txtFleetsXml, btnFleetsXml,
        jLabel2, txtTypesXml, btnTypesXml);

    this.add(pnl);
  }

  private void createComponents() {
    btnAreaXml = new JButton("(browse)");
    btnAreaXml.setMinimumSize(BUTTON_DIMENSION);

    btnTypesXml = new JButton("(browse)");
    btnTypesXml.setMinimumSize(BUTTON_DIMENSION);

    btnFleetsXml = new JButton("(browse)");
    btnFleetsXml.setMinimumSize(BUTTON_DIMENSION);

    jLabel1 = new JLabel("Area XML file:");
    jLabel2 = new JLabel("Plane type XML file:");
    jLabel3 = new JLabel("Company fleets file:");

    txtAreaXml = new JTextField();
    txtAreaXml.setMinimumSize(FILE_FIELD_DIMENSION);

    txtTypesXml = new JTextField();
    txtTypesXml.setMinimumSize(FILE_FIELD_DIMENSION);

    txtFleetsXml = new JTextField();
    txtFleetsXml.setMinimumSize(FILE_FIELD_DIMENSION);
  }

  private boolean checkAreaSanity() {
    boolean ret;
    String fileName = txtAreaXml.getText();

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

  private boolean checkFleetsSanity() {
    boolean ret;
    String fileName = txtFleetsXml.getText();

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

  private boolean checkTypesSanity() {
    boolean ret;
    String fileName = txtTypesXml.getText();

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

  @Override
  protected void fillBySettings(StartupSettings settings) {
    txtAreaXml.setText(settings.files.areaXmlFile);
    txtTypesXml.setText(settings.files.planesXmlFile);
    txtFleetsXml.setText(settings.files.fleetsXmlFile);
  }

  @Override
  protected boolean doWizardValidation() {
    if (checkAreaSanity() == false) {
      return false;
    }
    if (checkTypesSanity() == false) {
      return false;
    }

    if (checkFleetsSanity() == false) {
      return false;
    }
    return true;
  }

  @Override
  void fillSettingsBy(StartupSettings settings) {

    settings.files.areaXmlFile = txtAreaXml.getText();
    settings.files.planesXmlFile = txtTypesXml.getText();
    settings.files.fleetsXmlFile = txtFleetsXml.getText();

  }

}