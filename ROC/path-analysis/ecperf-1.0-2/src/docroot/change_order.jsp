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
<TR BGCOLOR="#666699" >
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
		<TR >
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

		<FORM METHOD=POST ACTION="change_order.jsp">

		&nbsp;
		<center>
		<br><b><font size=+4>Change Order</font></b></center>

		<br><b><font size=+4></font></b>&nbsp;
		<br><font size=+2></font>&nbsp;
		<br><font size=+2></font>

		<%@ page language="java" import="com.sun.ecperf.webbeans.*" errorPage="error.jsp" %>
		<jsp:useBean id='processList' scope='session' class='com.sun.ecperf.webbeans.ProcessListBean' />
		<jsp:useBean id='orderSes' scope='session' class='com.sun.ecperf.webbeans.OrderSesBean' />

		<%
			String order_number;
			order_number = request.getParameter("order_num");
		%>

		<CENTER>
		<font size=+2>Order Number <%= order_number %> </font> <INPUT NAME="order_num" TYPE=hidden VALUE=<%= order_number %> ><br>

		<%  

			String submit_value = request.getParameter("submit");
		%>
		<%
			if (submit_value != null) {
		%>
		<%
				if (submit_value.equals("Add")) {
					processList.addItem(request.getParameter("item_name"), request.getParameter("qty"));
				} else if (submit_value.equals("Remove")) {
					processList.removeItem(request.getParameter("item_name"), request.getParameter("qty"));
				} else if (submit_value.equals("Remove All")) {
					processList.resetCustomerList();
				} else if (submit_value.equals("Change")) {
					processList.setOrderNumber(order_number);
		%>
					<jsp:forward page="change_order_submit.jsp" />
		<%
				}
			} else {
				processList.setCustomerList(orderSes.getOrderStatus(order_number));
			}

			java.util.Vector customer_list;
			CustomerItem customer_item;
			String item_name;
			String item_id;
			int item_qty;
			customer_list = processList.getCustomerList();
			if (customer_list.size() > 0 ) {
		%>
				<HR>
				<BR> 
				<TABLE WIDTH="75%" BORDER=1 CELLSPACING=2>
				<CAPTION> <font size=+3> Current items in the list </font> </CAPTION>
				<TR>
				<font size=+2>
				<TH> Item Name </TH>
				<TH> Item ID </TH>
				<TH> Quantity </TH>
				</font>
				</TR>
			
		<%
				for (int i=0; i < customer_list.size(); i++) {
					customer_item = (CustomerItem) customer_list.elementAt(i);
					item_name = customer_item.item_name;
					item_id = customer_item.item_id;
					item_qty = customer_item.qty; 
		%>
					<TR ALIGN=CENTER>
					<TD> <%= item_name %> </TD>
					<TD> <%= item_id %> </TD>
					<TD> <%= item_qty %> </TD>
					</TR>
		<%
				}
		%>
			</TABLE>
			<BR>
			<HR>
		<%
			}
		%>
		<%

			java.util.Vector items_list;
			String item;

			items_list = processList.getItemsList();
			if (items_list.size() > 0) {
		%>


		<font size=+2>Please choose items interested in <br> <br></font>
				<SELECT NAME="item_name">


		<%
				for(int j =0; j< items_list.size(); j++) {
					item = (String)items_list.elementAt(j);
		%>	
					<OPTION> <%= item %>
		<% 
				}
		%>
				</SELECT>
				Quantity <INPUT TYPE=text name="qty" value="" size="4" maxlength="5">
				<BR><BR><BR>
				<INPUT TYPE=submit name="submit" value="Add">
				<INPUT TYPE=submit name="submit" value="Remove">
				<INPUT TYPE=submit name="submit" value="Remove All">
				
		<% 
			} else {	
		%>
				<BR> <font size=+2 > No items in the database !!! </font>
		<% 
			} 
		%>



		<font size=+2>	<p><INPUT TYPE=submit name="submit" value="Change"> </font>
		</CENTER>

		<p><font color="#3333FF"><font size=+2><a href="index.html">Back to Customer</a></font></font>
		<br><font size=+2></font>&nbsp;
		<BR><BR><BR><BR><BR><BR><BR><BR>
		</FORM>

</TR>
</TABLE>
</BODY>
</HTML>

