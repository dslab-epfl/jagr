package org.hs.jfc;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;  
import javax.swing.text.*;

/** 
 * A layout manager modeled after the HTML <TABLE> tag.  Components
 * are added to a specified row and column, and can span multiple rows.
 * Column widths are calculated in layoutComponents(), and adjustments
 * for compacting forms can be made with mode parameters.<p>
 * Separate interleaved objects called RowLayout and ColumnLayout
 * manage the horizontal and vertical layout settings, respectively.
 * The objects do not speak to each other.  RowLeader and ColumnLeader
 * sit here in FormLayout and have access to all Rows and all Columns,
 * respectively; they are responsible for coordinating addition of
 * new Rows and Columns, which are organized as linked lists.  A
 * floating point scheme is used to deal with extra space.  See the
 * HTML documentation for details about the interface and behavior.<p>
 * Copyright 1999 HawkinsSoftware<br>
 * This code is free for distribution and/or modification.<br>
 * Please do not remove the copyright.
 *
 * @author Byron Hawkins
 */
public class FormLayout implements LayoutManager, ComponentListener
{
	/** The gaps along the x-axis between the components.
	 */
	private int m_hgap = 5;
	/** The gaps along the y-axis between the components.
	 */
	private int m_vgap = 5;
	/** The two gaps along the x-axis between the rectangle occupied by the components of the layout and the edge of the container.
	 */
	private int m_externalHGap = 0;
	/** The two gaps along the y-axis between the rectangle occupied by the components of the layout and the edge of the container.
	 */
	private int m_externalVGap = 0;
	/** Justification percentage: consider a value that is <CODE>m_pct</CODE> of the difference between the actual width of a component and the width it would have to be to right-justify with the rest of the column.  If this value is large enough to extend the component such that it right-justifies, then do so; otherwise leave it as is.
	 */
	private double m_pct = 0;
	/** This flag indicates that the locations and sizes for all the components in the minimum layout scenario must be recalculated.
	 */
	private boolean m_refreshMinimum = true;
	/** This flag indicates that the locations and sizes for all the components in the preferred layout scenario must be recalculated.
	 */
	private boolean m_refreshPreferred = true;

	/** The ColumnLayouts that govern the x-coordinates of all components governed by this FormLayout.
	 */
    private hVector m_Columns = new hVector();

	/** A invisible entity that sits at the right end of the ColumnLayouts.
	 */
    private ColumnLeader m_ColumnLeader = null;
	/** An invisible entity that sits at the bottom of the linked list of RowLayouts; all recursive row operations start here.
	 */
    private RowLeader m_RowLeader = null;
    
	/** The object that has employed this FormLayout to lay out its components.
	 */
    private Container m_container = null;

	/** A collection of the components contained in this FormPanel, in the chronological order in which the components were added to the layout.
	 */
	private Vector m_ListOfAddedComponents = new Vector();

    /** Specifies default alignment for the associated component (used in the <code>FormLayout.add()</code> methods).
	 */ 
    public static final int DEFAULT      = 0;

    /** Special alignment: the associated label will not align with other labels in this column (used in certain <code>FormLayout.add()</code> methods).
	 */ 
    public static final int FREE_LABEL   = 1;

    /** Special alignment: the associated field will not align with other fields in this column (used in certain <code>FormLayout.add()</code> methods).
	 */ 
    public static final int FREE_FIELD   = 2;

    /** Special alignment: the label and field will appear in subsequent columns, starting with the one specified (used in certain <code>FormLayout.add()</code> methods).
	 */ 
    public static final int LABEL_ON_TOP = 3;
    
	/** Create a FormLayout with default gaps.
	 */
    public FormLayout()
    {
        init();
    }    

    /** Most specific gap access available.
	 * @param internalHGap the amount of space to reserve along the x-axis between components.
	 * @param internalVGap the amount of space to reserve along the y-axis between components.
	 * @param externalHGap the amount of space along the x-axis between the leftmost and rightmost components and the edge of the container (the left and right insets).
	 * @param externalVGap the amount of space along the y-axis between the topmost and bottommost components and the edge of the container (the top and bottom insets).
	 */
    public FormLayout(int internalHGap, int internalVGap, int externalHGap, int externalVGap)
    {
		m_hgap = internalHGap;
		m_vgap = internalVGap;
		m_externalHGap = externalHGap;
		m_externalVGap = externalVGap;
        init();
    }
    
    /** The amount of space along the x-axis between the leftmost component and the edge of the container.
	 * @return externalHGap + left inset of the container.
	 */
    protected int getLeftInset()
	{
		return m_externalHGap + m_container.getInsets().left;
	}
	
    /** The amount of space along the x-axis between the rightmost component and the edge of the container.
	 * @return externalHGap + right inset of the container.
	 */
	protected int getRightInset()
	{
		return m_externalHGap + m_container.getInsets().right;
	}
	
    /** The amount of space along the y-axis between the topmost component and the edge of the container.
	 * @return externalVGap + top inset of the container.
	 */
	protected int getTopInset()
	{
		return m_externalVGap + m_container.getInsets().top;
	}  		
		
    /** The amount of space along the y-axis between the bottommost component and the edge of the container.
	 * @return externalVGap + bottom inset of the container
	 */
	protected int getBottomInset()
	{
		return m_externalVGap + m_container.getInsets().bottom;
	}  		
		
	/** Accessor for the gap between components along the x-axis.
	 * @return the gap between components along the x-axis.
	 */
    public int getInternalHGap()
    {
        return m_hgap;
    }

