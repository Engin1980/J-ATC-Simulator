package eng.jAtcSim.newLib.textProcessing.base;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.speeches.Confirmation;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.*;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.ShortCutToFixNotOnRouteRejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.speeches.atc2atc.PlaneSwitchMessage;

public abstract class Formatter {

  public String format(ISpeech speech) {
    EAssert.Argument.isNotNull(speech);
    String ret;


    if (speech instanceof Confirmation) {
      ret = formatConfirmation((Confirmation) speech);
    } else if (speech instanceof Rejection) {
      ret = formatRejection((Rejection) speech);
    } else if (speech instanceof AltitudeRestrictionCommand)
      ret = formatAltitudeRestrictionCommand((AltitudeRestrictionCommand) speech);
    else if (speech instanceof ChangeAltitudeCommand) ret = formatChangeAltitudeCommand((ChangeAltitudeCommand) speech);
    else if (speech instanceof ChangeHeadingCommand) ret = formatChangeHeadingCommand((ChangeHeadingCommand) speech);
    else if (speech instanceof ChangeSpeedCommand) ret = formatChangeSpeedCommand((ChangeSpeedCommand) speech);
    else if (speech instanceof ClearedForTakeoffCommand)
      ret = formatClearedForTakeoffCommand((ClearedForTakeoffCommand) speech);
    else if (speech instanceof ClearedToApproachCommand)
      ret = formatClearedToApproachCommand((ClearedToApproachCommand) speech);
    else if (speech instanceof ClearedToRouteCommand) ret = formatClearedToRouteCommand((ClearedToRouteCommand) speech);
    else if (speech instanceof ContactCommand) ret = formatContactCommand((ContactCommand) speech);
    else if (speech instanceof DivertCommand) ret = formatDivertCommand((DivertCommand) speech);
    else if (speech instanceof GoAroundCommand) ret = formatGoAroundCommand((GoAroundCommand) speech);
    else if (speech instanceof HoldCommand) ret = formatHoldCommand((HoldCommand) speech);
    else if (speech instanceof ProceedDirectCommand) ret = formatProceedDirectCommand((ProceedDirectCommand) speech);
    else if (speech instanceof RadarContactConfirmationNotification)
      ret = formatRadarContactConfirmationNotification((RadarContactConfirmationNotification) speech);
    else if (speech instanceof ReportDivertTimeCommand)
      ret = formatReportDivertTimeNotification((ReportDivertTimeCommand) speech);
    else if (speech instanceof ShortcutCommand) ret = formatShortcutCommand((ShortcutCommand) speech);
    else if (speech instanceof ThenCommand) ret = formatThenCommand((ThenCommand) speech);
    else if (speech instanceof AfterAltitudeCommand) ret = formatAfterAltitudeCommand((AfterAltitudeCommand) speech);
    else if (speech instanceof AfterDistanceCommand) ret = formatAfterDistanceCommand((AfterDistanceCommand) speech);
    else if (speech instanceof AfterHeadingCommand) ret = formatAfterHeadingCommand((AfterHeadingCommand) speech);
    else if (speech instanceof AfterRadialCommand) ret = formatAfterRadialCommand((AfterRadialCommand) speech);
    else if (speech instanceof AfterSpeedCommand) ret = formatAfterSpeedCommand((AfterSpeedCommand) speech);
    else if (speech instanceof DivertingNotification) ret = formatDivertingNotification((DivertingNotification) speech);
    else if (speech instanceof DivertTimeNotification)
      ret = formatDivertTimeNotification((DivertTimeNotification) speech);
    else if (speech instanceof EmergencyNotification) ret = formatEmergencyNotification((EmergencyNotification) speech);
    else if (speech instanceof EstablishedOnApproachNotification)
      ret = formatEstablishedOnApproachNotification((EstablishedOnApproachNotification) speech);
    else if (speech instanceof GoingAroundNotification)
      ret = formatGoingAroundNotification((GoingAroundNotification) speech);
    else if (speech instanceof GoodDayNotification) ret = formatGoodDayNotification((GoodDayNotification) speech);
    else if (speech instanceof HighOrderedSpeedForApproach)
      ret = formatHighOrderedSpeedForApproach((HighOrderedSpeedForApproach) speech);
    else if (speech instanceof PassingClearanceLimitNotification)
      ret = formatPassingClearanceLimitNotification((PassingClearanceLimitNotification) speech);
    else if (speech instanceof RequestRadarContactNotification)
      ret = formatRequestRadarContactNotification((RequestRadarContactNotification) speech);
    else if (speech instanceof IllegalThenCommandRejection)
      ret = formatIllegalThenCommandRejection((IllegalThenCommandRejection) speech);
    else if (speech instanceof ShortCutToFixNotOnRouteRejection)
      ret = formatShortCutToFixNotOnRouteNotification((ShortCutToFixNotOnRouteRejection) speech);
    else if (speech instanceof UnableToEnterApproachFromDifficultPosition)
      ret = formatUnableToEnterApproachFromDifficultPosition((UnableToEnterApproachFromDifficultPosition) speech);
    else {
      throw new ApplicationException("Unable to format speech of type " + speech.getClass().getName() + ". Unsupported type.");
    }

    return ret;
  }

