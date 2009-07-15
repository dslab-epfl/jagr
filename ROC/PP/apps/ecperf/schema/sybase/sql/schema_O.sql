DROP TABLE O_customer
go
CREATE TABLE O_customer
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
	c_since		datetime
)
go

CREATE UNIQUE INDEX O_c_idx ON O_customer (c_id)
go

DROP TABLE O_orders
go
CREATE TABLE O_orders
(
	o_id		int not null,
	o_c_id		int,
	o_ol_cnt	int,
	o_discount	decimal(4,2),
	o_total		decimal(9,2),
	o_status	int,
	o_entry_date	datetime,
	o_ship_date	datetime
)
go

CREATE UNIQUE INDEX O_ords_idx ON O_orders (o_id)
go

DROP TABLE O_orderline
go
CREATE TABLE O_orderline
(
	ol_id		int not null,
	ol_o_id		int not null,
	ol_i_id		char(15),
	ol_qty		int,
	ol_status	int,
	ol_ship_date	datetime
)
go

CREATE UNIQUE INDEX O_ordl_idx ON O_orderline (ol_id, ol_o_id )
go

DROP TABLE O_item
go
CREATE TABLE O_item
(
	i_id			char(15) not null,
	i_name			char(20),
	i_desc			varchar(100),
	i_price			numeric(9,2),
	i_discount		numeric(6,4)
)
go

CREATE UNIQUE INDEX O_i_idx ON O_item (i_id)
go
