/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.Acc;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.AfterAltitudeCommand;
import jatcsimlib.commands.AfterNavaidCommand;
import jatcsimlib.commands.AfterSpeedCommand;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.ChangeHeadingCommand;
import jatcsimlib.commands.ChangeSpeedCommand;
import jatcsimlib.commands.ClearedForTakeoffCommand;
import jatcsimlib.commands.ClearedToApproachCommand;
import jatcsimlib.commands.Confirmation;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.commands.GoodDayCommand;
import jatcsimlib.commands.HoldCommand;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.commands.Rejection;
import jatcsimlib.commands.ShortcutCommand;
import jatcsimlib.commands.StringCommand;
import jatcsimlib.commands.ThenCommand;
import jatcsimlib.commands.ToNavaidCommand;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.Headings;

/**
 *
 * @author Marek Vajgl
 */
public class LongFormatter implements Formatter {

  private static Formatter instance = null;
  public static Formatter getInstance(){
    if (instance == null)
      instance = new LongFormatter();
    return instance;
  }
  
  @Override
  public String format(AfterAltitudeCommand cmd) {
    return "when passing " + Acc.toAltS(cmd.getAltitudeInFt(), true) + " ";
  }

  @Override
  public String format(AfterNavaidCommand cmd) {
    return "after " + cmd.getNavaid().getName() + " ";
  }

  @Override
  public String format(AfterSpeedCommand cmd) {
    return "at speed " + cmd.getSpeedInKts() + "kts ";
  }

  @Override
  public String format(ChangeAltitudeCommand cmd) {
    StringBuilder sb = new StringBuilder();
    switch (cmd.getDirection()) {
      case any:
        sb.append("");
        break;
      case climb:
        sb.append("climb and maintain ");
        break;
      case descend:
        sb.append("descend and maintain ");
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
      sb.append("fly current heading");
    } else {
      switch (cmd.getDirection()) {
        case any:
          sb.append("fly heading ");
          break;
        case left:
          sb.append("turn left ");
          break;
        case right:
          sb.append("turn right ");
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
      return "resume own speed";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("speed ");
      sb.append(cmd.getSpeedInKts());
      sb.append(" kts");
      switch (cmd.getDirection()) {
        case atLeast:
          sb.append(" or more");
          break;
        case atMost:
          sb.append(" or less");
          break;
      }
      return sb.toString();
    }
  }

  @Override
  public String format(ClearedForTakeoffCommand cmd) {
    return "cleared for takeoff " + cmd.getRunwayThreshold().getName();
  }

  @Override
  public String format(ClearedToApproachCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("cleared for ");
    sb.append(cmd.getApproach().getType());
    sb.append(" approach");
    sb.append(cmd.getApproach().getParent().getName());
    return sb.toString();
  }

  @Override
  public String format(ContactCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("contact ");
    sb.append(cmd.getAtcType());
    Atc atc = Acc.atc(cmd.getAtcType());
    sb.append(" at ");
    sb.append(atc.getFrequency());
    return sb.toString();
  }

  @Override
  public String format(HoldCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("hold over ");
    sb.append(cmd.getNavaid().getName());
    if (cmd.isPublished()) {
      sb.append(" as published");
    } else {
        sb.append("inbound ");
      sb.append(Headings.format(cmd.getInboundRadial()));
      sb.append(cmd.isLeftTurn() ? "left turns " : "right turns ");
    }
    return sb.toString();
  }

  @Override
  public String format(ProceedDirectCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("proceed direct ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

  @Override
  public String format(ShortcutCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("shortcut to ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

  @Override
  public String format(ThenCommand cmd) {
    return "then ";
  }

  @Override
  public String format(ToNavaidCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String format(Confirmation cmd) {
    return Formatters.format(cmd.getOrigin(), this);
  }

  @Override
  public String format(Rejection cmd) {
    return "Unable " + Formatters.format(cmd.getOrigin(), this) + ". " + cmd.getReason();
  }
  
  @Override
  public String format(StringCommand cmd){
    return cmd.getText();
  }

  private final String [] greetings = new String[]{"Good day", "Hello", "Hi"};
  @Override
  public String format(GoodDayCommand cmd) {
    double d = Acc.rnd().nextDouble();
    d = d * greetings.length;
    StringBuilder sb = new StringBuilder();
    sb.append(greetings[(int)d]).append(", ").append(cmd.getCallsign().toString()).append(" with you at ").append(cmd.getAltitudeInfoText());
    return sb.toString();
  }

}
