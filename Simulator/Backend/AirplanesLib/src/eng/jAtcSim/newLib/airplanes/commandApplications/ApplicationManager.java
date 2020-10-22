package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterCommand;

public class ApplicationManager {

  private static IMap<Class<? extends ICommand>, CommandApplication> cmdApps;
  private static IMap<Class<? extends IForPlaneSpeech>, NonCommandApplication> nonCmdApps;

  public static ConfirmationResult confirm(Airplane plane, ISpeech speech, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret;

    if (speech instanceof AfterCommand) {
      ret = new ConfirmationResult();
      ret.confirmation = new PlaneConfirmation((ICommand) speech);
    } else if (speech instanceof ICommand) {
      ICommand command = (ICommand) speech;
      CommandApplication ca = cmdApps.tryGet(command.getClass());
      assert ca != null : "Unknown application. Probably not added into cmdApps list?";
      assert plane != null;
      assert command != null;
      ret = ca.confirm(plane, command, checkStateSanity, checkCommandSanity);
    } else if (speech instanceof IForPlaneSpeech) {
      IForPlaneSpeech notification = (IForPlaneSpeech) speech;
      NonCommandApplication nca = nonCmdApps.get(notification.getClass());
      assert nca != null;
      ret = nca.confirm(plane, notification);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;

  }

  public static ApplicationResult apply(Airplane plane, ISpeech speech) {
    ApplicationResult ret;

    if (speech instanceof ICommand) {
      ICommand command = (ICommand) speech;
      ret = cmdApps.get(command.getClass()).apply(plane, command, false);
    } else if (speech instanceof IForPlaneSpeech) {
      IForPlaneSpeech notification = (IForPlaneSpeech) speech;
      ret = nonCmdApps.get(notification.getClass()).apply(plane, notification);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;
  }

  static {
    cmdApps = new EMap<>();

    cmdApps.set(ChangeAltitudeCommand.class, new ChangeAltitudeApplication());
    cmdApps.set(ChangeHeadingCommand.class, new ChangeHeadingApplication());
    cmdApps.set(ChangeSpeedCommand.class, new ChangeSpeedApplication());
    cmdApps.set(ClearedForTakeoffCommand.class, new ClearedForTakeOffCommandApplication());
    cmdApps.set(ClearedToApproachCommand.class, new ClearedToApproachApplication());
    cmdApps.set(HoldCommand.class, new HoldCommandApplication());
    cmdApps.set(ProceedDirectCommand.class, new ProceedDirectApplication());
    cmdApps.set(ShortcutCommand.class, new ShortcutCommandApplication());
    cmdApps.set(ContactCommand.class, new ContactCommandApplication());
    cmdApps.set(GoAroundCommand.class, new GoAroundCommandApplication());
    cmdApps.set(ReportDivertTimeCommand.class, new ReportDivertTimeCommandApplication());
    cmdApps.set(DivertCommand.class, new DivertCommandApplication());
    cmdApps.set(AltitudeRestrictionCommand.class, new AltitudeRestrictionApplication());
    cmdApps.set(ClearedToRouteCommand.class, new ClearedToRouteApplication());
    cmdApps.set(TaxiToHoldingPointCommand.class, new TaxiToHoldingPointApplication());

    nonCmdApps = new EMap<>();
    nonCmdApps.set(RadarContactConfirmationNotification.class, new RadarContactConfirmationNotificationApplication());
  }
}
