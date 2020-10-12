package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PlaneResponsibilityEvidence {

  private final IMap<Squawk, AtcId> inner = new EMap<>();
  private final ISet<Squawk> proceedingSwitches = new ESet<>();

  public void setResponsibleAtc(Squawk squawk, AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    EAssert.Argument.isNotNull(squawk, "squawk");
    this.inner.set(squawk, atcId);
  }

  public void addsquawk(Squawk squawk, AtcId atcId) {
    this.setResponsibleAtc(squawk, atcId);
  }

  public void removesquawk(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    inner.remove(squawk);
  }

  public void openProceedingSwitch(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    this.proceedingSwitches.add(squawk);
  }

  public void closeProceedingSwitch(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    this.proceedingSwitches.remove(squawk);
  }

  public AtcId getResponsibleAtc(Squawk squawk) {
    EAssert.isTrue(inner.containsKey(squawk), sf("Squawk '%s' not in evidence.", squawk));
    return inner.get(squawk);
  }

  public AtcId getResponsibleAtc(IAirplane plane) {
    return getResponsibleAtc(plane.getSqwk());
  }
}
