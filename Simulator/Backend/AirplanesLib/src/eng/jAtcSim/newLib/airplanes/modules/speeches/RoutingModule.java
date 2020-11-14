package eng.jAtcSim.newLib.airplanes.modules.speeches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.ConversionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.newLib.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.newLib.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.other.CommandQueueRecorder;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.RequestRadarContactNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.*;

public class RoutingModule extends eng.jAtcSim.newLib.airplanes.modules.Module {

  private enum CommandSource {
    shortcutSkipped,
    atc,
    route,
    extension
  }

  private final static AirplaneState[] FLYING_ROUTE_STATES = {
          AirplaneState.arrivingHigh,
          AirplaneState.arrivingLow,
          AirplaneState.arrivingCloseFaf,
          AirplaneState.flyingIaf2Faf,
          AirplaneState.departingHigh,
          AirplaneState.departingLow,
  };
  private final AfterCommandList afterCommands = new AfterCommandList();
  private String assignedDARouteName = null;
  private Navaid entryExitPoint;
  private final DelayedList<ICommand> queue = new DelayedList<>(2, 7); //Min/max item delay
  private ActiveRunwayThreshold runwayThreshold;
  private CommandQueueRecorder cqr;

  public RoutingModule(Airplane plane, Navaid entryExitPoint) {
    super(plane);
    this.entryExitPoint = entryExitPoint;
  }

  public void applyShortcut(Navaid navaid) {
    SpeechList<ICommand> skippedCommands = this.afterCommands.doShortcutTo(navaid);
    this.processSpeeches(skippedCommands, CommandSource.shortcutSkipped);
  }

  @Override
  public void elapseSecond() {
    obtainNewSpeeches();
    processNewSpeeches();
    processAfterSpeeches();

    logAfterCommands();
  }

  public String getAssignedDARouteName() {
    return assignedDARouteName;
  }

  public void setAssignedDARouteName(String assignedDARouteName) {
    EAssert.Argument.isNotNull(assignedDARouteName, "assignedDARouteName");
    this.assignedDARouteName = assignedDARouteName;
  }

  public Navaid getEntryExitPoint() {
    return entryExitPoint;
  }

  public void setEntryExitPoint(Navaid entryExitNavaid) {
    EAssert.Argument.isNotNull(entryExitNavaid, "entryExitNavaid");
    this.entryExitPoint = entryExitNavaid;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  public void setRunwayThreshold(ActiveRunwayThreshold activeRunwayThreshold) {
    EAssert.Argument.isNotNull(activeRunwayThreshold, "activeRunwayThreshold");
    this.runwayThreshold = activeRunwayThreshold;
  }

  public boolean hasLateralDirectionAfterCoordinate(Coordinate coordinate) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    return afterCommands.hasLateralDirectionAfterCoordinate(coordinate);
  }

  public boolean isGoingToFlightOverNavaid(Navaid navaid) {
    return afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
  }

  public void setCqr(CommandQueueRecorder commandQueueRecorder) {
    this.cqr = commandQueueRecorder;
  }

  public void setRouting(IReadOnlyList<ICommand> routeCommands) {
    EAssert.Argument.isNotNull(routeCommands, "routeCommands");

    SpeechList<ICommand> cmds = tryExpandThenCommands(routeCommands);
    EAssert.isNotNull(cmds, "Route commands should not be null.");

    afterCommands.clearAll();

    AfterCommand afterCommand = new AfterImmediatelyCommand();
    while (!cmds.isEmpty()) {
      ICommand cmd = cmds.get(0);
      cmds.removeAt(0);
      if (cmd instanceof AfterCommand)
        afterCommand = (AfterCommand) cmd;
      else
        afterCommands.addRoute(afterCommand, cmd);
    }
  }

  private void logAfterCommands() {
    String s = afterCommands.toLogString();
    this.cqr.log(s);
  }

  private void addNewSpeeches(SpeechList<IForPlaneSpeech> speeches) {
    this.queue.newRandomDelay();
    SpeechList<ICommand> tmp = new SpeechList<>(speeches.whereItemClassIs(ICommand.class, true));
    tryExpandThenCommands(tmp);
    for (ICommand speech : tmp) {
      this.queue.add(speech);
    }
  }

