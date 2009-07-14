package org.hs.jfc;


import java.awt.*;
import java.util.*;

/** 
 * An abstract layout manager to set the vertical position and height
 * of a set of components based on unique, ordered, non-sequential
 * column numbers, layout size, and component minimum and preferred
 * sizes.
 * <P>
 * Each ColumnLayout is responsible for a single column of components,
 * which may be in the form of label/field pairs, or single components
 * spanning the entire column.  Each component or pair of components
 * is stored in a SegmentLayout.  Columns are linked together by their
 * SegmentLayouts' links to other Columns' SegmentLayouts.  Each Column
 * calculates its minimum and preferred locations as the maximum of
 * its Segments' minimum and preferred locations.  ColumnLayout sets
 * its actual location on the basis of a percentage distance from
 * its minimum location to its preferred location; the percentage
 * is calculated by ColumnLeader (see FormLayout inner class).
 * <P>
 * Copyright 1999 HawkinsSoftware<br>
 * This code is free for distribution and/or modification.<br>
 * Please do not remove the copyright.
 *
 * @author Byron Hawkins
 */
class ColumnLayout
{

	/** The x-coordinate of the leftmost component governed by this ColumnLayout, when the minimum amount of space is available for the layout.
	 */
    protected int m_minLocation = 0;
	/** The x-coordinate of the leftmost component governed by this ColumnLayout, when the preferred amount of space (or more) is available for the layout.
	 */
    protected int m_prefLocation = 0;   
	/** The x-coordinate of the leftmost component of this ColumnLayout as currently layed out.
	 */
    protected int m_currLocation = 0;   
	/** The number of pixels covered on the x-axis by the label of this ColumnLayout.
	 */
    private int m_labelWidth = 0;
	/** The user-specified index of the column governed by this ColumnLayout.
	 */
    private int m_columnIndex = 0;

	/** The FormLayout that contains this ColumnLayout.
	 */
	protected FormLayout m_containingLayout = null;

	/** The SegmentLayout instances contained by this ColumnLayout.
	 */
    protected hVector m_segments = null; 

	/** Create a new ColumnLayout to govern the column specified by the user as <CODE>index</CODE> within the context of <CODE>containingLayout</CODE>.
	 * @param index The user-specified index of the column to be governed by the new ColumnLayout.
	 * @param containingLayout The FormLayout that contains the new ColumnLayout.
	 */
    public ColumnLayout(int index, FormLayout containingLayout)
    {
        m_segments = new hVector();
        m_columnIndex = index;
        m_containingLayout = containingLayout;
    }

	/** Create a segment with the specified index, and add it to this ColumnLayout.
	 * @param segmentIndex The index of the new segment.
	 * @return The newly instantiated SegmentLayout.
	 */
    private SegmentLayout addSegment(int segmentIndex)
    {
        SegmentLayout newSegment = new SegmentLayout(segmentIndex);
        m_segments.add(newSegment);
        return newSegment;
    }

	/** Calculate the value for m_minLocation based on the minimum locations of the SegmentLayout instances contained in {@link #m_segments}.
	 */
    protected void findMinimumLocation()
    {
		calculateLabelWidth();

        Enumeration e = m_segments.elements();
        int loc = 0;
        int segmentIndexLoc = 0;

        while (e.hasMoreElements())
        {
            segmentIndexLoc = ((SegmentLayout)e.nextElement()).getMinimumLocation();
            if (segmentIndexLoc > loc)
            {
                loc = segmentIndexLoc;
            }
        }
        setMinimumLocation(loc);
    }
    
	/** Calculate the value for m_prefLocation based on the preferred locations of the SegmentLayout instances contained in {@link #m_segments}.
	 */
    protected void findPreferredLocation()
    {
		calculateLabelWidth();

        Enumeration e = m_segments.elements();
        int loc = 0;
        int segmentLoc = 0;

        while (e.hasMoreElements())
        {
            segmentLoc = ((SegmentLayout)e.nextElement()).getPreferredLocation();
            if (segmentLoc > loc)
            {
                loc = segmentLoc;
            }
        }
        setPreferredLocation(loc);
    }

