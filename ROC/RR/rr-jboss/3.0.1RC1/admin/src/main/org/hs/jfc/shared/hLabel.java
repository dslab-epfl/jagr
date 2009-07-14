package org.hs.jfc.shared;

import javax.swing.JLabel;

public class hLabel extends JLabel
{
	public hLabel()
	{
		super();
	}

	public hLabel(String labelText)
	{
		super(labelText);
	}

    public hLabel(String labelText, int fontSize)
    {
        super(labelText);
        //setFont(getFont().deriveFont((float) fontSize));
        setFont(getFont().decode(getFont().getName() + "-" + fontSize));
    }

    public hLabel(String labelText, boolean visible)
    {
        super(labelText);
        setVisible(visible);
    }
}


		
