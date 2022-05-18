package randsampFM.featureModel;

import de.neominik.uvl.ast.Group;

import randsampFM.types.*;

import org.javatuples.Triplet;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Random;
import java.math.BigInteger;

public abstract class FeatureModel {
	
  protected final static int precision = 1000;
	
  protected final Feature label;
  protected BigInteger nbConfigurations;
	
  public FeatureModel(String label) {
    this.label = new Feature(label);
    this.nbConfigurations = null;
  }

  /** Returns all the features in the FM */
  public abstract Set<Feature> getFeatures();

  /** Returns all the mandatory features (from the start with a chain of mandatory features).
   * To be overridden on MandOptFM */
  public Set<Feature> getChainedMandatoryFeatures() {
    return Set.of(label);
  }

  /** All the return cases for the removeSubFeature function.
   * EMPTY means that the FM should be removed.
   * INCONSISTENT means that some constraints cannot be satisfied.
   * MANDATORY_UNMODIFIED means that the FM has to always be in the configurations, and is the same pointer as before (for memoization purpose).
   * MANDATORY_MODIFIED means that the FM has to always be in the configuration, but has been modified
   * UNMODIFIED means that the FM is the same (pointer) as before. This is important to propagate for memoization purpose.
   * MODIFIED is the last case when the FM has been modified, but is neither EMPTY nor MANDATORY (and not INCONSISTENT)
   */
  public enum BottomUpCase { EMPTY, INCONSISTENT, MANDATORY_UNMODIFIED, MANDATORY_MODIFIED, UNMODIFIED, MODIFIED }
    
  /** Force some features to be in the FM or not to be in.
   * The implementation supposes that forced and forbidden are disjoint. No check will be done here !
   * @param forced the set of features forced to be in the configurations
   * @param forbidden the set of features not in the configurations
   * @returns a triple containing:
   * - information data of the 'state' of the FM.
   * - the modified FM. It is `null` when the first element is EMPTY or INCONSISTENT
   * - the newly removed features (to set to 0 in the constraints). This does not include the features in forbidden, only the ones propagated from forbidden features (or forced features in a XOR group). It is be `null` when the first element is INCONSISTENT
   */
  public abstract Triplet<BottomUpCase, FeatureModel, Set<Feature>> fixFeatures(final Set<Feature> forced, final Set<Feature> forbidden);
  
  /** nbConfigurations is set to count() the first time count() is called */
  public abstract BigInteger count();
  
  public abstract ConfSet enumerate();
  
  public abstract Conf sample(final Random random);


  /** Parse a Feature from uvl-parser, and create the associated classes */
  public static FeatureModel parseFeatureModel(final de.neominik.uvl.ast.Feature feature) {
    List<Group> groups = Arrays.asList(feature.getGroups()); // retrieves all the groups under the feature
    /* 0 -> OR
     * 1 -> XOR
     * 2 -> MAND/OPT
     * 3 -> Cardinality
     */
    ArrayList<Integer> nbTypes = new ArrayList<Integer>(Arrays.asList(new Integer[4])); // counts how many times a type has been encountered below
    Collections.fill(nbTypes, 0); // nbTypes is now full of zero	
    for(Group group : groups) { 
      switch(group.getType()) { // got naming conventions from neominik/uvl-parser/resources/uvl.bnf 
      case "or":
        nbTypes.set(0, nbTypes.get(0)+1);
        break;
      case "alternative":
        nbTypes.set(1, nbTypes.get(1)+1);
        break;
      case "mandatory":
        nbTypes.set(2, nbTypes.get(2)+1); // same box than mandatory
        break;
      case "optional":
        nbTypes.set(2, nbTypes.get(2)+1); // same box than optional
        break;
      case "cardinalities":
        nbTypes.set(3, nbTypes.get(3)+1);
        break;
      default:
        throw new  UnsupportedOperationException("Type not handled.");
      }
    }
    
    int typeIndex = -1;
    long filteredTypes = nbTypes.stream().filter(y -> y > 0).count(); // how many different types ?
    if(filteredTypes > 1) { // Checks type consistency
      throw new  UnsupportedOperationException("Types are not consistent -> Feature Model building aborted.");
    }
    else {
      for(int i=0; i<4; i++) {
        if(nbTypes.get(i)>0) {
          typeIndex = i;
        }
      }
    }
    FeatureModel result;
    List<de.neominik.uvl.ast.Feature> children;
    Group currentGroup;
    
    switch(typeIndex){
    case -1: // LEAF
      result = new FMLeaf(feature.getName());
      break;
    case 0: //OR
      currentGroup = groups.stream().filter(g -> g.getType().equals("or")).findFirst().get();
      children = Arrays.asList(currentGroup.getChildren());
      result = FMOr.parse(feature.getName(),children);
      break;
    case 1://XOR
      currentGroup = groups.stream().filter(g -> g.getType().equals("alternative")).findFirst().get();
      children = Arrays.asList(currentGroup.getChildren());
      result = FMXor.parse(feature.getName(),children);
      break;
    case 2://MANDOPT
      // One of mandGroup or optGroup might be empty
      boolean bothEmpty = true;
      List<de.neominik.uvl.ast.Feature> rawMandChilds; //= List.of();
      List<de.neominik.uvl.ast.Feature> rawOptChilds; //= List.of();
      try {
        Group mandGroup = groups.stream().filter(g -> g.getType().equals("mandatory")).findFirst().get();
        rawMandChilds = Arrays.asList(mandGroup.getChildren());
        bothEmpty = false;
      }catch(NoSuchElementException e) { // no element in mandGroup match the predicate
        rawMandChilds = List.of();
      }		
      try {
        Group optGroup = groups.stream().filter(g -> g.getType().equals("optional")).findFirst().get();
        rawOptChilds = Arrays.asList(optGroup.getChildren());
        bothEmpty = false;
      }catch(NoSuchElementException e) { // no element in optGroup match the predicate
        rawOptChilds = List.of();
      }	
      if(bothEmpty) {
        throw new NoSuchElementException("Both Mandatory and optional groups are empty");
      } else {
        result = FMMandOpt.parse(feature.getName(), rawMandChilds, rawOptChilds);
      }		
      break;
      // DO NOT DELETE
      /*case 3://CARD - WRONG 
        currentGroup = groups.stream().filter(g -> g.getType().equals("cardinalities")).collect(Collectors.toList()).get(0);
			
        int lb = currentGroup.getLower();
        int ub = currentGroup.getUpper();
			
        children = Arrays.asList(currentGroup.getChildren());
        result = new FMCard(feature.getName(),children,lb,ub);
        break;*/
      // DO NOT DELETE
    default:
      throw new  UnsupportedOperationException("FilteredTypes not consistent with typeIndex"); // cannot happen 
    }
		
    return result; // return both type and feature model
  }

  public static List<ConfSet> enumerate(List<FeatureModel> fmList, boolean isOptional) {
    LinkedList<ConfSet> result = new LinkedList<ConfSet>();
    if(isOptional) {
      for(FeatureModel fm : fmList) {
        result.addLast(fm.enumerate());
      }
    }
    else {
      for(FeatureModel fm : fmList) {
        result.addLast(fm.enumerate().union(new ConfSet()));
      }
    }
    return List.copyOf(result);
  }	
	 
  @Override
  public String toString(){
    if(this.label == null) {
      return "null";
    } else {
      return label.toString();
    }
  }
}