	/** Calculate the location that is <CODE>pct</CODE> of the way from the minimum to the preferred location.
	 * @param pct The percent of the extra space allowed by the preferred location to use.
	 */
    protected void findIntermediateLocation(double pct)
    {
        m_currLocation = m_minLocation + (int)((m_prefLocation - m_minLocation) * pct);
    }

	/** A hook for subclasses who need to do something here.
	 * @param minLoc the new minimumlocation.
	 */
    protected void setMinimumLocation(int minLoc)
    {
        m_minLocation = minLoc;
    }

	/** A hook for sublcasses that need to do something here.
	 * @param prefLoc The new preferred location.
	 */
    protected void setPreferredLocation(int prefLoc)
    {
        m_prefLocation = prefLoc;
    }

	/** Layout the components governed by this ColumnLayout.
	 */
    protected void doLayout()
    {
        Enumeration e = m_segments.elements();
        while (e.hasMoreElements())
        {
            ((SegmentLayout)e.nextElement()).doLayout();
        }
    }

	/** Accessor the the minimum x-coordinate of the leftmost component governed by this ColumnLayout.
	 * @return the ColumnLayout's minimum x-coordinate.
	 */
    public int getMinimumLocation()
    {
        return m_minLocation;
    }

	/** Accessor the the preferred x-coordinate of the leftmost component governed by this ColumnLayout.
	 * @return The ColumnLayout's preferred x-coordinate.
	 */
    public int getPreferredLocation()
    {
        return m_prefLocation;
    }

	/** Accessor for the location of the leftmost component of this ColumnLayout, as last layed out.
	 * @return the x-coordinate of this ColumnLayout, as last layed out.
	 */
    private int getCurrentLocation()
    {
        return m_currLocation;
    }

	/** Accessor for the user-specified index of the column governed by this ColumnLayout.
	 * @return the user-specified index of this ColumnLayout.
	 */
    public int getIndex()
    {
        return m_columnIndex;
    }
    
	/** Calculate the number of pixels required to display the label governed by this ColumnLayout.
	 */
    private void calculateLabelWidth()
	{
		m_labelWidth = 0;
		
		Enumeration e = m_segments.elements();
		SegmentLayout nextSegment;
		
		while (e.hasMoreElements())
		{
			nextSegment = (SegmentLayout)e.nextElement();
			if (nextSegment.m_label == null)
			{
				continue;
			}
			if (nextSegment.m_labelMode == FormLayout.FREE_FIELD)
			{
				continue;
			}
			if (nextSegment.m_labelMode == FormLayout.FREE_LABEL)
			{
				continue;
			}
			if (nextSegment.m_label.getPreferredSize().width > m_labelWidth)
			{
				m_labelWidth = nextSegment.m_label.getPreferredSize().width;
			}
		}
	}

	/** Governs the x-coordinates of the components conatained by this ColumnLayout at a specific row.
	 */
    public class SegmentLayout
    {
		// May not be a label, but it will be treated like one regardless.
		// m_labelMode will be set to FREE_FIELD if m_label is not a label, so
		// it will not be forced to line up inside the other fields in this Column.
		/** The label governed by this SegmentLayout.
		 */
        private Component m_label = null;

		// the rest of the components.  Their positions are always treated as relative
		// to the space available; only the first of m_components will be aligned with
		/** The components governed by this SegmentLayout.
		 */
        private hVector m_components = null; // Component

		/** The user-specified index of the RowLayout that also contains the components of this SegmentLayout.
		 */
        private int m_segmentIndex = 0;

		/** Specifies the system to apply when determining the alignment of the components that follow the label in relation to the other labels in this ColumnLayout: one of {@link FormLayout#DEFAULT}, {@link FormLayout#FREE_LABEL}, {@link FormLayout#FREE_FIELD}, {@link FormLayout#LABEL_ON_TOP}.
		 */
        private int m_labelMode = 0;

		/** The SegmentLayout subsequent to <CODE>this</CODE> along the x-axis.
		 */
        private SegmentLayout m_followingSegment = null;

		/** The SegmentLayout previous to <CODE>this</CODE> along the x-axis; null if <CODE>this</CODE> is the leftmost segment in the row specified by {@link #m_segmentIndex}.
		 */
        private SegmentLayout m_previousSegment = null;

