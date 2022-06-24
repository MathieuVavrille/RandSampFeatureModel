package randsampFM;

import randsampFM.featureDiagram.*;
import randsampFM.constraints.*;
import randsampFM.types.*;
import randsampFM.splittedFM.*;
import randsampFM.parser.UVLParser;

//import de.neominik.uvl.ast.UVLModel;
//import de.neominik.uvl.UVLParser;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;

public class TestMain {

  public static void main(String[] args) {
    //assertCounts();
    //saveToDimacs("./models/simple.uvl", "./simple.dimacs");
    saveToDimacs("./models/jhipster.uvl", "./jhipster.dimacs");
    //saveToDimacs("../uvl-models/Feature_Models/Operating_Systems/KConfig/uClibc.uvl", "./uclib.dimacs");
    //testDimacs("./models/simple.uvl");
    //parseTest("../uvl-models/Feature_Models/Operating_Systems/KConfig/embtoolkit.uvl");
    //testFMCount("./models/simple.uvl");
    //testFMCount("../uvl-models/Feature_Models/Operating_Systems/KConfig/axTLS.uvl");
    //UVLParser.parse("./models/jhipster.uvl");
  }

  private static void saveToDimacs(final String path, final String outName) {
    FeatureModel fm = FeatureModel.parse(path);
    System.out.println(fm.enumerate().size());
    //SplittedFDList sfd = fm.removeConstraints();
    /*System.out.println(fm.count());
    System.out.println(sfd.count());
    System.out.println(fm.getMiniSatInstance().count(fm.getFeatureDiagram(), fm.getSiLink()));*/
    try {
      FileWriter myWriter = new FileWriter(outName);
      myWriter.write(fm.toDimacs());
      myWriter.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to file " + outName);
    }
  }

  private static void assertCounts() {
    assertCount("./models/simple.uvl");
    assertCount("./models/spark.uvl");
    assertCount("./models/jhipster.uvl");
    assertCount("./models/berkeleydb.uvl");
  }
  private static void assertCount(final String path) {
    FeatureModel fm = FeatureModel.parse(path);
    long startTime = System.nanoTime();
    if (fm.getMiniSatInstance().count(fm.getFeatureDiagram(), fm.getSiLink()) == fm.removeConstraints().count())
      throw new IllegalStateException("Counts not equal, bug somewhere");
    System.out.println("time = " + (System.nanoTime()-startTime)/1000000 + "ms");
  }

  private static void testFMiniSat(final String path) {
    System.out.println("\n");
    FeatureModel fm = FeatureModel.parse(path);
    //System.out.println(fm.getSiLink());
    MiniSat sat = fm.getMiniSatInstance();
    long startTime = System.nanoTime();
    System.out.println("sat instance generated");
    System.out.println(sat.count(fm.getFeatureDiagram(), fm.getSiLink()));
    //System.out.println(fm.enumerate().size());
    long middleTime = System.nanoTime();
    System.out.println("New count time = " + (middleTime-startTime)/1000000 + "ms");
    SplittedFDList sfd = fm.removeConstraints();
    System.out.println(sfd.count());
    /*for (FeatureDiagram fd : sfd.getFDs()) {
      System.out.println(fd.count());
      System.out.println(fd.toUVL(""));
      }*/
    long endTime = System.nanoTime();
    //System.out.println(sfd);
    System.out.println("Old count time = " + (endTime-middleTime)/1000000 + "ms");
  }

  /*private static void testMiniSat() {
    MiniSat sat = new MiniSat();
    int a = sat.newVariable();
    int b = sat.newVariable();
    int c = sat.newVariable();
    int d = sat.newVariable();
    System.out.println(a + " " + b + " " + c + " " + d);
    int at = MiniSat.makeLiteral(a, true);
    int bf = MiniSat.makeLiteral(b, false);
    int ct = MiniSat.makeLiteral(c, true);
    sat.addClause(at,bf,ct); // a !b c
    int af = MiniSat.makeLiteral(a, false);
    int bt = MiniSat.makeLiteral(b, true);
    int dt = MiniSat.makeLiteral(d, true);
    sat.addClause(af,bt,dt); // !a b t
    System.out.println(sat.count());
    }*/

  private static void testDimacs(final String path) {
    FeatureModel fm = FeatureModel.parse(path);
    System.out.println(fm.toDimacs());
    System.out.println(fm);
  }

  private static void parseTest(final String path) {
    long startTime = System.nanoTime();
    FeatureModel myParse = FeatureModel.parse(path);
    long middleTime = System.nanoTime();
    System.out.println("My parse time = " + (middleTime-startTime)/1000000 + "ms");
    FeatureModel neominikParse = FeatureModel.neominikParse(path);
    long endTime = System.nanoTime();
    System.out.println("Neominik parse time = " + (endTime-middleTime)/1000000 + "ms");
  }
  
  private static void testFMCount(final String path) {
    /*try {
    Thread.sleep(2000000);
    } catch (Exception e) {}*/
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    //System.out.println(fm.toUVL());
    long parseTime = System.nanoTime();
    System.out.println("parse time = " + (parseTime-startTime)/1000000);
    //System.out.println(fm);
    //BigInteger nbConfigurations = fm.count();
    SplittedFDList sfd = fm.removeConstraints();
    long removeTime = System.nanoTime();
    System.out.println("Remove time = " + (removeTime-parseTime)/1000000);
    BigInteger splittedNb = sfd.count();
    long countTime = System.nanoTime();
    System.out.println("Count time = " + (countTime-removeTime)/1000000);
    System.out.println(sfd.count());
    //System.out.println(splittedNb.equals(nbConfigurations));
    System.out.println("Total time = " + (System.nanoTime()-startTime)/1000000);
    System.out.println(sfd.countsRepartition());
    System.out.println(sfd.getFDs().size());
    //System.out.println(sfd.enumerate());
    //sfd.saveGraphvizToFolder("./graphviz_outputs/berkeleydb");
    //System.out.println(sfd.get(0).toUVL(""));
    //System.out.println(fm);
    /*for (FeatureDiagram fd : sfd.getFDs()) {
      System.out.println(fd);
      System.out.println(fd.enumerate());
      System.out.println(fd.count());
      }*/

  }
}
