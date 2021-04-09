package eng.jAtcSim.app;

import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.shared.logging.ProgressInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FrmProgress extends JFrame {
  private final JProgressBar prg;
  private final JLabel lbl;

  private final ProgressInfo progressInfo;

  public FrmProgress(ProgressInfo progressInfo) throws HeadlessException {
    this.progressInfo = progressInfo;
    
    this.progressInfo.onInitMaximum.add(this::progressInfo_initMaximum);
    this.progressInfo.onProgress.add(this::progressInfo_progress);
    this.progressInfo.onDone.add(this::progressInfo_done);

    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    JAtcSim.setIconToFrame(this, "logIcon.png");

    JLabel lblImage = JAtcSim.getAppImage(this);

    this.prg = new JProgressBar(JProgressBar.HORIZONTAL, 0, 1);
    this.prg.setValue(0);
    JPanel pnlProgress = LayoutManager.createBorderedPanel(8, prg);

    this.lbl = new JLabel("Initialization");
    JPanel pnlLabel = LayoutManager.createBorderedPanel(8, this.lbl);

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

  private void progressInfo_done() {
    this.setVisible(false);
  }

  private void progressInfo_initMaximum(Integer integer) {
    this.prg.setMaximum(integer);
  }

  private void progressInfo_progress(ProgressInfo.ProgressMessage progressMessage) {
    if (progressMessage.value > prg.getMaximum())
      Context.getApp().getAppLog().write(
              LogItemType.warning, "FrmProgress value runs out of maximum!!!");

    if (progressMessage.value < prg.getMaximum())
      prg.setValue(progressMessage.value);
    lbl.setText(progressMessage.text);

    JPanel pnl = (JPanel) this.getContentPane();
    pnl.paintImmediately(pnl.getVisibleRect());

    try {
      Thread.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
