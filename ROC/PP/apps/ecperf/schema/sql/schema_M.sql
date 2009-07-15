DROP TABLE M_parts;
CREATE TABLE M_parts
(
	p_id			char(15) not null,
	p_name			char(10),
	p_desc			varchar(100),
	p_rev			char(6),
	p_planner		integer,
	p_type			integer,
	p_ind			integer,
	p_lomark		integer,
	p_himark		integer
);

CREATE UNIQUE INDEX M_parts_idx ON M_parts (p_id);

DROP TABLE M_bom;
CREATE TABLE M_bom
(
	b_comp_id		char(15) not null,
	b_assembly_id		char(15) not null,
	b_line_no		integer,
	b_qty			integer,
	b_ops			integer,	
	b_eng_change		char(10),	
	b_ops_desc		varchar(100)
);

CREATE UNIQUE INDEX M_bom_idx ON M_bom (b_assembly_id, b_comp_id, b_line_no);

DROP TABLE M_workorder;
CREATE TABLE M_workorder
(
	wo_number		integer not null,
	wo_o_id			integer,
	wo_ol_id		integer,
	wo_status		integer,
	wo_assembly_id	char(15),
	wo_orig_qty		integer,
	wo_comp_qty		integer,
	wo_due_date		date,
	wo_start_date		timestamp
);

CREATE UNIQUE INDEX M_wo_idx ON M_workorder (wo_number);

DROP TABLE M_largeorder;
CREATE TABLE M_largeorder
(
	lo_id			integer not null,
	lo_o_id			integer,
	lo_ol_id		integer,
	lo_assembly_id	char(15),
	lo_qty			integer,
	lo_due_date		date
);

DROP TABLE M_inventory;
CREATE TABLE M_inventory
(
	in_p_id			char(15) not null,
	in_qty			integer,
	in_ordered		integer,
	in_location		char(20),	
	in_acc_code		integer,
	in_act_date		date
);

CREATE UNIQUE INDEX M_inv_idx ON M_inventory (in_p_id);
