package randsampFM;

import randsampFM.types.*;
import randsampFM.constraints.CrossConstraint;
import randsampFM.constraints.Clause;
import randsampFM.featureDiagram.FeatureDiagram;
import randsampFM.constraintsRemoval.*;
import randsampFM.splittedFM.SplittedFDList;
import randsampFM.parser.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import de.neominik.uvl.ast.UVLModel;
//import de.neominik.uvl.UVLParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.math.BigInteger;

public class FeatureModel implements FMSampleCountEnum {

  private final FeatureDiagram featureDiagram;
  private final List<CrossConstraint> crossConstraints;

  private BigInteger nbConfigurations;

  public FeatureModel(final FeatureDiagram featureDiagram, final List<CrossConstraint> crossConstraints) {
    this.featureDiagram = featureDiagram;
    this.crossConstraints = crossConstraints;
  }

  public static FeatureModel parse(final String fileName) {
    return UVLParser.parse(fileName);
  }

  public static FeatureModel neominikParse(final String fileName) {
    try {
      return FeatureModel.neominikParse((UVLModel) de.neominik.uvl.UVLParser.parse(Files.readString(Path.of(fileName))));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
      return null;
    }
  }

  public static FeatureModel neominikParse(final UVLModel uvlModel) {
    List<CrossConstraint> crossConstraints = new ArrayList<CrossConstraint>();
    for (Object cstr : uvlModel.getConstraints())
      crossConstraints.add(CrossConstraint.fromUVLConstraint(cstr));
    return new FeatureModel(FeatureDiagram.parse(uvlModel.getRootFeatures()[0]), crossConstraints);
    }

  public SplittedFDList removeConstraints() {
    //featureDiagram.saveGraphvizToFile("./out_graphs/start.dot");
    final Model model = new Model(Settings.init().setModelChecker(s -> true));
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    featureDiagram.addConstraints(model, featureToVar);
    featureToVar.get(featureDiagram.getRootFeature()).eq(1).post();
    final List<Constraint> modelCrossConstraints = new ArrayList<Constraint>();
    for (CrossConstraint cstr : crossConstraints) {
      ReExpression expr = cstr.getCPConstraint(featureToVar);
      if (expr instanceof BoolVar)
        try {
          ((BoolVar) expr).instantiateTo(1,null);
        } catch (Exception e) {
          throw new IllegalStateException("error on initialization");
        }
      else {
        Constraint currentModelCstr = expr.decompose(); // maybe use .extension()
        currentModelCstr.post();
        modelCrossConstraints.add(currentModelCstr);
      }
    }
    System.out.println("constraints created");
    final Solver solver = model.getSolver();
    solver.setSearch(VarInConstraintStrategy.findConstraints(model.retrieveBoolVars(), new HashSet<IntVar>(featureToVar.values()), modelCrossConstraints, model.getCstrs()));
    FeatureDiagramRecorder recorder = new FeatureDiagramRecorder(featureDiagram, featureToVar);
    solver.plugMonitor(recorder);
    while (solver.solve()) {}
    List<FeatureDiagram> reducedFDs = recorder.retrieveRecordedFDs();
    return new SplittedFDList(reducedFDs);
  }

  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      if (crossConstraints.size() > 0)
        return BigInteger.valueOf(enumerate().size());
      else
        nbConfigurations = featureDiagram.count();
    }
    return nbConfigurations;
  }

  @Override
  public ConfSet enumerate() { // TODO, improve by lowering the constraints, but requires a deeper implementation
    ConfSet notConstrained = featureDiagram.enumerate();
    for (CrossConstraint cstr : crossConstraints)
      notConstrained = cstr.filterConfSet(notConstrained);
    return notConstrained;
  }

  @Override
  public Configuration sample(final Random random) {
    Configuration sample;
    boolean satisfiesAllConstraints;
    do {
      sample = featureDiagram.sample(random);
      satisfiesAllConstraints = true;
      for (CrossConstraint cstr : crossConstraints) {
        if (!cstr.isSatisfied(sample))
          satisfiesAllConstraints = false;
      }
    } while (!satisfiesAllConstraints);
    return sample;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(featureDiagram.toString());
    for (CrossConstraint cstr : crossConstraints) {
      builder.append("\n");
      builder.append(cstr.toString());
    }
    return builder.toString();
  }

  public String toUVL() {
    StringBuilder builder = new StringBuilder("namespace mainFD\n\nfeatures\n");
    builder.append(featureDiagram.toUVL("\t"));
    builder.append("\nconstraints\n");
    for (CrossConstraint cc : crossConstraints) {
      builder.append("\t"+cc.toUVL()+"\n");
    }
    return builder.toString();
  }

  public String toDimacs() {
    StringIntLink siLink = StringIntLink.fromSet(featureDiagram.getFeatures());
    List<Clause> clausesTree = new ArrayList<Clause>();
    featureDiagram.addTreeClauses(clausesTree, siLink);
    List<Clause> clausesCC = new ArrayList<Clause>();
    //for (CrossConstraint cc : crossConstraints)
      //clausesCC.addAll(cc.getAllClauses(siLink));
    StringBuilder builder = new StringBuilder();
    builder.append(siLink.toDimacsComments());
    builder.append("p cnf " + siLink.size() + " " + (clausesTree.size()+clausesCC.size()+1) + "\n");
    builder.append(new Clause(List.of(siLink.getInt(featureDiagram.getRootFeature().getName()))).toDimacs()+"\n");
    for (Clause treeClause : clausesTree)
      builder.append(treeClause.toDimacs()+"\n");
    for (Clause ccClause : clausesCC)
      builder.append(ccClause.toDimacs()+"\n");
    return builder.toString();
  }
}
