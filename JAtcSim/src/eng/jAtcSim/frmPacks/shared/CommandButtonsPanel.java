package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.SwingRadar.Coloring;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.radarBase.global.Color;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CommandButtonsPanel extends JPanel {

  private final static Color bgColor = new Color(176, 176, 0);
  private final static Color frColor = new Color(0, 0, 0);

  private final EventAnonymous<String> generatedEvent = new EventAnonymous();
  private final EventAnonymousSimple sendEvent = new EventAnonymousSimple();
  private final EventAnonymousSimple eraseEvent = new EventAnonymousSimple();
  private Airplane.Airplane4Display plane;
  private JPanel pnlSub;

  public CommandButtonsPanel() {
    initComponents();
  }

  public EventAnonymous<String> getGeneratedEvent() {
    return generatedEvent;
  }

  public EventAnonymousSimple getSendEvent() {
    return sendEvent;
  }

  public EventAnonymousSimple getEraseEvent() {
    return eraseEvent;
  }

  public void setPlane(Airplane.Airplane4Display plane) {
    this.plane = plane;
    if (this.plane == null) {
      LayoutManager.adjustComponents(this, c -> c.setEnabled(false));
    } else {
      LayoutManager.adjustComponents(this, c -> c.setEnabled(true));
    }
  }

  public void setPlane(Callsign callsign) {
    if (callsign == null)
      setPlane((Airplane.Airplane4Display) null);
    else {
      Airplane.Airplane4Display plane;
      for (Airplane.Airplane4Display item : Acc.sim().getPlanesToDisplay()) {
        if (item.callsign() == callsign) {
          setPlane(item);
          break;
        }
      }
    }
  }

  private void initComponents() {

    JButton btn;

    JPanel pnlPlane = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("#ID");
    btn.addActionListener(o -> generatedEvent.raise(plane.callsign().toString()));
    pnlPlane.add(btn);

    btn = new JButton("rc");
    btn.addActionListener(o -> generatedEvent.raise("RC"));
    pnlPlane.add(btn);

    JPanel pnlAtc = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("-> CTR");
    btn.addActionListener(o -> generatedEvent.raise("+" + plane.squawk().toString()));
    pnlAtc.add(btn);

    btn = new JButton("-> TWR");
    btn.addActionListener(o -> generatedEvent.raise("-" + plane.squawk().toString()));
    pnlAtc.add(btn);

    JPanel pnlHSA = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("HDG");
    btn.addActionListener(o -> buildHeadingPanel());
    pnlHSA.add(btn);

    btn = new JButton("SPD");
    btn.addActionListener(o -> buildSpeedPanel());
    pnlHSA.add(btn);

    btn = new JButton("ALT↓");
    btn.addActionListener(o -> buildAltitudePanel(true));
    pnlHSA.add(btn);
    btn = new JButton("ALT↑");
    btn.addActionListener(o -> buildAltitudePanel(false));
    pnlHSA.add(btn);

    pnlSub = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    JPanel pnlSend = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("(send)");
    btn.addActionListener(o -> sendEvent.raise());
    pnlSend.add(btn);

    btn = new JButton("(erase)");
    btn.addActionListener(o -> eraseEvent.raise());
    pnlSend.add(btn);

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.center, 4,
        pnlPlane, pnlAtc, pnlHSA, pnlSub, pnlSend);

    LayoutManager.adjustComponents(this, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
      c.setEnabled(false);
    });
    this.setBackground(Coloring.get(frColor));
  }

  private void buildAltitudePanel(boolean isDescend) {
    List<Integer> alts = new ArrayList();

    if (isDescend){
      int alt = plane.altitude() / 1000 * 10;
      while (alt >= 30){
        alts.add(alt);
        alt-=10;
      }
    } else {
      int alt = plane.altitude() / 1000*10;
      while (alt >= 170){
        alts.add(alt);
        alt+=10;
      }
    }

    pnlSub.removeAll();
    for (Integer alt : alts) {
      JButton btn = new JButton(Integer.toString(alt));

      pnlSub.add(btn);
    }
    LayoutManager.adjustComponents(pnlSub, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
    });
  }

  private void buildSpeedPanel() {
  }

  private void buildHeadingPanel() {

  }
}
