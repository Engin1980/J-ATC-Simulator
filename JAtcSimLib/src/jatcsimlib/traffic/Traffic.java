/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import jatcsimlib.airplanes.Airplane;

/**
 *
 * @author Marek Vajgl
 */
public abstract class Traffic {
  
  public abstract Airplane[] getNewAirplanes();
  
}