		/** true if this SegmentLayout serves as a placeholder for a column structure that spans multiple rows; see FormLayout.addMultiRow(...).
		 */
        private boolean m_isGhost = false;

		/** Justification proximity percentage: if a component can be right-justified by stretching (m_pct * (preferredWidth - minimumWidth)), then it will be
		 */
		private double m_pct = 0;

		/** If <CODE>this</CODE> is a ghost, m_lastGhost identifies the index of the RowLayout in which the last ghost of the multi-row structure is contained.
		 */
		private int m_lastGhost = Integer.MAX_VALUE;

		/** Create a SegmentLayout to govern components in this ColumnLayout and the RowLayout specified by the user as <CODE>segmentIndex</CODE>.
		 * @param segmentIndex The user-specified index of the RowLayout that also governs the components in this SegmentLayout.
		 */
		public SegmentLayout(int segmentIndex)
        {
            m_components = new hVector();
            // prev and following columns will be set via ColumnLeader

            m_segmentIndex = segmentIndex;
        }

		/** Add <CODE>component</CODE> to this SegmentLayout, and apply pct to the extra space allowed by the preferred layout scenario, when it is available.
		 * @param component The component to be governed by this SegmentLayout.
		 * @param pct The percentage of the extra space allowed by the preferred layout scenario to use when available.
		 */
        public void add(Component component, double pct)
        {
			add(component, m_labelMode, pct);
        }

		/** Add <CODE>component</CODE> to this SegmentLayout; apply pct to the extra space allowed by the preferred layout scenario, when it is available; and apply <CODE>mode</CODE> to the location of the first component following the label.
		 * @param component The component to be governed by this SegmentLayout.
		 * @param mode The mode to apply in calculating the location of the first component following the label: see {@link #m_labelMode}.
		 * @param pct The percentage of the extra space allowed by the preferred layout scenario to use when available.
		 */
        public void add(Component component, int mode, double pct)
        {
			setPct(pct);

			if (m_label == null)
			{
				m_label = component;
	            m_labelMode = mode;
	            if ((mode == FormLayout.DEFAULT) && (component.getPreferredSize().width > m_labelWidth))
	            {
	                m_labelWidth = component.getPreferredSize().width;
	            }
			}
			else
			{
				m_components.add(component);
                if (m_isGhost)
                {
                    m_labelMode = mode;
                }
			}
        }

		/** Identify this SegmentLayout as a ghost that supports a multi-row extension of <CODE>component</CODE>.
		 * @param component The component for which to reserve space in the layout.
		 */
        public void addGhost(Component component)
        {
            m_isGhost = true;
            add(component, 0);
        }

		/** Identify this SegmentLayout as a ghost that supports a multi-row extension of <CODE>component</CODE>, which has the label mode <CODE>mode</CODE>.
		 * @param component The component for which to reserve space in the layout.
		 * @param mode The label mode of <CODE>component</CODE>.
		 */
        public void addGhost(Component component, int mode)
        {
            m_isGhost = true;
            add(component, mode, 0);
        }

		/** When <CODE>this</CODE> is a ghost for a component, identify the index of the RowLayout that contains the last ghost that reserves space for the component.
		 * @param lastGhost The index of the last row that reserves space for the component for which this ghost is reserving space.
		 */
		public void setLastGhost(int lastGhost)
		{
			m_lastGhost = lastGhost;
		}

		/** Remove <CODE>component</CODE> from this SegmentLayout.
		 * @param component The component to no longer govern.
		 */
		protected void removeLayoutComponent(Component component)
		{
			// removing the label
			if (m_label == component)
			{
				if (m_components.size() == 0)
				{
					// nothing left in this segmentIndex
					removeThisSegment();
				}
				else
				{
					// make the first component the new label
					m_label = (Component)m_components.remove(0);
					m_labelMode = 0;
					int labelWidth = m_label.getPreferredSize().width;
	                if (labelWidth > m_labelWidth)
	                {
	                    m_labelWidth = labelWidth;
	                }
				}
				return;
			}
			if (m_components.remove(component))
			{
				return;
			}
			// not here.  check leftwards
			if (m_previousSegment != null)
			{
				m_previousSegment.removeLayoutComponent(component);
			}
		}

