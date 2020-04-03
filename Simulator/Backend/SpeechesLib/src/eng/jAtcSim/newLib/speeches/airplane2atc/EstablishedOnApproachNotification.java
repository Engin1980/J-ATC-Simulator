package eng.jAtcSim.newLib.speeches.airplane2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.INotification;

public class EstablishedOnApproachNotification implements INotification {

  private String thresholdName;

  public EstablishedOnApproachNotification(String thresholdName) {
    EAssert.Argument.isNonemptyString(thresholdName);
    this.thresholdName = thresholdName;
  }

  public String getThresholdName() {
    return thresholdName;
  }

  @Override
  public String toString(){
    String ret = "Established on approach {notification}";

    return ret;
  }

}
