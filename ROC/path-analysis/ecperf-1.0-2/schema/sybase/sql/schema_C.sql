DROP TABLE C_customer
go
CREATE TABLE C_customer
(
	c_id		int not null,
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
	c_since		datetime,
	c_balance	numeric(9,2),
	c_credit	char(2),
	c_credit_limit	numeric(9,2),
	c_ytd_payment	numeric(9,2)
)
go

CREATE UNIQUE INDEX C_c_idx ON C_customer (c_id)
go

DROP TABLE C_supplier
go
CREATE TABLE C_supplier
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

CREATE UNIQUE INDEX C_supp_idx ON C_supplier (supp_id)
go

DROP TABLE C_site
go
CREATE TABLE C_site
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

CREATE UNIQUE INDEX C_site_idx ON C_site (site_id)
go

DROP TABLE C_parts
go
CREATE TABLE C_parts
(
	p_id			char(15) not null,
	p_name			char(10),
	p_desc			varchar(100),
	p_rev			char(6),
	p_unit			char(10),
	p_cost			numeric(9,2),
	p_price			numeric(9,2),
	p_planner		int,
	p_type			int,
	p_ind			int,
        p_lomark                int,
        p_himark                int
)
go

CREATE UNIQUE INDEX C_p_idx ON C_parts (p_id)
go

DROP TABLE C_rule
go
CREATE TABLE C_rule
(
	r_id		varchar(20) not null,
	r_text		text
)
go

CREATE UNIQUE INDEX C_r_idx on C_rule (r_id)
go

DROP TABLE C_discount
go
CREATE TABLE C_discount
(	
	d_id		varchar(64) not null,
	d_percent	int
)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('PlatinumCustomer', 40)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('GoldCustomer', 30)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('SilverCustomer', 20)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('LongTimeCustomer', 10)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('FirstTimeCustomer', 15)
go

INSERT INTO C_discount (d_id, d_percent) VALUES
('JustACustomer', 0)
go

CREATE UNIQUE INDEX C_d_idx on C_discount (d_id)
go
