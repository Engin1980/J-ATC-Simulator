package eng.jAtcSim.frmPacks.mdi;

import eng.jAtcSim.AppSettings;
import eng.jAtcSim.frmPacks.shared.ScheduledFlightListPanel;
import eng.jAtcSim.lib.Simulation;

import javax.swing.*;
import java.awt.*;

public class FrmScheduledTrafficListing extends JFrame {

  private ScheduledFlightListPanel pnlFlights;

  private static final Dimension FRAME_DIMENSION = new Dimension(250, 800);

  public FrmScheduledTrafficListing() {
    initComponents();
  }

  public void init(Simulation sim, AppSettings appSettings){
    pnlFlights = new ScheduledFlightListPanel();
    pnlFlights.init(sim, appSettings);

    this.add(pnlFlights);
  }


  private void initComponents() {

    this.setPreferredSize(FRAME_DIMENSION);
    this.setLayout(new BorderLayout());

    pack();
  }

}
