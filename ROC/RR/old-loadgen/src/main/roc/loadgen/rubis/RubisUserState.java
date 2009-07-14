/*
 * $Id: RubisUserState.java,v 1.1 2004/08/18 23:12:49 candea Exp $
 */

package roc.loadgen.rubis;

/**
 * Representation of a simulated RUBiS user's state.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class RubisUserState
{
    private String name; // The name of this state (free-form string)
    private int    id; // Unique, non-negative id, that can be used as an index

    /**
     * Constructor.
     */
    public RubisUserState( int id, String name )
    {
	assert name != null;
	assert id >= 0;

	this.name = name;
	this.id   = id;
    }

    /**
     * Getters
     */
    public String getName() { return name; }
    public int    getId() { return id; }

    public String toString()
    {
	return "[" + id + ": " + name + "]";
    }

    /**
     * Override the 'equals' method so we can compare RubisUserStates.
     */
    public boolean equals( Object obj )
    {
	RubisUserState other = (RubisUserState) obj;
	return id==other.getId() && name.equals( other.getName() );
    }
}

