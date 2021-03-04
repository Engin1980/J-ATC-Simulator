package eng.jAtcSim.newPacks;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.layouting.JFrameFactory;
import eng.jAtcSim.layouting.Layout;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.settings.AppSettings;

public class NewPack {
  private IGame game;
  private ISimulation sim;
  private Area area;
  private Airport aip;
  private AppSettings settings;



  public void init(IGame game, Layout layout, AppSettings appSettings) {
    settings = appSettings;

    // init sim & area
    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();
    this.aip = sim.getAirport();

    JFrameFactory frameFactory = new JFrameFactory();
    //TODO how to set menu?
    ISet<JFrameFactory.JFrameInfo> frames = frameFactory.build(layout);

    ISet<Tuple<IView, JFrameFactory.JPanelInfo>> view2panelMap = new ESet<>();
    for (JFrameFactory.JFrameInfo frame : frames) {
      for (JFrameFactory.JPanelInfo panel : frame.getPanels()) {
        String viewName = panel.getViewName();
        IView view = ViewFactory.getView(viewName);
        view2panelMap.add(new Tuple<>(view, panel));
      }
    }

    for (Tuple<IView, JFrameFactory.JPanelInfo> item : view2panelMap) {
      item.getA().init(item.getB().getPanel(), this.sim, this.settings);
    }
  }

  public void start(){

  }

}
