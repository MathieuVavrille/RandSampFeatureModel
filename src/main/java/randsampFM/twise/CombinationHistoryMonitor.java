package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinationHistoryMonitor implements IMonitorSolution {

  private final List<Map<Feature,Boolean>> history;
  private final FeatureWiseCombinations combinations;
  private final Map<Feature,BoolVar> featureToVar;

  public CombinationHistoryMonitor(final List<Map<Feature,Boolean>> history, final FeatureWiseCombinations combinations, final Map<Feature,BoolVar> featureToVar) {
    this.history = history;
    this.combinations = combinations;
    this.featureToVar = featureToVar;
  }

  @Override
  public void onSolution() {
    Map<Feature,Boolean> currentSolution = featureToVar.entrySet().stream().collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().getValue() == 1));
    for (Map.Entry<Feature,Boolean> entry : currentSolution.entrySet())
      combinations.set(entry.getKey(), entry.getValue());
    history.add(currentSolution);
  }
  
}
