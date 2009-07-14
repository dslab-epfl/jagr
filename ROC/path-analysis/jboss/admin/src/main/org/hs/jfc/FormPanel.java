package org.hs.jfc;

import java.awt.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;

/** 
 * This convenient class is just a JPanel with FormLayout as its default layout manager.  For all uncommented entities, refer to {@link FormLayout}.
 * @author Byron Hawkins
 */
public class FormPanel extends JPanel
{
	public FormPanel()
	{
		super.setLayout(new FormLayout());
	}

	public FormPanel(int internalHGap, int internalVGap, int externalHGap, int externalVGap)
	{
		super.setLayout(new FormLayout(internalHGap, internalVGap, externalHGap, externalVGap));
	}

	public int getInternalHGap()
	{
		return ((FormLayout)getLayout()).getInternalHGap();
	}

	public int getInternalVGap()
	{
		return ((FormLayout)getLayout()).getInternalVGap();
	}

	public int getExternalHGap()
	{
		return ((FormLayout)getLayout()).getExternalHGap();
	}

	public int getExternalVGap()
	{
		return ((FormLayout)getLayout()).getExternalVGap();
	}

	public void setInternalVGap(int gap)
	{
		((FormLayout)getLayout()).setInternalVGap(gap);
	}

	public void setInternalHGap(int gap)
	{
		((FormLayout)getLayout()).setInternalHGap(gap);
	}

	public void setExternalVGap(int gap)
	{
		((FormLayout)getLayout()).setExternalVGap(gap);
	}

	public void setExternalHGap(int gap)
	{
		((FormLayout)getLayout()).setExternalHGap(gap);
	}

	public Dimension getPreferredSize()
	{
		long currentTime = System.currentTimeMillis();
		Dimension preferredSize = ((FormLayout)getLayout()).preferredLayoutSize(this);
		return preferredSize;
	}

	public void add(Component component, int row, int column)
	{
		super.add(component);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(component, row, column);
	}

	public void add(Component label, Component field, int row, int column)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(label, field, row, column);
	}

	public void add(Component label, Component field, int row, int column, int mode)
	{
		if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
		{
			add(label, field, row, column);
		}

		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(label, field, row, column, mode);
	}

	// all addMultiRow() methods add components that span multiple rows

	public void addMultiRow(Component component, int startRow, int endRow, int column)
	{
		super.add(component);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(component, startRow, endRow, column);
	}

	public void addMultiRow(Component label, Component field, int startRow, int endRow, int column)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(label, field, startRow, endRow, column);
	}

	public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, int mode)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(label, field, startRow, endRow, column, mode);
	}

	public void add(Component component, int row, int column, double fillRightPct)
	{
		super.add(component);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(component, row, column, fillRightPct);
	}

	public void add(Component label, Component field, int row, int column, double fillRightPct)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(label, field, row, column, fillRightPct);
	}

	public void add(Component label, Component field, int row, int column, int mode, double fillRightPct)
	{
		if ((mode < FormLayout.DEFAULT) || (mode > FormLayout.LABEL_ON_TOP))
		{
			add(label, field, row, column);
		}

		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).add(label, field, row, column, mode, fillRightPct);
	}

	public void addMultiRow(Component component, int startRow, int endRow, int column, double fillRightPct)
	{
		super.add(component);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(component, startRow, endRow, column, fillRightPct);
	}

	public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, double fillRightPct)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(label, field, startRow, endRow, column, fillRightPct);
	}

	public void addMultiRow(Component label, Component field, int startRow, int endRow, int column, int mode, double fillRightPct)
	{
		super.add(label);
		super.add(field);

		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).addMultiRow(label, field, startRow, endRow, column, mode, fillRightPct);
	}

	public void remove(Component comp)
	{
		super.remove(comp);
		long currentTime = System.currentTimeMillis();
		((FormLayout)getLayout()).removeLayoutComponent(comp);
	}

	public void setDefaultFillRightPct(double fillRightPct)
	{
		((FormLayout)getLayout()).setDefaultFillRightPct(fillRightPct);
	}

	/** Warning: not implemented!  Prints a warning to System.err.
	 */
	public Component add(Component comp)
	{
		warn("FormPanel.FormPanel.add(Component): Warning!  Use of unsupported add method");
		return null;
	}
	/** Warning: not implemented!  Prints a warning to System.err.
	 */
	public Component add(String name, Component comp)
	{
		warn("FormPanel.FormPanel.add(String, Component): Warning!  Use of unsupported add method");
		return null;
	}
	/** Warning: not implemented!  Prints a warning to System.err.
	 */
	public Component add(Component comp, int index)
	{
		warn("FormPanel.FormPanel.add(Component, int): Warning!  Use of unsupported add method");
		return null;
	}
	
	/** Warning: not implemented!  Prints a warning to System.err.
	 */
	public void add(Component comp, Object constraints) { warn("FormPanel.FormPanel.add(Component, Object): Warning!  Use of unsupported add method"); }
	/** Warning: not implemented!  Prints a warning to System.err.
	 */
	public void add(Component comp, Object constraints, int index) { warn("FormPanel.FormPanel.add(Component, Object, int): Warning!  Use of unsupported add method"); }
	// --------------------------- //

	// warn out
	/** Spew <CODE>message</CODE> to System.err.
	 * @param message spew this
	 */
	protected void warn(String message)
	{
		System.out.println(message);
	}

	public void setChronologicalFocus()
	{
		((FormLayout)getLayout()).setChronologicalFocus();
	}
}
