/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg;

import cz.afri.smg.graphs.ReadableSMG;

import java.util.HashSet;

public interface SMGConcretisation {
  HashSet<ReadableSMG> execute(ReadableSMG pSMG);
}
