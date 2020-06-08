package eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.area.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class EstablishedOnApproachNotification implements IAirplaneNotification {

  private ActiveRunwayThreshold threshold;

  public EstablishedOnApproachNotification(ActiveRunwayThreshold threshold) {
    this.threshold = threshold;
  }

  public ActiveRunwayThreshold getThreshold() {
    return threshold;
  }

  @Override
  public String toString(){
    String ret = "Established on approach {notification}";

    return ret;
  }

}
