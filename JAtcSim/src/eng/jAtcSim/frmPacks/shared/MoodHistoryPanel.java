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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MoodHistoryPanel extends JPanel {
  private class PlaneRow extends JPanel {
    public PlaneRow(ETime time, Callsign callsign, int points) {

      JLabel lblTime = new JLabel(time.toString());
      LayoutManager.setFixedWidth(lblTime, labelWidth);

      JLabel lblCallsign = new JLabel(callsign.toString());
      LayoutManager.setFixedWidth(lblCallsign, labelWidth);

      JLabel lblPoints = new JLabel((points < 0) ? Integer.toString(points) : ("+" + points));

      JPanel pnlContent = LayoutManager.createFlowPanel(lblTime, lblCallsign, lblPoints);
      pnlContent = LayoutManager.createBorderedPanel(4, pnlContent);
      LayoutManager.fillBorderedPanel(this, null, null, pnlContent, null, null);
      LayoutManager.setFixedHeight(this);
    }
  }

  public class ResultRow extends JPanel {
    public ResultRow(MoodExperienceResult experienceResult) {
      JLabel lblTime = new JLabel(
          experienceResult.getTime() == null ? "" : experienceResult.getTime().toString());
      LayoutManager.setFixedWidth(lblTime, labelWidth);

      JLabel lblPoints = new JLabel(
          experienceResult.getPoints() < 0 ? Integer.toString(experienceResult.getPoints()) : "+" + experienceResult.getPoints());
      LayoutManager.setFixedWidth(lblPoints, labelWidth);

      JLabel lblDescription = new JLabel(experienceResult.getDescription());

      JPanel pnlContent = LayoutManager.createFlowPanel(lblTime, lblPoints, lblDescription);
      pnlContent = LayoutManager.createBorderedPanel(4, pnlContent);
      LayoutManager.fillBorderedPanel(this, null, null, pnlContent, null, null);
      LayoutManager.setFixedHeight(this);
    }
  }

  private static final Color selectedBackgroundColor = Color.white;
  private static int labelWidth = 150;
  private static Dimension panelDimension = new Dimension(500, 300);
  private static Dimension formDimension = new Dimension(500, 500);
  private JPanel pnlTop;
  private JPanel pnlBottom;
  private IList<MoodResult> dataSet;
  private Integer selectedIndex = null;
  private Color defaultBackgroundColor = null;

  public MoodHistoryPanel() {
    initializeComponents();
    layoutComponents();
  }

  public void init(IReadOnlyList<MoodResult> results) {
    pnlTop.removeAll();
    this.dataSet = results.orderBy(q -> q.getTime());
    this.dataSet.reverse();
    int globalIndex = 0;
    for (MoodResult result : this.dataSet) {
      final int index = globalIndex;
      PlaneRow pnlPlane = new PlaneRow(result.getTime(), result.getCallsing(), result.getPoints());
      pnlTop.add(pnlPlane);
      pnlPlane.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          rowSelectionChanged(index);
        }
      });
      globalIndex++;
    }
  }

  private void layoutComponents() {
    JScrollPane scrTop = new JScrollPane(pnlTop);
    JScrollPane scrBottom = new JScrollPane(pnlBottom);
    JSplitPane pnlSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrTop, scrBottom);
    pnlSplit.setOneTouchExpandable(true);
    pnlSplit.setDividerLocation(250);

    scrTop.setMinimumSize(panelDimension);
    scrBottom.setMinimumSize(panelDimension);

    this.setLayout(new BorderLayout());
    this.add(pnlSplit, BorderLayout.CENTER);

    this.setPreferredSize(formDimension);
  }

  private void initializeComponents() {
    pnlTop = LayoutManager.createBoxPanel();
    pnlBottom = LayoutManager.createBoxPanel();
  }

  private void rowSelectionChanged(int index) {
    higlightRow(index);
    MoodResult mr = dataSet.get(index);
    init2(mr.getExperiences());
  }

  private void higlightRow(int index) {
    if (selectedIndex != null) {
      Component prev = pnlTop.getComponent(selectedIndex);
      ComponentUtils.adjustComponentTree(prev, q -> q.setBackground(defaultBackgroundColor));
    }
    selectedIndex = index;
    Component prev = pnlTop.getComponent(selectedIndex);
    defaultBackgroundColor = prev.getBackground();
    ComponentUtils.adjustComponentTree(prev, q -> q.setBackground(selectedBackgroundColor));
  }

  private void init2(IReadOnlyList<MoodExperienceResult> exps) {
    pnlBottom.removeAll();
    IList<MoodExperienceResult> tmp = exps.orderBy(q -> q.getTime() == null ? new ETime(0) : q.getTime());
    for (MoodExperienceResult result : tmp) {
      ResultRow pnlResult = new ResultRow(result);
      pnlBottom.add(pnlResult);
    }
    pnlBottom.revalidate();
    pnlBottom.repaint();
  }

}

