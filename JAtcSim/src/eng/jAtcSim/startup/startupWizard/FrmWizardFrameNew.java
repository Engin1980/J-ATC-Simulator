package eng.jAtcSim.startup.startupWizard;

import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class FrmWizardFrameNew extends JFrame {

  private StartupWizard wizard;
  private int pageIndex;
  private JWizardPanel pnl;

  public FrmWizardFrameNew(StartupWizard wizard, int pageIndex, JWizardPanel content) {
    this.wizard = wizard;
    this.pageIndex = pageIndex;
    this.pnl = content;

    JPanel pnlBottom = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 8);

    JButton btn;
    btn = new JButton("<<");
    btn.addActionListener(o -> navigate(false,-1));
    pnlBottom.add(btn);
    btn = new JButton(">>");
    btn.addActionListener(o -> {
      if (this.pnl.doWizardValidation()) {
        navigate(true, 1);
      }
    });
    pnlBottom.add(btn);

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnlBottom, BorderLayout.PAGE_END);
    this.getContentPane().add(content, BorderLayout.CENTER);

    this.pack();
  }

  private void navigate(boolean isOk, int delta) {
    this.setVisible(false);
    if (isOk)
      wizard.navigateToPage(pageIndex, pageIndex + delta);
    else
      wizard.navigateToPage(pageIndex + delta);
  }
}
