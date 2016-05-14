/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.global.ETime;

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
  
}
