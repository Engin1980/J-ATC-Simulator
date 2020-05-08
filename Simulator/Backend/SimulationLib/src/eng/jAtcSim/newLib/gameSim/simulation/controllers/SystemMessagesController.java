package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.jAtcSim.newLib.airplanes.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.gameSim.simulation.InternalAcc;
import eng.jAtcSim.newLib.messaging.*;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.textProcessing.base.IParser;
import eng.jAtcSim.newLib.weather.WeatherAcc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemMessagesController {
  private static final String SYSMES_COMMANDS = "?";
  private static final Pattern SYSMES_CHANGE_SPEED = Pattern.compile("TICK (\\d+)");
  private static final Pattern SYSMES_METAR = Pattern.compile("METAR");
  private static final Pattern SYSMES_REMOVE = Pattern.compile("REMOVE (\\d{4})");
  private static final Pattern SYSMES_SHORTCUT = Pattern.compile("SHORTCUT ([\\\\?A-Z0-9]+)( (.+))?");

  public void elapseSecond() {
    IList<Message> systemMessages =
        MessagingAcc.getMessenger().getMessagesByListener(Participant.createSystem(), true);

    for (Message m : systemMessages) {
      processSystemMessage(m);
    }
  }

  private void printCommandsHelps(Participant target) {
    String txt = InternalAcc.getParser().getHelp();
    sendMessage(target, new StringMessageContent(txt));
  }

  private void processSystemMessage(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    if (msgText.equals(SYSMES_COMMANDS)) {
      printCommandsHelps(m.getSource());
    } else if (SYSMES_CHANGE_SPEED.asPredicate().test(msgText)) {
      processSystemMessageTick(m);
    } else if (SYSMES_METAR.asPredicate().test(msgText)) {
      String metarText = WeatherAcc.getWeather().toInfoString();
      MessagingAcc.getMessenger().send(new Message(
          Participant.createSystem(),
          m.getSource(),
          new StringMessageContent(metarText)));
    } else if (SYSMES_REMOVE.asPredicate().test(msgText)) {
      processSystemMessageRemove(m);
    } else if (SYSMES_SHORTCUT.asPredicate().test(msgText)) {
      processSystemMessageShortcut(m);
    } else {
      String msg = m.<StringMessageContent>getContent().getMessageText();
      String resp = InternalAcc.getParser().getHelp(msg);
      if (resp == null)
        sendMessage(m.getSource(),
            new StringMessageContent("Unknown system command '%s'.", msg));
      else
        sendMessage(m.getSource(),
            new StringMessageContent(resp));
    }
  }

  private void processSystemMessageRemove(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_REMOVE.matcher(msgText);
    if (!matcher.find()) {
      sendMessage(m.getSource(),
          new StringMessageContent("Illegal {remove} command format. Try ?remove <squawk>."));
    }
    String sqwk = matcher.group(1);

    IAirplane plane = AirplaneAcc.getAirplanes().tryGet(Squawk.create(sqwk));

    if (plane == null) {
      sendMessage(
          m.getSource(),
          new StringMessageContent("Unable to remove airplane from game. Squawk {%s} not found.", sqwk));
    } else {
      AirplaneAcc.getAirplanesController().unregisterPlane(plane.getCallsign());
      sendMessage(
          m.getSource(),
          new StringMessageContent("Airplane %s {%s} removed from game.",
              plane.getCallsign().toString(),
              plane.getSqwk().toString()));
    }
  }

  private void processSystemMessageShortcut(Message m) {
    IParser parser = InternalAcc.getParser();
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_SHORTCUT.matcher(msgText);
    matcher.find();
    String key = matcher.group(1);
    if (matcher.group(2) == null) {

      if (key.equals("?")) {
        // print all keys
        EStringBuilder sb = new EStringBuilder();
        sb.appendLine("Printing all shortcuts:");
        ISet<Map.Entry<String, String>> shortcuts = parser.getShortcuts().getEntries();
        for (Map.Entry<String, String> shortcut : shortcuts) {
          sb.appendFormatLine("Shortcut key '%s' is expanded as '%s'.", shortcut.getKey(), shortcut.getValue());
        }
        sendMessage(m.getSource(),
            new StringMessageContent(sb.toString()));
      } else {
        // delete key
        parser.getShortcuts().remove(key);
        sendMessage(m.getSource(),
            new StringMessageContent("Command shortcut key '%s' removed.", key));
      }
    } else {
      String value = matcher.group(3);
      if (value.equals("?")) {
        // print current
        value = parser.getShortcuts().tryGet(key);
        sendMessage(m.getSource(),
            new StringMessageContent("Command shortcut '%s' has expansion '%s'.", key, value));
      } else {
        parser.getShortcuts().add(key, value);
        sendMessage(m.getSource(),
            new StringMessageContent("Command shortcut '%s' is now defined as '%s'.", key, value));
      }
    }
  }

  private void processSystemMessageTick(Message m) {
    String msgText = m.<StringMessageContent>getContent().getMessageText();
    Matcher matcher = SYSMES_CHANGE_SPEED.matcher(msgText);
    matcher.find();
    String tickS = matcher.group(1);
    int tickI;
    try {
      tickI = Integer.parseInt(tickS);
    } catch (NumberFormatException ex) {
      int interval = InternalAcc.getTimerProvider().getTickInterval();
      MessagingAcc.getMessenger().send(
          new Message(
              Participant.createSystem(),
              m.getSource(),
              new StringMessageContent("Current tick speed is " + interval + ". To change use ?tick <value>.", tickS)));
      return;
    }
    InternalAcc.getTimerProvider().setTickInterval(tickI);

    MessagingAcc.getMessenger().send(
        new Message(
            Participant.createSystem(),
            m.getSource(),
            new StringMessageContent("Tick speed changed to %d milliseconds.", tickI))
    );
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
