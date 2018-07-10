package eng.jAtcSim.lib.speaking.formatting;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneDataFormatter;
import eng.jAtcSim.lib.airplanes.commandApplications.ClearedToRouteApplication;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.DataFormat;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.*;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

public class LongFormatter extends Formatter {

  private static Formatter instance = null;
  private final String[] greetings = new String[]{"Good day", "Hello", "Hi"};

  public static Formatter getInstance() {
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

  public String format(AfterHeadingCommand cmd) {
    return "after hdg " + cmd.getHeading() + " ";
  }

  public String format(AfterRadialCommand cmd) {
    return "after radial " + cmd.getNavaid().getName() + "/" + cmd.getRadial() + " ";
  }

  public String format(AfterDistanceCommand cmd) {
    return "after distance " + cmd.getNavaid().getName() + "/" + cmd.getDistanceInNm() + " ";
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
        throw new UnsupportedOperationException();
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
          throw new UnsupportedOperationException();
      }
      sb.append(Headings.format(cmd.getHeading()));
    }
    return sb.toString();
  }

  public String format(GoAroundCommand cmd) {
    String ret = "go around";
    return ret;
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

  public String format(SetAltitudeRestriction cmd) {
    if (cmd.getRestriction() == null) {
      return "cancel altitude restrictions";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("altitude ");
      sb.append(
          AirplaneDataFormatter.formatAltitudeShort(
              cmd.getRestriction().value, false));
      switch (cmd.getRestriction().direction) {
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
    switch (cmd.getType()) {
      case ils_I:
        sb.append("ILS");
        break;
      case ils_II:
        sb.append("ILS category II");
        break;
      case ils_III:
        sb.append("ILS category III");
        break;
      case ndb:
        sb.append("NDB");
        break;
      case gnss:
        sb.append("GNSS");
        break;
      case vor:
        sb.append("VOR-DME");
        break;
      case visual:
        sb.append("visual");
        break;
    }
    sb.append(" approach at runway ");
    sb.append(cmd.getThresholdName());
    return sb.toString();
  }

  public String format(ContactCommand cmd) {
    Atc atc = Acc.atc(cmd.getAtcType());
    String ret = String.format("Contact %s at %.3f", atc.getName(), atc.getFrequency());
    return ret;
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

  public String format(PassingClearanceLimitNotification cmd) {
    return "Approaching to clearance limit, will continue on current heading";
  }

  public String format(Confirmation cmd) {
    return super.format(cmd.getOrigin());
  }

  public String format(Rejection cmd) {
    return "Unable " + super.format(cmd.getOrigin()) + ". " + cmd.getReason();
  }

  public String format(RequestRadarContactNotification cmd) {
    return "Unable to follow ordered fromAtc, please confirm our radar contact first";
  }

  public String format(EmergencyNotification cmd){
    return "Pan-Pan-Pan, we have an emergency situation, request landing at " + Acc.airport().getIcao() + " immediately";
  }

  public String format(ClearedToRouteCommand cmd){
    String type;
    switch (cmd.getRoute().getType()){
      case sid:
        type = "departure";
        break;
      case star:
        type = "arrival";
        break;
      case transition:
        type = "transition";
        break;
      case vectoring:
        type = "via vectoring";
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return "Clear to proceed " + cmd.getRoute().getName() + " " + type;
  }

  public String format(UnableToEnterApproachFromDifficultPosition cmd) {
    return cmd.reason;
  }

  public String format(HighOrderedSpeedForApproach cmd) {
    return
        String.format(
            "we have ordered to high speed %d for approach, but we need %d at most",
            cmd.getOrderedSpeed(),
            cmd.getRequiredSpeed()
        );
  }

  public String format(RadarContactConfirmationNotification cmd) {
    return "radar contact";
  }

  public String format(GoodDayNotification cmd) {
    double d = Acc.rnd().nextDouble();
    d = d * greetings.length;
    StringBuilder sb = new StringBuilder();
    sb
        .append(greetings[(int) d])
        .append(", ");
    if (cmd.isEmergency())
      sb.append("mayday ");

    sb.append(cmd.getCallsign().toString())
        .append(" with you at ")
        .append(DataFormat.Altitude.toStandardAltitudeOrFL(cmd.getAltitude(), Acc.airport().getTransitionAltitude()));
    return sb.toString();
  }

  public String format(EstablishedOnApproachNotification speech) {
    return "Short final " + speech.getThreshold().getName();
  }

  public String format(DivertTimeNotification speech) {
    StringBuilder sb = new StringBuilder();
    sb.append("we will need to divert in ");
    sb.append(speech.getMinutesToDivert());
    sb.append(" minutes");
    return sb.toString();
  }

  public String format(GoingAroundNotification cmd) {
    String ret = "Missed approach. " + (cmd.getReason() == null ? "" : cmd.getReason());
    return ret;
  }

  public String format(ReportDivertTime cmd) {
    String ret = ""; // this is empty as this is used as a confirmation.
    return ret;
  }

  public String format(DivertCommand cmd) {
    String ret = "divert now";
    return ret;
  }

  public String format(DivertingNotification cmd) {
    String ret = "we are diverting via " + cmd.getExitNavaid().getName();
    return ret;
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
