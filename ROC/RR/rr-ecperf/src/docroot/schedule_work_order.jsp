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

		<center>
		<br><b><font size=+4>Schedule Work Order </font></b></center>
		<CENTER>	

		<br><b><font size=+4></font></b>&nbsp;
		<br><font size=+2></font>&nbsp;
		<br><font size=+2></font>
		<BR>
		<%@ page errorPage="error_internal.jsp" %>
		<jsp:useBean id='workOrderSes' scope='session' class='com.sun.ecperf.webbeans.WorkOrderSesBean' />
		<BR>
		<BR>
		<BR>
		<% 

			Integer order_id;

			order_id = workOrderSes.scheduleWorkOrder(request.getParameter("assembly_id"),request.getParameter("qty"), request.getParameter("due_date"), request.getParameter("order_line_num"), request.getParameter("sales_order_id"));
		%>
			<font size=+3>  Scheduled the order. The order number is <%= order_id.intValue() %></font>
			<BR><BR><BR><BR>
			<font size=+2> 
			The current status of the order is <%= workOrderSes.getWorkOrderStatus(order_id) %>
			<BR><BR><BR><BR>
			Would you like to update the order ?
			</font>
			<FORM METHOD=POST ACTION="update_work_order.jsp" >
			<INPUT TYPE=hidden NAME="order_id" VALUE=<%= order_id.toString() %> >
			<INPUT TYPE=submit VALUE="Yes">
			</FORM>	
			<BR><BR><BR><BR>

		<BR>
		<BR>
		</CENTER>	

		<p><font color="#3333FF"><font size=+2><a href="manufacturing.html">Back to Manufacturing</a></font></font>
		<br><font size=+2></font>&nbsp;
		<BR><BR><BR><BR><BR>

</TR>
</TABLE>
</BODY>
</HTML>

