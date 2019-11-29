package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

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
