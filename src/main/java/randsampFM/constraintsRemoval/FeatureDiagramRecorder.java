package randsampFM.constraintsRemoval;

import randsampFM.types.Feature;
import randsampFM.featureDiagram.FeatureDiagram;

import org.javatuples.Triplet;

import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class FeatureDiagramRecorder implements IMonitorSolution {

  private final FeatureDiagram mainFD;
  private final Map<Feature, BoolVar> featureToVar;
  private final List<FeatureDiagram> recordedFDs = new ArrayList<FeatureDiagram>();

  public FeatureDiagramRecorder(final FeatureDiagram mainFD, final Map<Feature, BoolVar> featureToVar) {
    this.mainFD = mainFD;
    this.featureToVar = featureToVar;
  }
  
  @Override
  public void onSolution() {
    final Set<Feature> forced = new HashSet<Feature>();
    final Set<Feature> forbidden = new HashSet<Feature>();
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet()) {
      if (entry.getValue().isInstantiatedTo(0))
        forbidden.add(entry.getKey());
      else if (entry.getValue().isInstantiatedTo(1))
        forced.add(entry.getKey());
    }
    System.out.println(Arrays.toString(featureToVar.get(new ArrayList<Feature>(forced).get(0)).getModel().retrieveIntVars(true)));
    System.out.println("Solution");
    System.out.println(forced);
    System.out.println(forbidden);
    final Triplet<FeatureDiagram.BottomUpCase, FeatureDiagram, Set<Feature>> reducedFD = mainFD.fixFeatures(forced, forbidden);
    if (reducedFD.getValue2().size() > 0) {
      System.out.println("Error, I was able to extract more information");
      System.out.println(forced);
      System.out.println(forbidden);
      System.out.println(mainFD);
      System.out.println(reducedFD.getValue1());
      System.out.println(reducedFD.getValue2());
      throw new IllegalStateException("Either choco did not propagate enough, or FeatureDiagram.fixFeatures propagated too much");
    }
    System.out.println(reducedFD.getValue1().toUVL(""));
    System.out.println(reducedFD.getValue1().count());
    recordedFDs.add(reducedFD.getValue1());
  }

  public List<FeatureDiagram> retrieveRecordedFDs() {
    return recordedFDs;
  }
}
