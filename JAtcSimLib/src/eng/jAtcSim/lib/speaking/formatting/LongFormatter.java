package eng.jAtcSim.lib.speaking.formatting;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.PlaneSwitchMessage;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.EstablishedOnApproachNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.RequestRadarContactNotification;

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
    switch (cmd.getApproach().getType()){
      case ILS_I:
        sb.append("ILS");
        break;
      case ILS_II:
        sb.append("ILS category II");
        break;
      case ILS_III:
        sb.append("ILS category III");
        break;
      case NDB:
        sb.append("NDB");
        break;
      case GNSS:
        sb.append("GNSS");
        break;
      case VORDME:
        sb.append("VOR-DME");
        break;
      case Visual:
        sb.append("visual");
        break;
    }
    sb.append(" approach at runway ");
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
      sb.append(" inbound ");
      sb.append(Headings.format(cmd.getInboundRadial()));
      sb.append(cmd.isLeftTurn() ? " left turns " : " right turns ");
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

  public String format(EstablishedOnApproachNotification speech){
    return "Established, short final.";
  }

  public String format(GoingAroundNotification cmd){
    return "Missed approach. " + cmd.getReason();
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
