package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.SwingRadar.Coloring;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.radarBase.global.Color;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;
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

    this.setPreferredSize(new Dimension(200, 500));

    JButton btn;

    JPanel pnlPlane = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("#ID");
    btn.addActionListener(o -> generatedEvent.raise(plane.callsign().toString()));
    pnlPlane.add(btn);

    btn = new JButton("RC");
    btn.addActionListener(o -> generatedEvent.raise("RC"));
    pnlPlane.add(btn);

    JPanel pnlAtc = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("-> CTR");
    btn.addActionListener(o -> generatedEvent.raise("+" + plane.squawk().toString()));
    pnlAtc.add(btn);

    btn = new JButton("-> TWR");
    btn.addActionListener(o -> generatedEvent.raise("-" + plane.squawk().toString()));
    pnlAtc.add(btn);

    JPanel pnlHS = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("HDG");
    btn.addActionListener(o -> buildHeadingPanel());
    pnlHS.add(btn);

    btn = new JButton("SPD");
    btn.addActionListener(o -> buildSpeedPanel());
    pnlHS.add(btn);

    btn = new JButton("PD");
    btn.addActionListener(o -> buildProceedDirectPanel());
    pnlHS.add(btn);

    JPanel pnlA = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);


    btn = new JButton("ALT↓");
    btn.addActionListener(o -> buildAltitudePanel(true));
    pnlA.add(btn);
    btn = new JButton("ALT↑");
    btn.addActionListener(o -> buildAltitudePanel(false));
    pnlA.add(btn);

    pnlSub = new JPanel();
    pnlSub.setLayout(new FlowLayout());
    pnlSub.setPreferredSize(new Dimension(200, 200));
    pnlSub.setVisible(false);

    JPanel pnlSend = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4);

    btn = new JButton("(send)");
    btn.addActionListener(o -> sendEvent.raise());
    pnlSend.add(btn);

    btn = new JButton("(erase)");
    btn.addActionListener(o -> eraseEvent.raise());
    pnlSend.add(btn);

    LayoutManager.fillBoxPanel(this, LayoutManager.eHorizontalAlign.center, 0,
        pnlPlane, pnlAtc, pnlHS, pnlA, pnlSub, pnlSend);

    LayoutManager.adjustComponents(this, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
      c.setEnabled(false);
    });
    this.setBackground(Coloring.get(frColor));
    pnlSub.setBackground(Coloring.get(frColor));
  }

  private void buildProceedDirectPanel() {
    pnlSub.removeAll();
    pnlSub.setVisible(true);

    JButton lbtn = new JButton("(back)");
    lbtn.addActionListener(o -> pnlSub.setVisible(false));
    pnlSub.add(lbtn);

    for (Navaid navaid : Acc.area().getNavaids()) {
      if (navaid.getType() != Navaid.eType.fix) continue;

      JButton btn = new JButton(navaid.getName());
      btn.addActionListener(o -> {
        String cmd = "PD " + btn.getText();
        this.getGeneratedEvent().raise(cmd);
        pnlSub.removeAll();
        pnlSub.setVisible(false);
      });
      pnlSub.add(btn);
    }
    LayoutManager.adjustComponents(pnlSub, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
    });
    pnlSub.setBackground(Coloring.get(frColor));
  }

  private void buildAltitudePanel(boolean isDescend) {
    pnlSub.setVisible(true);
    List<Integer> alts = new ArrayList();

    if (isDescend) {
      int alt = plane.altitude() / 1000 * 10;
      while (alt >= 30) {
        alts.add(alt);
        alt -= 10;
      }
    } else {
      int alt = plane.altitude() / 1000 * 10;
      while (alt <= 170) {
        alts.add(alt);
        alt += 10;
      }
    }

    pnlSub.removeAll();
    JButton lbtn = new JButton("(back)");
    lbtn.addActionListener(o -> pnlSub.setVisible(false));
    pnlSub.add(lbtn);

    for (Integer alt : alts) {
      JButton btn = new JButton(Integer.toString(alt));
      btn.addActionListener(o -> {
        String cmd = (isDescend ? "DM " : "CM ") + btn.getText();
        this.getGeneratedEvent().raise(cmd);
        pnlSub.removeAll();
        pnlSub.setVisible(false);
      });
      pnlSub.add(btn);
    }
    LayoutManager.adjustComponents(pnlSub, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
    });
    pnlSub.setBackground(Coloring.get(frColor));
  }

  private void buildSpeedPanel() {
    pnlSub.setVisible(true);

    pnlSub.removeAll();
    {
      JButton btn = new JButton("(back)");
      btn.addActionListener(o -> pnlSub.setVisible(false));
      pnlSub.add(btn);

      btn = new JButton("SR");
      btn.addActionListener(o -> {
        String cmd = "SR";
        this.getGeneratedEvent().raise(cmd);
        pnlSub.removeAll();
        pnlSub.setVisible(false);
      });
      pnlSub.add(btn);
    }
    for (int i = 140; i < 300; i += 10) {
      JButton btn = new JButton(Integer.toString(i));
      btn.addActionListener(o -> {
        String cmd = "SE " + btn.getText();
        this.getGeneratedEvent().raise(cmd);
        pnlSub.removeAll();
        pnlSub.setVisible(false);
      });
      pnlSub.add(btn);
    }
    LayoutManager.adjustComponents(pnlSub, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
    });
    pnlSub.setBackground(Coloring.get(frColor));
  }

  private void buildHeadingPanel() {
    pnlSub.setVisible(true);

    pnlSub.removeAll();

    {
      JButton btn = new JButton("(back)");
      btn.addActionListener(o -> pnlSub.setVisible(false));
      pnlSub.add(btn);
    }

    for (int i = 0; i < 360; i += 15) {
      JButton btn = new JButton(Integer.toString(i));
      btn.addActionListener(o -> {
        String cmd = "FH " + btn.getText();
        this.getGeneratedEvent().raise(cmd);
        pnlSub.removeAll();
        pnlSub.setVisible(false);
      });
      pnlSub.add(btn);
    }
    LayoutManager.adjustComponents(pnlSub, (c) -> {
      c.setBackground(Coloring.get(bgColor));
      c.setForeground(Coloring.get(frColor));
    });
    pnlSub.setBackground(Coloring.get(frColor));
  }
}
