package eng.jAtcSim.newLib.area;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class Area {

  public static Area load(XElement source) {
    Area area = new Area();
    area.read(source);
    area.checkForDuplicits();
    return area;
  }

  private IList<Airport> airports;
  private NavaidList navaids;
  private IList<Border> borders;
  private String icao;

  private Area() {
  }

  private void checkForDuplicits() {
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

  public void checkSanity() {
    checkForDuplicits();
//    checkRouteCommands();
  }

  public IList<Airport> getAirports() {
    return airports;
  }

  public IList<Border> getBorders() {
    return borders;
  }

  public String getIcao() {
    return icao;
  }

  public NavaidList getNavaids() {
    return navaids;
  }

  private void read(XElement source) {
    XmlLoader.setContext(source);
    this.icao = XmlLoader.loadString("icao");

    this.navaids = new NavaidList();
    XmlLoader.loadList(
        source.getChild("navaids").getChildren("navaid"),
        navaids,
        q -> Navaid.load(q)
    );

    this.borders = new EList<>();
    XmlLoader.loadList(
        source.getChild("borders").getChildren("border"),
        this.borders,
        q -> Border.load(q, this));

    this.airports = new EList<>();
    XmlLoader.loadList(
        source.getChild("airports").getChildren("airport"),
        this.airports,
        q -> Airport.load(q, this));
  }

  private void tryFailDuplicits(String type, ISet<String> items) {
    if (items.isEmpty()) return;

    EStringBuilder sb = new EStringBuilder();
    sb.append(items.size() + " duplicate record(s) were found in " + type + ". E.g.: ");
    sb.appendItems(items, q -> q, "; ");
    throw new EApplicationException(sb.toString());
  }

}
