package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

public class Panel extends Block {
  private String id;
  private final String view;

  public Panel(String view) {
    EAssert.Argument.isNotNull(view, "view");
    this.view = view;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getView() {
    return view;
  }
}
