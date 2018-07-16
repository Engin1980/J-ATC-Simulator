/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IlsApproach;

/**
 * @author Marek
 */
public class Area {

  private final IList<Airport> airports = new EList<>();
  private final NavaidList navaids = new NavaidList();
  private final IList<Border> borders = new EList();
  private String icao;

  public static Area create() {
    Area ret = new Area();
    Acc.setArea(ret);
    return ret;
  }

  private Area() {
  }

  public IList<Airport> getAirports() {
    return airports;
  }

  public String getIcao() {
    return icao;
  }

  public NavaidList getNavaids() {
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

  public void checkForDuplicits() {
    ISet<String> set;

    set = this.airports.getDuplicateItems(q -> q.getIcao()).select(q -> q.getIcao()).first(5);
    tryFailDuplicits("Airports", set);

    set = this.navaids.getDuplicateItems(q -> q.getName()).select(q -> q.getName()).first(5);
    tryFailDuplicits("Navaids", set);

    for (Airport airport : this.airports) {
      set = airport.getRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).first(3);
      tryFailDuplicits("Active runways of " + airport.getName(), set);

      set = airport.getInactiveRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).first(3);
      tryFailDuplicits("Inactive runways of " + airport.getName(), set);

      set = airport.getAtcTemplates().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).first(3);
      tryFailDuplicits("ATC templates of " + airport.getName(), set);

      set = airport.getHolds().getDuplicateItems(q -> q.getNavaid().getName()).select(q -> q.getNavaid().getName()).first(3);
      tryFailDuplicits("Published holds of " + airport.getName(), set);

      for (Runway runway : airport.getRunways()) {
        set = runway.getThresholds().getDuplicateItems(q -> q.getName()).select(q -> q.getName());
        tryFailDuplicits("Active runway thresholds of " + runway.getName() + " of " + airport.getName(), set);

        for (RunwayThreshold runwayThreshold : runway.getThresholds()) {
          for (Approach approach : runwayThreshold.getApproaches()) {
            if (!(approach instanceof IlsApproach)) continue;
            IlsApproach ils = (IlsApproach) approach;
            set = ils.getCategories().getDuplicateItems(q -> q.getType()).select(q -> q.getType().toString());
            tryFailDuplicits("ILS category types of ILS approach of " + runway.getName() + " of " + airport.getName(), set);
          }
        }
      }

      for (InactiveRunway runway : airport.getInactiveRunways()) {
        set = runway.getThresholds().getDuplicateItems(q -> q.getName()).select(q -> q.getName());
        tryFailDuplicits("Inactive runway thresholds of " + runway.getName() + " of " + airport.getName(), set);
      }
    }
  }

  private void tryFailDuplicits(String type, ISet<String> items) {
    if (items.isEmpty()) return;

    EStringBuilder sb = new EStringBuilder();
    sb.append(items.size() + " duplicate record(s) were found in " + type + ". E.g.: ");
    sb.appendItems(items, q->q, "; ");
    throw new EApplicationException(sb.toString());
  }

  private void bind() {
    for (Border border : borders) {
      border.bind();
    }

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
          a.bindEntryExitPointsByRoutes(t.getRoutes());

          for (Approach p : t.getApproaches()) {
            p.bind();
          }
        }

        for (EntryExitPoint eep : a.getEntryExitPoints()) {
          eep.bind();
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
    Parser parser = new ShortBlockParser();
    for (Airport a : this.getAirports()) {
      Acc.setAirport(a);
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          for (Approach p : t.getApproaches()) {
            try {
              parser.parseMultipleCommands(p.getGaRoute());
            } catch (Exception ex) {
              throw new EApplicationException(
                  String.format("airport %s runway %s approach %s has invalid go-around route fromAtc: %s (error: %s)",
                      a.getIcao(), t.getName(), p.toString(), p.getGaRoute(), ex.getMessage()));
            }
          } // for (Approach

          for (Route o : t.getRoutes()) {
            try {
              parser.parseMultipleCommands(o.getRoute());
            } catch (Exception ex) {
              throw new EApplicationException(
                  String.format("airport %s runway %s route %s has invalid fromAtc: %s (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute(), ex.getMessage()));
            }
//            try {
//              n = o.getMainNavaid();
//            } catch (ERuntimeException ex) {
//              throw new EApplicationException(
//                  String.format(
//                      "airport %s runway %s route %s has no main fix. SID last/STAR first command must be PD FIX (error: %s)",
//                      a.getIcao(), t.getName(), o.getName(), o.getRoute()));
//            }
          }
        } // for (RunwayThreshold
      } // for (Runway
    } // for (airport
    Acc.setAirport(null);
  }

}
