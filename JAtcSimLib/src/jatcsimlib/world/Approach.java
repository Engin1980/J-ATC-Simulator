/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.commands.CommandFormat;
import jatcsimlib.commands.CommandList;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.MustBeBinded;
import jatcsimlib.global.UnitProvider;
import jatcsimlib.global.XmlOptional;

/**
 *
 * @author Marek
 */
public class Approach extends MustBeBinded implements  KeyItem<Approach.eType> {

  @Override
  public eType getKey() {
    return type;
  }

  @Override
  protected void _bind() {
    _gaCommands = 
        CommandFormat.parseMulti(gaRoute);
  }

  public double getGlidePathPerNM() {
    return UnitProvider.nmToFt(Math.tan(glidePathPercentage* Math.PI / 180));
  }
 
  public enum eType{
    ILS_I,
    ILS_II,
    ILS_III,
    VORDME,
    NDB,
    GPS,
    Visual
  }
  
  private eType type;
  private int dh;
  private int radial;
  @XmlOptional
  private double glidePathPercentage = 3;
  private Coordinate point;
  private String gaRoute;
  private CommandList _gaCommands; 
  private RunwayThreshold parent;

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }

  public CommandList getGaCommands() {
    return _gaCommands;
  }
  
  public String getGaRoute() {
    return gaRoute;
  }

  public eType getType() {
    return type;
  }

  public int getDh() {
    return dh;
  }

  public double getGlidePathPercentage() {
    return glidePathPercentage;
  }
  
  public int getRadial() {
    return radial;
  }

  public Coordinate getPoint() {
    return point;
  }
 
  
}
  
