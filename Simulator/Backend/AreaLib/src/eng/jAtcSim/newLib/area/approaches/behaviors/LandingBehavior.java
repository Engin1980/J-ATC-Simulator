package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class LandingBehavior implements IApproachBehavior {
  public static LandingBehavior create() {
    return new LandingBehavior();
  }

  @XmlConstructor
  @XConstructor
  private LandingBehavior() {
  }
}
