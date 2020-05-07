/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.abstractRadar.global.events;

import eng.eSystem.geo.Coordinate;

/**
 *
 * @author Marek
 */
public class WithCoordinateEventArg {
  public final Coordinate coordinate;

  public WithCoordinateEventArg(Coordinate coordinate) {
    this.coordinate = coordinate;
  }
}
