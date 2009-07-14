package org.hs.jfc.shared;

import javax.swing.JTextField;

public class hTextField extends JTextField
{
	public hTextField()
	{
		super();
	}

	public hTextField(int maxLength)
	{
		super(maxLength);
	}

    public hTextField(int maxLength, int fontSize)
    {
        super(maxLength);
//        setFont(getFont().deriveFont((float) fontSize));
        setFont(getFont().decode(getFont().getName() + "-" + fontSize));
    }
}
		
