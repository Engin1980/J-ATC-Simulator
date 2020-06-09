package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.stats.IStatsProvider;
import eng.jAtcSim.newLib.stats.recent.RecentStats;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {

  private static final Color bgColor = new Color(50, 50, 50);
  private static final Color frColor = new Color(200, 200, 200);

  private static String toTime(double seconds) {
    String ret;
    int tmp = (int) Math.floor(seconds);
    int hrs = tmp / 3600;
    tmp = tmp % 3600;
    int min = tmp / 60;
    tmp = tmp % 60;
    int sec = tmp;
    if (hrs == 0) {
      ret = String.format("%d:%02d", min, sec);
    } else {
      ret = String.format("%d:%02d:%02d", hrs, min, sec);
    }
    return ret;
  }

  private final JLabel lblBusyDuration = new JLabel("Recalc duration:");
  private final JLabel lblCurApp = new JLabel("Now under APP:");
  private final JLabel lblCurInGame = new JLabel("Now in simulation:");
  private final JLabel lblDelay = new JLabel("Delay:");
  private final JLabel lblElapsed = new JLabel("Seconds elapsed:");
  private final JLabel lblErrors = new JLabel("Errs (a/m):");
  private final JLabel lblFinished = new JLabel("Served planes:");
  private final JLabel lblHpCount = new JLabel("H-P count:");
  private final JLabel lblHpTime = new JLabel("H-P time:");
  private final JLabel lblMaxApp = new JLabel("Max under APP:");
  private final JLabel lblMaxInGame = new JLabel("Max in simulation:");
  private final JLabel lblMovementsGame = new JLabel("Moves/hour:");
  private final JLabel lvlBusyDuration = new JLabel("-");
  private final JLabel lvlCurApp = new JLabel("-");
  private final JLabel lvlCurInGame = new JLabel("-");
  private final JLabel lvlDelay = new JLabel("-");
  private final JLabel lvlElapsed = new JLabel("-");
  private final JLabel lvlErrors = new JLabel("-");
  private final JLabel lvlFinished = new JLabel("-");
  private final JLabel lvlHpCount = new JLabel("-");
  private final JLabel lvlHpTime = new JLabel("-");
  private final JLabel lvlMaxApp = new JLabel("-");
  private final JLabel lvlMaxInGame = new JLabel("-");
  private final JLabel lvlMovementsGame = new JLabel("-");
  private IStatsProvider stats;

  public StatsPanel() {
    initComponents();
  }

  public void init(ISimulation sim) {
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

    RecentStats view = stats.getRecentStats();

    lvlElapsed.setText(toTime(stats.getElapsedSeconds()));
    lvlBusyDuration.setText(String.format("%.3f (%.3f)",
        view.getElapsedSecondDuration().getMean(),
        view.getElapsedSecondDuration().getMaximum()));

    updateFinished(view);
    updateMaxAll(view);
    updateMaxApp(view);
    updateCurAll(view);
    updateCurApp(view);
    updateMovementsPerHour(view);
    updateHoldingPointInfo(view);
    updateDelays(view);
    updateAirproxAndMrvas(view);
  }

  private void updateAirproxAndMrvas(RecentStats view) {
    double air = view.getErrors().getAirproxErrorsPromile();
    double mrv = view.getErrors().getMrvaErrorsPromile();
    String s = String.format("%.1f‰ / %.1f‰", air, mrv);
    lvlErrors.setText(s);
  }

  private void updateCurAll(RecentStats view) {
    int a = view.getCurrentPlanesCount().getArrivals();
    int d = view.getCurrentPlanesCount().getDepartures();
    String s = String.format("%d / %d / %d",
        d, a, a + d);
    lvlCurInGame.setText(s);
  }

  private void updateCurApp(RecentStats view) {
    int a = view.getCurrentPlanesCount().getArrivalsUnderApp();
    int d = view.getCurrentPlanesCount().getDeparturesUnderApp();
    String s = String.format("%d / %d / %d",
        d, a, a + d);
    lvlCurApp.setText(s);
  }

  private void updateDelays(RecentStats view) {
    ETimeStamp tmean = new ETimeStamp((int) view.getDelays().getMean());
    ETimeStamp tmax = new ETimeStamp((int) view.getDelays().getMaximum());

    String s = String.format("%s / %s", tmean.toMinuteSecondString(), tmax.toMinuteSecondString());
    lvlDelay.setText(s);
  }

  private void updateFinished(RecentStats view) {
    int a = view.getFinishedPlanes().getArrivals();
    int d = view.getFinishedPlanes().getDepartures();
    String s = String.format("%d / %d / %d",
        d, a, d + a);
    lvlFinished.setText(s);
  }

  private void updateHoldingPointInfo(RecentStats view) {
    String tmp = String.format("%d / %d",
        view.getHoldingPoint().getCount(),
        view.getHoldingPoint().getMaximum());
    lvlHpCount.setText(tmp);

    tmp = String.format("%s / %s",
        toTime(view.getHoldingPoint().getAverageDelay()),
        toTime(view.getHoldingPoint().getMaximumDelay())
    );
    lvlHpTime.setText(tmp);
  }

  private void updateMaxAll(RecentStats view) {
    int a = view.getCurrentPlanesCount().getMaximalArrivals();
    int d = view.getCurrentPlanesCount().getMaximalDepartures();
    int t = view.getCurrentPlanesCount().getMaximal();
    String s = String.format("%d / %d / %d",
        d, a, t);
    lvlMaxInGame.setText(s);
  }

  private void updateMaxApp(RecentStats view) {
    int a = view.getCurrentPlanesCount().getMaximalArrivalsUnderApp();
    int d = view.getCurrentPlanesCount().getMaximalDeparturesUnderApp();
    int t = view.getCurrentPlanesCount().getMaximalUnderApp();
    String s = String.format("%d / %d / %d",
        d, a, t);
    lvlMaxApp.setText(s);
  }

  private void updateMovementsPerHour(RecentStats view) {
    double deps = view.getMovementsPerHour().getDepartures();
    double arrs = view.getMovementsPerHour().getArrivals();
    double tots = deps + arrs;
    String s = String.format("%.0f / %.0f / %.0f",
        deps,
        arrs,
        tots
    );
    lvlMovementsGame.setText(s);
  }
}
