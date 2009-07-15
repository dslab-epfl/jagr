DROP TABLE M_parts
go
CREATE TABLE M_parts
(
	p_id			char(15) not null,
	p_name			char(10),
	p_desc			varchar(100),
	p_rev			char(6),
	p_planner		int,
	p_type			int,
	p_ind			int,
	p_lomark		int,
	p_himark		int
)
go

CREATE UNIQUE INDEX M_parts_idx ON M_parts (p_id)
go

DROP TABLE M_bom
go
CREATE TABLE M_bom
(
	b_comp_id		char(15) not null,
	b_assembly_id		char(15) not null,
	b_line_no		int,
	b_qty			int,
	b_ops			int,	
	b_eng_change		char(10),	
	b_ops_desc		varchar(100)
)
go

CREATE UNIQUE INDEX M_bom_idx ON M_bom (b_comp_id, b_assembly_id)
go

DROP TABLE M_workorder
go
CREATE TABLE M_workorder
(
	wo_number		int not null,
	wo_o_id			int,
	wo_ol_id		int,
	wo_status		int,
	wo_assembly_id	char(15),
	wo_orig_qty		int,
	wo_comp_qty		int,
	wo_due_date		datetime,
	wo_start_date	datetime
)
go

CREATE UNIQUE INDEX M_wo_idx ON M_workorder (wo_number)
go

DROP TABLE M_largeorder
go
CREATE TABLE M_largeorder
(
	lo_id			int not null,
	lo_o_id			int,
	lo_ol_id		int,
	lo_assembly_id	char(15),
	lo_qty			int,
	lo_due_date		datetime
)
go

DROP TABLE M_inventory
go
CREATE TABLE M_inventory
(
	in_p_id			char(15) not null,
	in_qty			int,
	in_ordered		int,
	in_location		char(20),	
	in_acc_code		int,
	in_act_date		datetime
)
go

CREATE UNIQUE INDEX M_inv_idx ON M_inventory (in_p_id)
go
