package eng.jAtcSim.newLib.traffic.models.base;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import static eng.jAtcSim.newLib.shared.SharedFactory.getRnd;

public abstract class DayGeneratedTrafficModel {
  private final CallsignGenerator callsignGenerator = new CallsignGenerator();
  private double delayProbability;
  private int perStepDelay;
//  private boolean useExtendedCallsigns;

  public DayGeneratedTrafficModel(double delayProbability, int perStepDelay, boolean useExtendedCallsigns) {
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, delayProbability, 1));
    EAssert.isTrue(perStepDelay >= 0);
    this.delayProbability = delayProbability;
    this.perStepDelay = perStepDelay;
//    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  public abstract IReadOnlyList<MovementTemplate> generateMovementsForOneDay();

  protected int generateDelayMinutes() {
    int ret = 0;
    while (getRnd().nextDouble() < this.delayProbability) {
      int del = getRnd().nextInt(this.perStepDelay);
      ret += del;
    }
    return ret;
  }

//  protected Callsign generateRandomCallsign(@Nullable String prefix, boolean isCommercialFlight) {
//    Callsign ret = this.callsignGenerator.generateCallsign(prefix, isCommercialFlight, useExtendedCallsigns);
//    return ret;
//  }
}

