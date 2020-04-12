package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.newLib.airplanes.LocalInstanceProvider;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.RequestRadarContactNotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.IllegalThenCommandRejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.*;

public class SpeechesModule extends eng.jAtcSim.newLib.airplanes.modules.Module {

  public enum CommandSource {
    procedure,
    atc,
    route,
    extension
  }

  private final DelayedList<ISpeech> queue = new DelayedList<>(2, 7); //Min/max item delay
  private final IMap<String, SpeechList<ISpeech>> saidText = new EMap<>();

  public SpeechesModule(IModulePlane plane) {
    super(plane);
  }

  @Override
  public void elapseSecond() {
    obtainNewSpeeches();
    processNewSpeeches();
    processAfterSpeeches();
    flushSaidTextToAtc();
  }

  public void processNewSpeeches() {
    SpeechList<ISpeech> current = new SpeechList<>(this.queue.getAndElapse());

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

  private void addNewSpeeches(SpeechList<ISpeech> speeches) {
    this.queue.newRandomDelay();
    expandThenCommands(speeches);
    for (ISpeech speech : speeches) {
      this.queue.add(speech);
    }

  }

  private void expandThenCommands(SpeechList<ISpeech> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          plane.sendMessage(
              new IllegalThenCommandRejection(
                  (ThenCommand) speeches.get(i),
                  "{Then} command cannot be first or last in queue. The whole command block is ignored.")
          );
          speeches.clear();
          return;
        }
        ISpeech prev = speeches.get(i - 1);

        AfterCommand n; // new
        if (prev instanceof ProceedDirectCommand) {
          n = AfterDistanceCommand.create(((ProceedDirectCommand) prev).getNavaidName(), 0, AfterValuePosition.exactly);
        } else if (prev instanceof ChangeAltitudeCommand) {
          ChangeAltitudeCommand ca = (ChangeAltitudeCommand) prev;
          AfterValuePosition restriction;
          switch (ca.getDirection()) {
            case any:
              restriction = AfterValuePosition.exactly;
              break;
            case climb:
              restriction = AfterValuePosition.aboveOrAfter;
              break;
            case descend:
              restriction = AfterValuePosition.belowOrBefore;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          n = new AfterAltitudeCommand(ca.getAltitudeInFt(), restriction);
        } else if (prev instanceof ChangeSpeedCommand) {
          n = AfterSpeedCommand.create(((ChangeSpeedCommand) prev).getSpeedInKts(), AfterValuePosition.exactly);
        } else if (prev instanceof ChangeHeadingCommand) {
          n = AfterHeadingCommand.create(((ChangeHeadingCommand) prev).getHeading(), AfterValuePosition.exactly);
        } else {
          plane.sendMessage(
              new IllegalThenCommandRejection(
                  (ThenCommand) speeches.get(i),
                  "{Then} command is antecedent a strange command, it does not make sense. The whole command block is ignored."));
          speeches.clear();
          return;
        }
        speeches.set(i, n);
      }
    }
  }

  private void obtainNewSpeeches() {
    IList<Message> msgs = LocalInstanceProvider.getMessenger().getMessagesByListener(
        Participant.createAirplane(plane.getCallsign()), true);

    // only responds to messages from tuned atc
    msgs = msgs.where(q -> q.getSource().equals(Participant.createAtc(plane.getTunedAtc())));

    // extract contents
    IList<IMessageContent> contents = msgs.select(q -> q.getContent());
    for (IMessageContent c : contents) {
      @SuppressWarnings("debil")
      SpeechList<ISpeech> cmds;
      if (c instanceof SpeechList)
        cmds = (SpeechList<ISpeech>) c;
      else {
        cmds = new SpeechList<>((ISpeech) c);
      }
      this.addNewSpeeches(cmds);
    }
  }

  private void processNormalSpeech(
      SpeechList<? extends ISpeech> queue, ISpeech cmd, CommandSource cs) {

    eng.jAtcSim.newLib.area.airplanes.commandApplications.ConfirmationResult cres =
        eng.jAtcSim.newLib.area.airplanes.commandApplications.ApplicationManager.confirm(
            plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      affectAfterCommands(cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == eng.jAtcSim.newLib.area.airplanes.modules.RoutingModule.CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      eng.jAtcSim.newLib.area.airplanes.commandApplications.ApplicationResult ares = eng.jAtcSim.newLib.area.airplanes.commandApplications.ApplicationManager.apply(parent, cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }

    queue.removeAt(0);
  }

  private void processSpeeches(SpeechList<ISpeech> queue, CommandSource cs) {
    while (!queue.isEmpty()) {
      ISpeech cmd = queue.getFirst();
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(queue, cmd, cs);
      }
    }
  }

  private void say(ISpeech speech) {
    String atc = plane.getTunedAtc();

    // if no tuned atc, nothing is said
    if (atc == null) return;

    saidText.getOrSet(atc, new SpeechList<>()).add(speech);
  }
}
