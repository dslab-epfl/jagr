<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tpai="http://blueprints.j2ee.sun.com/TPAInvoice"
	xmlns:tpali="http://blueprints.j2ee.sun.com/TPALineItem"
	version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java Pet Store: Shipped Order <xsl:value-of select="//OrderId|//tpai:OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/Invoice|/tpai:Invoice">
		<b>Thank you</b> for placing an order with <b>Java Pet Store 1.3.1_02</b><br />
		This part of your order <font color="red"><xsl:value-of select="//OrderId|//tpai:OrderId" /></font> has been shipped
		<p /> The contents of this shipment are:<br />
		<table border="1">
			<tr><td>Category</td><td>Product #</td><td>Quantity</td><td>Unit Price</td></tr>
			<xsl:apply-templates select="LineItems/LineItem|tpai:LineItems/tpali:LineItem"/>
		</table>
	</xsl:template>

	<xsl:template match="LineItem|tpali:LineItem">
		<tr>
			<td><xsl:value-of select="@categoryId" /></td>
			<td><xsl:value-of select="@productId" /></td>
			<td align="right"><xsl:value-of select="@quantity" /></td>
			<td align="right"><xsl:value-of select="format-number(@unitPrice, '&#x24;#,##0.00')" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
