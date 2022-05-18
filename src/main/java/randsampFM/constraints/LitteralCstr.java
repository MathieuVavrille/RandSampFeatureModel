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
  public Pair<Boolean,Constraint> fixVariable(final Feature feature) {
    if (feature.equals(litteral))
      return new Pair<Boolean, Constraint>(true, new TrueCstr());
    else
      return new Pair<Boolean, Constraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public String toString() {
    return litteral.toString();
  }
}
