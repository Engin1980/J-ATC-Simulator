package jatcsim.frmPacks.simple;


import jatcsimdraw.mainRadar.BasicRadar;
import jatcsimdraw.mainRadar.canvases.EJComponent;
import jatcsimdraw.mainRadar.canvases.EJComponentCanvas;
import jatcsimlib.events.EventListener;

import javax.swing.*;
import java.awt.*;

public class FrmView extends JFrame {

  private static Dimension initDimension = new Dimension(500, 300);
  private Pack parent;
  private int refreshRate;
  private int refreshRateCounter;
  private EJComponent radarComponent;
  private JPanel pnlContent;

  public FrmView() {
    initComponents();
  }

  private void initComponents() {
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    ;

    this.pnlContent = new JPanel();
    this.getContentPane().add(this.pnlContent);

    layout = new BorderLayout();
    this.pnlContent.setLayout(layout);

    this.setPreferredSize(initDimension);

    pack();
  }

  void elapseSecond() {
    this.refreshRateCounter++;
    if (this.refreshRateCounter >= this.refreshRate) {
      this.refreshRateCounter = 0;
      radarComponent.repaint();
    }
  }

  void init(Pack pack) {
    this.parent = pack;

    this.refreshRateCounter = 0;

    // generování hlavního radaru
    EJComponentCanvas canvas = new EJComponentCanvas();
    BasicRadar r = new BasicRadar(canvas,
        this.parent.getSim().getActiveAirport().getRadarRange(),
        this.parent.getSim(),
        this.parent.getArea(),
        this.parent.getDisplaySettings());
    this.radarComponent = canvas.getEJComponent();

    // otevření hlavního formuláře
    this.pnlContent.add(this.radarComponent);
    this.setVisible(true);

    FrmView me = this;

    EventListener el = new EventListener<Pack, Object>() {
      @Override
      public void raise(Pack parent, Object e) {
        me.elapseSecond();
      }
    };
    parent.getElapseSecondEvent().addListener(el);
  }
}
