package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.newXmlUtils.annotations.XmlConstructor;

public class LandingBehavior implements IApproachBehavior {
  public static LandingBehavior create() {
    return new LandingBehavior();
  }

  @XmlConstructor
  private LandingBehavior() {
  }
}
