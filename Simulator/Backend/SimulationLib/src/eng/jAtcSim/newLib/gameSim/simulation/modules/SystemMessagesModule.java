package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.IList;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.system.system2user.*;
import eng.jAtcSim.newLib.speeches.system.user2system.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SystemMessagesModule extends SimModule {
  private static final int MIN_TICK_LENGTH_INTERVAL = 100;
  private static final int MAX_TICK_LENGTH_INTERVAL = 5000;

  public SystemMessagesModule(ISimulationModuleParent parent) {
    super(parent);
  }

  public void elapseSecond() {
    IList<Message> systemMessages =
        MessagingAcc.getMessenger().getMessagesByListener(Participant.createSystem(), true);

    for (Message m : systemMessages) {
      processSystemMessage(m);
    }
  }

  private void processDeletePlaneRequest(DeletePlaneRequest content, Participant source) {
    try {
      parent.getSimulation().deletePlane(content.getSquawk());
      sendMessage(source, new SystemConfirmation(content));
    } catch (Exception ex) {
      sendMessage(source, new SystemRejection(content, ex.getMessage()));
    }
  }

  private void processGetHelpRequest(GetHelpRequest content, Participant targetAtc) {
    throw new ToDoException();
  }

  private void processMetarRequest(MetarRequest content, Participant targetAtc) {
    sendMessage(targetAtc, new MetarNotification(false));
  }

  private void processShortcutRequest(ShortcutRequest content, Participant source) {
    if (parent.getIO().isShortcutAvailable() == false) {
      sendMessage(source, new SystemRejection(content, "Shortcuts are not available in the current mode."));
      return;
    }

    if (content.getType() == ShortcutRequest.eType.delete
        || (content.getType() == ShortcutRequest.eType.set && StringUtils.isNullOrWhitespace(content.getValue()))) {
      parent.getIO().shortcutDeletion(content.getKey());
      sendMessage(source, new SystemConfirmation(content));
    } else if (content.getType() == ShortcutRequest.eType.set) {
      parent.getIO().shortcutSet(content.getKey(), content.getValue());
      sendMessage(source, new SystemConfirmation(content));
    } else if (content.getType() == ShortcutRequest.eType.get) {
      sendMessage(source, new ShorcutsOverviewNotification(parent.getIO().shortcutList()));
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
    else
      throw new UnsupportedOperationException("Unknown system message content of type " + content.getClass().getName());
  }

  private void processTickSpeedRequest(TickSpeedRequest content, Participant source) {
    if (content.getValue() == null) {
      sendMessage(source, new CurrentTickNotification(parent.getSimulation().getTickLength(), false));
    } else {
      int newInterval = content.getValue();
      if (newInterval < MIN_TICK_LENGTH_INTERVAL)
        sendMessage(source, new SystemRejection(content, sf("Tick-length must be greater than %d (request was %d).",
            MIN_TICK_LENGTH_INTERVAL, newInterval)));
      else if (newInterval > MAX_TICK_LENGTH_INTERVAL)
        sendMessage(source, new SystemRejection(content, sf("Tick-length must be lower than %d (request was %d).",
            MAX_TICK_LENGTH_INTERVAL, newInterval)));
      else {
        parent.getSimulation().setTickLength(newInterval);
        sendMessage(source, new CurrentTickNotification(parent.getSimulation().getTickLength(), true));
      }
    }
  }

  private void sendMessage(Participant receiver, IMessageContent content) {
    MessagingAcc.getMessenger().send(
        new Message(
            Participant.createSystem(),
            receiver,
            content
        )
    );
  }
}
