package randsampFM;

public class OrCstr extends Constraint {
  private final Constraint left;
  private final Constraint right;
  
  public OrCstr(final Constraint left, final Constraint right) {
    this.left = left;
    this.right = rigth;
  }
}