	/** Accessor for the gap between components along the y-axis.
	 * @return the gap between components along the y-axis.
	 */
    public int getInternalVGap()
    {
        return m_vgap;
    }

	/** Accessor for the space between the block of components and the edge of the container along the x-axis.
	 * @return the amount of space between the block of components and the edge of the container along the x-axis.
	 */
    public int getExternalHGap()
    {
        return m_externalHGap;
    }

	/** Accessor for the space between the block of components and the edge of the container along the y-axis.
	 * @return the amount of space between the block of components and the edge of the container along the y-axis.
	 */
    public int getExternalVGap()
    {
        return m_externalVGap;
    }

	/** Set the amount of space between components along the y-axis.
	 * @param gap the amount of space between components along the y-axis.
	 */
    public void setInternalVGap(int gap)
    {
        m_vgap = gap;
		m_refreshMinimum = true;
		m_refreshPreferred = true;
    }

	/** Set the amount of space between components along the x-axis.
	 * @param gap the amount of space between components along the x-axis.
	 */
    public void setInternalHGap(int gap)
    {
        m_hgap = gap;
		m_refreshMinimum = true;
		m_refreshPreferred = true;
    }

	/** Set the amount of space between the block of components and the edge of the container along the y-axis.
	 * @param gap the amount of space between the block of components and the edge of the container along the y-axis.
	 */
    public void setExternalVGap(int gap)
    {
        m_externalVGap = gap;
		m_refreshMinimum = true;
		m_refreshPreferred = true;
    }

	/** Set the amount of space between the block of components and the edge of the container along the x-axis.
	 * @param gap the amount of space between the block of components and the edge of the container along the x-axis.
	 */
    public void setExternalHGap(int gap)
    {
        m_externalHGap = gap;
		m_refreshMinimum = true;
		m_refreshPreferred = true;
    }

	/** Initialize the layout.
	 */
    private void init()
    {
        m_ColumnLeader = new ColumnLeader(this);
        m_Columns.add(m_ColumnLeader); 
        m_RowLeader = new RowLeader(this);
		m_refreshMinimum = true;
		m_refreshPreferred = true;
    }

	/** Set the location and size of each component governed by this FormLayout.
	 * @param parent The container whose components are to be laid out.
	 */
    public void layoutContainer(Container parent)
    {
        m_container = parent;

        m_RowLeader.layoutRows(parent.getSize().height);
        m_ColumnLeader.layoutColumns(parent.getSize().width);
		m_refreshMinimum = false;
		m_refreshPreferred = false;
    }

	/** Calculate and return the minimum number of pixels required to lay out the components in <CODE>parent</CODE>.
	 * @param parent the container for which to calculate the minimum size.
	 * @return the number of pixels required to lay out <CODE>parent</CODE>.
	 */
    public Dimension minimumLayoutSize(Container parent)
    {
        m_container = parent;
        
        Dimension minimumSize = new Dimension(m_ColumnLeader.getMinimumLocation(), m_RowLeader.getMinimumLocation());

		m_refreshMinimum = false;
		
        return minimumSize;
    }
    
	/** Calculate and return the preferred number of pixels required to lay out the components in <CODE>parent</CODE>.
	 * @param parent the container for which to calculate the preferred size.
	 * @return the number of pixels required to optimally lay out <CODE>parent</CODE>.
	 */
    public Dimension preferredLayoutSize(Container parent)
    {
        m_container = parent;
        
        Dimension preferredSize = new Dimension(m_ColumnLeader.getPreferredLocation(), m_RowLeader.getPreferredLocation());

		m_refreshPreferred = false;
		
        return preferredSize;
    }

	/** remove <CODE>component</CODE> from the layout structure.
	 * @param component the component for which the location and size will no longer be governed by this FormLayout.
	 */
    public void removeLayoutComponent(Component component)
    {
		m_RowLeader.removeLayoutComponent(component);
		m_ColumnLeader.removeLayoutComponent(component);
		ignore(component);
    }

    /** Add a <code>component</code> that will align with the labels in <code>column</code>.
	 * @param component the component to add
	 * @param row the index of the row to add <CODE>component</CODE> to
	 * @param column the index of the column to add <CODE>component</CODE> to
	 */
    public void add(Component component, int row, int column)
    {
        m_RowLeader.add(component, row);
        m_ColumnLeader.add(component, row, column, m_pct);
		listen(component);
    }

    /** Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param row the index of the row to add <CODE>label</CODE> and <CODE>field</CODE> to
	 * @param column the index of the column to add <CODE>label</CODE> and <CODE>field</CODE> to.
	 */
    public void add(Component label, Component field, int row, int column)
    {
        m_RowLeader.add(label, field, row);
        m_ColumnLeader.add(label, field, row, column, m_pct);
		listen(label);
		listen(field);
    }
    
