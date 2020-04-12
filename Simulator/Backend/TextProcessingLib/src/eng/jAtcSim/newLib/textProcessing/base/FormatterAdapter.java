package eng.jAtcSim.newLib.textProcessing.base;

import eng.jAtcSim.newLib.speeches.airplane2atc.*;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.ShortCutToFixNotOnRouteNotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.speeches.atc2atc.PlaneSwitchMessage;

public class FormatterAdapter extends Formatter {
  @Override
  protected String formatAfterAltitudeCommand(AfterAltitudeCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatAfterDistanceCommand(AfterDistanceCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatAfterHeadingCommand(AfterHeadingCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatAfterRadialCommand(AfterRadialCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatAfterSpeedCommand(AfterSpeedCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatAltitudeRestrictionCommand(AltitudeRestrictionCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatChangeAltitudeCommand(ChangeAltitudeCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatChangeHeadingCommand(ChangeHeadingCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatChangeSpeedCommand(ChangeSpeedCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatClearedForTakeoffCommand(ClearedForTakeoffCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatClearedToApproachCommand(ClearedToApproachCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatClearedToRouteCommand(ClearedToRouteCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatContactCommand(ContactCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatDivertCommand(DivertCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatDivertTimeNotification(DivertTimeNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatDivertingNotification(DivertingNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatEmergencyNotification(EmergencyNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatEstablishedOnApproachNotification(EstablishedOnApproachNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatGoAroundCommand(GoAroundCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatGoingAroundNotification(GoingAroundNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatGoodDayNotification(GoodDayNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatHighOrderedSpeedForApproach(HighOrderedSpeedForApproach command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatHoldCommand(HoldCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatIllegalThenCommandRejection(IllegalThenCommandRejection command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatPassingClearanceLimitNotification(PassingClearanceLimitNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatProceedDirectCommand(ProceedDirectCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatRadarContactConfirmationNotification(RadarContactConfirmationNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatReportDivertTimeNotification(ReportDivertTimeCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatRequestRadarContactNotification(RequestRadarContactNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatShortCutToFixNotOnRouteNotification(ShortCutToFixNotOnRouteNotification command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatShortcutCommand(ShortcutCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatThenCommand(ThenCommand command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatUnableToEnterApproachFromDifficultPosition(UnableToEnterApproachFromDifficultPosition command) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }

  @Override
  protected String formatPlaneSwitchMessage(PlaneSwitchMessage speech) {
    throw new UnsupportedOperationException("This operation is not implemented.");
  }
}
