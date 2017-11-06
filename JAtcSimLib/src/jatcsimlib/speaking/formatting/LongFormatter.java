package jatcsimlib.speaking.formatting;

import jatcsimlib.Acc;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.PlaneSwitchMessage;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.Headings;
import jatcsimlib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import jatcsimlib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import jatcsimlib.speaking.fromAtc.commands.*;
import jatcsimlib.speaking.fromAtc.commands.afters.*;
import jatcsimlib.speaking.fromAirplane.notifications.GoodDayNotification;
import jatcsimlib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import jatcsimlib.speaking.fromAirplane.notifications.RequestRadarContactNotification;

public class LongFormatter extends Formatter {

  private static Formatter instance = null;
  public static Formatter getInstance(){
    if (instance == null)
      instance = new LongFormatter();
    return instance;
  }

  public String format(AfterAltitudeCommand cmd) {
    return "when passing " + Acc.toAltS(cmd.getAltitudeInFt(), true) + " ";
  }

   
  public String format(AfterNavaidCommand cmd) {
    return "after " + cmd.getNavaid().getName() + " ";
  }

   
  public String format(AfterSpeedCommand cmd) {
    return "at speed " + cmd.getSpeedInKts() + "kts ";
  }

   
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

   
  public String format(ClearedForTakeoffCommand cmd) {
    return "cleared for takeoff " + cmd.getRunwayThreshold().getName();
  }

   
  public String format(ClearedToApproachCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("cleared for ");
    sb.append(cmd.getApproach().getType());
    sb.append(" approach");
    sb.append(cmd.getApproach().getParent().getName());
    return sb.toString();
  }

   
  public String format(ContactCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("contact ");
    sb.append(cmd.getAtcType());
    Atc atc = Acc.atc(cmd.getAtcType());
    sb.append(" at ");
    sb.append(atc.getFrequency());
    return sb.toString();
  }

   
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

   
  public String format(ProceedDirectCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("proceed direct ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

   
  public String format(ShortcutCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("shortcut to ");
    sb.append(cmd.getNavaid().getName());
    return sb.toString();
  }

   
  public String format(ThenCommand cmd) {
    return "then ";
  }

   
  public String format(ToNavaidCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

   
  public String format(Confirmation cmd) {
    return super.format(cmd.getOrigin());
  }

   
  public String format(Rejection cmd) {
    return "Unable " + super.format(cmd.getOrigin()) + ". " + cmd.getReason();
  }

   
  public String format(RequestRadarContactNotification cmd) {
    return "Unable to follow ordered fromAtc. Please confirm our radar contact first.";
  }

   
  public String format(RadarContactConfirmationNotification cmd) {
    return "Radar contact.";
  }

  private final String [] greetings = new String[]{"Good day", "Hello", "Hi"};
   
  public String format(GoodDayNotification cmd) {
    double d = Acc.rnd().nextDouble();
    d = d * greetings.length;
    StringBuilder sb = new StringBuilder();
    sb.append(greetings[(int)d]).append(", ").append(cmd.getCallsign().toString()).append(" with you at ").append(cmd.getAltitudeInfoText());
    return sb.toString();
  }

  @Override
  public String format(Atc sender, PlaneSwitchMessage msg) {
      String ret = String.format(
          "%s {%s} %s",
          msg.plane.getSqwk(),
          msg.plane.getCallsign().toString(),
          msg.message,
          sender.getName());
      return ret;
  }
}
