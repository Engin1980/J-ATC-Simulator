package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;

import static eng.jAtcSim.lib.stats.Statistics.toTime;

public class StatsPanel extends JPanel {

  private static Color bgColor = new Color(50, 50, 50);
  private static Color frColor = new Color(200, 200, 200);
  Statistics stats;
  JLabel lblElapsed = new JLabel("Seconds elapsed:");
  JLabel lvlElapsed = new JLabel("-");
  JLabel lblBusyDuration = new JLabel("Recalc duration:");
  JLabel lvlBusyDuration = new JLabel("-");
  JLabel lblFinished = new JLabel("Served planes:");
  JLabel lvlFinished = new JLabel("-");
  JLabel lblMaxInGame = new JLabel("Max in simulation:");
  JLabel lvlMaxInGame = new JLabel("-");
  JLabel lblMovementsGame = new JLabel("Moves/hour:");
  JLabel lvlMovementsGame = new JLabel("-");
  JLabel lblMaxApp = new JLabel("Max under APP:");
  JLabel lvlMaxApp = new JLabel("-");
  JLabel lblCurInGame = new JLabel("Now in simulation:");
  JLabel lvlCurInGame = new JLabel("-");
  JLabel lblCurApp = new JLabel("Now under APP:");
  JLabel lvlCurApp = new JLabel("-");
  JLabel lblHpCount = new JLabel("H-P count:");
  JLabel lvlHpCount = new JLabel("-");
  JLabel lblHpTime = new JLabel("H-P time:");
  JLabel lvlHpTime = new JLabel("-");

  JLabel lblErrors = new JLabel("Errs (a/m):");
  JLabel lvlErrors = new JLabel("-");

  JLabel lblDelay = new JLabel("Delay:");
  JLabel lvlDelay = new JLabel("-");

  public StatsPanel() {
    initComponents();
  }

  public void init(Simulation sim) {
    this.stats = sim.getStats();
    sim.getOnSecondElapsed().add((a) -> update());
  }

  private void initComponents() {
    this.setPreferredSize(
        new Dimension(200, 200)
    );
    this.setBackground(
        new Color(50, 50, 50)
    );


    JLabel lbl = new JLabel("Stats:");
    lbl.setForeground(Color.white);

    JPanel pnlLabels = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        lblElapsed,
        lblBusyDuration,
        lblFinished,
        lblMovementsGame,
        lblMaxInGame,
        lblCurInGame,
        lblMaxApp,
        lblCurApp,
        lblErrors,
        lblDelay,
        lblHpCount,
        lblHpTime
    );

    JPanel pnlValues = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        lvlElapsed,
        lvlBusyDuration,
        lvlFinished,
        lvlMovementsGame,
        lvlMaxInGame,
        lvlCurInGame,
        lvlMaxApp,
        lvlCurApp,
        lvlErrors,
        lvlDelay,
        lvlHpCount,
        lvlHpTime
    );

    JPanel pnlStats = LayoutManager.createBorderedPanel(null, null, pnlLabels, pnlValues, null);

    LayoutManager.fillBorderedPanel(this, 4, pnlStats);

    this.add(pnlStats);

    setDarkTheme(this);
  }

  private void setDarkTheme(Component c) {
    c.setBackground(bgColor);
    c.setForeground(frColor);

    if (c instanceof Container) {
      for (Component i : ((Container) c).getComponents()) {
        setDarkTheme(i);
      }
    }


  }

  private void update() {

    lvlElapsed.setText(toTime(stats.secondsElapsed.get()));
    lvlBusyDuration.setText(String.format("%.3f", stats.durationOfSecondElapse.get()));

    updateFinished();
    updateMaxAll();
    updateMaxApp();
    updateCurAll();
    updateCurApp();
    updateMovementsPerHour();
    updateHoldingPointInfo();
    updateDelays();
    updateAirproxAndMrvas();

  }

  private void updateAirproxAndMrvas() {
    double air = stats.airproxes.get() * 1000;
    double mrv = stats.mrvaErrors.get() * 1000;
    String s = String.format("%.1f‰ / %.1f‰", air, mrv);
    lvlErrors.setText(s);
  }

  private void updateDelays() {
    ETime tmean = new ETime((int) stats.delays.mean.get());
    ETime tmax = new ETime((int) stats.delays.max.get());

    String s = String.format("%s / %s", tmean.toMinuteSecondString(), tmax.toMinuteSecondString());
    lvlDelay.setText(s);
  }

  private void updateHoldingPointInfo() {
    String tmp = String.format("%d / %d",
        stats.holdingPointInfo.currentHoldingPointCount,
        stats.holdingPointInfo.maximumHoldingPointCount.getInt());
    lvlHpCount.setText(tmp);

    tmp = String.format("%s / %s",
        toTime(stats.holdingPointInfo.meanHoldingPointTime.get()),
        toTime(stats.holdingPointInfo.maximumHoldingPointTime.get())
    );
    lvlHpTime.setText(tmp);
  }

  private void updateMovementsPerHour() {
    String s = String.format("%.1f / %.1f / %.1f",
        stats.movementsPerHour.getDepartures(),
        stats.movementsPerHour.getArrivals(),
        stats.movementsPerHour.getTotal()
    );
    lvlMovementsGame.setText(s);
  }

  private void updateCurApp() {
    String s = String.format("%d / %d / %d",
        stats.currentPlanes.appDepartures,
        stats.currentPlanes.appArrivals,
        stats.currentPlanes.appTotal
    );
    lvlCurApp.setText(s);
  }

  private void updateCurAll() {
    String s = String.format("%d / %d / %d",
        stats.currentPlanes.departures,
        stats.currentPlanes.arrivals,
        stats.currentPlanes.total
    );
    lvlCurInGame.setText(s);
  }

  private void updateMaxApp() {
    String s = String.format("%d / %d / %d",
        stats.maximumResponsiblePlanes.departures.getInt(),
        stats.maximumResponsiblePlanes.arrivals.getInt(),
        stats.maximumResponsiblePlanes.total.getInt()
    );
    lvlMaxApp.setText(s);
  }

  private void updateMaxAll() {
    String s = String.format("%d / %d / %d",
        stats.maxumumTotalPlanes.departures.getInt(),
        stats.maxumumTotalPlanes.arrivals.getInt(),
        stats.maxumumTotalPlanes.total.getInt()
    );
    lvlMaxInGame.setText(s);
  }

  private void updateFinished() {
    String s = String.format("%d / %d / %d",
        stats.finishedDepartures.get(),
        stats.finishedArrivals.get(),
        stats.finishedDepartures.get() + stats.finishedArrivals.get()
    );
    lvlFinished.setText(s);
  }
}
