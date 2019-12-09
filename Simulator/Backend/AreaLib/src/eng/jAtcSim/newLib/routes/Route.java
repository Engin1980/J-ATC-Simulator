package eng.jAtcSim.newLib.routes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Parentable;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.sharedLib.exceptions.ToDoException;

import javax.swing.text.StyledEditorKit;

public abstract class Route extends Parentable<Airport> {

  //  protected static IList<XElement> lookForElementRecursively(XElement source, String elementName) {
//    IList<XElement> ret = new EList<>();
//    extractElementsRecursively(source, elementName, ret);
//    return ret;
//  }
//
//  private static void extractElementsRecursively(XElement source, String elementName, IList<XElement> targetList) {
//    for (XElement child : source.getChildren()) {
//      if (child.getName().equals(elementName))
//        targetList.add(child);
//      else
//        extractElementsRecursively(child, elementName, targetList);
//    }
//  }
//
//  protected static IList<IAtcCommand> loadCommands(XElement source, NavaidList navaids, IReadOnlyList<PublishedHold> holds) {
//    IList<IAtcCommand> ret = new EList<>();
//    IAtcCommand cmd;
//    for (XElement child : source.getChildren()) {
//      switch (child.getName()) {
//        case "then":
//          cmd = loadThen(child);
//          break;
//        case "proceedDirect":
//          cmd = loadProceedDirect(child, navaids);
//          break;
//        case "speed":
//          cmd = loadSpeed(child);
//          break;
//        case "altitude":
//          cmd = loadAltitude(child);
//          break;
//        case "heading":
//          cmd = loadHeading(child);
//          break;
//        case "hold":
//          cmd = loadHold(child, navaids, holds);
//          break;
//        default:
//          throw new EApplicationException(sf(
//              "Cannot load route command from xml-element '%s'. Unknown element name.", child.getName()));
//      }
//      ret.add(cmd);
//    }
//
//    return ret;
//  }
  private final IList<IAtcCommand> routeCommands = new EList<>();
  //
//  private static IAtcCommand loadHold(XElement source, NavaidList navaids, IReadOnlyList<PublishedHold> publishedHolds) {
//    String navaidName = XmlLoader.loadString(source, "fix");
//    String turns = XmlLoader.loadStringRestricted(source, "turns", new String[]{"left", "right"}, null);
//    Integer inboundRadial = XmlLoader.loadInteger(source, "inboundRadial", null);
//    HoldCommand ret;
//    if (inboundRadial == null) {
//      PublishedHold hold = publishedHolds.getFirst(
//          q -> q.getNavaid().getName().equals(navaidName),
//          new EApplicationException(
//              sf("Unable to find published hold over '%s' required to load hold command.", navaidName)));
//      ret = new HoldCommand(hold);
//    } else {
//      Navaid navaid = navaids.get(navaidName);
//      ret = new HoldCommand(navaid, inboundRadial, turns.equals("left"));
//    }
//    return ret;
//  }
//
//  private static IAtcCommand loadHeading(XElement source) {
//    int value = XmlLoader.loadInteger(source, "value");
//    String restrictionS = XmlLoader.loadStringRestricted(source, "restriction",
//        new String[]{"left", "right", "nearest"}, "nearest");
//    ChangeHeadingCommand.eDirection direction;
//    if (restrictionS == null) {
//      direction = ChangeHeadingCommand.eDirection.any;
//    } else
//      switch (restrictionS) {
//        case "left":
//          direction = ChangeHeadingCommand.eDirection.left;
//          break;
//        case "right":
//          direction = ChangeHeadingCommand.eDirection.right;
//          break;
//        case "nearest":
//          direction = ChangeHeadingCommand.eDirection.any;
//          break;
//        default:
//          throw new EEnumValueUnsupportedException(restrictionS);
//      }
//    ChangeHeadingCommand cmd = new ChangeHeadingCommand(value, direction);
//    return cmd;
//  }
//
//  private static IAtcCommand loadAltitude(XElement source) {
//    int value = XmlLoader.loadInteger(source, "value");
//    String restrictionS = XmlLoader.loadStringRestricted(source, "restriction",
//    new String[]{"descend", "climb", "set"}, "set");
//    ChangeAltitudeCommand.eDirection restrictionDirection;
//    switch (restrictionS) {
//      case "descend":
//        restrictionDirection = ChangeAltitudeCommand.eDirection.descend;
//        break;
//      case "climb":
//        restrictionDirection = ChangeAltitudeCommand.eDirection.climb;
//        break;
//      case "set":
//        restrictionDirection = ChangeAltitudeCommand.eDirection.any;
//        break;
//      default:
//        throw new EEnumValueUnsupportedException(restrictionS);
//    }
//    ChangeAltitudeCommand cmd = new ChangeAltitudeCommand(restrictionDirection, value);
//    return cmd;
//  }
//
//  private static IAtcCommand loadThen(XElement source) {
//    return new ThenCommand();
//  }
//
//  private static IAtcCommand loadProceedDirect(XElement source, NavaidList navaids) {
//    String navaidName = XmlLoader.loadString(source, "fix");
//    Navaid navaid = navaids.get(navaidName);
//    ProceedDirectCommand cmd = new ProceedDirectCommand(navaid);
//    return cmd;
//  }
//
//  private static IAtcCommand loadSpeed(XElement source) {
//    int value = XmlLoader.loadInteger(source, "value");
//    String restrictionS = XmlLoader.loadStringRestricted(source, "restriction",
//        new String[]{"below", "above", "exactly"}, "exactly");
//    Restriction.eDirection restrictionDirection;
//    switch (restrictionS) {
//      case "below":
//        restrictionDirection = Restriction.eDirection.atMost;
//        break;
//      case "above":
//        restrictionDirection = Restriction.eDirection.atLeast;
//        break;
//      case "exactly":
//        restrictionDirection = Restriction.eDirection.exactly;
//        break;
//      default:
//        throw new EEnumValueUnsupportedException(restrictionS);
//    }
//    ChangeSpeedCommand cmd = new ChangeSpeedCommand(restrictionDirection, value);
//    return cmd;
//  }
  private final IList<String> mapping = new EList<>();

  protected Route() {
  }

  protected Route(IReadOnlyList<IAtcCommand> routeCommands) {
    this.routeCommands.add(routeCommands);
  }

  public IReadOnlyList<IAtcCommand> getRouteCommands() {
    return routeCommands;
  }

  public boolean isMappingMatch(IList<String> otherMapping) {

    return mapping.isAny(q -> otherMapping.contains(q));
  }

  public boolean isMappingMatch(String otherMapping) {
    assert otherMapping != null;
    IList<String> tmp = new EList<>(otherMapping.split(";"));
    return isMappingMatch(tmp);
  }

  public void read(XElement source, String mapping) {
    this.mapping.add(mapping.toLowerCase().split(";"));
    IReadOnlyList<XElement> routeElements = source.getChildren();
    for (XElement routeElement : routeElements) {
      IAtcCommand cmd = XmlRouteFactory.load(
          routeElement, this.getParent().getParent());
      this.routeCommands.add(cmd);
    }
  }

  protected void fill(IList<IAtcCommand> routeCommands){
    if (routeCommands == null) {
        throw new IllegalArgumentException("Value of {routeCommands} cannot not be null.");
    }
    this.routeCommands.clear();
    this.routeCommands.add(routeCommands);
  }
}
