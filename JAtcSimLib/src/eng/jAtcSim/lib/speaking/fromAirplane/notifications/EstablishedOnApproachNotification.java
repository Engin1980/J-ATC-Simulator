package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class EstablishedOnApproachNotification implements IAirplaneNotification {

  private RunwayThreshold threshold;

  public EstablishedOnApproachNotification(RunwayThreshold threshold) {
    this.threshold = threshold;
  }

  public RunwayThreshold getThreshold() {
    return threshold;
  }

  @Override
  public String toString(){
    String ret = "Established on approach {notification}";

    return ret;
  }

}
