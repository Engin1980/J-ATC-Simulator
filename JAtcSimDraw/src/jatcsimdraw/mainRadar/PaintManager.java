/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar;

import jatcismdraw.global.radarBase.Visualiser;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.newMessaging.Message;
import jatcsimlib.newMessaging.Messenger;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Area;
import jatcsimlib.world.Border;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import jatcsimlib.world.Runway;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 *
 * @author Marek
 */
public class PaintManager {

  private final Simulation simulation;
  private final Area area;
  private final Visualiser visualiser;

  public PaintManager(Simulation simulation, Area area, Visualiser visualiser) {
    this.simulation = simulation;
    this.area = area;
    this.visualiser = visualiser;
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
    List<Message> msgs = ms.getByTarget(simulation.getAppAtc(), false);

    boolean containsAtcMessage;
    boolean containsPlaneMessage;
    containsAtcMessage = msgs.stream().anyMatch(q -> q.getSource() instanceof Atc);
    containsPlaneMessage = msgs.stream().anyMatch(q-> q.getSource() instanceof Airplane);
    if (containsAtcMessage){
      SoundManager.playAtcNewMessage();
    } else if (containsPlaneMessage){
      SoundManager.playPlaneNewMessage();
    }

    visualiser.drawMessages(msgs);
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
    for (Route r : simulation.getActiveRunwayThreshold().getRoutes()){
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
