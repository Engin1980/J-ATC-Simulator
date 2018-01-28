/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.MustBeBinded;
import jatcsimlib.global.UnitProvider;
import eng.eSystem.xmlSerialization.XmlOptional;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.fromAtc.IAtcCommand;
import jatcsimlib.speaking.parsing.Parser;
import jatcsimlib.speaking.parsing.shortParsing.ShortParser;

/**
 *
 * @author Marek
 */
public class Approach extends MustBeBinded implements KeyItem<Approach.eType> {

  public enum eType {

    ILS_I,
    ILS_II,
    ILS_III,
    VORDME,
    NDB,
    GNSS,
    Visual
  }

  private eType type;
  private int da;
  private int radial;
  @XmlOptional
  private double glidePathPercentage = 3;
  private Coordinate point;
  private String gaRoute;
  private SpeechList<IAtcCommand> _gaCommands;
  private RunwayThreshold parent;

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return _gaCommands;
  }

  public String getGaRoute() {
    return gaRoute;
  }

  public eType getType() {
    return type;
  }

  public int getDecisionAltitude(){
    return da;
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

  @Override
  public eType getKey() {
    return type;
  }

  @Override
  protected void _bind() {
    Parser p = new ShortParser();
    _gaCommands
        = p.parseMultipleCommands(gaRoute);
  }

  public double getGlidePathPerNM() {
    return UnitProvider.nmToFt(Math.tan(glidePathPercentage * Math.PI / 180));
  }
}
