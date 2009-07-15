<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java ペット屋さん: 注文完了 <xsl:value-of select="//OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/PurchaseOrder">
                <b>Java ペット屋さん 1.3.1_02</b> にご注文いただきまして<b>ありがとうございました。</b><br />
		ご注文番号<font color="red"><xsl:value-of select="//OrderId" /></font>が完了しました。
		<p />ご注文の内容は以下のようになっております:<br />
		<table border="1">
			<tr><td>カテゴリ</td><td>商品#</td><td>品数</td><td>単価</td></tr>
			<xsl:apply-templates select="LineItem" />
		</table>
	</xsl:template>

	<xsl:template match="LineItem">
		<tr>
			<td><xsl:value-of select="CategoryId" /></td>
			<td><xsl:value-of select="ProductId" /></td>
			<td align="right"><xsl:value-of select="Quantity" /></td>
			<td align="right"><xsl:value-of select="format-number(UnitPrice, '￥#,##0')" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
