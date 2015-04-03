/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.Acc;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.*;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.Headings;

/**
 *
 * @author Marek Vajgl
 */
public class ShortFormatter implements Formatter {

  private static Formatter instance = null;
  public static Formatter getInstance(){
    if (instance == null)
      instance = new ShortFormatter();
    return instance;
  }
  
  @Override
  public String format(AfterAltitudeCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(AfterNavaidCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(AfterSpeedCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(ChangeAltitudeCommand cmd) {
    StringBuilder sb = new StringBuilder();
    switch (cmd.getDirection()) {
      case any:
        sb.append("");
        break;
      case climb:
        sb.append("CM ");
        break;
      case descend:
        sb.append("DM ");
        break;
      default:
        throw new ENotSupportedException();
    }
    sb.append(Acc.toAltS(cmd.getAltitudeInFt(), true));
    return sb.toString();
  }

  @Override
  public String format(ChangeHeadingCommand cmd) {
    StringBuilder sb = new StringBuilder();

    if (cmd.isCurrentHeading()) {
      sb.append("FCH");
    } else {
      switch (cmd.getDirection()) {
        case any:
          sb.append("FH ");
          break;
        case left:
          sb.append("TL ");
          break;
        case right:
          sb.append("TR ");
          break;
        default:
          throw new ENotSupportedException();
      }
      sb.append(Headings.format(cmd.getHeading()));
    }
    return sb.toString();
  }

  @Override
  public String format(ChangeSpeedCommand cmd) {
    if (cmd.isResumeOwnSpeed()) {
      return "SR";
    } else {
      StringBuilder sb = new StringBuilder();
      switch (cmd.getDirection()){
        case atLeast:
          sb.append(">");
          break;
        case atMost:
          sb.append("<");
          break;
      }
      sb.append(cmd.getSpeedInKts());      
      sb.append("kts");      
      return sb.toString();
    }
  }

  @Override
  public String format(ClearedForTakeoffCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(ClearedToApproachCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("C ");
    sb.append(cmd.getApproach().getType());
    sb.append(" ");
    sb.append(cmd.getApproach().getParent().getName());
    return sb.toString();
  }

  @Override
  public String format(ContactCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("c-atc ");
    sb.append(cmd.getAtcType());
    return sb.toString();
  }

  @Override
  public String format(HoldCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(ProceedDirectCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("PD ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

  @Override
  public String format(ShortcutCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("SH ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

  @Override
  public String format(ThenCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(ToNavaidCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
