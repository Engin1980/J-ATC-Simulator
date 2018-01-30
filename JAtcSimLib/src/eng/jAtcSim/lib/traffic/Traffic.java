/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;

/**
 *
 * @author Marek Vajgl
 */
public abstract class Traffic {
  
  /**
   * Returns new airplanes after specified time.
   * @return New airplanes
   */
  public abstract Airplane[] getNewAirplanes();

  /**
   * Generates new airplanes for future, if required.
   */
  public abstract void generateNewMovementsIfRequired();

  public abstract Movement[] getScheduledMovements();
}