  protected abstract String formatAfterAltitudeCommand(AfterAltitudeCommand command);

  protected abstract String formatAfterDistanceCommand(AfterDistanceCommand command);

  protected abstract String formatAfterHeadingCommand(AfterHeadingCommand command);

  protected abstract String formatAfterRadialCommand(AfterRadialCommand command);

  protected abstract String formatAfterSpeedCommand(AfterSpeedCommand command);

  protected abstract String formatAltitudeRestrictionCommand(AltitudeRestrictionCommand command);

  protected abstract String formatChangeAltitudeCommand(ChangeAltitudeCommand command);

  protected abstract String formatChangeHeadingCommand(ChangeHeadingCommand command);

  protected abstract String formatChangeSpeedCommand(ChangeSpeedCommand command);

  protected abstract String formatClearedForTakeoffCommand(ClearedForTakeoffCommand command);

  protected abstract String formatClearedToApproachCommand(ClearedToApproachCommand command);

  protected abstract String formatClearedToRouteCommand(ClearedToRouteCommand command);

  protected String formatConfirmation(Confirmation cmd) {
    return this.format(cmd.getOrigin());
  }

  protected abstract String formatContactCommand(ContactCommand command);

  protected abstract String formatDivertCommand(DivertCommand command);

  protected abstract String formatDivertTimeNotification(DivertTimeNotification command);

  protected abstract String formatDivertingNotification(DivertingNotification command);

  protected abstract String formatEmergencyNotification(EmergencyNotification command);

  protected abstract String formatEstablishedOnApproachNotification(EstablishedOnApproachNotification command);

  protected abstract String formatGoAroundCommand(GoAroundCommand command);

  protected abstract String formatGoingAroundNotification(GoingAroundNotification command);

  protected abstract String formatGoodDayNotification(GoodDayNotification command);

  protected abstract String formatHighOrderedSpeedForApproach(HighOrderedSpeedForApproach command);

  protected abstract String formatHoldCommand(HoldCommand command);

  protected abstract String formatIllegalThenCommandRejection(IllegalThenCommandRejection command);

  protected abstract String formatPassingClearanceLimitNotification(PassingClearanceLimitNotification command);

  protected abstract String formatProceedDirectCommand(ProceedDirectCommand command);

  protected abstract String formatRadarContactConfirmationNotification(RadarContactConfirmationNotification command);

  protected String formatRejection(Rejection cmd) {
    return "Unable " + this.format(cmd.getOrigin()) + ". " + cmd.getReason();
  }

  protected abstract String formatReportDivertTimeNotification(ReportDivertTimeCommand command);

  protected abstract String formatRequestRadarContactNotification(RequestRadarContactNotification command);

  protected abstract String formatShortCutToFixNotOnRouteNotification(ShortCutToFixNotOnRouteRejection command);

  protected abstract String formatShortcutCommand(ShortcutCommand command);

  protected abstract String formatThenCommand(ThenCommand command);

  protected abstract String formatUnableToEnterApproachFromDifficultPosition(UnableToEnterApproachFromDifficultPosition command);

  protected abstract String formatPlaneSwitchMessage(PlaneSwitchMessage speech);
}
