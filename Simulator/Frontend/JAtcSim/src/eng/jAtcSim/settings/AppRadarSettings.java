package eng.jAtcSim.settings;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import exml.IXPersistable;
import exml.annotations.XAttribute;

import java.nio.file.Path;

public class AppRadarSettings implements IXPersistable {
  public static class DisplaySettings implements IXPersistable {

    public static DisplaySettings load(XElement elm) {
      DisplaySettings ret = new DisplaySettings();

      ret.tma = SmartXmlLoaderUtils.loadBoolean(elm, "tma");
      ret.country = SmartXmlLoaderUtils.loadBoolean(elm, "country");
      ret.mrva = SmartXmlLoaderUtils.loadBoolean(elm, "mrva");
      ret.mrvaLabel = SmartXmlLoaderUtils.loadBoolean(elm, "mrvaLabel");
      ret.ctr = SmartXmlLoaderUtils.loadBoolean(elm, "ctr");
      ret.vor = SmartXmlLoaderUtils.loadBoolean(elm, "vor");
      ret.ndb = SmartXmlLoaderUtils.loadBoolean(elm, "ndb");
      ret.sid = SmartXmlLoaderUtils.loadBoolean(elm, "sid");
      ret.star = SmartXmlLoaderUtils.loadBoolean(elm, "star");
      ret.fix = SmartXmlLoaderUtils.loadBoolean(elm, "fix");
      ret.routeFix = SmartXmlLoaderUtils.loadBoolean(elm, "routeFix");
      ret.minorFix = SmartXmlLoaderUtils.loadBoolean(elm, "minorFix");
      ret.rings = SmartXmlLoaderUtils.loadBoolean(elm, "rings");
      ret.history = SmartXmlLoaderUtils.loadBoolean(elm, "history");
      ret.minAltitude = SmartXmlLoaderUtils.loadInteger(elm, "minAltitude");
      ret.maxAltitude = SmartXmlLoaderUtils.loadInteger(elm, "maxAltitude");
      return ret;
    }

    @XAttribute public boolean airport = true;
    @XAttribute public boolean country = true;
    @XAttribute public boolean ctr = true;
    @XAttribute public boolean fix = true;
    @XAttribute public boolean history = true;
    @XAttribute public int maxAltitude = 99000;
    @XAttribute public int minAltitude = 0;
    @XAttribute public boolean minorFix = true;
    @XAttribute public boolean mrva = true;
    @XAttribute public boolean mrvaLabel = true;
    @XAttribute public boolean ndb = true;
    @XAttribute public boolean rings = true;
    @XAttribute public boolean routeFix = true;
    @XAttribute public boolean sid = true;
    @XAttribute public boolean star = true;
    @XAttribute public boolean tma = true;
    @XAttribute public boolean vor = true;

    public RadarDisplaySettings toRadarDisplaySettings() {
      RadarDisplaySettings ret = new RadarDisplaySettings();
      ret.setAirportVisible(this.airport);
      ret.setCountryBorderVisible(this.country);
      ret.setCtrBorderVisible(this.ctr);
      ret.setFixMinorVisible(this.minorFix);
      ret.setFixRouteVisible(this.routeFix);
      ret.setFixVisible(this.fix);
      ret.setMrvaBorderAltitudeVisible(this.mrvaLabel);
      ret.setMrvaBorderVisible(this.mrva);
      ret.setNdbVisible(this.ndb);
      ret.setRingsVisible(this.rings);
      ret.setSidVisible(this.sid);
      ret.setStarVisible(this.star);
      ret.setTmaBorderVisible(this.tma);
      ret.setVorVisible(this.vor);
      ret.setPlaneHistoryVisible(this.history);
      ret.setMinAltitude(this.minAltitude);
      ret.setMaxAltitude(this.maxAltitude);
      return ret;
    }
  }

  public DisplaySettings displaySettings;
  public int displayTextDelay;
  public Path styleSettingsFile;
}