  private void affectAfterCommands(ICommand cmd, CommandSource cs) {
    final Class[] lateralCommands = new Class[]{ProceedDirectCommand.class, ChangeHeadingCommand.class, HoldCommand.class};
    switch (cs) {
      case shortcutSkipped:
        // nothing
        break;
      case route:
        // nothing
        break;
      case atc:

        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 2
          this.afterCommands.clearRoute();
          this.afterCommands.clearExtensionsByConsequent(lateralCommands);
        } else if (cmd instanceof ShortcutCommand) {
          // rule 3
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof ChangeAltitudeCommand) {
          // rule 4
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), rdr.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 5
            this.afterCommands.clearChangeSpeedClass(
                    tmp.getRestriction().value, rdr.isArrival(), AfterCommandList.Type.route);
            this.afterCommands.clearChangeSpeedClass(
                    tmp.getRestriction().value, rdr.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 6
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                    null, rdr.isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 12
          this.afterCommands.clearAll();
        }
        break;
      case extension:
        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 7
          this.afterCommands.clearRoute();
        } else if (cmd instanceof ShortcutCommand) {
          // rule 8
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof AfterAltitudeCommand) {
          // rule 9
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), rdr.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 10
            this.afterCommands.clearChangeSpeedClass(tmp.getRestriction().value, rdr.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 11
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                    null, rdr.isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 13
          this.afterCommands.clearAll();
        }
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void obtainNewSpeeches() {
    IList<Message> msgs = Context.getMessaging().getMessenger().getMessagesByListener(
            Participant.createAirplane(rdr.getCallsign()), true);

    // only responds to messages from tuned atc
    msgs = msgs.where(q -> q.getSource().equals(Participant.createAtc(rdr.getAtc().getTunedAtc())));

    // extract contents
    IList<IMessageContent> contents = msgs.select(q -> q.getContent());

    SpeechList<IForPlaneSpeech> speechList = convertMessagesToSpeechList(contents);

    processRadarContactNotificationIfExists(speechList);

    this.addNewSpeeches(speechList);
  }

  private void processRadarContactNotificationIfExists(SpeechList<IForPlaneSpeech> speechList) {
    IList<RadarContactConfirmationNotification> radarContacts = speechList
            .whereItemClassIs(RadarContactConfirmationNotification.class, false);
    RadarContactConfirmationNotification radarContact = radarContacts.tryGetLast();

    if (radarContact != null) {
      processNormalSpeech(radarContact, CommandSource.atc);
      speechList.removeMany(radarContacts);
    }
  }

  private SpeechList<IForPlaneSpeech> convertMessagesToSpeechList(IList<IMessageContent> contents) {
    SpeechList<IForPlaneSpeech> ret = new SpeechList<>();

    for (IMessageContent content : contents) {
      if (content instanceof SpeechList)
        ret.addMany((SpeechList<IForPlaneSpeech>) content);
      else if (content instanceof IForPlaneSpeech)
        ret.add((IForPlaneSpeech) content);
      else
        throw new EApplicationException("Unexpected type of cmd: " + content.getClass().getName());
    }

    return ret;
  }

  private void processAfterSpeechWithConsequents(IList<? extends ICommand> queue, CommandSource cs) {

    AirplaneState[] unableProcessAfterCommandsStates = {
            AirplaneState.flyingIaf2Faf,
            AirplaneState.approachEnter,
            AirplaneState.approachDescend,
            AirplaneState.longFinal,
            AirplaneState.shortFinal,
            AirplaneState.landed,
            AirplaneState.takeOffRoll,
            AirplaneState.takeOffGoAround
    };

    AfterCommand af = (AfterCommand) queue.get(0);
    queue.removeAt(0);

    if (cs == CommandSource.atc && rdr.getState().is(unableProcessAfterCommandsStates)) {
      IFromPlaneSpeech rej = new PlaneRejection(af, "Unable to process after-command during approach/take-off.");
      say(rej);
      return;
    }

    ConfirmationResult cres;
    boolean sayConfirmations = cs == CommandSource.atc;

    cres = ApplicationManager.confirm(plane, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      ICommand sp = queue.get(0);
      if (sp instanceof AfterCommand)
        break;
      else {
        assert sp instanceof ICommand : "Instance of " + sp.getClass().getName() + " is not ICommand";
        ICommand cmd = (ICommand) sp;

        queue.removeAt(0);
        cres = ApplicationManager.confirm(plane, cmd, true, false);
        if (sayConfirmations) say(cres.confirmation);

        if (cs == CommandSource.shortcutSkipped) {
          afterCommands.addRoute(af, cmd);
        } else
          afterCommands.addExtension(af, cmd);
      }
    }
  }

