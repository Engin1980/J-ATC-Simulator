package eng.jAtcSim.abstractRadar.support;

import eng.eSystem.collections.EList;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.FormattersSet;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;

import java.util.ArrayList;
import java.util.List;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class VisualisedMessageManager {
  private final int delay;
  private FormattersSet<String> formatters;
  private List<VisualisedMessage> items = new ArrayList<>();

  public VisualisedMessageManager(int delay, FormattersSet<String> formatters) {
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, delay, 30));
    EAssert.Argument.isNotNull(formatters, "formatters");
    this.delay = delay;
    this.formatters = formatters;
  }

  public void add(Message message) {
    String text = convertMessageToText(message);
    VisualisedMessage di = new VisualisedMessage(message, text, delay);
    items.add(di);
  }

  private String convertMessageToText(Message message) {
    String ret = this.format(message);
    return ret;
  }

  public void decreaseMessagesLifeCounter() {
    for (VisualisedMessage item : items) {
      item.decreaseLifeCounter();
    }
    items.removeIf(q -> q.getLifeCounter() <= 0);
  }

  public List<VisualisedMessage> getCurrent() {
    return items;
  }


  private String format(Message message) {
    String ret;

    try {
      ret = tryFormat(message);
    } catch (Exception ex) {
      throw new ApplicationException(
          sf("Failed to format message '%s'.", message.getContent()),
          ex);
    }
    return ret;
  }

  public String tryFormat(Message message) {
    String ret;
    IMessageContent content = message.getContent();
    if (content instanceof SpeechList)
      ret = formatSpeechList((SpeechList<IPlaneSpeech>) content);
    else if (content instanceof IAtcSpeech)
      ret = this.formatters.atcFormatter.format((IAtcSpeech) content);
    else if (content instanceof ISystemNotification)
      ret = this.formatters.systemFormatter.format((ISystemNotification) content);
    else
      throw new ApplicationException(sf(
          "Unable to find appropriate formatter for type '%s'.",
          content.getClass().getName()));
    return ret;
  }

  private String formatSpeechList(SpeechList<IPlaneSpeech> content) {
    EList<String> lst = new EList<>();
    for (IPlaneSpeech speech : content) {
      String tmp = this.formatters.planeFormatter.format(speech);
      lst.add(tmp);
    }

    String ret;

    ret = StringUtils.join(",", lst) + ".";

    return ret;
  }
}
