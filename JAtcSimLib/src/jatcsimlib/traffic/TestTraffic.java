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
public abstract class TestTraffic extends Traffic{

  private boolean done = false;
  
  @Override
  public Airplane[] getNewAirplanes() {
    if (done) return new Airplane[0];
    
    Airplane [] ret = generatePlanes();
    
    done = true;
    
    return ret;
  }
  
  protected abstract Airplane[] generatePlanes();
}
