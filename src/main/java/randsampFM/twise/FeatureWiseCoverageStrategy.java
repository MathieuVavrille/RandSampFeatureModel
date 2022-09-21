package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Pair;

import java.util.Map;

public class FeatureWiseCoverageStrategy extends AbstractStrategy<BoolVar> {


  private final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations;
  private final Map<Feature, BoolVar> featureToVar;
  private final IStateBool isFirstDecision;

  private final boolean startWithPositiveLiteral = true;


  public FeatureWiseCoverageStrategy(final Model model, final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations, final Map<Feature, BoolVar> featureToVar) {
    this.combinations = combinations;
    this.featureToVar = featureToVar;
    this.isFirstDecision = model.getEnvironment().makeBool(true);
  }
  

  @Override
  public Decision<BoolVar> getDecision() {
    Pair<Feature,Boolean> chosenCombination = getUnseenCombination();
    if (isFirstDecision.get()) {
      if (chosenCombination == null)
        return new FailDecision(getOneVar());
      else
        return new IntEqRootDecision(featureToVar.get(chosenCombination.getValue0()), chosenCombination.getValue1(), isFirstDecision, chosenCombination.getValue0(), combinations);
    }
    else {
      if (chosenCombination == null)
        return null;
      else
        return new BoolDecision(featureToVar.get(chosenCombination.getValue0()), chosenCombination.getValue1());
    }
  }

  /** Returns one variable. There is no property, and it should only be used as a placeholder */
  private BoolVar getOneVar() {
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet())
      return entry.getValue();
    throw new IllegalStateException("There are no variables");
  }

  private Pair<Feature,Boolean> getUnseenCombination() {
    for (Map.Entry<Feature, Pair<CombinationStatus, CombinationStatus>> entry : combinations.entrySet()) {
      if (!featureToVar.get(entry.getKey()).isInstantiated()) {
        if (startWithPositiveLiteral) {
          if (entry.getValue().getValue1() == CombinationStatus.UNSEEN)
            return new Pair<Feature,Boolean>(entry.getKey(), true);
          else if (entry.getValue().getValue0() == CombinationStatus.UNSEEN)
            return new Pair<Feature,Boolean>(entry.getKey(), false);
        }
        else {
          if (entry.getValue().getValue0() == CombinationStatus.UNSEEN)
            return new Pair<Feature,Boolean>(entry.getKey(), false);
          else if (entry.getValue().getValue1() == CombinationStatus.UNSEEN)
            return new Pair<Feature,Boolean>(entry.getKey(), true);
        }
      }
    }
    return null; // No more combination with non instantiated variable
  }

}
