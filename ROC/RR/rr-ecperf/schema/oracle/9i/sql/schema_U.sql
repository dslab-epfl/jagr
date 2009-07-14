DROP TABLE U_sequences;
CREATE TABLE U_sequences
(
	s_id		varchar(50) not null,
	s_nextnum	integer,
	s_blocksize	integer
)
TABLESPACE U_space
STORAGE (INITIAL 1K NEXT 1K PCTINCREASE 0);

CREATE UNIQUE INDEX U_s_idx ON U_sequences (s_id)
TABLESPACE U_space
INITRANS 3
STORAGE (INITIAL 1K NEXT 1K PCTINCREASE 0);
