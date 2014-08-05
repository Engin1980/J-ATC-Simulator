/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes;

/**
 *
 * @author Marek
 */
public class AirplaneType {
  
  public enum eType{
    prop,
    jet
  }
  
  public enum eSize{
    light,
    medium,
    heavy
  }
  
  private String name;
  private int maxAltitude;
  private int vR;
  private int vMin;
  private int vMax;
  private int vCruise;
  private int vDep;
  private int vApp;
  private int lowClimbRate;
  private int highClimbRate;
  private int highDescendRate;
  private int lowDescendRate;
  private eType type;
  private eSize size;
}
