/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.MustBeBinded;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;
import eng.jAtcSim.lib.world.RunwayThreshold;

/**
 *
 * @author Marek
 */
@Deprecated
public class ApproachOld extends MustBeBinded implements KeyItem<ApproachOld.eType> {

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
