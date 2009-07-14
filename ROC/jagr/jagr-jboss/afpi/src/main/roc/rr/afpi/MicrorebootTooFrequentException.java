/*
  $Id: MicrorebootTooFrequentException.java,v 1.1 2004/06/08 13:33:04 emrek Exp $
  MicrorebootTooFrequentException:
    If RecoveryService receives the request of microrebooting an ejb 
    during the inactive time of that, then this exception will be thrown.
*/

package roc.rr.afpi;

public class MicrorebootTooFrequentException extends java.lang.Exception {
    public MicrorebootTooFrequentException(){}
}
