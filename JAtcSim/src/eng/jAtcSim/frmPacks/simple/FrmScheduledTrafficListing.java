package eng.jAtcSim.frmPacks.simple;

import eng.jAtcSim.startup.LayoutManager;
import jatcsimlib.traffic.Movement;

import javax.swing.*;
import java.awt.*;

public class FrmScheduledTrafficListing extends JFrame {

  private static final Dimension CALLSIGN_DIMENSION = new Dimension(100, 1);
  private static final Dimension FLAG_DIMENSION = new Dimension(30, 1);
  private static final Dimension TIME_DIMENSION = new Dimension(50, 1);
  private static final Dimension DELAY_DIMENSION = new Dimension(30, 1);
  private static final Dimension FRAME_DIMENSION = new Dimension(250, 700);
  private static final Dimension ROW_PREFFERED_DIMENSION = new Dimension(250, 1);

  public FrmScheduledTrafficListing() {
    this.setPreferredSize(FRAME_DIMENSION);
    initComponents(new Movement[0]);
  }

  public void refresh(Movement[] movements){
    initComponents(movements);
  }

  private void initComponents(Movement[] movements) {
    JPanel[] movementPanels = new JPanel[movements.length];

    for (int i = 0; i < movements.length; i++) {
      Movement movement = movements[i];
      JLabel lblCallsign = new JLabel(movement.getCallsign().toString());
      lblCallsign.setPreferredSize(CALLSIGN_DIMENSION);
      JLabel lblDepartureArrival = new JLabel(movement.isDeparture() ? "DEP" : "ARR");
      lblDepartureArrival.setPreferredSize(FLAG_DIMENSION);
      JLabel lblIfrVfr = new JLabel(movement.isIfr() ? "IFR" : "VFR");
      lblIfrVfr.setPreferredSize(FLAG_DIMENSION);
      JLabel lblTime = new JLabel(movement.getInitTime().toTimeString());
      lblTime.setPreferredSize(TIME_DIMENSION);
      JLabel lblDelay = new JLabel(Integer.toString(movement.getDelayInMinutes()));
      lblDelay.setPreferredSize(DELAY_DIMENSION);

      JPanel pnl = eng.jAtcSim.startup.LayoutManager.createFlowPanel(eng.jAtcSim.startup.LayoutManager.eVerticalAlign.baseline,  10,
          lblCallsign,  lblDepartureArrival,  lblIfrVfr,  lblTime,  lblDelay);
      pnl.setPreferredSize(ROW_PREFFERED_DIMENSION);
      movementPanels[i] = pnl;
    }

    JPanel pnlMain = eng.jAtcSim.startup.LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center,  0, movementPanels);

    this.setContentPane(pnlMain);
    pack();
  }

}
