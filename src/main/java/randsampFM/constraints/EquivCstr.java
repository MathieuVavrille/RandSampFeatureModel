package randsampFM;

public class EquivCstr extends Constraint {
  private final Constraint left;
  private final Constraint right;
  
  public EquivCstr(final Constraint left, final Constraint right) {
    this.left = left;
    this.right = rigth;
  }
}
