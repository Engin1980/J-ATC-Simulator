package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class FrmLog extends JFrame {
  private final JTextPane txt = new JTextPane();
  private final JScrollPane scr = new JScrollPane(txt);

  private final Color bgColor = Color.DARK_GRAY;
  private final Color infoColor = Color.LIGHT_GRAY;
  private final Color warningColor = Color.WHITE;
  private final Color criticalColor = Color.RED;

  public FrmLog() {

    this.setTitle("JAtcSim - fast log view");
    this.setPreferredSize(new Dimension(600, 300));
    this.scr.setPreferredSize(this.getPreferredSize());
    this.getContentPane().add(scr);
    this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    this.pack();
    this.addWindowFocusListener(new MyFocusListener(this));

    Stylist.apply(this, true);

    txt.setBackground(bgColor);

    SharedAcc.getAppLog().getOnNewMessage().add(this::newLogMessage);
  }

  private void newLogMessage(ApplicationLog.Message message) {
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

    if (message.type != ApplicationLog.eType.info) {
      this.setVisible(true);
      this.requestFocus();
    }
  }
}

class MyFocusListener implements WindowFocusListener {
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