		/** Remove this segment from the x-coordinate-oriented linked list of segments.
		 */
		private void removeThisSegment()
		{
			if (m_previousSegment == null)
			{
				m_followingSegment.m_previousSegment = null;
			}
			else
			{
				m_followingSegment.m_previousSegment = m_previousSegment;
				m_previousSegment.m_followingSegment = m_followingSegment;
			}
			// this will be cleaned up, right?
		}

		/** Set the percentage of the extra space allowed by the preferred layout scenario to make use of.
		 * @param pct the percentage of the extra space allowed by the preferred layout scenario to make use of.
		 */
		private void setPct(double pct)
		{
			if (pct > 1)
			{
				pct = 1;
			}
			if (pct < 0)
			{
				pct = 0;
			}
			m_pct = pct;
		}

		/** This method is used to find the SegmentLayout that governs the components of a specified RowLayout and ColumnLayout.  The method traces recursively leftward across the x-coordinate-oriented linked list of SegmentLayout's until the SegmentLayout in the requested <CODE>column</CODE> is found.
		 * @param column The ColumnLayout that contains the SegmentLayout to be obtained.
		 * @return The SegmentLayout to be obtained.
		 */
        public SegmentLayout getSegment(ColumnLayout column)
        {
			// looking for this Segment
            if (column.getIndex() == m_columnIndex)
            {
                return this;     
            }
            // it belongs somewhere to the left of here
            else if (column.getIndex() < m_columnIndex) 
            {
				// it does not exist: create it and link it up
                if (m_previousSegment == null)
                {
            		// it's new, and is the first in this segmentIndex
                    SegmentLayout newSegment = column.addSegment(m_segmentIndex);

                    newSegment.m_previousSegment = null;
                    newSegment.m_followingSegment = this;
                    m_previousSegment = newSegment;

                    return newSegment;
                }
				// it might exist: let the leftward neighbor deal with it
                else
                {
                    return m_previousSegment.getSegment(column);
                }
            }
			// it belongs to the right of here and does not exist: create it and link it up
            else
            {
                SegmentLayout newSegment = column.addSegment(m_segmentIndex);

                newSegment.m_followingSegment = m_followingSegment;
                m_followingSegment.m_previousSegment = newSegment;
                newSegment.m_previousSegment = this;
                m_followingSegment = newSegment;

                return newSegment;
            }
        }

