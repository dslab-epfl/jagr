<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java 宠物店：定单<xsl:value-of select="//OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="//Order">
                <b>感谢您</b>订购<b>Java Pet Store 1.3.2</b>的商品<br />
		您的定单<font color="red"><xsl:value-of select="OrderId" /></font>已经<xsl:apply-templates select="//OrderStatus" />
		<br />感谢光临本店。<br />
	</xsl:template>

	<xsl:template match="OrderStatus[contains(., 'APPROVED')]">
	 	被批准！<br />我们正在完成您的定单
	</xsl:template>

	<xsl:template match="OrderStatus[contains(., 'DENIED')]">
		不幸被拒绝。<br />抱歉不能接受您的定单
	</xsl:template>

</xsl:stylesheet>

