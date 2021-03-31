package eng.jAtcSim.settings;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.EApplicationException;
import exml.IXPersistable;
import exml.annotations.XAttribute;
import exml.implemented.AwtFontDeserializer;
import exml.implemented.HexToAwtColorParser;
import exml.loading.XLoadContext;

import java.awt.*;
import java.nio.file.Path;

public class FlightStripSettings implements IXPersistable {
  public static class Clrs implements IXPersistable {
    @XAttribute public Color even;
    @XAttribute public Color odd;
  }

  public Dimension flightStripSize;
  public Dimension scheduledFlightStripSize;
  public int stripBorder;
  public Color textColor;
  public Clrs twr;
  public Clrs ctr;
  public Clrs app;
  public Color uncontrolled;
  public Color airprox;
  public Color selected;
  public Font font;

  public static FlightStripSettings load(Path fileName) {
    FlightStripSettings ret;
    try {
      XLoadContext ctx = new XLoadContext().withDefaultParsers();
      ctx.setDeserializer(
              Dimension.class,
              e -> new Dimension(
                      Integer.parseInt(e.getAttribute("width")),
                      Integer.parseInt(e.getAttribute("height"))));
      ctx.setParser(Color.class, new HexToAwtColorParser());
      ctx.setDeserializer(java.awt.Font.class, new AwtFontDeserializer());

      ret = ctx.loadObject(XDocument.load(fileName).getRoot(), FlightStripSettings.class);
    } catch (Exception ex) {
      throw new EApplicationException("Failed to load flight-strip-settings from " + fileName, ex);
    }
    return ret;
  }
}
