package eng.jAtcSim.newLib.speeches.atc.planeSwitching;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

public class PlaneSwitchRequest implements IAtcSpeech {
  private final PlaneSwitchRequestRouting routing;
  private final Squawk squawk;
  private final boolean repeated;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Plane-Switch-Request ");
    sb.append(squawk.toString());
    if (routing != null)
      sb.append(" via " ).append(routing.toString());
    if (repeated)
      sb.append(" repeated");
    return sb.toString();
  }

  public PlaneSwitchRequest(Squawk squawk, PlaneSwitchRequestRouting routing) {
    EAssert.Argument.isNotNull(routing, "routing");
    EAssert.Argument.isNotNull(squawk, "squawk");
    this.squawk = squawk;
    this.routing = routing;
    this.repeated = false;
  }

  public PlaneSwitchRequest(Squawk squawk, boolean repeated) {
    this.squawk = squawk;
    this.repeated = repeated;
    this.routing = null;
  }

  public boolean isRepeated() {
    return repeated;
  }

  public PlaneSwitchRequestRouting getRouting() {
    return routing;
  }

  public boolean isRouting() {
    return routing != null;
  }

  public Squawk getSquawk() {
    return squawk;
  }


}
