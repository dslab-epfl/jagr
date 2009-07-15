<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

    	<xsl:template match="/">
      		<html> 
			<head> 
	  			<title>Java Pet Store: Order <xsl:value-of select="//OrderId" /></title> 
			</head> 
			<body bgcolor="#ffffff">
				<basefont color="black" size="7">
					<xsl:apply-templates />
				</basefont>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="//Order">
		<br /><b>Thank you</b> for placing an order with <b>Java Pet Store 1.3.1_02</b><br />
		Your order <font color="red"><xsl:value-of select="OrderId" /></font> has been <xsl:apply-templates select="//OrderStatus" />
		<br />Thanks for shopping with us.<br />
	</xsl:template>

	<xsl:template match="OrderStatus[contains(., 'APPROVED')]">
	 	approved!<br />We will now fulfill your order.
	</xsl:template>

	<xsl:template match="OrderStatus[contains(., 'DENIED')]">
		denied unfortunately.<br />Sorry we could not place your order.
	</xsl:template>

</xsl:stylesheet>

