package eng.jAtcSim.newPacks.views;

import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newPacks.IView;
import eng.jAtcSim.newPacks.context.ViewContext;
import eng.jAtcSim.newPacks.utils.ViewGameInfo;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class AppLogView implements IView {

  private static class MyFocusListener implements WindowFocusListener {
    private final JFrame frm;

    public MyFocusListener(JFrame frm) {
      this.frm = frm;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
      if (frm.isVisible())
        frm.requestFocus();
    }
  }

  private final JTextPane txt = new JTextPane();
  private final JScrollPane scr = new JScrollPane(txt);
  private JPanel parent;

  private final Color bgColor = Color.DARK_GRAY;
  private final Color infoColor = Color.LIGHT_GRAY;
  private final Color warningColor = Color.WHITE;
  private final Color criticalColor = Color.RED;

  @Override
  public void init(JPanel panel, ViewGameInfo initInfo, IReadOnlyMap<String, String> options, ViewContext context) {
    this.parent = panel;
    parent.add(scr);
    // keeps window on top; disabled
    // this.addWindowFocusListener(new eng.jAtcSim.frmPacks.shared.MyFocusListener(this));

    txt.setBackground(bgColor);

    Context.getApp().getAppLog().onNewMessage.add(this::newLogMessage);
  }

  private void newLogMessage(ApplicationLog.AppLogMessage message) {
    String s = message.type.toString() + " : " + message.text;

    Color c;
    switch (message.type) {
      case critical:
        c = criticalColor;
        break;
      case info:
        c = infoColor;
        break;
      case warning:
        c = warningColor;
        break;
      default:
        c = Color.GREEN;
    }
    StyledDocument doc = txt.getStyledDocument();
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setForeground(set, c);
    try {
      doc.insertString(doc.getLength(), s + "\n", set);
    } catch (BadLocationException e) {
      throw new EApplicationException("Unable to print line to FrmLog.");
    }

    JScrollBar bar = scr.getVerticalScrollBar();
    bar.setValue(bar.getMaximum());

    if (message.type != LogItemType.info) {
      forceShowMyFrame();
    }
  }

  private void forceShowMyFrame() {
    JFrame frm = null;
    Component c = this.parent;
    while (c instanceof JFrame == false) {
      c = c.getParent();
    }

    frm = (JFrame) c;
    frm.setVisible(true);
    frm.requestFocus();
  }
}


