package org.hs.jfc;

import java.awt.*;
import java.util.*;
import java.lang.*;

import javax.swing.*;	

/** 
 * An abstract layout manager to set the horizontal positions of
 * a set of components based on unique, ordered, non-sequential
 * row numbers, layout size, and component minimum and preferred
 * sizes.<p>
 * Each RowLayout is responsible for a single row of components.
 * Each component is stored in a component layout, which performs
 * the actual layout functions.  Rows are linked together in a
 * doubly linked list.  Rows use the same minimum and preferred
 * location floating point scheme as ColumnLayout, except that they
 * will not expand to fill extra space.<p>
 * Copyright 1999 HawkinsSoftware<br>
 * This code is free for distribution and/or modification.<br>
 * Please do not remove the copyright.
 *
 * @author Byron Hawkins
 */
class RowLayout
{
	/** The components for which the y-coordinate is governed by this RowLayout, wrapped in ComponentLayout instances.
	 */
	private hVector m_components = null; 

	/** The RowLayout immediately above this one.
	 */
    private RowLayout m_previousRow = null;

	/** The RowLayout immediately below this one.
	 */
    private RowLayout m_followingRow = null;
    
	/** The FormLayout that contains this RowLayout.
	 */
    FormLayout m_containingLayout = null;

	/** The y-coordinate of the tallest component governed by this RowLayout, in the minimum layout scenario.
	 */
    protected int m_minLocation = 0;
	/** The y-coordinate of the tallest component governed by this RowLayout, in the preferred layout scenario.
	 */
    protected int m_prefLocation = 0;
	/** The y-coordinate of the tallest component governed by this RowLayout, in the current layout scenario.
	 */
    protected int m_currLocation = 0;

	/** The minimum height of this RowLayout, calculated as the greatest minimum height of the components governed by it.
	 */
    private int m_minSize = 0;
	/** The preferred height of this RowLayout, calculated as the greatest preferred height of the components governed by it.
	 */
    private int m_prefSize = 0;
	/** The height of this RowLayout in the current layout.
	 */
    private int m_currSize = 0;

	/** The user-specified index of this row.
	 */
    private int m_index = 0;

	/** Create a new RowLayout with the user-specified <CODE>index</CODE> within <CODE>containingLayout</CODE>.
	 * @param index the user-specified index of the row of Component governed by this RowLayout.
	 * @param containingLayout contains this RowLayout.
	 */
     public RowLayout(int index, FormLayout containingLayout)
	{
		m_index = index;
		m_components = new hVector();
		m_containingLayout = containingLayout;
	}

	/** Accessor for the user-specified index of this row.
	 * @return the index of this row.
	 */
    public int getIndex()
    {
        return m_index;
    }

	/** Include <CODE>addMe</CODE> in the components governed by this RowLayout.
	 * @param addMe the component gor which to set the y-coordinate.
	 */
	public void add(Component addMe)
	{
		m_components.add(new ComponentLayout(addMe));
	}

	/** Include <CODE>addMe</CODE> in the components governed by this RowLayout, but do not set its y-coordinate.
	 * @param addMe The new component to regard when calculating layout locations.
	 * @param anchorRow The index of the RowLayout that sets the y-coordinate of <CODE>addMe</CODE>.
	 */
	public void addFloater(Component addMe, int anchorRow)
	{
		if (anchorRow == m_index)
		{
			try
			{
				((JComponent)addMe).setAlignmentY(Component.TOP_ALIGNMENT);
				add(addMe);
				return;
			}
			catch (ClassCastException e)
			{}
		}

		ComponentLayout componentLayout = new ComponentLayout(addMe);
		m_components.add(componentLayout);

		getRow(anchorRow).setAnchor(componentLayout);
	}

	/** Include <CODE>addMe</CODE> in the ComponentLayouts contained in this RowLayout.
	 * @param addMe The ComponentLayout to add.
	 */
	private void setAnchor(ComponentLayout addMe)
	{
		addMe.setAnchor(this);
		m_components.add(addMe);
	}