    /** Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>,
	 * subject to the specified <code>mode</code>.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param row the index of the row to add <CODE>label</CODE> and <CODE>field</CODE> to
	 * @param column the index of the column to add <CODE>label</CODE> and <CODE>field</CODE> to.
	 * @param mode identifies a system to apply when positioning the label: one of {@link #FREE_LABEL}, {@link #FREE_FIELD}, {@link #LABEL_ON_TOP}, {@link #DEFAULT}.
	 */
    public void add(Component label, Component field, int row, int column, int mode)
    {
		listen(label);
		listen(field);
        if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
        {
            add(label, field, row, column);
        }

        if ((column == 0) && (mode == FormLayout.FREE_LABEL))
        {
            add(label, field, row, column);
        }

		if (mode == FormLayout.LABEL_ON_TOP)
		{
			if (row >= (Integer.MAX_VALUE - 1))
			{
				add(label, field, row, column);
			}

			try
			{
				((JComponent)label).setAlignmentY(Component.BOTTOM_ALIGNMENT);
			}
			catch (ClassCastException e) {}
			
			// label goes on its own row
			m_RowLeader.add(label, row);
			m_ColumnLeader.add(label, row, column, m_pct);

			m_RowLeader.add(field, row + 1);
			m_ColumnLeader.add(field, row + 1, column, m_pct);
			return;
		}

        m_RowLeader.add(label, field, row);
        m_ColumnLeader.add(label, field, row, column, mode, m_pct);
    }

    /** Add a <code>component</code> that will align with the labels in <code>column</code>,
	 * and span from <code>startRow</code> to <code>endRow</code>.
	 * @param component the component to be governed by this FormLayout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param column the index of the column to add <CODE>component</CODE> to
	 */
    public void addMultiRow(Component component, int startRow, int endRow, int column)
    {
		listen(component);
        m_RowLeader.addMultiRow(component, startRow, endRow);
        m_ColumnLeader.addMultiRow(component, startRow, endRow, column, m_pct);
    }

    /** Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>.
	 * and span from <code>startRow</code> to <code>endRow</code>.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param column the index of the column to add <CODE>component</CODE> to */
    public void addMultiRow(Component label, Component field, int startRow, int endRow, int column)
    {
		listen(label);
		listen(field);
        m_RowLeader.addMultiRow(label, field, startRow, endRow);
        m_ColumnLeader.addMultiRow(label, field, startRow, endRow, column, m_pct);
    }
    
