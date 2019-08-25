package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class Route {
  protected static IList<XElement> lookForElementRecursively(XElement source, String elementName) {
    IList<XElement> ret = new EList<>();
    extractElementsRecursively(source, elementName, ret);
    return ret;
  }

  private static void extractElementsRecursively(XElement source, String elementName, IList<XElement> targetList) {
    for (XElement child : source.getChildren()) {
      if (child.getName().equals(elementName))
        targetList.add(child);
      else
        extractElementsRecursively(child, elementName, targetList);
    }
  }

  protected static IList<IAtcCommand> loadCommands(XElement source, NavaidList navaids) {
    IList<IAtcCommand> ret = new EList<>();
    IAtcCommand cmd;
    for (XElement child : source.getChildren()) {
      switch (child.getName()) {
        case "then":
          cmd = loadThen(child);
          break;
        case "proceedDirect":
          cmd = loadProceedDirect(child, navaids);
          break;
        case "speed":
          cmd = loadSpeed(child);
          break;
        case "altitude":
          cmd = loadAltitude(child);
          break;
        case "heading":
          cmd = loadHeading(child);
          break;

        analyse another kinds of commands

        default:
          throw new EApplicationException(sf(
              "Cannot load route command from xml-element '%s'. Unknown element name.", child.getName()));
      }
      ret.add(cmd);
    }

    return ret;
  }

  private static IAtcCommand loadHeading(XElement source) {
    int value = XmlLoader.loadInteger(source, "value", true);
    String restrictionS = XmlLoader.loadString(source, "restriction", false);
    ChangeHeadingCommand.eDirection direction;
    if (restrictionS == null) {
      direction = ChangeHeadingCommand.eDirection.any;
    } else
      switch (restrictionS) {
        case "left":
          direction = ChangeHeadingCommand.eDirection.left;
          break;
        case "right":
          direction = ChangeHeadingCommand.eDirection.right;
          break;
        case "nearest":
          direction = ChangeHeadingCommand.eDirection.any;
          break;
        default:
          throw new EEnumValueUnsupportedException(restrictionS);
      }
    ChangeHeadingCommand cmd = new ChangeHeadingCommand(value, direction);
    return cmd;
  }

  private static IAtcCommand loadAltitude(XElement source) {
    int value = XmlLoader.loadInteger(source, "value", true);
    String restrictionS = XmlLoader.loadString(source, "restriction", true);
    ChangeAltitudeCommand.eDirection restrictionDirection;
    switch (restrictionS) {
      case "descend":
        restrictionDirection = ChangeAltitudeCommand.eDirection.descend;
        break;
      case "climb":
        restrictionDirection = ChangeAltitudeCommand.eDirection.climb;
        break;
      case "set":
        restrictionDirection = ChangeAltitudeCommand.eDirection.any;
        break;
      default:
        throw new EEnumValueUnsupportedException(restrictionS);
    }
    ChangeAltitudeCommand cmd = new ChangeAltitudeCommand(restrictionDirection, value);
    return cmd;
  }

  private static IAtcCommand loadThen(XElement source) {
    return new ThenCommand();
  }

  private static IAtcCommand loadProceedDirect(XElement source, NavaidList navaids) {
    String navaidName = XmlLoader.loadString(source, "fix", true);
    Navaid navaid = navaids.get(navaidName);
    ProceedDirectCommand cmd = new ProceedDirectCommand(navaid);
    return cmd;
  }

  private static IAtcCommand loadSpeed(XElement source) {
    int value = XmlLoader.loadInteger(source, "value", true);
    String restrictionS = XmlLoader.loadString(source, "restriction", true);
    Restriction.eDirection restrictionDirection;
    switch (restrictionS) {
      case "below":
        restrictionDirection = Restriction.eDirection.atMost;
        break;
      case "above":
        restrictionDirection = Restriction.eDirection.atLeast;
        break;
      case "exactly":
        restrictionDirection = Restriction.eDirection.exactly;
        break;
      default:
        throw new EEnumValueUnsupportedException(restrictionS);
    }
    ChangeSpeedCommand cmd = new ChangeSpeedCommand(restrictionDirection, value);
    return cmd;
  }

  private final IList<IAtcCommand> routeCommands = new EList<>();
  private final IList<String> mapping = new EList<>();

  public Route(String mapping, IList<IAtcCommand> routeCommands) {
    this.mapping.add(mapping.split(";"));
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
}