	/** Recurse through the linked list of RowLayout and locate the one with index <CODE>row</CODE>.
	 * @param row The user-specified index of the RowLayout to obtain.
	 * @return The RowLayout with user-specified index <CODE>row</CODE>, or null if no such row is contained in this layout.
	 */
    public RowLayout getRow(int row)
    {
        if (row == m_index)
        {
            return this;     
        }

        if (row < m_index)
        {
			// requested row belongs above this
            if (m_previousRow == null)
            {
                // row does not exist: create it as first row 
                RowLayout rowLayout = new RowLayout(row, m_containingLayout);
                rowLayout.m_previousRow = null;
                rowLayout.m_followingRow = this;
                m_previousRow = rowLayout;

                return rowLayout;
            }
            else
            {
				// it's above the this' previous row -- let it deal with it
                return m_previousRow.getRow(row);
            }
        }
        else
        {
            // requested row belongs below and does not exist -- create it
            RowLayout rowLayout = new RowLayout(row, m_containingLayout);
            m_followingRow.m_previousRow = rowLayout;
            rowLayout.m_followingRow = m_followingRow;
            m_followingRow = rowLayout;
            rowLayout.m_previousRow = this;

            return rowLayout;
        }
    }

	/** Remove <CODE>removeMe</CODE> from the list of components to govern.
	 * @param removeMe The component to no longer govern.
	 */
	public void removeLayoutComponent(Component removeMe)
	{
		if (m_previousRow != null)
		{
			m_previousRow.removeLayoutComponent(removeMe);
		}
		
		Enumeration e = m_components.elements();
		while (e.hasMoreElements())
		{
			ComponentLayout componentLayout = (ComponentLayout)e.nextElement();
			if ( componentLayout.hasComponent(removeMe) )
			{
				m_components.remove(componentLayout);
			}
		}
		removeIfEmpty();
	}

	/** If this RowLayout contains no ColumnLayouts, remove it from the linked list of rows.
	 */
	protected void removeIfEmpty()
	{
		if (m_components.size() == 0)
		{
			if (m_previousRow == null)
			{
				m_followingRow.m_previousRow = null;
			}
			else
			{
				m_followingRow.m_previousRow = m_previousRow;
				m_previousRow.m_followingRow = m_followingRow;
			}
		}
	}

	/** Calculate the y-coordinate of the tallest component governed by this RowLayout given the preferred layout scenario.
	 */
    protected void findPreferredLocation()
	{
		if (m_previousRow != null)
        {
            m_previousRow.findPreferredLocation();
			int prefLocation = m_previousRow.getPrefLowerBoundary();
			if (m_followingRow != null)
			{
				prefLocation += m_containingLayout.getInternalVGap();
			}
            setPreferredLocation(prefLocation);
        }
        else
        {
            m_prefLocation = m_containingLayout.getTopInset();
        }
        // ComponentLayout.getPreferredSize() relies on m_prefLocation!

        Enumeration e = m_components.elements();
        m_prefSize = 0;

		while (e.hasMoreElements())
		{
			ComponentLayout nextComponent;

			nextComponent = (ComponentLayout)e.nextElement();
			if (nextComponent.getPreferredSize(this) > m_prefSize)
			{
				m_prefSize = nextComponent.getPreferredSize(this);
			}
		}
	}

	/** Calculate the y-coordinate of the tallest component governed by this RowLayout given the minimum layout scenario.
	 */
	protected void findMinimumLocation()
	{
		if (m_previousRow != null)
        {
            m_previousRow.findMinimumLocation();
            int minLoc = m_previousRow.getMinLowerBoundary();
            if (m_followingRow != null) // RowLeader uses external gap
            {
                minLoc += m_containingLayout.getInternalVGap();
            }
            setMinimumLocation(minLoc);
        }
        else
        {
            m_minLocation = m_containingLayout.getTopInset();
        }
        // ComponentLayout.getMinimumSize() relies on m_minLocation!

        Enumeration e = m_components.elements();
        m_minSize = 0;

		while (e.hasMoreElements())
		{
			ComponentLayout nextComponent;

			nextComponent = (ComponentLayout)e.nextElement();
			if (nextComponent.getMinimumSize(this) > m_minSize)
			{
				m_minSize = nextComponent.getMinimumSize(this);
			}
		}
	}

