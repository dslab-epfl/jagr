<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns:tpaso="http://blueprints.j2ee.sun.com/TPASupplierOrder"
	 xmlns:tpali="http://blueprints.j2ee.sun.com/TPALineItem"
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">

 	<xsl:output method="xml" indent="yes" encoding="UTF-8"
	        doctype-public="-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD SupplierOrder 1.1//EN"
       		doctype-system="/com/sun/j2ee/blueprints/supplierpo/rsrc/schemas/SupplierOrder.dtd"/>

	<xsl:strip-space elements="*" />


  	<xsl:template match="/">
		<SupplierOrder>
			<OrderId><xsl:value-of select="/tpaso:SupplierOrder/tpaso:OrderId" /></OrderId>
			<OrderDate><xsl:value-of select="/tpaso:SupplierOrder/tpaso:OrderDate" /></OrderDate>
			<xsl:apply-templates select=".//tpaso:ShippingAddress|.//tpali:LineItem"/>
		</SupplierOrder>
	</xsl:template>

	<xsl:template match="/tpaso:SupplierOrder/tpaso:ShippingAddress">
		<ShippingInfo>
			<ContactInfo>
				<FamilyName><xsl:value-of select="tpaso:FirstName" /></FamilyName>
				<GivenName><xsl:value-of select="tpaso:LastName" /></GivenName>
				<Address>
					<StreetName><xsl:value-of select="tpaso:Street" /></StreetName>
					<City><xsl:value-of select="tpaso:City" /></City>
					<State><xsl:value-of select="tpaso:State" /></State>
					<ZipCode><xsl:value-of select="tpaso:ZipCode" /></ZipCode>
					<Country><xsl:value-of select="tpaso:Country" /></Country>
				</Address>
				<Email><xsl:value-of select="tpaso:Email" /></Email>
				<Phone><xsl:value-of select="tpaso:Phone" /></Phone>
			</ContactInfo>
		</ShippingInfo>
	</xsl:template>

	<xsl:template match="/tpaso:SupplierOrder/tpaso:LineItems/tpali:LineItem">
		<LineItem>
			<CategoryId><xsl:value-of select="@categoryId" /></CategoryId>
			<ProductId><xsl:value-of select="@productId" /></ProductId>
			<ItemId><xsl:value-of select="@itemId" /></ItemId>
			<LineNum><xsl:value-of select="@lineNo" /></LineNum>
			<Quantity><xsl:value-of select="@quantity" /></Quantity>
			<UnitPrice><xsl:value-of select="@unitPrice" /></UnitPrice>
		</LineItem>
	</xsl:template>

</xsl:stylesheet>
