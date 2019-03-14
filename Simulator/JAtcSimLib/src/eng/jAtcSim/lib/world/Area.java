/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IlsApproach;

/**
 * @author Marek
 */
public class Area {

  private final IList<Airport> airports;
  private final NavaidList navaids;
  private final IList<Border> borders;
  private final String icao;

  public Area(String icao, IList<Airport> airports, NavaidList navaids, IList<Border> borders) {
    this.icao = icao;
    this.airports = airports;
    this.navaids = navaids;
    this.borders = borders;
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

  public void checkSanity() {
    checkForDuplicits();
//    checkRouteCommands();
  }

  public void checkForDuplicits() {
    ISet<String> set;

    set = this.airports.getDuplicateItems(q -> q.getIcao()).select(q -> q.getIcao()).selectCount(5);
    tryFailDuplicits("Airports", set);

    set = this.navaids.getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(5);
    tryFailDuplicits("Navaids", set);

    for (Airport airport : this.airports) {
      set = airport.getRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
      tryFailDuplicits("Active runways of " + airport.getName(), set);

      set = airport.getInactiveRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
      tryFailDuplicits("Inactive runways of " + airport.getName(), set);

      set = airport.getAtcTemplates().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
      tryFailDuplicits("ATC templates of " + airport.getName(), set);

      set = airport.getHolds().getDuplicateItems(q -> q.getNavaid().getName()).select(q -> q.getNavaid().getName()).selectCount(3);
      tryFailDuplicits("Published holds of " + airport.getName(), set);

      for (ActiveRunway runway : airport.getRunways()) {
        set = runway.getThresholds().getDuplicateItems(q -> q.getName()).select(q -> q.getName());
        tryFailDuplicits("Active runway thresholds of " + runway.getName() + " of " + airport.getName(), set);

        for (ActiveRunwayThreshold runwayThreshold : runway.getThresholds()) {
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
    sb.appendItems(items, q -> q, "; ");
    throw new EApplicationException(sb.toString());
  }

  // pobably useless as everything is now parsed when converted from xml -> model
//  private void checkRouteCommands() {
//    Parser parser = new ShortBlockParser();
//    for (Airport a : this.getAirports()) {
//      Acc.setAirport(a);
//      for (ActiveRunway r : a.getRunways()) {
//        for (ActiveRunwayThreshold t : r.getThresholds()) {
//          for (Approach p : t.getApproaches()) {
//            try {
//              parser.parseMultipleCommands(p.getGaRoute());
//            } catch (Exception ex) {
//              throw new EApplicationException(
//                  String.format("airport %s runway %s approach %s has invalid go-around route fromAtc: %s (error: %s)",
//                      a.getIcao(), t.getName(), p.toString(), p.getGaRoute(), ex.getMessage()));
//            }
//          } // for (Approach
//
//          for (Route o : t.getRoutes()) {
//            try {
//              parser.parseMultipleCommands(o.get o .getRoute());
//            } catch (Exception ex) {
//              throw new EApplicationException(
//                  String.format("airport %s runway %s route %s has invalid fromAtc: %s (error: %s)",
//                      a.getIcao(), t.getName(), o.getName(), o.getRoute(), ex.getMessage()));
//            }
//          }
//        } // for (RunwayThreshold
//      } // for (Runway
//    } // for (airport
//    Acc.setAirport(null);
//  }

}
