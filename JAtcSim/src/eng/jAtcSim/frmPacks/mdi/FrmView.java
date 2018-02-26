package eng.jAtcSim.frmPacks.mdi;



import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;

import javax.swing.*;
import java.awt.*;

public class FrmView extends JFrame {

  private static Dimension initDimension = new Dimension(500, 300);
  private Pack parent;
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

  void init(Pack pack) {

    this.parent = pack;

    BehaviorSettings behSett = new BehaviorSettings(false, new LongFormatter(),10);

    SwingRadarPanel srp = new SwingRadarPanel();
    srp.init(this.parent.getSim().getActiveAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett);

    this.pnlContent.add(srp);
  }

}
