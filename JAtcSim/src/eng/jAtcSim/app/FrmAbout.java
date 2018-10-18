package eng.jAtcSim.app;

import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.eSystem.swing.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class FrmAbout extends JFrame {

  private String[] lines = {
      "Log window icon made by Designmodo from www.flaticon.com."
  };

  public FrmAbout() {
    JAtcSim.setAppIconToFrame(this);

    this.setTitle("JAtcSimulation - about");

    JPanel pnl = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 8);

    JLabel lblImage = JAtcSim.getAppImage(this);
    pnl.add(LayoutManager.createBorderedPanel(8, lblImage));

    JLabel lblTitle = new JLabel("J-Atc Simulator");
    pnl.add(LayoutManager.createBorderedPanel(8, lblTitle));
    JLabel lblName = new JLabel("Marek Vajgl");
    pnl.add(LayoutManager.createBorderedPanel(8, lblName));
    JTextField txtUrl = new JTextField("https://github.com/Engin1980/J-ATC-Simulator");
    txtUrl.setEditable(false);
    pnl.add(LayoutManager.createBorderedPanel(8, txtUrl));

    JTextArea txtOther = new JTextArea();
    txtOther.setEditable(false);
    txtOther.setPreferredSize(new Dimension(1, 200));
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        txtOther.append("\n");
      }
      String s = lines[i];
      txtOther.append(s);
    }
    JScrollPane pnlOther = new JScrollPane(txtOther);
    pnl.add(LayoutManager.createBorderedPanel(8, pnlOther));

    this.getContentPane().add(pnl);
    this.pack();

    Stylist.apply(this, true);

    lblTitle.setFont(new Font(lblTitle.getFont().getName(), lblTitle.getFont().getStyle(), lblTitle.getFont().getSize() + 4));

  }

}
