/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.eSystem.xmlSerialization.XmlOptional;

/**
 * @author Marek Vajgl
 */
public abstract class Traffic {
  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;
  private String title;
  @XmlOptional
  private String description;

  public double getDelayProbability() {
    return delayProbability;
  }

  public int getMaxDelayInMinutesPerStep() {
    return maxDelayInMinutesPerStep;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public abstract GeneratedMovementsResponse generateMovements(Object syncObject);

}