	/** A hook for subclasses that need to do something here.
	 * @param prefLoc The new preferred location of this RowLayout (the y-coordinate of the tallest component in the preferred layout scenario).
	 */
    protected void setPreferredLocation(int prefLoc)
    {
        m_prefLocation = prefLoc;
    }

	/** A hook for subclasses that need to do something here.
	 * @param minLoc The new minimum location of this RowLayout (the y-coordinate of the tallest component in the minimum layout scenario).
	 */
    protected void setMinimumLocation(int minLoc)
    {
        m_minLocation = minLoc;
    }

	/** Calculate the location of this RowLayout in terms of the previously calculated minimum and preferred locations, and <CODE>pct</CODE>.
	 * @param pct The percentage of the extra space allowed by the preferred layout scenario to make use of.
	 */
    protected void setLocation(double pct)
    {
		if (m_previousRow != null)
		{
			m_previousRow.setLocation(pct);
		}

        m_currLocation = (int) (m_minLocation + ((m_prefLocation - m_minLocation) * pct));
    }

	/** Calculate and return the y-coordinate of the bottom of the tallest component in this RowLayout, given the preferred layout scenario.
	 * @return the lower boundary of this RowLayout, in the preferred layout scenario.
	 */
    protected int getPrefLowerBoundary()
    {
        return m_prefLocation + m_prefSize;
    }

	/** Calculate and return the y-coordinate of the bottom of the tallest component in this RowLayout, given the minimum layout scenario.
	 * @return the lower boundary of this RowLayout, in the minimum layout scenario.
	 */
    protected int getMinLowerBoundary()
    {
		if (m_minSize == 0)
		{
			return m_minLocation;
		}
        return m_minLocation + m_minSize;
    }

	/** Calculate and return the y-coordinate of the top of the tallest component in this RowLayout, as currently layed out.
	 * @return the upper boundary of this RowLayout.
	 */
    protected int getUpperBoundary()
    {
        return m_currLocation;
    }

	/** Set the y-coordinates of all the components governed by this RowLayout (except floaters that are not anchored here).
	 */
	public void doLayout()	
	{
        if (m_previousRow != null)
        {
            m_previousRow.doLayout();
        }

        if (m_followingRow != null)
        {
            int room = 
            	(m_followingRow.getUpperBoundary() - m_containingLayout.getInternalVGap()) 
            		- m_currLocation;
    
    		Enumeration e = m_components.elements();
    
    		while (e.hasMoreElements())
    		{
                ((ComponentLayout)e.nextElement()).doLayout(room, this);
    		}
        }
	}

	/** A wrapper for each component governed by this RowLayout.
	 */
    class ComponentLayout
    {
		/** The component governed by this ComponentLayout.
		 */
        private Component m_component = null;

		// m_component spans multiple rows, m_anchor is the first, and this is 
		// the last; intermediate rows do not contain or know about m_component
		/** If {@link #m_component} is a floater, m_anchor is the RowLayout that sets its y-coordinate; otherwise, m_anchor is null.
		 */
        private RowLayout m_anchor = null;

		/** Create a new ComponentLayout to govern the y-coordinate of <CODE>component</CODE>.
		 * @param component The component for which to set the y-coordinate.
		 */
        public ComponentLayout(Component component)
        {
            m_component = component;
        }

		/** Identify this ComponentLayout as the anchor of a floater.
		 * @param thisRow Bogus -- java does not scope <CODE>this</CODE> for inner classes.
		 */
        public void setAnchor(RowLayout thisRow)
        {
            m_anchor = thisRow;
        }

