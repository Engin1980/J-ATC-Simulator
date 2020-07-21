package eng.jAtcSim.newLib.textProcessing.implemented.debugPlaneFormatter;

import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.*;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.textProcessing.contextLocal.Context;

class FormatMethodImplementations {
  private final String[] greetings = new String[]{"Good day", "Hello", "Hi"};

  public String format(AfterAltitudeCommand cmd) {
    return "when passing " + Format.Altitude.toAlfOrFLLong(cmd.getAltitude()) + " ";
  }

  public String format(AfterNavaidCommand cmd) {
    return "after " + cmd.getNavaidName() + " ";
  }

  public String format(AfterHeadingCommand cmd) {
    return "after hdg " + cmd.getHeading() + " ";
  }

  public String format(AfterRadialCommand cmd) {
    return "after radial " + cmd.getNavaidName() + "/" + cmd.getRadial() + " ";
  }

  public String format(AfterDistanceCommand cmd) {
    return "after distance " + cmd.getNavaidName() + "/" + cmd.getDistance() + " ";
  }

  public String format(AfterSpeedCommand cmd) {
    return "at speed " + cmd.getSpeed() + "kts ";
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
    sb.append(Format.Altitude.toAlfOrFLLong(cmd.getAltitudeInFt()));
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
      sb.append(cmd.getRestriction().value);
      sb.append(" kts");
      switch (cmd.getRestriction().direction) {
        case above:
          sb.append(" or more");
          break;
        case below:
          sb.append(" or less");
          break;
      }
      return sb.toString();
    }
  }

  public String format(AltitudeRestrictionCommand cmd) {
    if (cmd.getRestriction() == null) {
      return "cancel altitude restrictions";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("altitude ");
      sb.append(
          Format.Altitude.toAlfOrFLLong(cmd.getRestriction().value));
      switch (cmd.getRestriction().direction) {
        case above:
          sb.append(" or more");
          break;
        case below:
          sb.append(" or less");
          break;
      }
      return sb.toString();
    }
  }

  public String format(ClearedForTakeoffCommand cmd) {
    return "cleared for takeoff " + cmd.getRunwayThresholdName();
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
    String ret = String.format("Contact %s at %.3f", cmd.getAtc().getName(), cmd.getAtc().getFrequency());
    return ret;
  }

  public String format(HoldCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("hold over ");
    sb.append(cmd.getNavaidName());
    if (cmd.isPublished()) {
      sb.append(" as published");
    } else {
      sb.append(" inbound ");
      sb.append(Headings.format(cmd.getInboundRadial()));
      sb.append(cmd.getTurn() == LeftRight.left ? " left turns " : " right turns ");
    }
    return sb.toString();
  }

  public String format(ProceedDirectCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("proceed direct ");
    sb.append(cmd.getNavaidName());
    return sb.toString();
  }

  public String format(ShortcutCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("shortcut to ");
    sb.append(cmd.getNavaidName());
    return sb.toString();
  }

  public String format(ThenCommand cmd) {
    return "then ";
  }

  public String format(ToNavaidCommand cmd) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public String format(PassingClearanceLimitNotification cmd) {
    return "Approaching to clearance limit";
  }

  public String format(RequestRadarContactNotification cmd) {
    return "Unable to follow ordered fromAtc, please confirm our radar contact first";
  }

  public String format(EmergencyNotification cmd) {
    return "Pan-Pan-Pan, we have an emergency situation, request landing immediately";
  }

  public String format(ClearedToRouteCommand cmd) {
    String type;
    switch (cmd.getRouteType()) {
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
    return "Clear to proceed " + cmd.getRouteName() + " " + type;
  }

  public String format(UnableToEnterApproachFromDifficultPosition cmd) {
    return cmd.getReason();
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
    int greetingIndex = Context.getShared().getRnd().nextInt(greetings.length);
    StringBuilder sb = new StringBuilder();
    sb
        .append(greetings[greetingIndex])
        .append(", ");
    if (cmd.isEmergency())
      sb.append("mayday ");

    sb.append(cmd.getCallsign().toString())
        .append(" with you at ")
        .append(Format.Altitude.toAlfOrFLLong(cmd.getAltitude()));
    return sb.toString();
  }

  public String format(EstablishedOnApproachNotification speech) {
    return "Short final " + speech.getThresholdName();
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

  public String format(ReportDivertTimeCommand cmd) {
    String ret = ""; // this is empty as this is used as a confirmation.
    return ret;
  }

  public String format(DivertCommand cmd) {
    String ret = "divert now";
    return ret;
  }

  public String format(DivertingNotification cmd) {
    String ret = "we are diverting via " + cmd.getExitNavaidName();
    return ret;
  }
}
