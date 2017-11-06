/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar;

import jatcismdraw.global.radarBase.VisualisedMessage;
import jatcismdraw.global.radarBase.Visualiser;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.PlaneSwitchMessage;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.messaging.IMessageParticipant;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.messaging.StringMessageContent;
import jatcsimlib.speaking.ISpeech;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.formatting.Formatter;
import jatcsimlib.world.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marek
 */
public class PaintManager {

  class MessageManager {
    private final int delay;
    private List<VisualisedMessage> items = new ArrayList<>();

    public MessageManager(int delay) {
      this.delay = delay;
    }

    public void add(IMessageParticipant source, String text) {
      VisualisedMessage di = new VisualisedMessage(source, text, delay);
      items.add(di);
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
  }
  private final Simulation simulation;
  private final Area area;
  private final Visualiser visualiser;
  private final MessageManager messageManager;
  private final Formatter formatter;

  public PaintManager(Simulation simulation, Area area, Visualiser visualiser,
                      int messageDisplayInSeconds, Formatter formatter) {
    this.simulation = simulation;
    this.area = area;
    this.visualiser = visualiser;
    this.formatter = formatter;
    this.messageManager = new MessageManager(messageDisplayInSeconds);
  }

  public void draw() {
    visualiser.beforeDraw();
    drawBackground();
    drawBorders();
    drawStars();
    drawSids();
    drawApproaches();
    drawNavaids();
    drawAirports();
    drawAirplanes();
    drawCaptions();
    drawTime();
    visualiser.afterDraw();
  }

  private void drawCaptions() {
    Messenger ms = simulation.getMessenger();
    List<Message> msgs = ms.getByTarget(simulation.getAppAtc(), true);

    for (Message msg : msgs) {
      String formattedText =
          getMessageContentAsString(msg);
      messageManager.add(msg.getSource(), formattedText);
    }

    boolean containsAtcMessage =
        msgs.stream().anyMatch(q -> q.isSourceOfType(Atc.class));
    boolean containsPlaneMessage =
        msgs.stream().anyMatch(q -> q.isSourceOfType(Airplane.class));

    if (containsAtcMessage) {
      SoundManager.playAtcNewMessage();
    } else if (containsPlaneMessage) {
      SoundManager.playPlaneNewMessage();
    }

    visualiser.drawMessages(messageManager.getCurrent());

    messageManager.decreaseMessagesLifeCounter();
  }

  private String getMessageContentAsString(Message msg) {
    String ret;
    if (msg.isSourceOfType(Airplane.class)) {
      if (msg.isContentOfType(List.class)) {
        EStringBuilder esb = new EStringBuilder();
        SpeechList<ISpeech> lst = msg.getContent();
        for (int i = 0; i < lst.size(); i++) {
          ISpeech sp = lst.get(i);
          String sentence = formatter.format(sp);
          if (i == 0) {
            esb.append(makeBeginSentence(sentence));
          } else
            esb.append(sentence);
          if (i < lst.size() - 1)
            esb.append(", ");
          else
            esb.append(".");
        }
        ret = esb.toString();
      } else {
        ISpeech sp = msg.getContent();
        ret = formatter.format(sp);
      }
    } else if (msg.isSourceOfType(Atc.class)) {
      if (msg.isContentOfType(PlaneSwitchMessage.class)) {
        PlaneSwitchMessage psm = msg.<PlaneSwitchMessage>getContent();
        ret = formatter.format(msg.getSource(), psm);
      } else if (msg.isContentOfType(StringMessageContent.class)) {
        ret = msg.<StringMessageContent>getContent().getMessageText();
      } else {
        throw new ENotSupportedException();
      }
    } else {
      // system messages
      ret = msg.<StringMessageContent>getContent().getMessageText();
    }
    return ret;
  }

  private String makeBeginSentence(String sentence) {
    StringBuilder ret = new StringBuilder();

    if (sentence.length() > 0)
      ret.append(Character.toUpperCase(sentence.charAt(0)));

    if (sentence.length() > 1)
      ret.append(sentence.substring(1));

    return ret.toString();
  }

  private void drawBorders() {
    for (Border b : area.getBorders()) {
      visualiser.drawBorder(b);
    }
  }

  private void drawNavaids() {
    for (Navaid n : area.getNavaids()) {
      visualiser.drawNavaid(n);
    }
  }

  private void drawAirports() {
    for (Airport a : area.getAirports()) {
      drawAirport(a);
    }
  }

  private void drawAirport(Airport a) {
    for (Runway r : a.getRunways()) {
      visualiser.drawRunway(r);
    }
  }

  private void drawBackground() {
    visualiser.clear();
  }

  private void drawAirplanes() {
    for (Airplane.AirplaneInfo ai : simulation.getPlaneInfos()) {
      visualiser.drawPlane(ai);
    }
  }

  private void drawStars() {
    for (Route r : simulation.getActiveRunwayThreshold().getRoutes()) {
      if (r.getType() == Route.eType.sid) continue;
      visualiser.drawStar(r.getNavaids());
    }
  }

  private void drawSids() {
    for (Route r : simulation.getActiveRunwayThreshold().getRoutes()) {
      if (r.getType() != Route.eType.sid) continue;
      visualiser.drawSid(r.getNavaids());
    }
  }

  private void drawApproaches() {
    Approach a = simulation.getActiveRunwayThreshold().getHighestApproach();
    if (a != null) {
      visualiser.drawApproach(a);
    }
  }

  private void drawTime() {
    visualiser.drawTime(simulation.getNow());
  }

}
