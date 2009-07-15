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
	  			<title>Java ペット屋さん: 発送</title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/Invoice|/tpai:Invoice">
		<b>Java ペット屋さん 1.3.2</b> にご注文いただきまして<b>ありがとうございました。</b><br />
		このメールはご注文番号<font color="red"><xsl:value-of select="//OrderId|//tpai:OrderId" /></font> のうち下記の発送に関するお知らせです。<br /><br />
		<table border="1">
			<tr><td>カテゴリ</td><td>商品#</td><td>匹数</td><td>単価</td></tr>
		<xsl:apply-templates select="LineItems/LineItem|tpai:LineItems/tpali:LineItem"/>
		</table>
	</xsl:template>

	<xsl:template match="LineItem|tpali:LineItem">
		<tr>
			<td><xsl:value-of select="@categoryId" /></td>
			<td><xsl:value-of select="@productId" /></td>
			<td align="right"><xsl:value-of select="@quantity" /></td>
			<td align="right"><xsl:value-of select="format-number(@unitPrice, '￥#,##0')" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
