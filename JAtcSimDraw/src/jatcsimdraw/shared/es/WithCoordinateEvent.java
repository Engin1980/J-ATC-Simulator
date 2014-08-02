/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.shared.es;

import jatcsimlib.types.Coordinate;

/**
 *
 * @author Marek
 */
public class WithCoordinateEvent {
  public final Coordinate coordinate;

  public WithCoordinateEvent(Coordinate coordinate) {
    this.coordinate = coordinate;
  }
  
  
}
