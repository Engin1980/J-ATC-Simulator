package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;

import javax.swing.*;
import java.awt.*;

public class MoodHistoryPanel extends JPanel {

  JPanel pnlTop;
  JPanel pnlBottom;

  public MoodHistoryPanel() {
    initializeComponents();
    layoutComponents();
  }

  private void layoutComponents() {
    JScrollPane scrTop = new JScrollPane(pnlTop);
    JScrollPane scrBottom = new JScrollPane(pnlBottom);
    JSplitPane pnlSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrTop, scrBottom);
    pnlSplit.setOneTouchExpandable(true);
    pnlSplit.setDividerLocation(250);

    Dimension minSize = new Dimension(300,100);
    scrTop.setMinimumSize(minSize);
    scrBottom.setMinimumSize(minSize);

    this.getRootPane().add(pnlSplit);
  }

  private void initializeComponents() {
    pnlTop = new JPanel();
    pnlBottom = new JPanel();
  }

  public void init(IReadOnlyList<MoodResult> results){

  }

}
