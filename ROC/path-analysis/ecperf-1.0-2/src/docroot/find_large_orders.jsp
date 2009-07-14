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
		<br><b><font size=+4>Large Orders Information </font></b></center>

		<br><b><font size=+4></font></b>&nbsp;
		<br><font size=+2></font>&nbsp;
		<br><font size=+2></font>
		<BR>
		<%@ page language="java" import="com.sun.ecperf.webbeans.*" errorPage="error_internal.jsp" %>
		<jsp:useBean id='largeOrderSes' scope='session' class='com.sun.ecperf.webbeans.LargeOrderSesBean' />
		<BR>
		<BR>
		<BR>
		<% 

			java.util.Vector orders_list;
			LargeOrder large_order;
			String assembly_id;
			int qty;
			String due_date;
			int order_line_num;
			int sales_order_id;

			orders_list = largeOrderSes.findLargeOrders();
			if ( orders_list.size() == 0) {
		%>
				&nbsp&nbsp<font size=+3> No pending large orders at this time </font>
				<BR><BR><BR><BR><BR><BR><BR><BR><BR><BR>
				<BR><BR><BR><BR><BR><BR><BR><BR><BR><BR>
		<%
			} else {
		%>
				&nbsp&nbsp <font size=+2> There are <%= orders_list.size() %> large orders. They are listed below. 
				<BR> <BR> &nbsp&nbsp To schedule an order please click <STRONG> Assembly ID </STRONG> of the order </font>
				<font size=+2>
				<BR><BR><BR><BR>
				<CENTER>
				<TABLE WIDTH="75%" BORDER=1 CELLSPACING=2>
				<CAPTION> <font size=+3> Large Orders</font> </CAPTION>
				<TR>
				<font size=+2>
				<TH> Assembly ID </TH>
				<TH> Quantity </TH>
				<TH> Due Date </TH>
				</font>
				</TR>

		<% 
				for (int i =0; i < orders_list.size(); i++) {
					large_order = (LargeOrder) orders_list.elementAt(i);
					assembly_id = large_order.assembly_id;
					qty = large_order.qty;
					due_date = large_order.due_date;
					order_line_num = large_order.order_line_num;
					sales_order_id = large_order.sales_order_id;
		%>
					<TR ALIGN=CENTER>
					<TD> 
					<A HREF="schedule_work_order.jsp?assembly_id=<%= assembly_id %>&qty=<%= qty %>&due_date=<%=due_date %>&order_line_num=<%= order_line_num %>&sales_order_id=<%= sales_order_id %> ">
					<%= assembly_id %> 
					</A>
					</TD>
					<TD> <%= qty %> </TD>
					<TD> <%= due_date %> </TD>
		<%
				}	
		%>
				</TABLE>
				</CENTER>
				<BR><BR><BR>
		<%
			}
		%>
		<BR>
		<BR>

		<p><font color="#3333FF"><font size=+2><a href="manufacturing.html">Back to Manufacturing</a></font></font>
		<br><font size=+2></font>&nbsp;
		<BR><BR><BR><BR><BR><BR>

</TR>
</TABLE>
</BODY>
</HTML>

