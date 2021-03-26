package eng.jAtcSim.app.controls;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

  private BufferedImage image;
  private final int width;
  private final int height;

  public ImagePanel(int width, int height) {
    this.width = width;
    this.height = height;
    Dimension d = new Dimension(width, height);
    this.setPreferredSize(d);
    this.setMinimumSize(d);
    this.setMaximumSize(d);
  }

  public synchronized void paint(Graphics g) {
    super.paint(g);
    if (this.image != null)
      g.drawImage(image, 0, 0, width, height, null);
  }

  public synchronized void setImage(BufferedImage image) {
    this.image = image;
  }
}
