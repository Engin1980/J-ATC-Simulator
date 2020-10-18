/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.frmPacks.mdi;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eXmlSerialization.XmlSerializer;
import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
import eng.jAtcSim.xmlLoading.XmlSerialization;
import eng.jAtcSim.xmlLoading.XmlSerializationFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FrmMain extends javax.swing.JFrame {

  private Pack parent;
  private JPanel pnlContent;
  private SwingRadarPanel pnlRadar;
  private JPanel pnlTop;

  public FrmMain() {
    initComponents();
  }

  private void formFocusGained(java.awt.event.FocusEvent evt) {
    this.pnlRadar.requestFocus();
  }

  void init(Pack pack) {

    this.parent = pack;

//    IPlaneFormatter<String> fmtPlane = (IPlaneFormatter<String>) pack.getSim().getParseFormat().getPlaneFormatter();
//    ISystemFormatter<String> fmtSystem = (ISystemFormatter<String>) pack.getSim().getParseFormat().getSystemFormatter();
//    IAtcFormatter<String> fmtAtc = (IAtcFormatter<String>) pack.getSim().getParseFormat().getAtcFormatter();
    RadarBehaviorSettings behSett = new RadarBehaviorSettings(true); //, fmtPlane, fmtAtc, fmtSystem);
    RadarDisplaySettings dispSett = pack.getAppSettings().radar.displaySettings.toRadarDisplaySettings();

    this.pnlRadar = new SwingRadarPanel();
    this.pnlRadar.init(this.parent.getSim().getAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(), this.parent.getSim().getUserAtcIds().getFirst(),
        this.parent.getDisplaySettings(), dispSett, behSett, this.parent.getDynamicPlaneFormatter());

    this.pnlContent.add(this.pnlRadar);

  }

  private void initComponents() {

    // top panel
    pnlTop = new JPanel();
    pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));

    // content (radar) panel
    pnlContent = new JPanel();
    pnlContent.setLayout(new BorderLayout());
    pnlContent.setBackground(Color.white);
    Dimension prefferedSize = new Dimension(1032, 607);
    pnlContent.setPreferredSize(prefferedSize);

    // content pane
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    this.getContentPane().add(pnlTop, BorderLayout.PAGE_START);
    this.getContentPane().add(pnlContent, BorderLayout.CENTER);


    this.pack();

    addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        formFocusGained(evt);
      }
    });
  }

}
