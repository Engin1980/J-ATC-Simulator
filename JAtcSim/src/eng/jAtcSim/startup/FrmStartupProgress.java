package eng.jAtcSim.startup;

import eng.eSystem.swing.extenders.ListBoxExtender;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FrmStartupProgress extends JFrame {
  private final int listenerId;
  private final ListBoxExtender<String> lst;
  private final JProgressBar prg;

  public FrmStartupProgress(int expectedProgressCount) {
    listenerId = Acc.log().getOnNewMessage().add(q -> appendInfo(q));
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        Acc.log().getOnNewMessage().remove(listenerId);
      }
    });

    prg = new JProgressBar(JProgressBar.HORIZONTAL, 0, expectedProgressCount);
    prg.setValue(0);
    JPanel pnlProgress = LayoutManager.createBorderedPanel(32, prg);

    lst = new ListBoxExtender<>();
    JPanel pnlList = LayoutManager.createBorderedPanel(8, lst.getControl());
    pnlList.setPreferredSize(new Dimension(400, 300));

    JPanel pnl = LayoutManager.createBorderedPanel(pnlProgress, null, null, null, pnlList);
    this.getContentPane().add(pnl);
    this.pack();
    this.setLocationRelativeTo(null);
    Stylist.apply(this, true);
  }

  private void appendInfo(ApplicationLog.Message q) {
    if (q.type == ApplicationLog.eType.info) {
      int val = prg.getValue() + 1;
      System.out.println("val: " + val);
      System.out.println("max:" + prg.getMaximum());
      if (val < prg.getMaximum())
        prg.setValue(val);
      lst.addItem(q.text);
      lst.ensureLastVisible();
      prg.paintImmediately(prg.getVisibleRect());
      lst.getControl().paintImmediately(lst.getControl().getVisibleRect());

      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
