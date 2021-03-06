/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.global.ETime;

/**
 * @author Marek Vajgl
 */
public abstract class Traffic {

  public static class ExpectedMovement{
    public ETime time;
    public boolean isArrival;
    public boolean isCommercial;
    public char category;

    public ExpectedMovement(ETime time, boolean isArrival, boolean isCommercial, char category) {
      this.time = time;
      this.isArrival = isArrival;
      this.isCommercial = isCommercial;
      this.category = category;
    }
  }

  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;

  public double getDelayProbability() {
    return delayProbability;
  }

  public int getMaxDelayInMinutesPerStep() {
    return maxDelayInMinutesPerStep;
  }

  public abstract GeneratedMovementsResponse generateMovements(Object syncObject);

  public abstract IReadOnlyList<ExpectedMovement> getExpectedTimesForDay();
}


