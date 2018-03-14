/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.frmPacks.mdi;

import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;

import javax.swing.*;
import java.awt.*;

/**
 * @author Marek
 */
public class FrmMain extends javax.swing.JFrame {

  private JPanel pnlContent;
  private Pack parent;
  private JPanel pnlTop;
  private SwingRadarPanel pnlRadar;

  public FrmMain() {
    initComponents();
  }

  private void initComponents() {

    // top panel
    pnlTop = new JPanel();
    pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
    //JButton btn = new JButton("test");
    //btn.addActionListener(this::btn_click);
    //pnlTop.add(btn);

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

  private void formFocusGained(java.awt.event.FocusEvent evt) {
    this.pnlRadar.requestFocus();
  }

  void init(Pack pack) {

    this.parent = pack;

    // behavior settings for this radar
    BehaviorSettings behSett = new BehaviorSettings(true, new LongFormatter(), 10);

    this.pnlRadar = new SwingRadarPanel();
    this.pnlRadar.init(this.parent.getSim().getActiveAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett );

    this.pnlContent.add(this.pnlRadar);

  }

}