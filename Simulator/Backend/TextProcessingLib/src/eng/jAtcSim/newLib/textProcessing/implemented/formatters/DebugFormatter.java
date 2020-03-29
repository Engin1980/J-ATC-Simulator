package eng.jAtcSim.newLib.textProcessing.implemented.formatters;

import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.SharedInstanceProvider;
import eng.jAtcSim.newLib.speeches.airplane2atc.*;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.ShortCutToFixNotOnRouteNotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.speeches.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class DebugFormatter extends Formatter {

  private final String[] greetings = new String[]{"Good day", "Hello", "Hi"};

  public String format(ReportDivertTimeNotification cmd) {
    String ret = ""; // this is empty as this is used as a confirmation.
    return ret;
  }

  @Override
  protected String formatAfterAltitudeCommand(AfterAltitudeCommand cmd) {
    return "when passing " + Format.Altitude.toAlfOrFLLong(cmd.getAltitude());
  }

  @Override
  protected String formatAfterDistanceCommand(AfterDistanceCommand cmd) {
    if (cmd.getDistance() != 0) {
      return sf("after %s %d miles from %s",
          cmd.getPosition(),
          cmd.getDistance(),
          cmd.getNavaidName());
    } else
      return "after " + cmd.getNavaidName();
  }

  @Override
  protected String formatAfterHeadingCommand(AfterHeadingCommand cmd) {
    return "after hdg " + cmd.getHeading();
  }

  @Override
  protected String formatAfterRadialCommand(AfterRadialCommand cmd) {
    return "after radial " + cmd.getNavaidName() + "/" + cmd.getRadial();
  }

  @Override
  protected String formatAfterSpeedCommand(AfterSpeedCommand cmd) {
    return "at speed " + cmd.getSpeed() + "kts ";
  }

  @Override
  protected String formatAltitudeRestrictionCommand(AltitudeRestrictionCommand cmd) {
    if (cmd.getRestriction() == null) {
      return "cancel altitude restrictions";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("altitude ");
      sb.append(
          Format.Altitude.toFLShort(cmd.getRestriction().value));
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

  @Override
  protected String formatChangeAltitudeCommand(ChangeAltitudeCommand cmd) {
    StringBuilder sb = new StringBuilder();
    switch (cmd.getDirection()) {
      case any:
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

  @Override
  protected String formatChangeHeadingCommand(ChangeHeadingCommand cmd) {
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

  @Override
  protected String formatChangeSpeedCommand(ChangeSpeedCommand cmd) {
    if (cmd.isResumeOwnSpeed()) {
      return "resume own speed";
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("speed ");
      sb.append(cmd.getSpeedInKts());
      sb.append(" kts");
      switch (cmd.getDirection()) {
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

  @Override
  protected String formatClearedForTakeoffCommand(ClearedForTakeoffCommand cmd) {
    return "cleared for takeoff " + cmd.getRunwayThresholdName();
  }

  @Override
  protected String formatClearedToApproachCommand(ClearedToApproachCommand cmd) {
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

  @Override
  protected String formatClearedToRouteCommand(ClearedToRouteCommand cmd) {
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

  @Override
  protected String formatContactCommand(ContactCommand cmd) {
    String ret = String.format("Contact %s at %.3f", cmd.getAtcName(), cmd.getAtcFrequency());
    return ret;
  }

  @Override
  protected String formatDivertCommand(DivertCommand cmd) {
    String ret = "divert now";
    return ret;
  }

  @Override
  protected String formatDivertTimeNotification(DivertTimeNotification cmd) {
    String sb = "we will need to divert in " +
        cmd.getMinutesToDivert() +
        " minutes";
    return sb;
  }

  @Override
  protected String formatDivertingNotification(DivertingNotification cmd) {
    String ret = "we are diverting via " + cmd.getExitNavaidName();
    return ret;
  }

  @Override
  protected String formatEmergencyNotification(EmergencyNotification cmd) {
    return "Pan-Pan-Pan, we have an emergency situation, request landing at immediately";
  }

  @Override
  protected String formatEstablishedOnApproachNotification(EstablishedOnApproachNotification cmd) {
    return "Short final " + cmd.getThresholdName();
  }

  @Override
  protected String formatGoAroundCommand(GoAroundCommand cmd) {
    String ret = "go around";
    return ret;
  }

  @Override
  protected String formatGoingAroundNotification(GoingAroundNotification cmd) {
    String ret = "Missed approach. " + (cmd.getReason() == null ? "" : cmd.getReason());
    return ret;
  }

  @Override
  protected String formatGoodDayNotification(GoodDayNotification cmd) {
    double d = SharedInstanceProvider.getRnd().nextDouble();
    d = d * greetings.length;
    StringBuilder sb = new StringBuilder();
    sb
        .append(greetings[(int) d])
        .append(", ");
    if (cmd.isEmergency())
      sb.append("mayday ");

    sb.append(cmd.getCallsign().toString())
        .append(" with you at ")
        .append(Format.Altitude.toAlfOrFLLong((int) cmd.getAltitude()));
    return sb.toString();
  }

  @Override
  protected String formatHighOrderedSpeedForApproach(HighOrderedSpeedForApproach cmd) {
    return
        String.format(
            "we have ordered to high speed %d for approach, but we need %d at most",
            cmd.getOrderedSpeed(),
            cmd.getRequiredSpeed()
        );
  }

  @Override
  protected String formatHoldCommand(HoldCommand cmd) {
    StringBuilder sb = new StringBuilder();
    sb.append("hold over ");
    sb.append(cmd.getNavaidName());
    if (cmd.isPublished()) {
      sb.append(" as published");
    } else {
      sb.append(" inbound ");
      sb.append(Headings.format(cmd.getInboundRadial()));
      sb.append(cmd.isLeftTurn() ? " left turns " : " right turns ");
    }
    return sb.toString();
  }

  @Override
  protected String formatIllegalThenCommandRejection(IllegalThenCommandRejection cmd) {
    return null;
  }

  @Override
  protected String formatPassingClearanceLimitNotification(PassingClearanceLimitNotification cmd) {
    return "Approaching to clearance limit";
  }

  @Override
  protected String formatProceedDirectCommand(ProceedDirectCommand cmd) {
    String sb = "proceed direct " + cmd.getNavaidName();
    return sb;
  }

  @Override
  protected String formatRadarContactConfirmationNotification(RadarContactConfirmationNotification cmd) {
    return "radar contact";
  }

  @Override
  protected String formatReportDivertTimeNotification(ReportDivertTimeNotification cmd) {
    String ret = ""; // this is empty as this is used as a confirmation.
    return ret;
  }

  @Override
  protected String formatRequestRadarContactNotification(RequestRadarContactNotification cmd) {
    return "Unable to follow ordered fromAtc, please confirm our radar contact first";
  }

  @Override
  protected String formatShortCutToFixNotOnRouteNotification(ShortCutToFixNotOnRouteNotification cmd) {
    return null;
  }

  @Override
  protected String formatShortcutCommand(ShortcutCommand cmd) {
    String sb = "shortcut to " + cmd.getNavaidName();
    return sb;
  }

  @Override
  protected String formatThenCommand(ThenCommand cmd) {
    return "then ";
  }

  @Override
  protected String formatUnableToEnterApproachFromDifficultPosition(UnableToEnterApproachFromDifficultPosition cmd) {
    return cmd.getReason();
  }

  @Override
  protected String formatPlaneSwitchMessage(PlaneSwitchMessage speech) {
//    @Override
//    public String format(Atc sender, PlaneSwitchMessage msg) {
//      String ret = String.format(
//          "%s {%s} %s",
//          msg.plane.getSqwk(),
//          msg.plane.getFlightModule().getCallsign().toString(),
//          msg.getMessageText(),
//          sender.getName());
//      return ret;
//    }
    throw new UnsupportedOperationException("TODO");
  }
}
