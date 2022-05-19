package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

public class LitteralCstr extends Constraint {
  private final Feature litteral;

  public LitteralCstr(final Feature litteral) {
    this.litteral = litteral;
  }

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Set<Feature> forced, final Set<Feature> forbidden) {
    if (forced.contains(litteral))
      return new Pair<Boolean, Constraint>(true, new TrueCstr());
    else if (forbidden.contains(litteral))
      return new Pair<Boolean, Constraint>(true, new FalseCstr());
    else
      return new Pair<Boolean, Constraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForTrue() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(label),Set.of());
  }
  @Override
  public Pair<Set<Feature>,Set<Feature>> forcedFeaturesForFalse() {
    return new Pair<Set<Feature>,Set<Feature>>(Set.of(),Set.of(label));
  }

  @Override
  public String toString() {
    return litteral.toString();
  }
}
