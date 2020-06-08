package eng.airCompanyFleetDownloader;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eXmlSerialization.annotations.XmlOptional;

public class AirlineInfo {
  private final String name;
  private final String url;
  private final boolean active;
  @XmlOptional
  private IList<String> codes =new EList<>();
  @XmlOptional
  private IMap<String, Integer> fleet = new EMap<>();
  @XmlOptional
  private boolean decoded = false;

  private AirlineInfo() {
    name = null;
    url = null;
    active = false;
  }

  public IMap<String, Integer> getFleet() {
    return fleet;
  }

  public IList<String> getCodes() {
    return codes;
  }

  public AirlineInfo(String name, String url, boolean active) {
    this.name = name;
    this.url = url;
    this.active = active;
  }

  public boolean isDecoded() {
    return decoded;
  }

  public void setDecoded(boolean decoded) {
    this.decoded = decoded;
  }

  public boolean isActive() {
    return active;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "AirlineInfo{" +
        "name='" + name + '\'' +
        ", url='" + url + '\'' +
        ", active=" + active +
        '}';
  }
}
