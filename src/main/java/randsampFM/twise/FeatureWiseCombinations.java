package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.solver.variables.BoolVar;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.BitSet;

public class FeatureWiseCombinations {

  private static Map<Feature,Integer> featureToId = null;
  private static List<Feature> idToFeature = null;

  private final BitSet falseComb;
  private final BitSet trueComb;

  public static void setFeatures(final List<Feature> features) {
    idToFeature = features;
    if (featureToId != null)
      throw new IllegalStateException("Cannot change the features of the FeatureWiseCombinations");
    featureToId = new HashMap<Feature,Integer>();
    for (int i = 0; i < features.size(); i++)
      featureToId.put(features.get(i), i);
  }
  
  public FeatureWiseCombinations() {
    this(new BitSet(featureToId.size()), new BitSet(featureToId.size()));
  }
  
  public FeatureWiseCombinations(final boolean initialValue) {
    this(new BitSet(featureToId.size()), new BitSet(featureToId.size()));
    falseComb.set(0, idToFeature.size(), initialValue);
    trueComb.set(0, idToFeature.size(), initialValue);
  }

  public FeatureWiseCombinations(final BitSet falseComb, final BitSet trueComb) {
    this.falseComb = falseComb;
    this.trueComb = trueComb;
  }

  public FeatureWiseCombinations clone() {
    return new FeatureWiseCombinations((BitSet) falseComb.clone(), (BitSet) trueComb.clone());
  }

  public void unionInPlace(final FeatureWiseCombinations other) {
    falseComb.or(other.falseComb);
    trueComb.or(other.trueComb);
  }

  public FeatureWiseCombinations unionNew(final FeatureWiseCombinations other) {
    FeatureWiseCombinations newFWC = this.clone();
    newFWC.unionInPlace(other);
    return newFWC;
  }

  public boolean contains(final Feature feature, final boolean truthValue) {
    if (truthValue)
      return trueComb.get(featureToId.get(feature));
    else
      return falseComb.get(featureToId.get(feature));
  }

  public void set(final Feature feature, final boolean truthValue) {
    if (truthValue)
      trueComb.set(featureToId.get(feature));
    else
      falseComb.set(featureToId.get(feature));
  }

  public void setVariables(final Map<Feature,BoolVar> featureToVar) { 
    for (int i = falseComb.nextSetBit(0); i >= 0; i = falseComb.nextSetBit(i+1))
      featureToVar.get(idToFeature.get(i)).eq(0).post();
    for (int i = trueComb.nextSetBit(0); i >= 0; i = trueComb.nextSetBit(i+1))
      featureToVar.get(idToFeature.get(i)).eq(1).post();
  }


  @Override
  public String toString() {
    return "("+idToFeature + "," + falseComb + "," + trueComb + ")";
  }
  
}
