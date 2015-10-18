/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg;

import java.util.HashSet;

import cz.afri.smg.graphs.ReadableSMG;

public interface SMGConcretisation {
  HashSet<ReadableSMG> execute(ReadableSMG pSMG);
}
