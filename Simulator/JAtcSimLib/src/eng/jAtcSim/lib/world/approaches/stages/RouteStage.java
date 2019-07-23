package eng.jAtcSim.lib.world.approaches.stages;

import eng.jAtcSim.lib.speaking.SpeechList;

public class RouteStage implements IApproachStage {

  private SpeechList route;

  public RouteStage(SpeechList route) {
    this.route = route;
  }

  public SpeechList getRoute(){
    return this.route;
  }

}
