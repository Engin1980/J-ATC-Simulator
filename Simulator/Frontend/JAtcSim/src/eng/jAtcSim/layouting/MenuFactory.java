package eng.jAtcSim.layouting;

import eng.eSystem.functionalInterfaces.Consumer;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class MenuFactory {

  private static class SimProxy {

    public enum Views {
      FLIGHT_STRIP,
      STATS,
      RADAR,
      MOODS,
      SCHEDULED;
    }

    public enum Tools {
      COMMAND_BUTTONS,
      RECORDING
    }

    public void quit() {
//      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//      this.setVisible(false);
    }

    public void recordingViewRequest() {
//      viewRecordingPanel()
    }

    public void saveSimulation() {

    }

    public void setSimulationSpeed(int speedInMs) {

    }

    public void showAboutPage() {
      //about_app
//      new FrmAbout().setVisible(true)
    }

    public void showProjectPage() {
//      {
//        ProcessBuilder pb;
//        String url = "https://github.com/Engin1980/J-ATC-Simulator/wiki";
//        String osName = System.getProperty("os.name");
//        if (osName.contains("Windows"))
//          pb = new ProcessBuilder("cmd", "/c", "start", url);
//        else
//          pb = new ProcessBuilder("xsd-open", url);
//        try {
//          pb.start();
//        } catch (IOException e) {
//          //TODO
//          //Context.getApp().getAppLog().write(ApplicationLog.eType.warning, "Failed to start project web pages." + ExceptionUtils.toFullString(e));
//        }
//      });
    }

    public void togglePause() {
//      parent.getSim().pauseUnpauseSim();
    }

    public void toggleSound() {
//      SoundManager.switchEnabled();
//      s.setState(SoundManager.isEnabled());
    }

    public void toolRequest(Tools tool) {
//COMMANDS
//      boolean isVis = pnlCommands.isVisible();
//      isVis = !isVis;
//      pnlCommands.setVisible(isVis);
//      s.setState(isVis);
    }

    public void viewRequest(Views view) {

      //mood
//      {
//        MoodHistoryPanel pnl = new MoodHistoryPanel();
//        pnl.init(this.parent.getSim().getStats().getFullMoodHistory());
//        SwingFactory.show(pnl, "Rating board");
//      })

      //RADAR
//      {
//        FrmView f = new FrmView();
//        f.init(this.parent);
//        f.setVisible(true);
//      });

      //STATS
//      StatsGraphPanel pnl = new StatsGraphPanel();
//      pnl.init(this.parent.getSim().getStats());
//      SwingFactory.show(pnl, "Statistical graphs");
//    });

      // right recent stats + cosi
//      boolean isVis = pnlRight.isVisible();
//      isVis = !isVis;
//      pnlRight.setVisible(isVis);
//      s.setState(isVis);

      // left flight-strip view
//      {
//        boolean isVis = pnlLeft.isVisible();
//        isVis = !isVis;
//        pnlLeft.setVisible(isVis);
//        s.setState(isVis);
//      }
    }
  }

  public static void buildMenu(JFrame frame) {
    JMenuBar mnu = buildMenu();
    frame.setJMenuBar(mnu);
  }

  private static JMenuBar buildMenu() {
    JMenuBar ret = new JMenuBar();
    SimProxy sp = new SimProxy();

    JMenu mnuFile = new JMenu("File");
    mnuFile.setMnemonic(KeyEvent.VK_F);
    ret.add(mnuFile);

    JMenu mnuSimulation = new JMenu("Simulation");
    mnuSimulation.setMnemonic(KeyEvent.VK_S);
    ret.add(mnuSimulation);

    JMenu mnuView = new JMenu("View");
    mnuView.setMnemonic(KeyEvent.VK_V);
    ret.add(mnuView);

    JMenu mnuHelp = new JMenu("Help");
    mnuHelp.setMnemonic(KeyEvent.VK_H);
    ret.add(mnuHelp);

    {
      buildMenuItem(mnuFile, "Save", 's', s -> sp.saveSimulation());
      mnuFile.addSeparator();
      buildMenuItem(mnuFile, "Quit", 'q', s -> sp.quit());
    }

    {
      buildMenuItem(mnuSimulation, "Pause/Resume", 'p', s -> sp.togglePause());
      JMenu mnuSpeed = new JMenu("Set speed");
      mnuSpeed.setMnemonic(KeyEvent.VK_S);
      mnuSimulation.add(mnuSpeed);
      {
        buildMenuItem(mnuSpeed, "Frozen (5000ms)", null, s -> sp.setSimulationSpeed(5000));
        buildMenuItem(mnuSpeed, "Slow (2000ms)", null, s -> sp.setSimulationSpeed(2000));
        buildMenuItem(mnuSpeed, "Real (1000ms)", null, s -> sp.setSimulationSpeed(1000));
        buildMenuItem(mnuSpeed, "Accelerated (750ms)", null, s -> sp.setSimulationSpeed(750));
        buildMenuItem(mnuSpeed, "Arcade (500ms)", null, s -> sp.setSimulationSpeed(500));
        buildMenuItem(mnuSpeed, "Fast (250ms)", null, s -> sp.setSimulationSpeed(250));
        buildMenuItem(mnuSpeed, "Skip (50ms)", null, s -> sp.setSimulationSpeed(50));
      }
      mnuSimulation.addSeparator();
      buildCheckMenuItem(mnuSimulation, "Sounds", true, 's', s -> sp.toggleSound());
      buildMenuItem(mnuSimulation, "Recording", 'r', s -> sp.recordingViewRequest());
    }

    {
      buildCheckMenuItem(mnuView, "Flight strips", true, 'f',
              s -> sp.viewRequest(SimProxy.Views.FLIGHT_STRIP));
      buildCheckMenuItem(mnuView, "Command buttons", true, 'c',
              s -> sp.toolRequest(SimProxy.Tools.COMMAND_BUTTONS));
      buildCheckMenuItem(mnuView, "Scheduled & Stats", true, 's',
              s -> sp.viewRequest(SimProxy.Views.SCHEDULED));

      mnuView.addSeparator();
      buildMenuItem(mnuView, "Show mood results", null, s -> sp.viewRequest(SimProxy.Views.MOODS));
      buildMenuItem(mnuView, "Show stats graphs", null, s -> sp.viewRequest(SimProxy.Views.STATS));
      buildMenuItem(mnuView, "Add new radar view", 'r', s -> sp.viewRequest(SimProxy.Views.RADAR));
    }

    {
      buildMenuItem(mnuHelp, "Project web pages", null, s -> sp.showProjectPage());
      mnuHelp.addSeparator();
      buildMenuItem(mnuHelp, "About", 'o', s -> sp.showAboutPage());
    }

    return ret;
  }

  private static void buildMenuItem(JMenu mnu, String label, Character charMnemonic, Consumer<JMenuItem> action) {
    JMenuItem item = new JMenuItem(label);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.invoke(item));
  }

  private static void buildCheckMenuItem(JMenu mnu, String label, boolean checkBoxState,
                                         Character charMnemonic,
                                         Consumer<JCheckBoxMenuItem> action) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(label, checkBoxState);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.invoke(item));
  }
}
