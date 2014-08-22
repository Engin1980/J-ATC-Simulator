/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.coordinates.RadarRange;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.KeyList;

/**
 *
 * @author Marek
 */
public class Airport implements KeyItem<String> {
  private String icao;
  private String name;
  private int transitionAltitude;
  private final RadarRange radarRange= new RadarRange();
  private final KeyList<Runway, String> runways = new KeyList();

  @Override
  public String getKey() {
    return icao;
  }

  public String getIcao() {
    return icao;
  }

  public String getName() {
    return name;
  }

  public int getTransitionAltitude() {
    return transitionAltitude;
  }
  
  public Coordinate getLocation (){
    return runways.get(0).getThresholdA().getCoordinate();
  }
  
  public KeyList<Runway, String> getRunways(){
    return runways;
  }

  public RadarRange getRadarRange() {
    return radarRange;
  }
}
