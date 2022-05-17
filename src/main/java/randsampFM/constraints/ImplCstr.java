package randsampFM;

public class ImplCstr extends Constraint {
  private final Constraint left;
  private final Constraint right;
  
  public ImplCstr(final Constraint left, final Constraint right) {
    this.left = left;
    this.right = rigth;
  }
}
