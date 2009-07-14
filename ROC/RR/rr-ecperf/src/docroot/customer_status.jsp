<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
   
<TITLE>ECPerf Page</TITLE>


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
<TR BGCOLOR="#666699">
	<TD  WIDTH="50%">
		&nbsp&nbsp&nbsp
	<TD align=right valign=TOP WIDTH="50%">
		<font face="Arial,Helvetica" COLOR="#FFFFFF" size=+2>  Customer &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp;&nbsp;&nbsp&nbsp&nbsp&nbsp</font>
<TR>
	<TD  BGCOLOR="#666699" VALIGN=TOP>
		<CENTER>
		<P><BR>
		<IMG SRC="images/orders.gif" WIDTH="143" HEIGHT="137">
		<TABLE BORDER=0 WIDTH="100%" >
		<TR>
			<TD ALIGN=CENTER> <P><BR></P> <A href="new_order.jsp"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> New Order </font> </A> 
		<TR>
			<TD ALIGN=CENTER> <A href="new_order_ssbean.jsp">  <font color="#FFFFFF" face=helvetica,arial,san-serif size="-2"> (using session bean) </font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="change_order.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Change Order</font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="cancel_order.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Cancel Order</font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="order_status.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Order Status </font> </A>
		<TR>
			<TD ALIGN=CENTER> <A href="customer_status.html"> <font color="#FFFFFF" face=helvetica,arial,san-serif size="+1"> Customer Status </font> </A>
		</TABLE>
		</CENTER>
	<TD colspan=2>

		<center>
		<br><b><font size=+4>Customer Status</font></b></center>

		<br><b><font size=+4></font></b>&nbsp;
		<br><font size=+2></font>&nbsp;
		<br><font size=+2></font>
		<BR>
		<%@ page language="java" import="com.sun.ecperf.webbeans.*" errorPage="error.jsp" %>
		<jsp:useBean id='orderSes' scope='session' class='com.sun.ecperf.webbeans.OrderSesBean' />
		<BR>
		<% 

			String cust_id ;
			StatusCustomer [] customer_status; 
			String item_id;
			int item_qty;

			cust_id = request.getParameter("cust_id");

			customer_status = orderSes.getCustomerStatus(cust_id);
		%>
		<font size=+3>  &nbsp&nbsp Status for customer <%= cust_id %>  </font>
		<BR><BR><BR>
		<%
			for (int i=0; i < customer_status.length; i++) {
		%>
				<HR>
				<BR>
				&nbsp&nbsp Order Number : <font size=+2 ><%= customer_status[i].order_num %> </font>
				<BR>
				&nbsp&nbsp Ship Date : <font size=+2> <%= customer_status[i].ship_date %> </font>
				<BR>
				<CENTER>
				<TABLE WIDTH="60%" BORDER=1 CELLSPACING=2>
				<CAPTION> <font size=+3> Items in the order </font> </CAPTION>	
				<TR>
				<font size=+2>
				<TH> Item Name </TH>
				<TH> Quantity </TH>
				</font>
				</TR>
		<% 
				for (int j =0; j < customer_status[i].cust_items.length; j++) {
					item_id = customer_status[i].cust_items[j].item_id;
					item_qty = customer_status[i].cust_items[j].qty;
		%>
					<TR ALIGN=CENTER>
					<TD> <%= item_id %> </TD>
					<TD> <%= item_qty %> </TD>
					</TR>
		<%
				}
		%>
				</CENTER>
				</TABLE>
				<BR>
		<%
			}
		%>
			<HR>
				

		<BR>
		<BR>
		<BR>
		<BR>
		<BR>
		<BR>

		<p><font color="#3333FF"><font size=+2><a href="index.html">Back to Customer</a></font></font>
		<br><font size=+2></font>&nbsp;
		<BR><BR><BR><BR><BR><BR>

</TR>
</TABLE>
</BODY>
</HTML>

