package eng.jAtcSim.startup;

import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.startup.extenders.XmlFileSelectorExtender;

import javax.swing.*;

public class FrmWizardAreaAndPlaneTypes extends FrmWizardFrame {

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
  public FrmWizardAreaAndPlaneTypes() {
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
    this.setTitle("");
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();

    pack();

    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
  }

  private void createLayout() {
    JPanel pnl = LayoutManager.createFormPanel(3, 3,
        jLabel1, txtAreaXml, btnAreaXml,
        jLabel3, txtFleetsXml, btnFleetsXml,
        jLabel2, txtTypesXml, btnTypesXml);

    pnl = super.wrapWithContinueButton(pnl, btnContinue);

    this.getContentPane().add(pnl);
  }

  private void createComponents() {
    btnAreaXml = new JButton("(browse)");
    btnAreaXml.setMinimumSize(BUTTON_DIMENSION);

    btnContinue = new JButton("Continue");
    btnContinue.addActionListener(this::btnContinueActionPerformed);
    btnContinue.setMinimumSize(BUTTON_DIMENSION);

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

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {
    super.closeDialogIfValid();
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
  protected void fillBySettings() {
    txtAreaXml.setText(settings.files.areaXmlFile);
    txtTypesXml.setText(settings.files.planesXmlFile);
  }

  @Override
  protected boolean isValidated() {
    if (checkAreaSanity() == false) {
      return false;
    }
    if (checkTypesSanity() == false) {
      return false;
    }

    if (checkFleetsSanity() == false) {
      return false;
    }

    this.settings.files.areaXmlFile = txtAreaXml.getText();
    this.settings.files.planesXmlFile = txtTypesXml.getText();
    this.settings.files.fleetsXmlFile = txtFleetsXml.getText();

    return true;
  }

}