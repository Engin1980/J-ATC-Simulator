/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.eSystem.collections.EDistinctList;

/**
 *
 * @author Marek
 */
public class AirplaneList extends EDistinctList<Airplane> {

  public AirplaneList(boolean duplicitCheckEnabled) {
    super(duplicitCheckEnabled ? Behavior.exception : Behavior.ignore);
  }
}
