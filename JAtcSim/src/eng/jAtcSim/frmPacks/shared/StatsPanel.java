package eng.jAtcSim.frmPacks.shared;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {

  public StatsPanel(){
    initComponents();
  }

  private void initComponents() {
    this.setPreferredSize(
        new Dimension(200, 200)
    );
    this.setBackground(
        new Color(50,50,50)
    );
    JLabel lbl = new JLabel("Stats:");
    lbl.setForeground(Color.white);
    this.add(lbl);
  }
}
