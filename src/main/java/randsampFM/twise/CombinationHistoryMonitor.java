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
  private final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations;
  private final Map<Feature,BoolVar> featureToVar;

  public CombinationHistoryMonitor(final List<Map<Feature,Boolean>> history, final Map<Feature, Pair<CombinationStatus, CombinationStatus>> combinations, final Map<Feature,BoolVar> featureToVar) {
    this.history = history;
    this.combinations = combinations;
    this.featureToVar = featureToVar;
  }

  @Override
  public void onSolution() {
    Map<Feature,Boolean> currentSolution = featureToVar.entrySet().stream().collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().getValue() == 1));
    for (Map.Entry<Feature,Boolean> entry : currentSolution.entrySet()) {
      if (combinations.containsKey(entry.getKey())) {
        Pair<CombinationStatus, CombinationStatus> currentState = combinations.get(entry.getKey());
        combinations.put(entry.getKey(), new Pair<CombinationStatus, CombinationStatus>(entry.getValue() ? currentState.getValue0() : CombinationStatus.FOUND, entry.getValue() ? CombinationStatus.FOUND : currentState.getValue1()));
      }
    }
    history.add(currentSolution);
  }
  
}
