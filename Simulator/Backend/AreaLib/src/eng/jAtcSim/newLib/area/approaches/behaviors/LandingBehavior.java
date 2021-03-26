package eng.jAtcSim.newLib.area.approaches.behaviors;


import exml.annotations.XConstructor;

public class LandingBehavior implements IApproachBehavior {
  public static LandingBehavior create() {
    return new LandingBehavior();
  }


  @XConstructor
  private LandingBehavior() {
  }
}
