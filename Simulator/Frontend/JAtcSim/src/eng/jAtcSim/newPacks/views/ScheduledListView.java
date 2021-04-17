package eng.jAtcSim.newPacks.views;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.simulation.IScheduledMovement;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;
import eng.jAtcSim.newPacks.IView;
import eng.jAtcSim.newPacks.context.ViewGlobalEventContext;
import eng.jAtcSim.newPacks.utils.ViewGameInfo;
import eng.jAtcSim.settings.FlightStripSettings;

import javax.swing.*;
import java.awt.*;

public class ScheduledListView implements IView {

  private JPanel pnlContent;
  private JScrollPane pnlScroll;
  private ISimulation sim;
  private Callsign firstItemCallsign = null;
  private Callsign lastItemCallsign = null;
  private Integer itemCount = null;
  private JPanel parent;

  @Override
  public void init(JPanel panel, ViewGameInfo initInfo, IReadOnlyMap<String, String> options, ViewGlobalEventContext context) {
    this.parent = panel;
    this.sim = initInfo.getSimulation();
    ScheduledFlightStripPanel.setStripSettings(initInfo.getSettings().getFlightStripSettings());

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("ScheduledFlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.parent.setLayout(new BorderLayout());
    this.parent.add(pnlScroll);
    this.parent.setDoubleBuffered(true);

    pnlContent.setBackground(new Color(50, 50, 50));

    this.sim.registerOnSecondElapsed(s -> updateList());
  }

  private void updateList() {
    if (refreshNeeded()) {
      IReadOnlyList<IScheduledMovement> movements =
              this.sim.getScheduledMovements().orderBy(q -> q.getScheduledTimeWithDelay().getValue());

      pnlContent.removeAll();
      ScheduledFlightStripPanel.resetIndex();
      for (IScheduledMovement mvm : movements) {
        JPanel pnlItem = createMovementStrip(mvm);
        pnlItem.setName("MovementStrip_" + mvm.getCallsign().toString());
        pnlContent.add(pnlItem);
      }

      this.itemCount = this.sim.getScheduledMovements().size();
      if (itemCount == 0) {
        this.firstItemCallsign = null;
        this.lastItemCallsign = null;
      } else {
        this.firstItemCallsign = this.sim.getScheduledMovements().getFirst().getCallsign();
        this.lastItemCallsign = this.sim.getScheduledMovements().getLast().getCallsign();
      }

      this.parent.revalidate();
      this.parent.repaint();
    }
  }

  private boolean refreshNeeded() {
    IReadOnlyList<IScheduledMovement> mvm = this.sim.getScheduledMovements();
    if (itemCount == null || itemCount != mvm.size())
      return true;
    if (itemCount > 0) {
      if (this.firstItemCallsign == null || this.firstItemCallsign.equals(mvm.getFirst().getCallsign()) == false)
        return true;
      return this.lastItemCallsign == null || this.lastItemCallsign.equals(mvm.getLast().getCallsign()) == false;
    }
    return false;
  }

  private JPanel createMovementStrip(IScheduledMovement mvm) {
    JPanel ret = new ScheduledFlightStripPanel(mvm);
    return ret;
  }

}

class ScheduledFlightStripPanel extends JPanel {
  private static final Dimension CALLSIGN_DIMENSION = new Dimension(75, 15);
  private static final Dimension FLAG_DIMENSION = new Dimension(25, 15);
  private static final Dimension TIME_DIMENSION = new Dimension(75, 15);
  private static final Dimension DELAY_DIMENSION = FLAG_DIMENSION;
  private static FlightStripSettings stripSettings;
  private static int index = 0;
  private static Font normalFont;
  private static Font boldFont;

  public static void setStripSettings(FlightStripSettings stripSettings) {
    ScheduledFlightStripPanel.stripSettings = stripSettings;

    normalFont = new Font(stripSettings.font.getName(), 0, stripSettings.font.getSize());
    boldFont = new Font(stripSettings.font.getName(), Font.BOLD, stripSettings.font.getSize());
  }

  public static void resetIndex() {
    index = 0;
  }

  private static Color getColor(IScheduledMovement mvm) {
    Color ret;
    // pozadi
    boolean isEven = index++ % 2 == 0;
    if (mvm.getDirection() == DepartureArrival.departure) {
      ret = isEven ? stripSettings.twr.even : stripSettings.twr.odd;
    } else {
      ret = isEven ? stripSettings.ctr.even : stripSettings.ctr.odd;
    }
    return ret;
  }

  public ScheduledFlightStripPanel(IScheduledMovement mvm) {

    this.setLayout(new BorderLayout());

    LayoutManager.setFixedSize(this, stripSettings.scheduledFlightStripSize);

    Color color = ScheduledFlightStripPanel.getColor(mvm);
    this.setBackground(color);
    this.setForeground(stripSettings.textColor);

    fillContent(mvm, stripSettings.textColor, color);
  }

  private void fillContent(IScheduledMovement movement, Color frColor, Color bgColor) {
    JLabel lblCallsign = new JLabel(movement.getCallsign().toString());
    setLabelFixedSize(lblCallsign, CALLSIGN_DIMENSION);

    JLabel lblDepartureArrival = new JLabel(movement.getDirection() == DepartureArrival.departure ? "DEP" : "ARR");
    setLabelFixedSize(lblDepartureArrival, FLAG_DIMENSION);

    JLabel lblTypeName = new JLabel(movement.getAirplaneType().name);
    setLabelFixedSize(lblTypeName, CALLSIGN_DIMENSION);

    JLabel lblTime = new JLabel(movement.getScheduledTime().toHourMinuteString());
    setLabelFixedSize(lblTime, TIME_DIMENSION);
    JLabel lblDelay = new JLabel("+" + movement.getDelayInMinutes());
    setLabelFixedSize(lblDelay, DELAY_DIMENSION);

    JComponent[] cmps = new JComponent[]{lblCallsign, lblDepartureArrival, lblTypeName, lblTime, lblDelay, null};

    JPanel pnl = LayoutManager.createGridPanel(2, 3, 0, cmps);
    adjustComponentStyle(bgColor, frColor, normalFont, cmps);
    adjustComponentStyle(bgColor, frColor, normalFont, pnl);
    pnl = LayoutManager.createBorderedPanel(stripSettings.stripBorder, pnl);
    adjustComponentStyle(bgColor, frColor, normalFont, pnl);

    lblCallsign.setFont(boldFont);
    this.add(pnl);
  }

  private void setLabelFixedSize(JLabel lbl, Dimension dimension) {
    lbl.setLayout(new BorderLayout());
    lbl.setMinimumSize(dimension);
    lbl.setMaximumSize(dimension);
  }

  private void adjustComponentStyle(Color bgColor, Color frColor, Font font, JComponent... components) {
    for (JComponent component : components) {
      if (component == null) continue;
      component.setForeground(frColor);
      component.setBackground(bgColor);
      component.setFont(font);
    }
  }
}
