package jatcsim.frmPacks.simple;



import javax.swing.*;
import java.awt.*;

public class FrmView extends JFrame {

  private static Dimension initDimension = new Dimension(500, 300);
  private Pack parent;
  private int refreshRate = 3;
  private int refreshRateCounter;
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
      //radarComponent.repaint();
    }
  }

  void init(Pack pack) {
    throw new UnsupportedOperationException("TODO");
//    this.parent = pack;
//
//    this.refreshRateCounter = 0;
//
//    // generování hlavního radaru
//    EJComponentCanvas canvas = new EJComponentCanvas();
//    BasicRadar r = new BasicRadar(canvas,
//        this.parent.getSim().getActiveAirport().getRadarRange(),
//        this.parent.getSim(),
//        this.parent.getArea(),
//        this.parent.getDisplaySettings(), false);
//    this.radarComponent = canvas.getEJComponent();
//
//    // otevření hlavního formuláře
//    this.pnlContent.add(this.radarComponent);
//    this.setVisible(true);
//
//    FrmView me = this;
//
//    EventListener el = new EventListener<Pack, Object>() {
//      @Override
//      public void raise(Pack parent, Object e) {
//        me.elapseSecond();
//      }
//    };
//    parent.getElapseSecondEvent().addListener(el);
  }
}
