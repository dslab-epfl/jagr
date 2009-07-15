<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java 宠物店：订单完成<xsl:value-of select="//OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/PurchaseOrder">
	      <b>感谢您</b>订购<b>Java Pet Store 1.3.2</b>的商品<br />
              <p />您的定单<font color="red"><xsl:value-of select="//OrderId" /></font>
        已经完全运出，现在您的定单已经完成。

		<p /> 您的完整定单是：<br />
		<table border="1">
			<tr><td>类别</td><td>产品#</td><td>数量</td><td>单价</td></tr>
			<xsl:apply-templates select="LineItem" />
		</table>
	</xsl:template>

	<xsl:template match="LineItem">
		<tr>
			<td><xsl:value-of select="CategoryId" /></td>
			<td><xsl:value-of select="ProductId" /></td>
			<td align="right"><xsl:value-of select="Quantity" /></td>
			<td align="right"><xsl:value-of select="format-number(UnitPrice, '&#x24;#,##0.00')" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
