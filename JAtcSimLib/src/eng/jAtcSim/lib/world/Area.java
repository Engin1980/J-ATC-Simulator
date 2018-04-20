/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;
import eng.jAtcSim.lib.world.approaches.Approach;

/**
 *
 * @author Marek
 */
public class Area {

  private String icao;
  private final KeyList<Airport, String> airports = new KeyList();
  private final NavaidKeyList navaids = new NavaidKeyList();
  private final IList<Border> borders = new EList();

  public KeyList<Airport, String> getAirports() {
    return airports;
  }

  public String getIcao() {
    return icao;
  }

  public NavaidKeyList getNavaids() {
    return navaids;
  }

  public IList<Border> getBorders() {
    return borders;
  }

  public void init() {
    rebuildParentReferences();
    bind();
    checkRouteCommands();
  }

  private void bind() {
    for (Airport a : this.getAirports()) {
      Acc.setAirport(a);
      for (PublishedHold h : a.getHolds()) {
        h.bind();
      }
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          t.bind();
          
          for (Route o : t.getRoutes()) {
            o.bind();
          }
          
          for (Approach p : t.getApproaches()){
            p.bind();
          }
        }
      }
    }
    Acc.setAirport(null);
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
    Parser parser = new ShortParser();
    IList<IAtcCommand> cmds;
    Navaid n;
    for (Airport a : this.getAirports()) {
      Acc.setAirport(a);
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          for (Approach p : t.getApproaches()) {
            try {
              cmds = parser.parseMultipleCommands(p.getGaRoute());
            } catch (Exception ex) {
              throw new EApplicationException(
                  String.format("airport %s runway %s approach %s has invalid go-around route fromAtc: %s (error: %s)",
                      a.getIcao(), t.getName(), p.toString(), p.getGaRoute(), ex.getMessage()));
            }
          } // for (Approach

          for (Route o : t.getRoutes()) {
            try {
              cmds = parser.parseMultipleCommands(o.getRoute());
            } catch (Exception ex) {
              throw new EApplicationException(
                  String.format("airport %s runway %s route %s has invalid fromAtc: %s (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute(), ex.getMessage()));
            }
            try {
              n = o.getMainFix();
            } catch (ERuntimeException ex) {
              throw new EApplicationException(
                  String.format(
                      "airport %s runway %s route %s has no main fix. SID last/STAR first command must be PD FIX (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute()));
            }
          }
        } // for (RunwayThreshold
      } // for (Runway
    } // for (airport
    Acc.setAirport(null);
  }

  private Area() {
  }

  public static Area create() {
    Area ret = new Area();
    Acc.setArea(ret);
    return ret;
  }

}
