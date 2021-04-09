package eng.jAtcSim.newPacks;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.abstractRadar.global.SoundManager;
import eng.jAtcSim.app.FrmAbout;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.layouting.JFrameFactory;
import eng.jAtcSim.layouting.Layout;
import eng.jAtcSim.layouting.MenuFactory;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.GameFactoryAndRepository;
import eng.jAtcSim.newLib.speeches.system.user2system.TickSpeedRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
import eng.jAtcSim.newPacks.views.ViewInitInfo;
import eng.jAtcSim.settings.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class NewPack {
  private IGame game;
  private ISimulation sim;
  private Area area;
  private Airport aip;
  private AppSettings settings;
  private ISet<JFrameFactory.JFrameInfo> frameInfos;
  private String lastGameFileName;

  public void init(IGame game, Layout layout, AppSettings appSettings) {
    settings = appSettings;

    // init sim & area
    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();
    this.aip = sim.getAirport();

    JFrameFactory frameFactory = new JFrameFactory();
    //TODO how to set menu?
    frameInfos = frameFactory.build(layout);

    ISet<Tuple<IView, JFrameFactory.JPanelInfo>> view2panelMap = new ESet<>();
    for (JFrameFactory.JFrameInfo frame : frameInfos) {
      for (JFrameFactory.JPanelInfo panel : frame.getPanels()) {
        panel.getPanel().setBackground(Color.blue);
        String viewName = panel.getViewName();
        IView view = ViewFactory.getView(viewName);
        view2panelMap.add(new Tuple<>(view, panel));
      }

      if (frame.getMenuSimProxy() != null)
        bindMenuProxy(frame.getMenuSimProxy(), this.sim);

      Stylist.apply(frame.getFrame(), true);
    }

    ViewInitInfo vii = new ViewInitInfo();
    vii.setSimulation(this.sim);
    vii.setAirport(this.aip);
    vii.setSettings(this.settings);
    vii.setUserAtcId(this.sim.getUserAtcIds().get(0));
    vii.setDynamicAirplaneSpeechFormatter(this.settings.getDynamicPlaneFormatter()); //TODO improve somehow

    for (Tuple<IView, JFrameFactory.JPanelInfo> item : view2panelMap) {
      item.getA().init(item.getB().getPanel(), vii, item.getB().getOptions());
    }

    for (Tuple<IView, JFrameFactory.JPanelInfo> item : view2panelMap) {
      item.getA().postInit();
    }

    //printSummary(frames);
  }

  public void quit() {
    for (JFrameFactory.JFrameInfo frame : frameInfos) {
      frame.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getFrame().setVisible(false);
    }
    //TODO do it using something like system.close() ?
  }

  public void save() {
    boolean wasRunning = this.sim.isRunning();
    this.sim.stop();

    JFileChooser jf = SwingFactory.createFileDialog(SwingFactory.FileDialogType.game, lastGameFileName);
    int res = jf.showSaveDialog(this.frameInfos.getFirst().getFrame());
    if (res == JFileChooser.APPROVE_OPTION) {

      String fileName = jf.getSelectedFile().getAbsolutePath();

      if (!fileName.endsWith(SwingFactory.SAVED_SIMULATION_EXTENSION))
        fileName += SwingFactory.SAVED_SIMULATION_EXTENSION;

      //TODO implement custom object saving
      IMap<String, Object> customData = new EMap<>(); //this.parent.getDataToStore();

      new GameFactoryAndRepository().save(this.game, customData, fileName);
      lastGameFileName = fileName;
      //TODO do somehow:
//      this.parent.getSim().sendTextMessageForUser("Game saved.");
    }

    if (wasRunning)
      this.sim.start();
  }

  public void show() {
    frameInfos.forEach(q -> q.getFrame().setVisible(true));
    System.out.println("Viewed:");
  }

  private void bindMenuProxy(MenuFactory.MenuSimProxy menuSimProxy, ISimulation sim) {
    menuSimProxy.onTogglePause.add(() -> sim.pauseUnpauseSim());
    menuSimProxy.onShowProject.add(() -> {
      ProcessBuilder pb;
      String url = "https://github.com/Engin1980/J-ATC-Simulator/wiki";
      String osName = System.getProperty("os.name");
      if (osName.contains("Windows"))
        pb = new ProcessBuilder("cmd", "/c", "start", url);
      else
        pb = new ProcessBuilder("xsd-open", url);
      try {
        pb.start();
      } catch (IOException e) {
        //TODO
        //Context.getApp().getAppLog().write(LogItemType.warning, "Failed to start project web pages." + ExceptionUtils.toFullString(e));
      }
    });
    menuSimProxy.onSimSpeed.add(q -> sim.sendSystemCommandAnonymous(TickSpeedRequest.createSet(q)));
    menuSimProxy.onToggleSound.add(() -> {
      SoundManager.switchEnabled();
      //TODO do somehow: s.setState(SoundManager.isEnabled());
    });
    menuSimProxy.onQuit.add(() -> {
      this.quit();
    });
    menuSimProxy.onShowAbout.add(() -> {
      new FrmAbout().setVisible(true);
    });
    menuSimProxy.onRecordingRequest.add(() -> viewRecordingPanel());
    menuSimProxy.onSave.add(() -> {
      this.save();
    });
  }

  private void printSummary(ISet<JFrameFactory.JFrameInfo> frames) {
    for (JFrameFactory.JFrameInfo frame : frames) {
      System.out.println("Layout summary");
      System.out.println("Frame " + frame.getFrame().getTitle());
      printSummaryPanel((JPanel) frame.getFrame().getContentPane(), 0);
    }
  }

  private void printSummaryPanel(JPanel panel, int index) {
    for (int i = 0; i <= index; i++) {
      System.out.print("\t");
    }
    System.out.println(panel);
    for (Component component : panel.getComponents()) {
      if (component instanceof JPanel) {
        JPanel inner = (JPanel) component;
        printSummaryPanel(inner, index + 1);
      }
    }
  }

  private void viewRecordingPanel() {
    throw new ToDoException();
//    RecordingPanel pnl;
//    if (recording != null)
//      pnl = new RecordingPanel(recording.getSettings());
//    else
//      pnl = new RecordingPanel(null);
//    pnl.getRecordingStarted().add(q -> recording_recordingStarted(q));
//    pnl.getRecordingStopped().add(() -> recording_recordingStopped());
//    pnl.getViewRecordingFolderRequest().add(() -> recording_viewRecordingFolderRequest());
//    SwingFactory.show(pnl, "Recording");
  }

}
