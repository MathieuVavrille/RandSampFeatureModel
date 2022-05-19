package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

public class FalseCstr extends Constraint {

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    return new Pair<Boolean, Constraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of();
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of());
  }

  @Override
  public String toString() {
    return "FALSE";
  }
}
