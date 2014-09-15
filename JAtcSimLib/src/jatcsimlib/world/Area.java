/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.Acc;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Area {

  private String icao;
  private final KeyList<Airport, String> airports = new KeyList();
  private final KeyList<Navaid, String> navaids = new KeyList();
  private final List<Border> borders = new ArrayList();

  public KeyList<Airport, String> getAirports() {
    return airports;
  }

  public String getIcao() {
    return icao;
  }

  public KeyList<Navaid, String> getNavaids() {
    return navaids;
  }

  public List<Border> getBorders() {
    return borders;
  }

  public void initAfterLoad() {
    rebuildParentReferences();
    _bind();
    checkRouteCommands();
  }

  private void _bind() {
    for (Airport a : this.getAirports()) {
      for (PublishedHold h : a.getHolds()) {
        h.bind();
      }
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          for (Route o : t.getRoutes()) {
            o.bind();
          }
        }
      }
    }
  }

  private void rebuildParentReferences() {
    for (Airport a : this.getAirports()) {
      a.setParent(this);

      for (PublishedHold h : a.getHolds()) {
        h.setParent(a);
      }

      for (Runway r : a.getRunways()) {
        r.setParent(a);

        for (RunwayThreshold t : r.getThresholds()) {
          t.setParent(r);

          for (Route o : t.getRoutes()) {
            o.setParent(t);
          }
          for (Approach p : t.getApproaches()) {
            p.setParent(t);
          }
        }
      }
    }
  }

  private void checkRouteCommands() {
    List<Command> cmds;
    Navaid n;
    for (Airport a : this.getAirports()) {
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          for (Approach p : t.getApproaches()) {
            try {
              cmds = CommandFormat.parseMulti(p.getGaRoute());
            } catch (Exception ex) {
              throw new ERuntimeException(
                  String.format("Airport %s runway %s approach %s has invalid go-around route commands: %s (error: %s)",
                      a.getIcao(), t.getName(), p.getType(), p.getGaRoute(), ex.getMessage()));
            }
          } // for (Approach

          for (Route o : t.getRoutes()) {
            try {
              cmds = CommandFormat.parseMulti(o.getRoute());
            } catch (Exception ex) {
              throw new ERuntimeException(
                  String.format("Airport %s runway %s route %s has invalid commands: %s (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute(), ex.getMessage()));
            }
            try {
              n = o.getMainFix();
            } catch (ERuntimeException ex) {
              throw new ERuntimeException(
                  String.format(
                      "Airport %s runway %s route %s has no main fix. SID last/STAR first command must be PD FIX (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute()));
            }
          }
        } // for (RunwayThreshold
      } // for (Runway
    } // for (Airport
  }

  private Area() {
  }

  public static Area create() {
    Area ret = new Area();
    Acc.setArea(ret);
    return ret;
  }

}
