<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
   
<TITLE>ECPerf Manufacturing </TITLE>


</HEAD>
<BODY TEXT="#000000" BGCOLOR="#FFFFFF" LINK="#0000EE" VLINK="#551A8B" ALINK="#FF0000">

<TABLE BORDER=0 BGCOLOR="#FFFFFF" CELLPADDING=0 CELLSPACING=0 WIDTH="100%">
<TR>
	<TD width ="25%" align="center" rowspan="3" BGCOLOR="#666699">
	    <TABLE BGCOLOR="#FFFFCC" BORDER="0" CELLSPACING="1" CELLPADDING="0" WIDTH="100%">
	    <TR><TD>
		<TABLE COLOR="#666699" BGCOLOR="#FFFFCC" BORDER=0 WIDTH="100%">
		<TR>
			<TD align=center NOSAVE><font size=-7>&nbsp;</font><BR><A href="index.html" ><font size=+2 > Customer </font> </A> 
		<TR>
			<TD align=center> <A href="manufacturing.html" ><font size=+2 > Manufacturing </font></A><BR><font size=-7>&nbsp;</font>
		</TABLE>
	    </TD></TR>
	    </TABLE>
	<TD BGCOLOR="#666699"> 
		&nbsp&nbsp&nbsp
	<TD BGCOLOR="#666699"> 
		&nbsp&nbsp&nbsp
<TR>
	<TD  BGCOLOR="#666699" align=right rowspan="1" colspan="2">
		<IMG src="images/ecperf.gif">	
<TR BGCOLOR="#666699" >
	<TD  WIDTH="50%">
		&nbsp&nbsp&nbsp
	<TD align=right valign=TOP WIDTH="50%">
		<font face="Arial,Helvetica" COLOR="#FFFFFF" size=+2>   Manufacturing &nbsp&nbsp&nbsp&nbsp;&nbsp;&nbsp&nbsp&nbsp&nbsp</font>
<TR>
	<TD  BGCOLOR="#666699" VALIGN=TOP>
		<CENTER>
		<P><BR><BR>
		<IMG SRC="images/mfg.gif" WIDTH="143" HEIGHT="88">
		<TABLE BORDER=0 WIDTH="100%" >
		<TR >
			<TD ALIGN=CENTER> <P><BR><BR></P> <A href="find_large_orders.jsp"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Find Large Orders </font> </A>
		<TR>
            <TD ALIGN=CENTER> <A href="find_assembly_ids.jsp"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Schedule Work Order</font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="update_work_order.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Update Work Order</font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="complete_work_order.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Complete Work Order</font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="cancel_work_order.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Cancel Work Order </font> </A>
		</TABLE>
		</CENTER>
	<TD colspan=2>

		<%@ page isErrorPage="true" %>

		<center>
		<br><STRONG><font size=+3>An Exception Occurred !!</font></STRONG></center>
		<BR><BR><BR>
		<CENTER>
		The message of the exception is <br> <font color="#FF0000" size=+2> <STRONG> 
		<%= exception.getMessage() %> </STRONG> </font>
		</CENTER>
		<br> <br>
		<pre>
		<%
		    java.io.PrintWriter pout = new java.io.PrintWriter(out);
		    Throwable ex = exception;
		    for(;;) {
			if (ex == null)
			    break;
			ex.printStackTrace(pout);

			if (ex instanceof com.sun.ecperf.webbeans.OtherException) {
			    ex = ((com.sun.ecperf.webbeans.OtherException) ex).detail;
			    pout.println("\nNested Exception:");
			    continue;
			}
			else if (ex instanceof java.rmi.RemoteException) {
			    ex = ((java.rmi.RemoteException) ex).detail;
			    pout.println("\nNested Exception:");
			    continue;
			}
			else if (ex instanceof java.rmi.activation.ActivationException) {
			    ex = ((java.rmi.activation.ActivationException) ex).detail;
			    pout.println("\nNested Exception:");
			    continue;
			}
			else if (ex instanceof java.rmi.server.ServerCloneException) {
			    ex = ((java.rmi.server.ServerCloneException) ex).detail;
			    pout.println("\nNested Exception:");
			    continue;
			}
			else if (ex instanceof java.sql.SQLException) {
			    ex = ((java.sql.SQLException) ex).getNextException();
			    pout.println("\nNested Exception:");
			    continue;
			}
			break;
		    }
		%> </pre><br><br><br>

		<CENTER>
		<font size=+2 >Use the Back button on the browser to go back to the previous page </font>

		<br><br><br><font color="#3333FF"><font size=+2><a href="manufacturing.html">Back to Manufacturing</a></font></font>
		<br><font size=+2></font>&nbsp;
		</CENTER>
		<BR><BR><BR><BR><BR><BR><BR>
		<BR><BR><BR><BR><BR><BR><BR>
		</FORM>

</TR>
</TABLE>
</BODY>
</HTML>

