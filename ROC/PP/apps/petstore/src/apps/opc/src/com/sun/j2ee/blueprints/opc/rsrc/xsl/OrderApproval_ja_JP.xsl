<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java ペット屋さん: 注文確認</title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="//Order">
		<br /><b>Java ペット屋さん 1.3.1_02</b> にご注文いただきまして<b>ありがとうございます。</b><br />
		ご注文番号<font color="red"><xsl:value-of select="OrderId" /></font> を <xsl:apply-templates select="//OrderStatus" />
		<br />ご購入ありがとうございました。<br />
	</xsl:template>

	<xsl:template match="//OrderStatus[contains(., 'APPROVED')]">
	 	承りました!<br /> 発送の手配をいたします。
	</xsl:template>

	<xsl:template match="//OrderStatus[contains(., 'DENIED')]">
		受け取れません。<br />大変申し訳ございませんが、ご注文を受け付けることができませんでした。
	</xsl:template>

</xsl:stylesheet>
