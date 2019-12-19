/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

/**
 * @author Marek Vajgl
 */
public abstract class TrafficOld {

  private final double delayProbability;// = 0.3;
  private final int maxDelayInMinutesPerStep; // = 15;

  protected TrafficOld(double delayProbability, int maxDelayInMinutesPerStep) {
    assert delayProbability >= 0 && delayProbability <= 1 : "Delay probability must be between 0 - 1";
    assert maxDelayInMinutesPerStep >= 0;
    this.delayProbability = delayProbability;
    this.maxDelayInMinutesPerStep = maxDelayInMinutesPerStep;
  }

  public double getDelayProbability() {
    return delayProbability;
  }

  public abstract IReadOnlyList<ExpectedMovement> getExpectedTimesForDay();

  public int getMaxDelayInMinutesPerStep() {
    return maxDelayInMinutesPerStep;
  }

  public abstract IReadOnlyList<MovementTemplate> getMovements(
      ETimeStamp fromTimeInclusive, ETimeStamp toTimeExclusive);
}


