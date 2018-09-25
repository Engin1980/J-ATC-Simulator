package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.moods.MoodExperienceResult;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class MoodHistoryPanel extends JPanel {
  private static Dimension lblDimension = new Dimension(150, 1);

  private IList<MoodResult> dataSet;

  private class PlaneRow extends JPanel {
    public PlaneRow(ETime time, Callsign callsign, int points) {

      JLabel lblTime = new JLabel(time.toString());
      lblTime.setMinimumSize(lblDimension);
      JLabel lblCallsign = new JLabel(callsign.toString());
      lblCallsign.setMinimumSize(lblDimension);
      JLabel lblPoints = new JLabel((points < 0) ? Integer.toString(points) : ("+" + points));

      LayoutManager.fillFlowPanel(this, LayoutManager.eVerticalAlign.bottom, 4,
          lblTime, lblCallsign, lblPoints );
    }
  }

  public class ResultRow extends JPanel {
    public ResultRow(MoodExperienceResult experienceResult) {
      JLabel lblTime = new JLabel(
          experienceResult.getTime() == null ? "---" : experienceResult.getTime().toString());
      JLabel lblPoints = new JLabel(
          experienceResult.getPoints() < 0 ? Integer.toString(experienceResult.getPoints()) : "+" + experienceResult.getPoints());
      JLabel lblDescription = new JLabel(experienceResult.getDescription());

      LayoutManager.fillFlowPanel(this, LayoutManager.eVerticalAlign.bottom, 4,
          lblTime, lblPoints, lblDescription);
    }
  }

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

    Dimension minSize = new Dimension(300, 100);
    scrTop.setMinimumSize(minSize);
    scrBottom.setMinimumSize(minSize);

    this.add(pnlSplit);
  }

  private void initializeComponents() {
    pnlTop = new JPanel();
    pnlBottom = new JPanel();
  }

  public void init(IReadOnlyList<MoodResult> results) {
    pnlTop.removeAll();
    this.dataSet = results.orderBy(q -> q.getTime());
    this.dataSet.reverse();
    int globalIndex = 0;
    for (MoodResult result : results) {
      final int index = globalIndex;
      PlaneRow pnlPlane = new PlaneRow(result.getTime(), result.getCallsing(), result.getPoints());
      pnlTop.add(pnlPlane);
      ComponentUtils.adjustComponentTree(pnlPlane, q -> this.row_clicked(index));
      globalIndex++;
    }
  }

  private void init2(IReadOnlyList<MoodExperienceResult> exps) {
    pnlBottom.removeAll();
    IList<MoodExperienceResult> tmp = exps.orderBy(q -> q.getTime() == null ? new ETime(0) : q.getTime());
    for (MoodExperienceResult result : tmp) {
      ResultRow pnlResult = new ResultRow(result);
      pnlBottom.add(pnlResult);
    }
  }

  private void row_clicked(int index) {
    MoodResult mr = dataSet.get(index);

    init2(mr.getExperiences());
  }

}

