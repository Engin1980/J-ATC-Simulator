package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class Area {

//  public static class XmlLoader {
//    public static Area load(XElement source) {
//      Area area = new Area();
//      read(source, area);
//      checkForDuplicits(area);
//      return area;
//    }
//
//    private static void read(XElement source, Area area) {
//      XmlLoaderUtils.setContext(source);
//      area.icao = XmlLoaderUtils.loadString("icao");
//
//      area.navaids = new NavaidList();
//      XmlLoaderUtils.loadList(
//          source.getChild("navaids").getChildren("navaid"),
//          area.navaids,
//          q -> Navaid.XmlLoader.load(q)
//      );
//
//      area.borders = new EList<>();
//      XmlLoaderUtils.loadList(
//          source.getChild("borders").getChildren("border"),
//          area.borders,
//          q -> Border.XmlLoader.load(q, area));
//
//      area.airports = new EList<>();
//      XmlLoaderUtils.loadList(
//          source.getChild("airports").getChildren("airport"),
//          area.airports,
//          q -> Airport.XmlLoader.load(q, area));
//    }
//
//    private static void checkForDuplicits(Area area) {
//      ISet<String> set;
//
//      set = area.airports.getDuplicateItems(q -> q.getIcao()).select(q -> q.getIcao()).selectCount(5);
//      tryFailDuplicits("Airports", set);
//
//      set = area.navaids.getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(5);
//      tryFailDuplicits("Navaids", set);
//
//      for (Airport airport : area.airports) {
//        set = airport.getRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
//        tryFailDuplicits("Active runways of " + airport.getName(), set);
//
//        set = airport.getInactiveRunways().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
//        tryFailDuplicits("Inactive runways of " + airport.getName(), set);
//
//        set = airport.getAtcTemplates().getDuplicateItems(q -> q.getName()).select(q -> q.getName()).selectCount(3);
//        tryFailDuplicits("ATC templates of " + airport.getName(), set);
//
//        set = airport.getHolds().getDuplicateItems(q -> q.getNavaid().getName()).select(q -> q.getNavaid().getName()).selectCount(3);
//        tryFailDuplicits("Published holds of " + airport.getName(), set);
//
//        for (ActiveRunway runway : airport.getRunways()) {
//          set = runway.getThresholds().getDuplicateItems(q -> q.getName()).select(q -> q.getName());
//          tryFailDuplicits("Active runway thresholds of " + runway.getName() + " of " + airport.getName(), set);
//        }
//
//        for (InactiveRunway runway : airport.getInactiveRunways()) {
//          set = runway.getThresholds().getDuplicateItems(q -> q.getName()).select(q -> q.getName());
//          tryFailDuplicits("Inactive runway thresholds of " + runway.getName() + " of " + airport.getName(), set);
//        }
//      }
//    }
//
//    private static void tryFailDuplicits(String type, ISet<String> items) {
//      if (items.isEmpty()) return;
//
//      EStringBuilder sb = new EStringBuilder();
//      sb.append(items.size() + " duplicate record(s) were found in " + type + ". E.g.: ");
//      sb.appendItems(items, q -> q, "; ");
//      throw new EApplicationException(sb.toString());
//    }
//  }

  private final IList<Airport> airports;
  private final NavaidList navaids;
  private final IList<Border> borders;
  private final String icao;

  public static Area create(String icao, IList<Airport> airports, NavaidList navaids, IList<Border> borders){
    return new Area(icao, airports, navaids, borders);
  }

  private Area(String icao, IList<Airport> airports, NavaidList navaids, IList<Border> borders) {
    EAssert.Argument.isNotNull(icao, "Parameter 'icao' cannot be null.");
    EAssert.Argument.isNotNull(navaids, "Parameter 'navaids' cannot be null.");
    EAssert.Argument.isNotNull(borders, "Parameter 'borders' cannot be null.");
    EAssert.Argument.isNotNull(airports, "Parameter 'airports' cannot be null.");
    this.airports = airports;
    this.navaids = navaids;
    this.borders = borders;
    this.icao = icao;

    this.airports.forEach(q->{
      q.setParent(this);
      q.bind();
    });
  }

  public IReadOnlyList<Airport> getAirports() {
    return airports;
  }

  public IReadOnlyList<Border> getBorders() {
    return borders;
  }

  public String getIcao() {
    return icao;
  }

  public NavaidList getNavaids() {
    return navaids;
  }

}