  private void processAfterSpeeches() {

    SpeechList<ICommand> cmds;

    Coordinate targetCoordinate = rdr.getRouting().tryGetTargetOrHoldCoordinate();

    // TODO when this function uses plane.tryGetTargetOrHoldingCoordinate(), then
    // this can be evaluated in the following function
    cmds = afterCommands.getAndRemoveSatisfiedCommands(
            rdr, targetCoordinate, AfterCommandList.Type.extensions);
    wrt.getCVR().logProcessedAfterSpeeches(cmds, "extensions");
    processSpeeches(cmds, CommandSource.extension);

    if (ArrayUtils.contains(FLYING_ROUTE_STATES, rdr.getState())) {
      cmds = afterCommands.getAndRemoveSatisfiedCommands(
              rdr, targetCoordinate, AfterCommandList.Type.route);
      wrt.getCVR().logProcessedAfterSpeeches(cmds, "route");
      processSpeeches(cmds, CommandSource.route);
    }
  }

  private void processNewSpeeches() {
    SpeechList<ICommand> current = new SpeechList<>(this.queue.getAndElapse());

    if (current.isEmpty()) return;

    wrt.getCVR().logProcessedCurrentSpeeches(current);

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (rdr.getAtc().hasRadarContact() == false
            && !(current.getFirst() instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  private void processNormalSpeech(IForPlaneSpeech cmd, CommandSource cs) {

    ConfirmationResult cres =
            ApplicationManager.confirm(
                    plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      if (cmd instanceof ICommand) affectAfterCommands((ICommand) cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == RoutingModule.CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      ApplicationResult ares = ApplicationManager.apply(super.plane, cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }
  }

  private void processSpeeches(SpeechList<ICommand> queue, CommandSource cs) {
    while (!queue.isEmpty()) {
      ICommand cmd = queue.getFirst();
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(cmd, cs);
        queue.remove(cmd);
      }
    }
  }

  private void say(IFromPlaneSpeech speech) {
    wrt.sendMessage(speech);
  }

  private SpeechList<ICommand> tryExpandThenCommands(IReadOnlyList<ICommand> speeches) {
    if (speeches.isEmpty()) {
      return new SpeechList<>();
    }

    SpeechList<ICommand> ret = new SpeechList<>(speeches);

    for (int i = 0; i < ret.size(); i++) {
      if (ret.get(i) instanceof ThenCommand) {
        if (i == 0 || i == ret.size() - 1) {
          wrt.sendMessage(
                  new IllegalThenCommandRejection(
                          (ThenCommand) ret.get(i),
                          "{Then} command cannot be first or last in queue. The whole command block is ignored."));
          ret.clear();
          return null;
        }
        ICommand prev = ret.get(i - 1);

        AfterCommand n; // new
        if (prev instanceof ProceedDirectCommand) {
          n = AfterDistanceCommand.create(((ProceedDirectCommand) prev).getNavaidName(), 0, AboveBelowExactly.exactly);
        } else if (prev instanceof ChangeAltitudeCommand) {
          ChangeAltitudeCommand ca = (ChangeAltitudeCommand) prev;
          AboveBelowExactly restriction;
          switch (ca.getDirection()) {
            case any:
              restriction = AboveBelowExactly.exactly;
              break;
            case climb:
              restriction = AboveBelowExactly.above;
              break;
            case descend:
              restriction = AboveBelowExactly.below;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          n = new AfterAltitudeCommand(ca.getAltitudeInFt(), restriction);
        } else if (prev instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand cmd = (ChangeSpeedCommand) prev;
          n = AfterSpeedCommand.create(
                  cmd.getRestriction().value,
                  cmd.getRestriction().direction);
        } else if (prev instanceof ChangeHeadingCommand) {
          ChangeHeadingCommand cmd = (ChangeHeadingCommand) prev;
          n = AfterHeadingCommand.create(cmd.getHeading());
        } else {
          wrt.sendMessage(
                  new IllegalThenCommandRejection(
                          (ThenCommand) ret.get(i),
                          "{Then} command is antecedent a strange command, it does not make sense. The whole command block is ignored."));
          ret.clear();
          return null;
        }
        ret.set(i, n);
      }
    }
    return ret;
  }
}