		/** Layout the components governed by this SegmentLayout.
		 */
        public void doLayout()
        {
			// don't layout ghosts
            if (m_isGhost)
            {
                return;
            }

			// must have m_label, even if it isn't a label per se
            if (m_label == null)
            {
                return;
            }

            int labelLocation = 0;

            if (m_labelMode == FormLayout.FREE_LABEL)
            {
				// FREE_LABELs will always go as far to the left as they possibly can; 
				// the whole column will move right to accomodate if necessary
                int offset = m_label.getPreferredSize().width - m_labelWidth;
                if (offset < 0)
                {
					// this label doesn't need any freedom!
                    offset = 0;
                }
                labelLocation = m_currLocation - offset;
            }
            else
            {
                labelLocation = m_currLocation;
            }

			int labelMinWidth = m_label.getMinimumSize().width;
			int labelMaxWidth = m_label.getMaximumSize().width;
			int labelPrefWidth = m_label.getPreferredSize().width;
			if ((m_labelMode == FormLayout.FREE_FIELD) && (m_components.size() == 0))
			{
				// This is the only component in this Segment; treat it like a field.
				// Identify the amount of space available up to the right neighbor's
				// left boundary.

				int followingLeftBoundary = m_followingSegment.getLeftBoundary();
				if (m_lastGhost < Integer.MAX_VALUE)
				{
					// There are ghosts of this component in Segments below this one.
					// Account for the left boundaries of the segments to the right of
					// the ghosts
					SegmentLayout nextSegment = null;
					int nextFollowingLeftBoundary;
					Enumeration e = m_segments.elements();
					while (e.hasMoreElements())
					{
						nextSegment = (SegmentLayout)e.nextElement();
						if ((nextSegment.m_segmentIndex > m_segmentIndex) && (nextSegment.m_segmentIndex <= m_lastGhost))
						{
							nextFollowingLeftBoundary = nextSegment.m_followingSegment.getLeftBoundary();
							if (nextFollowingLeftBoundary < followingLeftBoundary)
							{
								followingLeftBoundary = nextFollowingLeftBoundary;
							}
						}
					}
				}
	            int room = followingLeftBoundary - m_currLocation;
				int labelWidth;
				if (labelMinWidth >= room)
				{
            		labelWidth = labelMinWidth;
				}
				if (m_pct == 1)
				{
					// always stretching all the way
                    if ((room <= labelMinWidth) || (labelMaxWidth <= labelPrefWidth))
                    {
                		labelWidth = room;
                    }
                    else
                    {
                		labelWidth = labelMaxWidth;
                    }
				}
				else
				{
					if (labelPrefWidth >= room) // squeezing 
					{
	            		labelWidth = room;
					}
					else // have extra space beyond preferred width
					{
						// can I stretch that far?
						if ( (labelPrefWidth + ((labelPrefWidth - labelMinWidth) * m_pct)) >= room)
						{
							// yes
	                        if ((room <= labelMaxWidth) || (labelMaxWidth <= labelPrefWidth))
	                        {
	    	            		labelWidth = room;
	                        }
	                        else
	                        {
	    	            		labelWidth = labelMaxWidth;
	                        }
						}
						else
						{
							// no, stop at preferred width
		            		labelWidth = labelPrefWidth;
						}
					}
				}
				m_label.setSize(labelWidth, m_label.getSize().height);
				if ((labelWidth < m_labelWidth) && (m_label.getAlignmentX() == Component.RIGHT_ALIGNMENT))
	        	{
	        		labelLocation += (m_labelWidth - labelWidth);
	        	}
	            m_label.setLocation(labelLocation, m_label.getLocation().y);
				return;
			}

			// labels never change size (unless they change content, but that's
			// static to this perspective)
            m_label.setSize(labelPrefWidth, m_label.getSize().height);
			if ((labelPrefWidth < m_labelWidth) && (m_label.getAlignmentX() == Component.RIGHT_ALIGNMENT))
        	{
        		labelLocation += (m_labelWidth - labelPrefWidth);
        	}
            m_label.setLocation(labelLocation, m_label.getLocation().y);

            int minSizeSum = 0; 
            int prefSizeSum = 0;

			if (m_components.size() == 0)
			{
				// all done
				return;
			}

			// total min and pref widths
            Enumeration e = m_components.elements();
            Component nextComponent = null;
            while (e.hasMoreElements())
            {
                nextComponent = (Component)e.nextElement();
                minSizeSum += nextComponent.getMinimumSize().width;
                prefSizeSum += nextComponent.getPreferredSize().width;
            }
            int gapSum = (m_components.size() - 1) * m_containingLayout.getInternalHGap();
            minSizeSum += gapSum;
            prefSizeSum += gapSum;

			int followingLeftBoundary = m_followingSegment.getLeftBoundary();
			if (m_lastGhost < Integer.MAX_VALUE)
			{
				// There are ghosts of this component in Segments below this one.
				// Account for the left boundaries of the segments to the right of
				// the ghosts
				int nextFollowingLeftBoundary;
				SegmentLayout nextSegment = null;
				e = m_segments.elements();
				while (e.hasMoreElements())
				{
					nextSegment = (SegmentLayout)e.nextElement();
					nextFollowingLeftBoundary = nextSegment.m_followingSegment.getLeftBoundary();
					if ((nextFollowingLeftBoundary < followingLeftBoundary) && 
					    ( (nextSegment.m_segmentIndex > m_segmentIndex) && (nextSegment.m_segmentIndex <= m_lastGhost) ) )
					{
						followingLeftBoundary = nextFollowingLeftBoundary;
					}
				}
			}
            int room = followingLeftBoundary - m_currLocation;
            int startx = m_currLocation;
            if (m_labelMode == FormLayout.FREE_FIELD)
            {
                room -= labelPrefWidth;
                startx += labelPrefWidth;
				// label might be blank
				if (labelPrefWidth > 0)
				{
		            room -= m_containingLayout.getInternalHGap();
		            startx += m_containingLayout.getInternalHGap();
				}
            }
            else
            {
                room -= m_labelWidth;
                startx += m_labelWidth;
				if ((m_labelWidth > 0) || (m_labelMode == FormLayout.FREE_LABEL))
				{
		            room -= m_containingLayout.getInternalHGap();
		            startx += m_containingLayout.getInternalHGap();
				}
            }

			// check if this component can reach the right boundary with (preferred size + (stretchability * m_pct))
			// where stretchability is (prefSizeSum - minSizeSum)
            if ( (room > prefSizeSum) && ( (prefSizeSum + ((prefSizeSum - minSizeSum) * m_pct)) < room ) && (m_pct < 1) )
            {
				// can't reach -- set to preferred and leave space to the right
                room = prefSizeSum;
            }

            double pctEx = 0;
            if ((prefSizeSum - minSizeSum) > 0)
            {
                pctEx = (double)((double)(room - minSizeSum) / (double)(prefSizeSum - minSizeSum));
            }

            e = m_components.elements();
            Component previous = null;
            Component current = null;

            if (e.hasMoreElements())
            {
				// place the first component
                current = (Component)e.nextElement();
                
                if (m_components.size() > 1)
            	{
			        current.setLocation(startx, current.getLocation().y);
            	}
            }

            while (e.hasMoreElements())
            {
                previous = current;
                current = (Component)e.nextElement();

				int previousMinWidth = previous.getMinimumSize().width;
				int previousPrefWidth = previous.getPreferredSize().width;
				int currentPrefWidth = current.getPreferredSize().width;
				int currentMaxWidth = current.getMaximumSize().width;
				
				// set the starting location of the next component
                if (previousMinWidth < previousPrefWidth) // stretchability > 0
                {
					// account for stretch 
                    startx += previousMinWidth + ((previousPrefWidth - previousMinWidth) * pctEx);
                }
                else
                {
					// can't stretch this one
                    startx += previousMinWidth;
                }
				if (previousMinWidth > 0)
				{
					// no gaps for invisible componentry
					startx += m_containingLayout.getInternalHGap();
				}
                current.setLocation(startx, current.getLocation().y);
                int width = startx - previous.getLocation().x - m_containingLayout.getInternalHGap();
                if ((width <= currentMaxWidth) || (currentMaxWidth <= currentPrefWidth))
                {
                    previous.setSize(width, previous.getSize().height);
                }
                else
                {
                    width = currentMaxWidth;
                    previous.setSize(width, previous.getSize().height);
                }
            }

			int currentWidth = 0;
			int currentPrefWidth = current.getPreferredSize().width;
			int currentMaxWidth = current.getMaximumSize().width;
			
			// will there be roundoff error?
			if (pctEx == 1)
			{
				// no
    		    currentWidth = currentPrefWidth;
			}
			else
			{
				// yes, so stretch per right neighbor's left boundary, regardless 
				// of m_pct, etc.
                currentWidth = (followingLeftBoundary - startx);
                if (!((currentWidth <= currentMaxWidth) || (currentMaxWidth <= currentPrefWidth)))
                {
                    currentWidth = currentMaxWidth;
                }
			}
			current.setSize(currentWidth, current.getSize().height);
			
			// if there's only one component and extra space, account for its X alignment
			if (m_components.size() == 1) 
			{
				if ( ((currentWidth + startx) < followingLeftBoundary) && (current.getAlignmentX() == Component.RIGHT_ALIGNMENT) )
		        {
		        	startx += (followingLeftBoundary - (currentWidth + startx));
		        }
		        current.setLocation(startx, current.getLocation().y);
			}
        }

