DROP TABLE S_component
go
CREATE TABLE S_component
(
	comp_id			char(15) not null,
	comp_name		char(10),
	comp_desc		varchar(100),
	comp_unit		char(10),
	comp_cost		numeric(9,2),
	qty_on_order            int,
	qty_demanded            int,
	lead_time               int,
	container_size          int
)
go

CREATE UNIQUE INDEX S_comp_idx ON S_component (comp_id)
go

DROP TABLE S_supp_component
go
CREATE TABLE S_supp_component
(
	sc_p_id			char(15) not null,
	sc_supp_id		int not null,
	sc_price		numeric(9,2),
	sc_qty			int,
	sc_discount		float,
	sc_del_date		int
)
go

CREATE UNIQUE INDEX S_sc_idx ON S_supp_component (sc_p_id, sc_supp_id)
go

DROP TABLE S_supplier
go
CREATE TABLE S_supplier
(
	supp_id			int not null,
	supp_name		char(16),
	supp_street1	char(20),	
	supp_street2	char(20),	
	supp_city		char(20),	
	supp_state		char(2),	
	supp_country	char(10),	
	supp_zip		char(9),	
	supp_phone		char(16),
	supp_contact	char(25)
)
go

CREATE UNIQUE INDEX S_supp_idx ON S_supplier (supp_id)
go

DROP TABLE S_site
go
CREATE TABLE S_site
(
	site_id			int not null,
	site_name		char(16),
	site_street1	char(20),	
	site_street2	char(20),	
	site_city		char(20),	
	site_state		char(2),	
	site_country	char(10),	
	site_zip		char(9)
)
go

CREATE UNIQUE INDEX S_site_idx ON S_site (site_id)
go

DROP TABLE S_purchase_order
go
CREATE TABLE S_purchase_order
(
	po_number		int not null,
	po_supp_id		int,
	po_site_id		int
)
go

CREATE UNIQUE INDEX S_po_idx ON S_purchase_order (po_number)
go

DROP TABLE S_purchase_orderline
go
CREATE TABLE S_purchase_orderline
(
	pol_number		int not null,
	pol_po_id		int not null,
	pol_p_id		char(15),
	pol_qty			int,
	pol_balance		numeric(9,2),
	pol_deldate		datetime,
	pol_message		varchar(100)
)
go

CREATE UNIQUE INDEX S_pol_idx ON S_purchase_orderline (pol_po_id, pol_number)
go

