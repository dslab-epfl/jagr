CREATE TABLE C_customer
(
	c_id		integer not null,
	c_first		char(16),
	c_last		char(16),
	c_street1	char(20),	
	c_street2	char(20),	
	c_city		char(20),	
	c_state		char(2),	
	c_country	char(10),	
	c_zip		char(9),	
	c_phone		char(16),
	c_contact	char(25),	
	c_since		date,
	c_balance	numeric(9,2),
	c_credit	char(2),
	c_credit_limit	numeric(9,2),
	c_ytd_payment	numeric(9,2)
);

CREATE UNIQUE INDEX C_c_idx ON C_customer (c_id);

CREATE TABLE C_supplier
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
);

CREATE UNIQUE INDEX C_supp_idx ON C_supplier (supp_id);

CREATE TABLE C_site
(
	site_id			integer not null,
	site_name		char(16),
	site_street1	char(20),	
	site_street2	char(20),	
	site_city		char(20),	
	site_state		char(2),	
	site_country	char(10),	
	site_zip		char(9)
);

CREATE UNIQUE INDEX C_site_idx ON C_site (site_id);

CREATE TABLE C_parts
(
	p_id			char(15) not null,
	p_name			char(10),
	p_desc			varchar(100),
	p_rev			char(6),
	p_unit			char(10),
	p_cost			numeric(9,2),
	p_price			numeric(9,2),
	p_planner		integer,
	p_type			integer,
	p_ind			integer,
        p_lomark                integer,
        p_himark                integer
);

CREATE UNIQUE INDEX C_p_idx ON C_parts (p_id);

CREATE TABLE C_rule
(
	r_id		varchar(20) not null,
	r_text		long
);

CREATE UNIQUE INDEX C_r_idx on C_rule (r_id);

CREATE TABLE C_discount
(	
	d_id		varchar(64) not null,
	d_percent	integer
);

INSERT INTO C_discount (d_id, d_percent) VALUES
('PlatinumCustomer', 40);

INSERT INTO C_discount (d_id, d_percent) VALUES
('GoldCustomer', 30);

INSERT INTO C_discount (d_id, d_percent) VALUES
('SilverCustomer', 20);

INSERT INTO C_discount (d_id, d_percent) VALUES
('LongTimeCustomer', 10);

INSERT INTO C_discount (d_id, d_percent) VALUES
('FirstTimeCustomer', 15);

INSERT INTO C_discount (d_id, d_percent) VALUES
('JustACustomer', 0);

CREATE UNIQUE INDEX C_d_idx on C_discount (d_id)