		/** Calculate and return the x-coordinate of the leftmost component governed by this SegmentLayout, on the basis of the containing FormLayout's insets, should only the minimum size be available.
		 * @return the x-coordinate of the leftmost component governed by this SegmentLayout in the minimum layout scenario.
		 */
        public int getMinimumLocation()
        {
            if (m_previousSegment == null)
            {
                return m_containingLayout.getLeftInset();
            }
			if (m_followingSegment == null)
			{
	            return m_previousSegment.getMinRightBoundary() + getLabelOffset() + m_containingLayout.getInternalHGap();
			}
            return m_previousSegment.getMinRightBoundary() + getLabelOffset() + m_containingLayout.getInternalHGap();
        }

		/** Calculate and return the x-coordinate of the leftmost component governed by this SegmentLayout, on the basis of the containing FormLayout's insets, should the preferred size be available.
		 * @return the x-coordinate of the leftmost component governed by this SegmentLayout in the preferred layout scenario.
		 */
        public int getPreferredLocation()
        {
            if (m_previousSegment == null)
            {
                return m_containingLayout.getLeftInset();
            }
			if (m_followingSegment == null)
			{
	            return m_previousSegment.getPrefRightBoundary() + getLabelOffset();
			}
            return m_previousSegment.getPrefRightBoundary() + getLabelOffset() + m_containingLayout.getInternalHGap();
        }

