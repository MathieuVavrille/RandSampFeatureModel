package randsampFM;

import randsampFM.types.*;

import java.util.Random;
import java.math.BigInteger;

public interface FMSampleCountEnum {
	
  public final static int PRECISION = 100;

  /** Preferably this is memoized the first time count() is called */
  public abstract BigInteger count();
  
  public abstract ConfSet enumerate();
  
  public abstract Configuration sample(final Random random);
}
