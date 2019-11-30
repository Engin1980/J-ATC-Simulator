/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.area;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

/**
 * @author Marek
 */
public final class Area {

  private IList<Airport> airports;
  private NavaidList navaids;
  private IList<Border> borders;
  private String icao;

  public static Area loadArea(XElement element) {
    Area ret = new Area();
    ret.icao = XmlLoader.loadString(element, "icao");
    ret.navaids = new NavaidList();
    XmlLoader.loadList(
        element.getChild("navaids").getChildren("navaid"),
        ret.navaids,
        q -> Navaid.load(q));
    ret.borders = new EList<>();
    XmlLoader.loadList(
        element.getChild("borders").getChildren("border"),
        ret.borders,
        q-> Border.load(q, ret));
    ret.airports = new EDistinctList<>();
    XmlLoader.loadList(
        element.getChild("airports").getChildren("airport"),
        ret.airports,
        a -> Airport.load(a, ret)
    );
    return ret;
  }

  private Area(){}

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

}
