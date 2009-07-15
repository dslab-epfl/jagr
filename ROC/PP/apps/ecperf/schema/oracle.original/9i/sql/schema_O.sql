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
)
TABLESPACE O_cust_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX O_c_idx ON O_customer (c_id)
TABLESPACE O_cust_space
INITRANS 3
STORAGE (INITIAL 100K NEXT 100K PCTINCREASE 0);

DROP TABLE O_orders;
CREATE TABLE O_orders
(
	o_id		integer not null,
	o_c_id		integer,
	o_ol_cnt	integer,
	o_discount	numeric(4,2),
	o_total		numeric(9,2),
	o_status	integer,
	o_entry_date	date,
	o_ship_date	date
)
TABLESPACE O_ords_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX O_ords_idx ON O_orders (o_id)
TABLESPACE O_ords_space
INITRANS 3
STORAGE (INITIAL 1m NEXT 1m PCTINCREASE 0);

CREATE INDEX O_oc_idx ON O_orders (o_c_id)
TABLESPACE O_ords_space
INITRANS 3
STORAGE (INITIAL 1m NEXT 1m PCTINCREASE 0);

DROP TABLE O_orderline;
CREATE TABLE O_orderline
(
	ol_id		integer not null,
	ol_o_id		integer not null,
	ol_i_id		char(15),
	ol_qty		integer,
	ol_status	integer,
	ol_ship_date	date
)
TABLESPACE O_ordl_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX O_ordl_idx ON O_orderline (ol_o_id, ol_id)
TABLESPACE O_ordl_space
INITRANS 3
STORAGE (INITIAL 1m NEXT 1m PCTINCREASE 0);

DROP TABLE O_item;
CREATE TABLE O_item
(
	i_id			char(15) not null,
	i_name			char(20),
	i_desc			varchar(100),
	i_price			numeric(9,2),
	i_discount		numeric(6,4)
)
TABLESPACE O_item_space
STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0);

CREATE UNIQUE INDEX O_i_idx ON O_item (i_id)
INITRANS 3
TABLESPACE O_item_space
STORAGE (INITIAL 100K NEXT 100K PCTINCREASE 0);

