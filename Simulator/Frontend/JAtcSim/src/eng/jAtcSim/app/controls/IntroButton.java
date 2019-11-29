package eng.jAtcSim.app.controls;

import eng.eSystem.events.EventSimple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class IntroButton extends JPanel {

  private String text = "IntroButton";
  private Color backColor = new Color(255, 255, 150, 128);
  private Color foreColor= new Color(0,0,255);
  private boolean isMouseOver = false;
  private final EventSimple<IntroButton> onClick = new EventSimple<>(this);

  public EventSimple<IntroButton> getOnClick() {
    return onClick;
  }

  public IntroButton(String text) {
    super(true);
    this.setOpaque(false);
    this.text = text;

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        this_mouseClick();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        this_mouseEnter();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        this_mouseExit();
      }
    });
  }

  private void this_mouseExit() {
    this.isMouseOver = false;
    this.repaint();
  }

  private void this_mouseEnter() {
    this.isMouseOver = true;
    this.repaint();
  }

  private void this_mouseClick() {
    this.onClick.raise();
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    if (text == null) text = "";
    this.text = text;
    this.revalidate();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D gg = (Graphics2D) g;
    gg.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
//
    gg.setComposite(AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER, 0.3f));
    gg.setColor(this.backColor);
    gg.fillRoundRect(0, 0, g.getClipBounds().width, g.getClipBounds().height, 48, 48);

    gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    gg.setColor(this.foreColor);

    Rectangle2D rect = gg.getFontMetrics().getStringBounds(this.text, gg).getBounds2D();
    int x = g.getClipBounds().width / 2;
    int y = g.getClipBounds().height / 2;
    x = x - (int) rect.getWidth() / 2;
    y = y + (int) rect.getHeight() / 2;
    gg.drawString(this.text, x, y);

    if (isMouseOver)
    {
      gg.setColor(Color.white);
      gg.setStroke(new BasicStroke(5));
      gg.drawRoundRect(0, 0, g.getClipBounds().width, g.getClipBounds().height, 48, 48);
    }
  }
}
