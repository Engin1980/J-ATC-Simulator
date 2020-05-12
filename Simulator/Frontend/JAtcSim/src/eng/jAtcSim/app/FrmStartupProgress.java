package eng.jAtcSim.app;

import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FrmStartupProgress extends JFrame {
  private final int listenerId;
  private final JProgressBar prg;
  private final JLabel lbl;

  public FrmStartupProgress(int expectedProgressCount) {

    JAtcSim.setIconToFrame(this, "logIcon.png");

    listenerId = SharedAcc.getAppLog().getOnNewMessage().add(q -> appendInfo(q));
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        SharedAcc.getAppLog().getOnNewMessage().remove(listenerId);
      }
    });

    JLabel lblImage = JAtcSim.getAppImage(this);

    prg = new JProgressBar(JProgressBar.HORIZONTAL, 0, expectedProgressCount);
    prg.setValue(0);
    JPanel pnlProgress = LayoutManager.createBorderedPanel(8, prg);

    lbl = new JLabel("Initialization");
    JPanel pnlLabel = LayoutManager.createBorderedPanel(8, lbl);

    JPanel pnl =
        LayoutManager.createBorderedPanel(
            LayoutManager.createBorderedPanel(8, lblImage),
            null, null, null,
            LayoutManager.createGridPanel(2, 1, 0, pnlProgress, pnlLabel)
        );
    pnl.setPreferredSize(new Dimension(400, 225));

    this.setUndecorated(true);
    this.getContentPane().add(pnl);
    this.pack();
    this.setLocationRelativeTo(null);
    Stylist.apply(this, true);
  }

  private void appendInfo(ApplicationLog.Message q) {
    if (q.type == ApplicationLog.eType.info) {
      int val = prg.getValue() + 1;
      if (val < prg.getMaximum())
        prg.setValue(val);
      lbl.setText(q.text);

      JPanel pnl = (JPanel) this.getContentPane();
      pnl.paintImmediately(pnl.getVisibleRect());

      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
