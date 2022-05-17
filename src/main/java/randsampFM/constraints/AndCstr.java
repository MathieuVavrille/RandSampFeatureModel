package randsampFM;

public class AndCstr extends Constraint {
  private final Constraint left;
  private final Constraint right;
  
  public AndCstr(final Constraint left, final Constraint right) {
    this.left = left;
    this.right = rigth;
  }
}
