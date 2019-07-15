package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.utilites.ConversionUtils;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.lib.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.HoldBehavior;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.DelayedList;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.RequestRadarContactNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.IllegalThenCommandRejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

import java.util.HashMap;
import java.util.Map;

public class RoutingModule {
  public enum CommandSource {
    procedure,
    atc,
    route,
    extension
  }

  private final Pilot.Pilot5Module parent;

  private Route assignedRoute;
  private ActiveRunwayThreshold expectedRunwayThreshold;
  private Navaid entryExitPoint;

  private final DelayedList<ISpeech> queue = new DelayedList<>(2, 7); //Min/max item delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final Map<Atc, SpeechList> saidText = new HashMap<>();

  public RoutingModule(Pilot.Pilot5Module parent) {
    assert parent != null;
    this.parent = parent;
  }

  public Navaid getEntryExitPoint() {
    return this.entryExitPoint;
  }

  public Route getAssignedRoute() {
    return this.assignedRoute;
  }

  public void updateAssignedRouting(Route newRoute, ActiveRunwayThreshold expectedRunwayThreshold) {
    this.expectedRunwayThreshold = expectedRunwayThreshold;
    this.assignedRoute = newRoute;
    this.afterCommands.clearRoute();

    SpeechList<IFromAtc> cmds;
    cmds = new SpeechList<>();
    cmds.add(assignedRoute.getCommands());
    expandThenCommands(cmds);
    processSpeeches(cmds, CommandSource.procedure);
  }

  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return expectedRunwayThreshold;
  }

  public void addNewSpeeches(SpeechList<IFromAtc> speeches) {
    this.queue.newRandomDelay();
    expandThenCommands(speeches);
    for (ISpeech speech : speeches) {
      this.queue.add(speech);
    }
  }

  private void processNewSpeeches() {
    SpeechList current = new SpeechList(this.queue.getAndElapse());

    if (current.isEmpty()) return;

    parent.getRecorder().logProcessedCurrentSpeeches(current);

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (parent.getAtcModule().getSecondsWithoutRadarContact() > 0
        && !(current.get(0) instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  private void processAfterSpeeches() {

    SpeechList<IAtcCommand> cmds;

    Coordinate targetCoordinate = parent.getSha().tryGetTargetCoordinate();
    if (targetCoordinate == null && parent.getBehaviorModule().is(HoldBehavior.class)){
      HoldBehavior hb = parent.getBehaviorModule().getAs(HoldBehavior.class);
      targetCoordinate = hb.navaid.getCoordinate();
    }

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), targetCoordinate, AfterCommandList.Type.extensions);
    parent.getRecorder().logProcessedAfterSpeeches(cmds, "extensions");
    processSpeeches(cmds, CommandSource.extension);

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), targetCoordinate, AfterCommandList.Type.route);
    parent.getRecorder().logProcessedAfterSpeeches(cmds, "route");
    processSpeeches(cmds, CommandSource.route);
  }

  private void processSpeeches(SpeechList<? extends IFromAtc> queue, CommandSource cs) {

    Airplane.Airplane4Command plane = this.parent.getPlane4Command();
    while (!queue.isEmpty()) {
      IFromAtc cmd = queue.get(0);
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(queue, cmd, cs, plane);
      }
    }
  }

  private void processNormalSpeech(
      SpeechList<? extends IFromAtc> queue, IFromAtc cmd,
      CommandSource cs, Airplane.Airplane4Command plane) {

    ConfirmationResult cres = ApplicationManager.confirm(plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      affectAfterCommands(cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      ApplicationResult ares = ApplicationManager.apply(parent.getPilot5Command(), cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }

    queue.removeAt(0);
  }

  private void affectAfterCommands(IFromAtc cmd, Pilot.CommandSource cs) {
    final Class[] lateralCommands = new Class[]{ProceedDirectCommand.class, ChangeHeadingCommand.class, HoldCommand.class};
    switch (cs) {
      case procedure:
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
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 5
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.route);
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 6
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.isArrival());
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
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 10
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 11
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.isArrival());
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

    if (cs == Pilot.CommandSource.atc && parent.getState().is(unableProcessAfterCommandsStates)) {
      ISpeech rej = new Rejection("Unable to process after-command during approach/take-off.", af);
      say(rej);
      return;
    }

    ConfirmationResult cres;
    boolean sayConfirmations = cs == Pilot.CommandSource.atc;

    cres = ApplicationManager.confirm(plane, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      ISpeech sp = queue.get(0);
      if (sp instanceof AfterCommand)
        break;
      else {
        assert sp instanceof IAtcCommand : "Instance of " + sp.getClass().getName() + " is not IAtcCommand";
        IAtcCommand cmd = (IAtcCommand) sp;
        if (cmd instanceof AfterCommand)
          break;

        queue.removeAt(0);
        cres = ApplicationManager.confirm(plane, cmd, true, false);
        if (sayConfirmations) say(cres.confirmation);

        if (cs == Pilot.CommandSource.procedure) {
          afterCommands.addRoute(af, cmd);
        } else
          afterCommands.addExtension(af, cmd);
      }
    }
  }

  private void say(ISpeech speech) {
    // if no tuned atc, nothing is said
    if (atc == null) return;

    if (saidText.containsKey(atc) == false) {
      saidText.put(atc, new SpeechList());
    }

    saidText.get(atc).add(speech);
  }

  private void flushSaidTextToAtc() {
    for (Atc a : saidText.keySet()) {
      SpeechList saidTextToAtc = saidText.get(a);
      if (!saidTextToAtc.isEmpty()) {
        parent.passMessageToAtc(a, saidText.get(a));
        saidText.put(a, new SpeechList());
        // here new list must be created
        // the old one is send to messenger for further processing
      }
    }
  }

  private void expandThenCommands(SpeechList<IFromAtc> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          parent.passMessageToAtc(
              atc,
              new IllegalThenCommandRejection("{Then} command cannot be first or last in queue. The whole command block is ignored.")
          );
          speeches.clear();
          return;
        }
        IAtcCommand prev = (IAtcCommand) speeches.get(i - 1);

        AfterCommand n; // new
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          ChangeAltitudeCommand ca = (ChangeAltitudeCommand) prev;
          AfterAltitudeCommand.ERestriction restriction;
          switch (ca.getDirection()) {
            case any:
              restriction = AfterAltitudeCommand.ERestriction.exact;
              break;
            case climb:
              restriction = AfterAltitudeCommand.ERestriction.andAbove;
              break;
            case descend:
              restriction = AfterAltitudeCommand.ERestriction.andBelow;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          n = new AfterAltitudeCommand(ca.getAltitudeInFt(), restriction);
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else if (prev instanceof ChangeHeadingCommand) {
          n = new AfterHeadingCommand(((ChangeHeadingCommand) prev).getHeading());
        } else {
          parent.passMessageToAtc(atc,
              new IllegalThenCommandRejection("{Then} command is antecedent a strange command, it does not make sense. The whole command block is ignored."));
          speeches.clear();
          return;
        }
        n.setDerivationSource(prev);
        speeches.set(i, n);
      }
    }
  }
}
