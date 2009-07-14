package roc.pinpoint.analysis.plugins2.eviction;

/**
 * common constants for eviction policies
 * @author emrek
 *
 */
public interface EvictionPolicyConstants {

    /**
     * attribute name for the eviction policy of a record collection.
     */
    static final String EVICTION_POLICY_ARG = "evictionPolicy";

    /**
     * argument name for whether a given eviction plugin is the default (if so,
     * this plugin should apply itself to record collections that don't
     * explicitly choose another eviction policy.
     */
    static final String IS_DEFAULT_EVICTION_POLICY_ARG =
        "isDefaultEvictionPolicy";

}
