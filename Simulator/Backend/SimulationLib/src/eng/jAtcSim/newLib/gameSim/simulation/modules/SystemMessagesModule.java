package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.speeches.system.StringMessage;
import eng.jAtcSim.newLib.speeches.system.system2user.*;
import eng.jAtcSim.newLib.speeches.system.user2system.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SystemMessagesModule extends SimulationModule {
  private static final int MAX_TICK_LENGTH_INTERVAL = 5000;
  private static final int MIN_TICK_LENGTH_INTERVAL = 50;

  public SystemMessagesModule(Simulation parent) {
    super(parent);
  }

  public void elapseSecond() {
    IList<Message> systemMessages =
            Context.getMessaging().getMessenger().getMessagesByListener(Participant.createSystem(), true);

    for (Message m : systemMessages) {
      processSystemMessage(m);
    }
  }

  public void init() {
    Context.getMessaging().getMessenger().registerListener(Participant.createSystem());
  }

  private void processDeletePlaneRequest(DeletePlaneRequest content, Participant source) {
    try {
      parent.getAirplanesModule().deletePlane(content.getSquawk());
      sendMessage(source, new SystemConfirmation(content));
    } catch (Exception ex) {
      sendMessage(source, new SystemRejection(content, ex.getMessage()));
    }
  }

  private void processStringMessage(StringMessage msg){
    sendMessage(Participant.createSystem(), msg);
  }

  private void processGetHelpRequest(GetHelpRequest content, Participant targetAtc) {
    throw new ToDoException();
  }

  private void processMetarRequest(MetarRequest content, Participant targetAtc) {
    sendMessage(targetAtc, new MetarNotification(false));
  }

  private void processShortcutRequest(ShortcutRequest content, Participant source) {
    if (parent.getIoModule().getKeyShortcutManager().isShortcutAvailable() == false) {
      sendMessage(source, new SystemRejection(content, "Shortcuts are not available in the current mode."));
      return;
    }

    if (content.getType() == ShortcutRequest.eType.delete
            || (content.getType() == ShortcutRequest.eType.set && StringUtils.isNullOrWhitespace(content.getValue()))) {
      parent.getIoModule().getKeyShortcutManager().shortcutDeletion(content.getKey());
      sendMessage(source, new SystemConfirmation(content));
    } else if (content.getType() == ShortcutRequest.eType.set) {
      parent.getIoModule().getKeyShortcutManager().shortcutSet(content.getKey(), content.getValue());
      sendMessage(source, new SystemConfirmation(content));
    } else if (content.getType() == ShortcutRequest.eType.get) {
      sendMessage(source, new ShorcutsOverviewNotification(parent.getIoModule().getKeyShortcutManager().shortcutList()));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private void processSystemMessage(Message m) {
    IMessageContent content = m.getContent();
    if (content instanceof MetarRequest)
      processMetarRequest((MetarRequest) content, m.getSource());
    else if (content instanceof ShortcutRequest)
      processShortcutRequest((ShortcutRequest) content, m.getSource());
    else if (content instanceof TickSpeedRequest)
      processTickSpeedRequest((TickSpeedRequest) content, m.getSource());
    else if (content instanceof GetHelpRequest)
      processGetHelpRequest((GetHelpRequest) content, m.getSource());
    else if (content instanceof DeletePlaneRequest)
      processDeletePlaneRequest((DeletePlaneRequest) content, m.getSource());
    else if (content instanceof StringMessage)
      processStringMessage((StringMessage) content);
    else
      throw new UnsupportedOperationException("Unknown system message content of type " + content.getClass().getName());
  }

  private void processTickSpeedRequest(TickSpeedRequest content, Participant source) {
    String returnAtcId = null;
    ISystemNotification notification;
    if (source.getType() == Participant.eType.atc) returnAtcId = source.getId();

    if (content.getValue() == null) {
      notification = new CurrentTickNotification(parent.getTimerModule().getTickInterval(), false);
    } else {
      int newInterval = content.getValue();
      if (newInterval < MIN_TICK_LENGTH_INTERVAL)
        notification = new SystemRejection(content, sf("Tick-length must be greater than %d (request was %d).",
                MIN_TICK_LENGTH_INTERVAL, newInterval));
      else if (newInterval > MAX_TICK_LENGTH_INTERVAL)
        notification = new SystemRejection(content, sf("Tick-length must be lower than %d (request was %d).",
                MAX_TICK_LENGTH_INTERVAL, newInterval));
      else {
        parent.getTimerModule().setTickInterval(newInterval);
        notification = new CurrentTickNotification(parent.getTimerModule().getTickInterval(), true);
      }
    }

    if (returnAtcId != null)
      sendMessage(source, notification);
    else {
      Context.getAtc().getAtcList()
              .where(q -> q.getType() == AtcType.app)
              .forEach(q -> sendMessage(
                      Participant.createAtc(q),
                      notification
              ));
    }
  }

  private void sendMessage(Participant receiver, IMessageContent content) {
    Context.getMessaging().getMessenger().send(
            new Message(
                    Participant.createSystem(),
                    receiver,
                    content
            )
    );
  }
}
