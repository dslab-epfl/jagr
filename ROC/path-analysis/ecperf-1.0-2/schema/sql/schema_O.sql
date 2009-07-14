DROP TABLE O_customer;
CREATE TABLE O_customer
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
	c_since		date
);

CREATE UNIQUE INDEX O_c_idx ON O_customer (c_id);

DROP TABLE O_orders;
CREATE TABLE O_orders
(
	o_id		integer not null,
	o_c_id		integer,
	o_ol_cnt	integer,
	o_discount	numeric(4,2),
	o_total		numeric(9,2),
	o_status	integer,
	o_entry_date	timestamp,
	o_ship_date	date
);

CREATE UNIQUE INDEX O_ords_idx ON O_orders (o_id);

CREATE INDEX O_oc_idx ON O_orders (o_c_id);

DROP TABLE O_orderline;
CREATE TABLE O_orderline
(
	ol_id		integer not null,
	ol_o_id		integer not null,
	ol_i_id		char(15),
	ol_qty		integer,
	ol_status	integer,
	ol_ship_date	date
);

CREATE UNIQUE INDEX O_ordl_idx ON O_orderline (ol_o_id, ol_id);

DROP TABLE O_item;
CREATE TABLE O_item
(
	i_id			char(15) not null,
	i_name			char(20),
	i_desc			varchar(100),
	i_price			numeric(9,2),
	i_discount		float
);

CREATE UNIQUE INDEX O_i_idx ON O_item (i_id);
