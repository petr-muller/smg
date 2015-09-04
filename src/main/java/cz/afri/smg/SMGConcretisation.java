/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg;

import cz.afri.smg.graphs.ReadableSMG;

public interface SMGConcretisation {
    ReadableSMG execute(ReadableSMG pSMG);
}
