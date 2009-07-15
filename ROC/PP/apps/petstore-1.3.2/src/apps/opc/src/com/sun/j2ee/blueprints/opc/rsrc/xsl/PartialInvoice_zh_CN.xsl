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
	  			<title>Java 宠物店：已发送定单<xsl:value-of select="//OrderId|//tpai:OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/Invoice|/tpai:Invoice">
		<b>感谢您</b>订购<b>Java Pet Store 1.3.2</b>的商品<br />
		定单<font color="red"><xsl:value-of select="//OrderId|//tpai:OrderId" /></font>的部分商品已经运出
		<p /> 运出的内容是：<br />
		<table border="1">
			<tr><td>类别</td><td>产品#</td><td>数量</td><td>单价</td></tr>
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
