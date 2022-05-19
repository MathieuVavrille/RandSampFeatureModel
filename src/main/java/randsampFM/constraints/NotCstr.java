package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

public class NotCstr extends Constraint {
  private final Constraint child;
  
  private NotCstr(final Constraint child) {
    this.child = child;
  }

  public static Constraint of(final Constraint child) {
    if (child instanceof TrueCstr)
      return new FalseCstr();
    else if (child instanceof FalseCstr)
      return new TrueCstr();
    else if (child instanceof NotCstr)
      return ((NotCstr) child).child;
    else
      return new NotCstr(child);
  }

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    Pair<Boolean, Constraint> fix = child.fixVariable(forced,forbidden);
    return new Pair<Boolean, Constraint>(fix.getValue0(), NotCstr.of(fix.getValue1()));
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return child.forcedFeatuersForFalse();
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return child.forcedFeatuersForTrue();
  }

  @Override
  public Set<Feature> getVariables() {
    return child.getVariables();
  }

  @Override
  public String toString() {
    return "NOT("+child.toString()+")";
  }
}
