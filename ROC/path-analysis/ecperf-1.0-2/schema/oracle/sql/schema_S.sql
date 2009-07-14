
DROP TABLE S_component;
CREATE TABLE S_component
(
	comp_id			char(15) not null,
	comp_name		char(10),
	comp_desc		varchar(100),
	comp_unit		char(10),
	comp_cost		numeric(9,2),
        qty_on_order            integer,
        qty_demanded            integer,
        lead_time               integer,
        container_size          integer
)
TABLESPACE S_comp_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX S_comp_idx ON S_component (comp_id)
TABLESPACE S_comp_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

DROP TABLE S_supp_component;
CREATE TABLE S_supp_component
(
	sc_p_id			char(15) not null,
	sc_supp_id		integer not null,
	sc_price		numeric(9,2),
	sc_qty			integer,
	sc_discount		float,
	sc_del_date		integer
)
TABLESPACE S_sc_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX S_sc_idx ON S_supp_component (sc_p_id, sc_supp_id)
TABLESPACE S_sc_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

DROP TABLE S_supplier;
CREATE TABLE S_supplier
(
	supp_id			integer not null,
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
TABLESPACE S_supp_space
STORAGE (INITIAL 100K NEXT 100K PCTINCREASE 0);

CREATE UNIQUE INDEX S_supp_idx ON S_supplier (supp_id)
TABLESPACE S_supp_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

DROP TABLE S_site;
CREATE TABLE S_site
(
	site_id			integer not null,
	site_name		char(16),
	site_street1	char(20),	
	site_street2	char(20),	
	site_city		char(20),	
	site_state		char(2),	
	site_country	char(10),	
	site_zip		char(9)
)
TABLESPACE S_site_space
STORAGE (INITIAL 100K NEXT 100K PCTINCREASE 0);

CREATE UNIQUE INDEX S_site_idx ON S_site (site_id)
TABLESPACE S_site_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

DROP TABLE S_purchase_order;
CREATE TABLE S_purchase_order
(
	po_number		integer not null,
	po_supp_id		integer,
	po_site_id		integer
)
TABLESPACE S_po_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX S_po_idx ON S_purchase_order (po_number)
TABLESPACE S_po_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

DROP TABLE S_purchase_orderline;
CREATE TABLE S_purchase_orderline
(
	pol_number		integer not null,
	pol_po_id		integer not null,
	pol_p_id		char(15),
	pol_qty			integer,
	pol_balance		numeric(9,2),
	pol_deldate		date,
	pol_message		varchar(100)
)
TABLESPACE S_po_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX S_pol_idx ON S_purchase_orderline (pol_po_id, pol_number)
TABLESPACE S_po_space
INITRANS 3
STORAGE (INITIAL 10K NEXT 10K PCTINCREASE 0);

