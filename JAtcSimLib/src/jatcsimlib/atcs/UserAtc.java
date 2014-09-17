/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.CommandList;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.IContent;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {

  private final AirplaneList departures = new AirplaneList();
  private final AirplaneList departuresForCtr = new AirplaneList();
  private final AirplaneList arrivals = new AirplaneList();
  private final AirplaneList arrivalsForTwr = new AirplaneList();

  public UserAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void elapseSecond() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void sendCommands(Airplane plane, CommandList commands) {
    Acc.messenger().addMessage(this, plane, commands);
  }

  public void sendCommands(Atc.eType type, Airplane plane) {
    Atc atc = Acc.atc(type);
    IContent cnt;

    if (this.arrivals.contains(plane)) {
      // je to zadost na vezku
      this.arrivalsForTwr.add(plane);
      this.arrivals.remove(plane);
      cnt = new PlaneSwitchMessage(plane);
    } else if (this.departures.contains(plane)) {
      // je to zadost na ctr
      this.departuresForCtr.add(plane);
      this.departures.remove(plane);
      cnt = new PlaneSwitchMessage(plane);
    } else {
      //  je to potvrzeni zadosti nebo se app zblaznil
      if (getPrm().isToSwitch(plane)) {
        getPrm().confirmSwitch(this, plane);
        cnt = new PlaneSwitchMessage(plane);
      } else {
        throw new ERuntimeException("Cannot confirm switch of something not asked to switch to... TODO");
      }
    }

    Acc.messenger().addMessage(this, atc, cnt);
  }

  public void sendError(String message) {
    Acc.messenger().addMessage(null, this, message);
  }

  public void sendSystem(String message) {
    throw new ENotSupportedException();
  }

}