		/** Check to see if this ComponentLayout governs <CODE>component</CODE>.
		 * @param component The component in question.
		 * @return true if this ComponentLayout governs <CODE>component</CODE>.
		 */
		public boolean hasComponent(Component component)
		{
			// address comparison
			return (m_component == component);
		}

		/** Set the y-coordinate of {@link #m_component}.
		 * @param room The amount of y-coordinate space available in which {@link #m_component} can be drawn.
		 * @param thisRow Bogus -- java does not scope <CODE>this</CODE> for inner classes.
		 */
        public void doLayout(int room, RowLayout thisRow)
        {
            if ((m_anchor == null) || (m_anchor.getIndex() == thisRow.getIndex())) 	//  no anchor || this is the anchor
            {
				int location = thisRow.m_currLocation;
				if (m_component.getPreferredSize().height <= room)
				{
					location += ((room - m_component.getPreferredSize().height) * m_component.getAlignmentY());
				}
               m_component.setLocation(m_component.getLocation().x, location);
            }
			// don't set size on an anchor
            if ((m_anchor == null) || (m_anchor.getIndex() != thisRow.getIndex()))	//	no anchor || this is not the anchor
            {
                if ((m_anchor != null) && (m_anchor.getIndex() != thisRow.getIndex())) //  this is the end of the multiRow
                {
                    room += (thisRow.m_currLocation - m_anchor.getUpperBoundary());	
                }
                if (m_component.getPreferredSize().height <= room)
                {
                    m_component.setSize(m_component.getSize().width, m_component.getPreferredSize().height);
                }
                else if (m_component.getMinimumSize().height < room)
                {
                    m_component.setSize(m_component.getSize().width, room);
                }
                else
                {
                    m_component.setSize(m_component.getSize().width, m_component.getMinimumSize().height);
                }
            }
        }

		/** Calculate and return the minimum height required by this ComponentLayout.
		 * @param thisRow Bogus -- java does not scope <CODE>this</CODE> for inner classes.
		 * @return the minimum height required by this ComponentLayout.
		 */
        public int getMinimumSize(RowLayout thisRow)
        {
            if ((m_anchor != null) && (m_anchor.getIndex() == thisRow.getIndex()))	//	anchor
            {
				// anchor expects no size allocation (will take it per below)
                return 0;
            }
            else if ((m_anchor != null) && (m_anchor.getIndex() != thisRow.getIndex()))	// last component in multi-row
            {
				// find out how much of m_component is contained in the above rows, and then
				// how much space we need to give it in this row
                int portionInThisRow = m_component.getMinimumSize().height - (thisRow.m_minLocation - m_anchor.m_minLocation);
                if (portionInThisRow <= 0)
                {
					// it fits in the above rows
                    return 0;
                }
                return portionInThisRow;
            }
            else
            {
				// single row
                return m_component.getMinimumSize().height;
            }
        }

		/** Calculate and return the preferred height required by this ComponentLayout.
		 * @param thisRow Bogus -- java does not scope <CODE>this</CODE> for inner classes.
		 * @return the preferred height required by this ComponentLayout.
		 */
        public int getPreferredSize(RowLayout thisRow)
        {
            if ((m_anchor != null) && (m_anchor.getIndex() == thisRow.getIndex()))	//	(m_mode == FLOAT_ANCHOR)
            {
                return 0;
            }
            else if ((m_anchor != null) && (m_anchor.getIndex() != thisRow.getIndex()))	//   (m_mode == FLOAT_BUOY)
            {
                int portionInThisRow = m_component.getPreferredSize().height - (thisRow.m_prefLocation - m_anchor.m_prefLocation);
                if (portionInThisRow <= 0)
                {
                    return 0;
                }
                return portionInThisRow;
            }
            else
            {
                return m_component.getPreferredSize().height;
            }
        }
    }
}
