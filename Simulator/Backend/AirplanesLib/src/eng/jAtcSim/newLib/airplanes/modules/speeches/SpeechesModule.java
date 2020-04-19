package eng.jAtcSim.newLib.airplanes.modules.speeches;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.utilites.ConversionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.newLib.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.newLib.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.RequestRadarContactNotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.*;

public class SpeechesModule extends eng.jAtcSim.newLib.airplanes.modules.Module {

  private enum CommandSource {
    shortcutSkipped,
    atc,
    route,
    extension
  }

  private final DelayedList<ICommand> queue = new DelayedList<>(2, 7); //Min/max item delay
  private final IMap<AtcId, SpeechList<ISpeech>> saidText = new EMap<>();
  private final AfterCommandList afterCommands = new AfterCommandList();
  private Navaid entryExitPoint;
  private ActiveRunwayThreshold runwayThreshold;

  public SpeechesModule(IPlaneInterface plane) {
    super(plane);
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
    flushSaidTextToAtc();
  }

  public Navaid getEntryExitPoint() {
    return entryExitPoint;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  public boolean hasLateralDirectionAfterCoordinate(Coordinate coordinate) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    return afterCommands.hasLateralDirectionAfterCoordinate(coordinate);
  }

  public boolean isGoingToFlightOverNavaid(Navaid navaid) {
    return afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
  }

  public void processNewSpeeches() {
    SpeechList<ICommand> current = new SpeechList<>(this.queue.getAndElapse());

    if (current.isEmpty()) return;

    plane.getCVR().logProcessedCurrentSpeeches(current);

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (plane.hasRadarContact() == false
        && !(current.getFirst() instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  public void setEntryExitPoint(Navaid entryExitNavaid) {
    EAssert.Argument.isNotNull(entryExitNavaid, "entryExitNavaid");
    this.entryExitPoint = entryExitNavaid;
  }

  public void setRouting(IReadOnlyList<ICommand> routeCommands) {
    SpeechList<ICommand> cmds = tryExpandThenCommands(routeCommands);
    if (cmds == null) return; // some error
    afterCommands.clearAll();
    processSpeeches(cmds, CommandSource.route);
  }

  public void setRunwayThreshold(ActiveRunwayThreshold activeRunwayThreshold) {
    EAssert.Argument.isNotNull(activeRunwayThreshold, "activeRunwayThreshold");
    this.runwayThreshold = activeRunwayThreshold;
  }

  private void addNewSpeeches(SpeechList<ICommand> speeches) {
    this.queue.newRandomDelay();
    tryExpandThenCommands(speeches);
    for (ICommand speech : speeches) {
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
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), plane.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 5
            this.afterCommands.clearChangeSpeedClass(
                tmp.getRestriction().value, plane.isArrival(), AfterCommandList.Type.route);
            this.afterCommands.clearChangeSpeedClass(
                tmp.getRestriction().value, plane.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 6
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, plane.isArrival());
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
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), plane.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 10
            this.afterCommands.clearChangeSpeedClass(tmp.getRestriction().value, plane.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 11
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, plane.isArrival());
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

  private SpeechList<ICommand> tryExpandThenCommands(IReadOnlyList<ICommand> speeches) {
    if (speeches.isEmpty()) {
      return new SpeechList<>();
    }

    SpeechList<ICommand> ret = new SpeechList<>(speeches);

    for (int i = 0; i < ret.size(); i++) {
      if (ret.get(i) instanceof ThenCommand) {
        if (i == 0 || i == ret.size() - 1) {
          plane.sendMessage(
              plane.getTunedAtc(),
              new IllegalThenCommandRejection(
                  (ThenCommand) ret.get(i),
                  "{Then} command cannot be first or last in queue. The whole command block is ignored.")
          );
          ret.clear();
          return null;
        }
        ISpeech prev = ret.get(i - 1);

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
          plane.sendMessage(
              plane.getTunedAtc(),
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

  private void flushSaidTextToAtc() {
    for (AtcId atcId : saidText.getKeys()) {
      SpeechList<ISpeech> saidTextToAtc = saidText.get(atcId);
      if (!saidTextToAtc.isEmpty()) {
        plane.sendMessage(atcId, saidTextToAtc);
        saidText.set(atcId, new SpeechList<>());
        // here new list must be created
        // the old one is send to messenger for further processing
      }
    }
  }

  private void obtainNewSpeeches() {
    IList<Message> msgs = LAcc.getMessenger().getMessagesByListener(
        Participant.createAirplane(plane.getCallsign()), true);

    // only responds to messages from tuned atc
    msgs = msgs.where(q -> q.getSource().equals(Participant.createAtc(plane.getTunedAtc())));

    // extract contents
    IList<IMessageContent> contents = msgs.select(q -> q.getContent());
    for (IMessageContent c : contents) {
      SpeechList<ICommand> cmds;
      if (c instanceof SpeechList) {
        SpeechList<ISpeech> tmp = (SpeechList) c;
        EAssert.isTrue(tmp.isAll(q -> q instanceof ICommand));
        cmds = new SpeechList<>();
        tmp.forEach(q -> cmds.add((ICommand) q));
      } else if (c instanceof ICommand) {
        cmds = new SpeechList<>((ICommand) c);
      } else
        throw new EApplicationException("Unexpected type of cmd: " + c.getClass().getName());
      this.addNewSpeeches(cmds);
    }
  }

  private void processAfterSpeechWithConsequents(IList<? extends ISpeech> queue, CommandSource cs) {

    Airplane.State[] unableProcessAfterCommandsStates = {
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround
    };

    AfterCommand af = (AfterCommand) queue.get(0);
    queue.removeAt(0);

    if (cs == CommandSource.atc && plane.getState().is(unableProcessAfterCommandsStates)) {
      ISpeech rej = new Rejection("Unable to process after-command during approach/take-off.", af);
      say(rej);
      return;
    }

    ConfirmationResult cres;
    boolean sayConfirmations = cs == CommandSource.atc;

    cres = ApplicationManager.confirm(plane, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      ISpeech sp = queue.get(0);
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

    Coordinate targetCoordinate = plane.tryGetTargetOrHoldCoordinate();

    // TODO when this function uses plane.tryGetTargetOrHoldingCoordinate(), then
    // this can be evaluated in the following function
    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        plane, targetCoordinate, AfterCommandList.Type.extensions);
    plane.getCVR().logProcessedAfterSpeeches(cmds, "extensions");
    processSpeeches(cmds, CommandSource.extension);

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        plane, targetCoordinate, AfterCommandList.Type.route);
    plane.getCVR().logProcessedAfterSpeeches(cmds, "route");
    processSpeeches(cmds, CommandSource.route);
  }

  private void processNormalSpeech(
      SpeechList<? extends ISpeech> queue, ICommand cmd, CommandSource cs) {

    ConfirmationResult cres =
        ApplicationManager.confirm(
            plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      affectAfterCommands(cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == SpeechesModule.CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      ApplicationResult ares = ApplicationManager.apply(super.plane, cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }

    queue.removeAt(0);
  }

  private void processSpeeches(SpeechList<ICommand> queue, CommandSource cs) {
    while (!queue.isEmpty()) {
      ICommand cmd = queue.getFirst();
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(queue, cmd, cs);
      }
    }
  }

  private void say(ISpeech speech) {
    AtcId atc = plane.getTunedAtc();

    // if no tuned atc, nothing is said
    if (atc == null) return;

    saidText.getOrSet(atc, new SpeechList<>()).add(speech);
  }
}
