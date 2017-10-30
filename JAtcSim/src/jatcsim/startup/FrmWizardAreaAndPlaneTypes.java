package jatcsim.startup;

import jatcsim.startup.extenders.XmlFileSelectorExtender;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.world.Area;
import jatcsimxml.serialization.Serializer;

import javax.swing.*;
import java.awt.*;

public class FrmWizardAreaAndPlaneTypes extends FrmWizardFrame {

  public FrmWizardAreaAndPlaneTypes() {
    super();
    initComponents();
    initExtenders();
  }

  private javax.swing.JButton btnAreaXml;
  private javax.swing.JButton btnContinue;
  private javax.swing.JButton btnTypesXml;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JTextField txtAreaXml;
  private javax.swing.JTextField txtTypesXml;

  private XmlFileSelectorExtender fsAreaFile;
  private XmlFileSelectorExtender fsTypesFile;

  private void initExtenders() {
    fsAreaFile = new XmlFileSelectorExtender(txtAreaXml, btnAreaXml);
    fsTypesFile = new XmlFileSelectorExtender(txtTypesXml, btnTypesXml);
  }

  private void initComponents() {
    this.setTitle("");
    this.setMinimumSize(LARGE_FRAME_FIELD_DIMENSION);

    createComponents();
    createLayout();

    pack();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
  }

  private void createLayout() {
    JPanel pnl = LayoutManager.createFormPanel(2, 3,
        jLabel1, txtAreaXml, btnAreaXml,
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

    jLabel1 = new JLabel("Area XML file:");
    jLabel2 = new JLabel("Plane type XML file:");

    txtAreaXml = new JTextField();
    txtAreaXml.setMinimumSize(FILE_FIELD_DIMENSION);

    txtTypesXml = new JTextField();
    txtTypesXml.setMinimumSize(FILE_FIELD_DIMENSION);
  }

  private void btnContinueActionPerformed(java.awt.event.ActionEvent evt) {
    super.closeDialogIfValid();
  }

  private boolean checkAreaSanity() {
    boolean ret;
    String fileName = txtAreaXml.getText();

    Area area = Area.create();
    Serializer ser = new Serializer();
    try {
      ser.fillObject(fileName, area);
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load area file " + fileName + ". Reason: " + ex.getMessage(), "Error...");
      ret = false;
    }
    return ret;
  }

  private boolean checkTypesSanity() {
    boolean ret;
    String fileName = txtTypesXml.getText();

    AirplaneTypes types = new AirplaneTypes();
    Serializer ser = new Serializer();
    try {
      ser.fillList(fileName, types);
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load plane types file " + fileName + ". Reason: " + ex.getMessage(), "Error...");
      ret = false;
    }
    return ret;
  }

  @Override
  protected void fillBySettings() {
    txtAreaXml.setText(settings.getAreaXmlFile());
    txtTypesXml.setText(settings.getPlanesXmlFile());
  }

  @Override
  protected boolean isValidated() {
    if (checkAreaSanity() == false) {
      return false;
    }
    if (checkTypesSanity() == false) {
      return false;
    }

    this.settings.setAreaXmlFile(txtAreaXml.getText());
    this.settings.setPlanesXmlFile(txtTypesXml.getText());

    return true;
  }

}