		/** Calculate and return the left boundary of this SegmentLayout, based on the ColumnLayout's current location, and the offsets required by the label mode and the containing FormLayout's insets.
		 * @return The left boundary of this SegmentLayout.
		 */
        public int getLeftBoundary()
        {
			if (m_followingSegment == null)
			{
				return m_currLocation;
			}
            return m_currLocation - getLabelOffset() - m_containingLayout.getInternalHGap();
        }

		/** Calculate and return the right boundary of this SegmentLayout, based on the ColumnLayout's current location, the offsets required by the label mode and the containing FormLayout's insets, and the sizes of the components governed by the SegmentLayout, in the minimum layout scenario.
		 * @return the right boundary of this SegmentLayout in the minimum layout scenario.
		 */
        public int getMinRightBoundary()
        {
            int width = m_labelWidth;
            if ((m_labelMode == FormLayout.FREE_FIELD) || ((m_label.getMinimumSize().width == 0) && !m_isGhost) ) //&& (m_label.getMinimumSize().width > m_labelWidth))
            {
                width = m_label.getMinimumSize().width;
            }

            Enumeration e = m_components.elements();
            while (e.hasMoreElements())
            {
                Component nextComponent = (Component)e.nextElement();
                width += nextComponent.getMinimumSize().width;
                if ((nextComponent.getMinimumSize().width > 0) /**/ && !m_isGhost)
                {
                    width += m_containingLayout.getInternalHGap();
                }
            }
            return m_minLocation + width;
        }

		/** Calculate and return the right boundary of this SegmentLayout, based on the ColumnLayout's current location, the offsets required by the label mode and the containing FormLayout's insets, and the sizes of the components governed by the SegmentLayout, in the preferred layout scenario.
		 * @return the right boundary of this SegmentLayout in the preferred layout scenario.
		 */
        public int getPrefRightBoundary()
        {
            int width = m_labelWidth;
            if ( (m_labelMode == FormLayout.FREE_FIELD) || ((m_label.getPreferredSize().width == 0) && !m_isGhost) )
            {
                width = m_label.getPreferredSize().width;
            }

            Enumeration e = m_components.elements();
            while (e.hasMoreElements())
            {
                Component nextComponent = (Component)e.nextElement();
                width += nextComponent.getPreferredSize().width;
                if ((nextComponent.getPreferredSize().width > 0) && !m_isGhost) 
                {
                    width += m_containingLayout.getInternalHGap();
                }
            }
            return m_prefLocation + width;
        }

		/** Calculate the offset for the label as specified by the label mode.
		 * @return the offset required to properly layout the label according to the system specified by the label mode.
		 */
        private int getLabelOffset()
        {
            int offset = 0;
            if (m_labelMode == FormLayout.FREE_LABEL)
            {
                if (m_label.getPreferredSize().width > m_labelWidth)
                {
                    offset = m_label.getPreferredSize().width - m_labelWidth;
                }
            }
            return offset;
        }

		/** Accessor for the index of the RowLayout that also governs the components in this SegmentLayout.
		 * @return The row index of this SegmentLayout (global to the entire FormLayout, not just the index within this ColumnLayout).
		 */
        public int getIndex()
        {
            return m_segmentIndex;
        }
    }
}
