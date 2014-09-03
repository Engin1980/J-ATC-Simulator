/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcismdraw.radarBase;

import jatcsimdraw.settings.Settings;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.messaging.Message;
import jatcsimlib.world.Border;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Runway;
import java.util.List;

/**
 *
 * @author Marek
 */
public abstract class Visualiser {
  protected final Painter p;
  protected final Settings sett;

  public Visualiser(Painter p, Settings sett) {
    this.p = p;
    this.sett = sett;
  }
  
  public abstract void drawBorder(Border border);
  public abstract void drawRunway(Runway runway);
  public abstract void drawNavaid (Navaid navaid);
  public abstract void drawPlane(Airplane plane);
  public abstract void drawMessages(List<Message> msgs);
  public abstract void clear();
}