    /** Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>,
	 * subject to the specified <code>mode</code>, and span from <code>startRow</code> to <code>endRow</code>.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param mode identifies a system to apply when positioning the label: one of {@link #FREE_LABEL}, {@link #FREE_FIELD}, {@link #LABEL_ON_TOP}, {@link #DEFAULT}.
	 * @param column the index of the column to add <CODE>component</CODE> to */
    public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, int mode)
    {
		listen(label);
		listen(field);
		if (startRow > endRow)
		{
			endRow = startRow;
		}

        if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
        {
            addMultiRow(label, field, startRow, endRow, column);
        }

		if (mode == FormLayout.LABEL_ON_TOP)
		{
			if (endRow >= (Integer.MAX_VALUE - 1))
			{
				add(label, field, startRow, endRow, column);
			}
			if (startRow >= endRow)
			{
				endRow = startRow + 1;
			}
			
			try
			{
				((JComponent)label).setAlignmentY(Component.BOTTOM_ALIGNMENT);
			}
			catch (ClassCastException e) {}
			
			m_RowLeader.add(label, startRow);
			m_ColumnLeader.add(label, startRow, column, m_pct);

			m_RowLeader.addMultiRow(field, startRow + 1, endRow);
			m_ColumnLeader.addMultiRow(field, startRow + 1, endRow, column, m_pct);
			return;
		}

        m_RowLeader.addMultiRow(label, field, startRow, endRow);
        m_ColumnLeader.addMultiRow(label, field, startRow, endRow, column, mode, m_pct);
    }

    /** Add a <code>component</code> that will align with the labels in <code>column</code>,
	 * and will stretch as far as <code>preferredSize.width * fillRightPct</code> to right justify.
	 * @param row the index of the row to add <CODE>component</CODE> to
	 * @param column the index of the column to add <CODE>component</CODE> to
	 * @param component The component to be governed by this FormLayout.
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
	 */
    public void add(Component component, int row, int column, double fillRightPct)
    {
		listen(component);
        m_RowLeader.add(component, row);
        m_ColumnLeader.add(component, row, column, fillRightPct);
    }

    /**
     * Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>.
     * and will stretch as far as <code>preferredSize.width * fillRightPct</code> to right justify.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param row the index of the row to add <CODE>label</CODE> and <CODE>field</CODE> to
	 * @param column the index of the column to add <CODE>label</CODE> and <CODE>field</CODE> to.
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
     */
    public void add(Component label, Component field, int row, int column, double fillRightPct)
    {
		listen(label);
		listen(field);
        m_RowLeader.add(label, field, row);
        m_ColumnLeader.add(label, field, row, column, fillRightPct);
    }
    
    /**
     * Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>,
     * subject to the specified <code>mode</code>, and will stretch as far as <code>preferredSize.width * fillRightPct</code> to right justify.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param row the index of the row to add <CODE>label</CODE> and <CODE>field</CODE> to
	 * @param column the index of the column to add <CODE>label</CODE> and <CODE>field</CODE> to.
	 * @param mode identifies a system to apply when positioning the label: one of {@link #FREE_LABEL}, {@link #FREE_FIELD}, {@link #LABEL_ON_TOP}, {@link #DEFAULT}.
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
     */
    public void add(Component label, Component field, int row, int column, int mode, double fillRightPct)
    {
		listen(label);
		listen(field);
        if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
        {
            add(label, field, row, column, fillRightPct);
        }

        if ((column == 0) && (mode == FormLayout.FREE_LABEL))
        {
            add(label, field, row, column, fillRightPct);
        }

		if (mode == FormLayout.LABEL_ON_TOP)
		{
			if (row >= (Integer.MAX_VALUE - 1))
			{
				add(label, field, row, column, fillRightPct);
			}

			try
			{
				((JComponent)label).setAlignmentY(Component.BOTTOM_ALIGNMENT);
			}
			catch (ClassCastException e) {}
			
			m_RowLeader.add(label, row);
			m_ColumnLeader.add(label, row, column, fillRightPct);

			m_RowLeader.add(field, row + 1);
			m_ColumnLeader.add(field, row + 1, column, fillRightPct);
		}

        m_RowLeader.add(label, field, row);
        m_ColumnLeader.add(label, field, row, column, mode, fillRightPct);
    }

    /**
     * Add a <code>component</code> that will align with the labels in <code>column</code>, 
     * will span from <code>startRow</code> to <code>endRow</code>, and will stretch as far
     * as <code>preferredSize.width * fillRightPct</code> to reach the right side.
	 * @param component the component to add to the layout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param column the index of the column to add <CODE>component</CODE> to
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
     */
    public void addMultiRow(Component component, int startRow, int endRow, int column, double fillRightPct)
    {
		listen(component);
        m_RowLeader.addMultiRow(component, startRow, endRow);
        m_ColumnLeader.addMultiRow(component, startRow, endRow, column, fillRightPct);
    }

    /**
     * Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>.
     * will span from <code>startRow</code> to <code>endRow</code>, and will stretch as far
     * as <code>preferredSize.width * fillRightPct</code> to reach the right side.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param column the index of the column to add <CODE>component</CODE> to
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
     */
    public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, double fillRightPct)
    {
		listen(label);
		listen(field);
        m_RowLeader.addMultiRow(label, field, startRow, endRow);
        m_ColumnLeader.addMultiRow(label, field, startRow, endRow, column, fillRightPct);
    }
    
    /**
     * Add <code>label</code> and <code>field</code> with alignment respective to the other labels and fields in <code>column</code>,
     * subject to the specified <code>mode</code>, will span from <code>startRow</code> to <code>endRow</code>, and will stretch as far
     * as <code>preferredSize.width * fillRightPct</code> to reach the right side.
	 * @param label the label to add to the layout
	 * @param field the field to add to the layout
	 * @param startRow the index of the first row to be occupied by <CODE>component</CODE>.
	 * @param endRow the index of the last row to be occupied by <CODE>component</CODE>.
	 * @param column the index of the column to add <CODE>component</CODE> to
	 * @param mode identifies a system to apply when positioning the label: one of {@link #FREE_LABEL}, {@link #FREE_FIELD}, {@link #LABEL_ON_TOP}, {@link #DEFAULT}.
	 * @param fillRightPct the right-justification proximity percentage: see {@link #m_pct}.
     */
    public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, int mode, double fillRightPct)
    {
		listen(label);
		listen(field);
		if (startRow > endRow)
		{
			endRow = startRow;
		}

        if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
        {
            addMultiRow(label, field, startRow, endRow, column, fillRightPct);
        }

		if (mode == FormLayout.LABEL_ON_TOP)
		{
			if (endRow >= (Integer.MAX_VALUE - 1))
			{
				add(label, field, startRow, endRow, column, fillRightPct);
			}
			if (startRow >= endRow)
			{
				endRow = startRow + 1;
			}

			try
			{
				((JComponent)label).setAlignmentY(Component.BOTTOM_ALIGNMENT);
			}
			catch (ClassCastException e) {}
			
			m_RowLeader.add(label, startRow);
			m_ColumnLeader.add(label, startRow, column, fillRightPct);

			m_RowLeader.addMultiRow(field, startRow + 1, endRow);
			m_ColumnLeader.addMultiRow(field, startRow + 1, endRow, column, fillRightPct);
			return;
		}

        m_RowLeader.addMultiRow(label, field, startRow, endRow);
        m_ColumnLeader.addMultiRow(label, field, startRow, endRow, column, mode, fillRightPct);
    }

    /** All subsequent <code>add()</code> calls that do not specify <code>fillRightPct</code> will use this <code>fillRightPct</code>.
	 * @param fillRightPct the new default justification proximity percentage: see {@link #m_pct}.
	 */
	public void setDefaultFillRightPct(double fillRightPct)
	{
		m_refreshMinimum = true;
		m_refreshPreferred = true;
		m_pct = fillRightPct;
	}

	/** Accessor for the default justification proximity percentage.
	 * @return the default justification proximity percentage: see {@link #m_pct}.
	 */
    public double getDefaultFillRightPct()
    {
        return m_pct;
    }

	// Return the ColumnLayout for "column"; create it if it doesn't exist
	/** Get the ColumnLayout with user-specified index <CODE>column</CODE>.
	 * @param column the index of the ColumnLayout to locate.
	 * @return the ColumnLayout with user-specified index <CODE>column</CODE>.
	 */
  	ColumnLayout getColumn(int column)
    {
        Enumeration e = m_Columns.elements();
        ColumnLayout nextColumn = null;
        int index = 0;
        while (e.hasMoreElements())
        {
            nextColumn = (ColumnLayout)e.nextElement();
            if (column == nextColumn.getIndex())
            {
                return nextColumn;
            }
            if (column < nextColumn.getIndex())
            {
                ColumnLayout newColumn = new ColumnLayout(column, this);
                m_Columns.insertElementAt(newColumn, index);
                return newColumn;
            }
            index++;
        }
        return null;
    }

	/** Terminated ancestor method.
	 * @param name ignored
	 * @param comp ignored
	 */
    public void addLayoutComponent(String name, Component comp)
    {
        System.out.println("FormLayout.addLayoutComponent(String, Component): Warning!  Use of unsupported method!");
    }

    // Oversees and coordinates RowLayouts
	/** One instance of RowLeader resides at the end of the linked list of rows.  All recursive row operations start with the RowLeader.  The RowLeader coordinates the addition of all components to the row structure.
	 */
    class RowLeader extends RowLayout
    {
		/** Instantiate the single RowLeader of <CODE>containingLayout</CODE>.
		 * @param containingLayout the FormLayout containing the new RowLeader.
		 */
        public RowLeader(FormLayout containingLayout)
        {
            super(Integer.MAX_VALUE, containingLayout);
        }

		/** Add <CODE>label</CODE> and <CODE>field</CODE> to the row structure, in a row with index <CODE>row</CODE> (create the row if it does not exist yet).
		 * @param label the new label
		 * @param field the new field
		 * @param row the user-specified index of the row to add label and field to; create a row with this index if it does not yet exist.
		 */
        public void add(Component label, Component field, int row)
        {
            RowLayout rowLayout = m_RowLeader.getRow(row);
            rowLayout.add(label);
            rowLayout.add(field);
        }

		/** Add component to the row structure in a row with index <CODE>row</CODE>; if a row with this index does not exist, create one.
		 * @param component the component to add to the row structure.
		 * @param row the row to add.
		 */
        public void add(Component component, int row)
        {
            RowLayout rowLayout = m_RowLeader.getRow(row);
            rowLayout.add(component);
        }

		/** Add <CODE>component</CODE> to the row structure in a RowLayout with index <CODE>startRow</CODE>, and add a floater to the RowLayout with index <CODE>endRow</CODE>.
		 * @param component the component to add
		 * @param startRow the user-specified index of the row that governs <CODE>component</CODE>.
		 * @param endRow the user-specified index of the last row in the layout that reserves space for <CODE>component</CODE>.
		 */
        public void addMultiRow(Component component, int startRow, int endRow)
        {
            getRow(endRow).addFloater(component, startRow);
        }

		/** Add <CODE>component</CODE> to the row structure in a RowLayout with index <CODE>startRow</CODE>, and add a floater to the RowLayout with index <CODE>endRow</CODE>.
		 * @param label the label to add
		 * @param field the field to add
		 * @param startRow the user-specified index of the row that governs <CODE>component</CODE>.
		 * @param endRow the user-specified index of the last row in the layout that reserves space for <CODE>component</CODE>.
		 */
        public void addMultiRow(Component label, Component field, int startRow, int endRow)
        {
            getRow(startRow).add(label);
            getRow(endRow).addFloater(field, startRow);
        }

		// never remove the RowLeader!
		/** overriden to never remove the RowLeader (because it is always empty and must always be present in the row structure).
		 */
		protected void removeIfEmpty() {}

		/** Set the y-coordinate and height of all the components in the layout, given <CODE>room</CODE> pixels to fit them in.
		 * @param room the number of y-coordinate pixels in which to fit the components in the layout.
		 */
		public void layoutRows(int room)
        {
            if (room < m_minLocation)
            {
                room = m_minLocation;
            }

            findPreferredLocation();
            findMinimumLocation();
        	
            double pct = 1;
            if (m_prefLocation > m_minLocation) // && (room >= m_minLocation))
            {
                pct = (double) ( ((double)(room - m_minLocation)) / ((double)(m_prefLocation - m_minLocation)) );
            }
			// never layout larger than the aggregate preferred size
            if (pct > 1)
            {
                pct = 1;
            }
            setLocation(pct);
            doLayout();
        }

		/** Get the y-coordinate of the RowLeader in the preferred layout scenario.
		 * @return the y-coordinate of the RowLeader in the preferred layout scenario.
		 */
        public int getPreferredLocation()
        {
			if (m_refreshPreferred)
			//if (true)
			{
            	findPreferredLocation();
			}
            return m_prefLocation; 
        }

		/** Get the y-coordinate of the RowLeader in the minimum layout scenario.
		 * @return the y-coordinate of the RowLeader in the minimum layout scenario.
		 */
        public int getMinimumLocation()
        {
			if (m_refreshMinimum)
			//if (true)
			{  
            	findMinimumLocation();
			}
            return m_minLocation;
        }

		/** called by the location calculation system; in this case, we just account for the container insets.
		 * @param minLoc the calculated minLocation, to adjust by the container insets.
		 */
        protected void setMinimumLocation(int minLoc)
        {
            m_minLocation = minLoc + getRightInset();
        }
    
		/** called by the location calculation system; in this case, we just account for the container insets.
		 * @param prefLoc the calculated minLocation, to adjust by the container insets.
		 */
        protected void setPreferredLocation(int prefLoc)
        {
            m_prefLocation = prefLoc + m_externalVGap;
        }

		/** Calculate the upper boundary of the RowLeader, based on its location in the current layout and the externalVGap.
		 * @return the largest pixel y-coordinate on which any component governed by this FormLayout is drawn.
		 */
        protected int getUpperBoundary()
        {
            return m_currLocation - m_externalVGap;
        }

		/** obtain the gap that this RowLayout adjusts for: in this case, the external gap.
		 * @return m_externalVGap
		 */
        protected int getGap()
        {
            return m_externalVGap;
        }
    }

	// oversees and coordinates ColumnLayout`s
	/** One ColumnLeader instance resides in each FormLayout; it coordinates the addition of components to the Column structure; all recursive SegmentLayout operations start at the ColumnLeader.
	 */
    class ColumnLeader extends ColumnLayout
    {
		/** The Ghost instances that reserve space for multi-row components in the rows that they cover but do not occupy.
		 */
        private hVector m_invisibleGhosts = new hVector();

		/** Create the ColumnLeader for <CODE>containingLayout</CODE>.
		 * @param containingLayout the FormLayout for which the new ColumnLeader governs column structure.
		 */
        public ColumnLeader(FormLayout containingLayout)
        {
            super(Integer.MAX_VALUE, containingLayout);
        }

		/** Add component to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param component the component to add.
		 * @param row the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void add(Component component, int row, int column, double pct)
        {
            SegmentLayout segmentLayout = getSegment(getColumn(column), row);
            segmentLayout.add(component, FormLayout.FREE_FIELD, pct);
        }

		/** Add label and field to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param label the label to add.
		 * @param field the field to add.
		 * @param row the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void add(Component label, Component field, int row, int column, double pct)
        {
            SegmentLayout segmentLayout = getSegment(getColumn(column), row);
            segmentLayout.add(label, pct);
            segmentLayout.add(field, pct);
        }

		/** Add label and field to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param label the label to add.
		 * @param field the field to add.
		 * @param row the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param mode the system to apply when choosing the relationship between label and field and the line dividing labels and fields in the column.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void add(Component label, Component field, int row, int column, int mode, double pct)
        {
            SegmentLayout segmentLayout = getSegment(getColumn(column), row);
            segmentLayout.add(label, mode, pct);
            segmentLayout.add(field, pct);
        }

		/** Add component to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param component the component to add.
		 * @param startRow the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param endRow the index of the SegmentLayout to add a ghost for <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void addMultiRow(Component component, int startRow, int endRow, int column, double pct)
        {
			add(component, startRow, column, pct);
    		addMultiRows(component, startRow, endRow, column, FormLayout.FREE_FIELD);
        }

		/** Add label and field to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param label the label to add.
		 * @param field the field to add.
		 * @param startRow the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param endRow the index of the SegmentLayout to add a ghost for <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, double pct)
        {
			add(label, field, startRow, column, pct);
    		addMultiRows(field, startRow, endRow, column, FormLayout.DEFAULT);
        }

		/** Add label and field to the ColumnLayout with index <CODE>column</CODE> and the SegmentLayout with index <CODE>row</CODE>, using the justification proximity percentage <CODE>pct</CODE>.
		 * @param label the label to add.
		 * @param field the field to add.
		 * @param startRow the index of the SegmentLayout to add <CODE>component</CODE> to.
		 * @param endRow the index of the SegmentLayout to add a ghost for <CODE>component</CODE> to.
		 * @param column the index of the ColumnLayout to add <CODE>component</CODE> to.
		 * @param mode the system to apply when choosing the relationship between label and field and the line dividing labels and fields in the column.
		 * @param pct the justification proximity percentage to apply to <CODE>component</CODE> (see {@link #m_pct}).
		 */
        public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, int mode, double pct)
        {
			add(label, field, startRow, column, mode, pct);
    		addMultiRows(field, startRow, endRow, column, FormLayout.DEFAULT);
        }

		/** internal facility to add a ghost to each SegmentLayout between (startRow + 1) and endRow.
		 * @param field the field to add
		 * @param startRow the index of the row that actually governs <CODE>field</CODE>
		 * @param endRow the index of the last row to add ghosts to.
		 * @param column the index of the column to add the ghosts in
		 * @param mode the system used by the SegmentLayout and <CODE>startRow</CODE> to lay out <CODE>field</CODE> and its corresponding label.
		 */
		private void addMultiRows(Component field, int startRow, int endRow, int column, int mode)
		{
            ColumnLayout columnLayout = getColumn(column);
			getSegment(columnLayout, startRow).setLastGhost(endRow);

            Enumeration e = m_segments.elements();
            SegmentLayout nextLeaderSegment  = null;
            SegmentLayout nextSegment = null;
    
            while (e.hasMoreElements())
            {
                nextLeaderSegment = (SegmentLayout)e.nextElement();
                if ((nextLeaderSegment.getIndex() > startRow) && (nextLeaderSegment.getIndex() <= endRow))
                {
                    nextSegment = nextLeaderSegment.getSegment(columnLayout);
                    nextSegment.addGhost(new JLabel(""));
                    nextSegment.add(field, mode, 0);
                }
            }
            m_invisibleGhosts.add(new Ghost(startRow, endRow, mode, field, columnLayout));
		}

		/** Remove <CODE>component</CODE> from the column structure, and any Ghost instances that reserve space for it.
		 * @param component the component to no longer govern in the column structure.
		 */
		public void removeLayoutComponent(Component component)
		{
			Enumeration e = m_segments.elements();
			while(e.hasMoreElements())
			{
				((SegmentLayout)e.nextElement()).removeLayoutComponent(component);
			}
		}

		// create new if not found
		/** Same as {@link #findSegment(int)}, but create the SegmentLayout if it doesn't exist.
		 * @param columnLayout the ColumnLayout in which to seek the SegmentLayout
		 * @param row the index of the sought SegmentLayout.
		 * @return the SegmentLayout sought, if it exists in the layout.
		 */
        public SegmentLayout getSegment(ColumnLayout columnLayout, int row)
        {
            SegmentLayout segmentLayout = findSegment(row);
            if (segmentLayout == null)
            {
                segmentLayout = new SegmentLayout(row);
                m_segments.add(segmentLayout);
                
                Enumeration e = m_invisibleGhosts.elements();
                while (e.hasMoreElements())
                {
                    ((Ghost) e.nextElement()).makeVisible(segmentLayout);
                }
            }
            return segmentLayout.getSegment(columnLayout);
        }

		/** Get the SegmentLayout contained by the ColumnLeader and the RowLayout with index <CODE>row</CODE>.
		 * @param row the index of the RowLayout that contains the SegmentLayout in question.
		 * @return The specified SegmentLayout, or null if it does not exist.
		 */
        private SegmentLayout findSegment(int row)
        {
            Enumeration e = m_segments.elements();
            SegmentLayout nextSegment = null;
    
            while (e.hasMoreElements())
            {
                nextSegment = (SegmentLayout)e.nextElement();
                if (nextSegment.getIndex() == row)
                {
                    return nextSegment;
                }
            }
            return null;
        }
    
		/** Set the x-coordinate and width of each component governed by this FormLayout, given that there are <CODE>room</CODE> pixels along the x-axis to work with.
		 * @param room the number of x-axis pixels to work with.
		 */
        public void layoutColumns(int room)
        {
            if (room < m_minLocation)
            {
                room = m_minLocation;
            }
            else if (room > m_prefLocation)
            {
                //if (m_pct < 1.0)
                //{
                    room = m_prefLocation;
                //}
            }

			room -= m_externalHGap;

            recalcMinimumLocations();
            recalcPreferredLocations();
                
            double pct = 1;
            if ((m_prefLocation - m_minLocation) > 0)
            {
                pct = (double)((double)(room - (m_minLocation - m_externalHGap)) / (double)((m_prefLocation - m_externalHGap) - (m_minLocation - m_externalHGap)));
            }

			// instruct each ColumnLayout to find its location (left extent of labels) for this layout size (by pct)
			// (roundoff error from pct made up in SegmentLayout.doLayout())
            Enumeration e = m_Columns.elements();
            while (e.hasMoreElements())
            {
                ColumnLayout nextColumn = (ColumnLayout)e.nextElement();
                nextColumn.findIntermediateLocation(pct);
            }
			m_currLocation = room;
            e = m_Columns.elements();
            while (e.hasMoreElements())
            {
                ColumnLayout nextColumn = (ColumnLayout)e.nextElement();
                nextColumn.doLayout();
            }
        }

		/** Calculate and return the preferred location of the ColumnLeader; recursively calls getPreferredLocation() on each ColumnLayout in this FormLayout.  Values are cached.
		 * @return the preferred location of the ColumnLeader.
		 */
        public int getPreferredLocation()
        {
			if (m_refreshPreferred)
			//if (true)
			{
            	recalcPreferredLocations();
			}
            return m_prefLocation;
        }

		/** Calculate and return the location of the ColumnLeader given the minimum amount of space; recursively calls getMinimumLocation() on each ColumnLayout in this FormLayout.  Values are cached.
		 * @return the minimum location of the ColumnLeader.
		 */
        public int getMinimumLocation()
        {
			if (m_refreshMinimum)
			//if (true)
			{
            	recalcMinimumLocations();
			}
            return m_minLocation;
        }

		/** Set the minimum location of the ColumnLeader, adjusting for the externalHGap.
		 * @param minLoc the minimum location as calculated for the ColumnLeader.
		 */
        protected void setMinimumLocation(int minLoc)
        {
            m_minLocation = minLoc + m_externalHGap;
        }
    
		/** Set the preferred location of the ColumnLeader, adjusting for the externalHGap.
		 * @param prefLoc the preferred location calculated for the ColumnLeader.
		 */
        protected void setPreferredLocation(int prefLoc)
        {
            m_prefLocation = prefLoc + m_externalHGap;
        }

		// Recalculate the minimum locations of each Column.
		// Call this every time m_minLocation and m_prefLocation are accessed,
		// because the componentry may have changed in some relevant way since
		// the last time this was called.
		/** Calculate the minimum location of each ColumnLayout in this FormLayout.
		 */
        void recalcMinimumLocations()
        {
            Enumeration e = m_Columns.elements();
            ColumnLayout nextColumn = null;
            while (e.hasMoreElements())
            {
                nextColumn = (ColumnLayout)e.nextElement();
                nextColumn.findMinimumLocation();
            }
        }

		// Recalculate the preferred locations of each Column.
		// Call this every time m_minLocation and m_prefLocation are accessed,
		// because the componentry may have changed in some relevant way since
		// the last time this was called.
		/** Calculate the preferred location of each ColumnLayout in this FormLayout.
		 */
        void recalcPreferredLocations()
        {
            Enumeration e = m_Columns.elements();
            ColumnLayout nextColumn = null;
            while (e.hasMoreElements())
            {
                nextColumn = (ColumnLayout)e.nextElement();
                nextColumn.findPreferredLocation();
            }
        }

		/** Reserves space for multi-row components it in the rows that they cover but do not occupy.
		 */
        class Ghost
        {
			/** The index of the row occupied by the multi-row component represented by this Ghost.
			 */
            private int             m_startRow;
			/** The index of the last row covered by the multi-row component represented by this Ghost.
			 */
            private int             m_endRow;
			/** The mode with which the component represented by this Ghost was added.
			 */
            private int             m_mode;
			/** The component represented by this Ghost.
			 */
            private Component       m_field;
			/** The ColumnLayout in which this Ghost reserves space.
			 */
            private ColumnLayout    m_columnLayout;
            
			/** Create a Ghost for a component <CODE>field</CODE> that is governed by the RowLayout with index <CODE>startRow</CODE>, that covers all subsequent rows up to that with index <CODE>endRow</CODE>, which behaves according to the rules of <CODE>mode</CODE>, and is contained in <CODE>columnLayout</CODE>.
			 * @param startRow index of the RowLayout that governs <CODE>field</CODE>
			 * @param endRow index of the last RowLayout covered by <CODE>field</CODE> (not necessarily instantiated)
			 * @param mode specifies the behavior of <CODE>columnLayout</CODE> in regard to <CODE>field</CODE>.
			 * @param field the field for which to reserve space
			 * @param columnLayout the ColumnLayout in which to reserve space for <CODE>field</CODE>
			 */
            public Ghost(int startRow, int endRow, int mode, Component field, ColumnLayout columnLayout)
            {
                m_startRow = startRow;
                m_endRow = endRow;
                m_mode = mode;
                m_field = field;
                m_columnLayout = columnLayout;
            }

			/** When a RowLayout is added to a FormLayout and overlaps the space reserved for a multi-row component by a Ghost, this method will create the necessary space reservation for the Ghost's component in the RowLayout.  If the RowLayout does not overlap the space reservation of this Ghost, then this method does nothing.
			 * @param segmentLayout The SegmentLayout of the ColumnLeader with the index of the RowLayout that has been added.
			 */
            public void makeVisible(SegmentLayout segmentLayout)
            {
                if ((segmentLayout.getIndex() >= m_startRow) && (segmentLayout.getIndex() <= m_endRow))
                {
                    SegmentLayout ghostSegment = segmentLayout.getSegment(m_columnLayout);
                    ghostSegment.addGhost(new JLabel(""));
                    ghostSegment.add(m_field, m_mode, 0);
                }
            }
        }
    }
	
	/** ComponentListener method -- null implementation
	 * @param e ignored
	 */
	public void componentHidden(ComponentEvent e) {}
	/** ComponentListener method -- null implementation
	 * @param e ignored
	 */
	public void componentMoved(ComponentEvent e) {}
	
	/** FormLayout listens to all components it governs: when a component is resized, the flags {@link #m_refreshMinimum} and {@link #m_refreshPreferred} are set, so that sizes will be recalculated on the next accessor calls, or the next call to {@link #layoutContainer(Container)}.
	 * @param e ignored
	 */
	public void componentResized(ComponentEvent e) 
	{
		m_refreshMinimum = true;
		m_refreshPreferred = true;
	}
	
	/** ComponentListener method -- null implementation
	 * @param e ignored
	 */
	public void componentShown(ComponentEvent e) {}
	
	/** Establish this FormLayout as a listener to <CODE>component</CODE>: see {@link #componentResized(ComponentEvent)}.
	 * @param component the component to listen to.
	 */
	protected void listen(Component component)
	{
		addComponent(component);
		component.addComponentListener(this);
		m_refreshMinimum = true;
		m_refreshPreferred = true;
	}
	
	/** Remove component from the list of components listened to by this FormLayout.
	 * @param component the component to no longer listen to.
	 */
	protected void ignore(Component component)
	{
		removeComponent(component);
		component.removeComponentListener(this);
		m_refreshMinimum = true;
		m_refreshPreferred = true;
	}
	
	/** Remove <CODE>comp</CODE> from the list of components that is maintained in the order they were added: m_ListOfAddedComponents.
	 * @param comp The component to remove.
	 */
	void removeComponent(Component comp)
	{
		m_ListOfAddedComponents.remove(comp);
	}		
	
	/** Add <CODE>comp</CODE> to the list of components that is maintained in the order they were added: m_ListOfAddedComponents.
	 * @param comp The component to add.
	 */
	void addComponent(Component comp)
	{
		m_ListOfAddedComponents.add(comp);
	}
	
	/** For each component in the FormPanel, excluding disabled subclasses of javax.swing.text.JTextComponent, set the focus order to the order in which the components were added to the panel.
	 */
	public void setChronologicalFocus()
	{
		Vector ListOfFocusableComponents = new Vector();
		Component comp;

		for(int index=0; index < m_ListOfAddedComponents.size(); index++)
		{
			comp = (Component) m_ListOfAddedComponents.get(index);
			if(comp instanceof JComponent && comp.isEnabled())
			{
				if(comp instanceof JTextComponent)
				{
					if( ((JTextComponent) comp).isEditable() )
					{
						ListOfFocusableComponents.add(comp);
					}
				}
				else
				{
					ListOfFocusableComponents.add(comp);
				}
			}
		}

		for(int index=0; index < ListOfFocusableComponents.size(); index++)
		{
			JComponent current = (JComponent) ListOfFocusableComponents.get(index);
			JComponent next;

			if(index == ListOfFocusableComponents.size() - 1)
			{
				next = (JComponent) ListOfFocusableComponents.get(0);
			}
			else
			{
				next = (JComponent) ListOfFocusableComponents.get(index+1);
			}
			current.setNextFocusableComponent(next);
		}
	}
}
