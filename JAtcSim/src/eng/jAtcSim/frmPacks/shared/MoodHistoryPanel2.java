package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.swing.extenders.ListBoxExtender;
import eng.jAtcSim.lib.airplanes.moods.MoodExperienceResult;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.global.ETime;

import javax.swing.*;
import java.awt.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class MoodHistoryPanel2 extends JPanel {
  private static Dimension dimension = new Dimension(150, 1);
  private ListBoxExtender<MoodResult> lstMoods;
  private ListBoxExtender<MoodExperienceResult> lstResults;

  public MoodHistoryPanel2() {
    initializeComponents();
    layoutComponents();
  }

  private void layoutComponents() {
    JScrollPane scrTop = new JScrollPane(lstMoods.getControl());
    JScrollPane scrBottom = new JScrollPane(lstResults.getControl());
    JSplitPane pnlSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrTop, scrBottom);
    pnlSplit.setOneTouchExpandable(true);
    pnlSplit.setDividerLocation(250);

    Dimension minSize = new Dimension(300, 100);
    scrTop.setMinimumSize(minSize);
    scrBottom.setMinimumSize(minSize);

    this.add(pnlSplit);
  }

  private void initializeComponents() {
    lstMoods = new ListBoxExtender<>();
    lstResults = new ListBoxExtender<>();

    lstMoods.getControl().setMinimumSize(dimension);
    lstResults.getControl().setMinimumSize(dimension);
  }

  public void init(IReadOnlyList<MoodResult> results) {
    IList<MoodResult> dataSet = results.orderBy(q -> q.getTime());
    dataSet.reverse();

    lstMoods.addItems(dataSet, q -> sf("%s - %s : %+d",
          q.getTime().toString(),
          q.getCallsing().toString(),
          q.getPoints()));

    lstMoods.getOnSelectionChanged().add(this::lstMoods_onSelectionChanged);
  }

  private void lstMoods_onSelectionChanged(ListBoxExtender<MoodResult> listBoxExtender) {
    MoodResult result = listBoxExtender.getSelectedItems().getFirst();
    init2(result.getExperiences());
  }

  private void init2(IReadOnlyList<MoodExperienceResult> exps) {
    IList<MoodExperienceResult> tmp = exps.orderBy(q -> q.getTime() == null ? new ETime(0) : q.getTime());

    lstResults.addItems(tmp, q ->
        sf("%s : %+d : %s",
            q.getTime() == null ? "---" : q.getTime().toString(),
            q.getPoints(),
            q.getDescription()));
  }

}

