package eng.jAtcSim.startup.controls;

import javax.swing.*;
import java.awt.*;

public class IntroButton extends JPanel {

  private String text = "IntroButton";
  private Color backColor;
  private Color foreColor;

  public IntroButton(String text) {
    super(true);
    this.setOpaque(false);
    this.text = text;
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
    gg.setComposite(AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER, 0.3f));
    gg.setColor(Color.yellow);
    gg.fillOval(10, 10, 120, 30);

    gg.setColor(Color.black);
    gg.fillRect(0, 0, g.getClipBounds().width , g.getClipBounds().height );
  }
}
