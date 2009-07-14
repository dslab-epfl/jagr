DROP TABLE if exists U_sequences;
CREATE TABLE U_sequences
(
	s_id		varchar(50) not null,
	s_nextnum	integer,
	s_blocksize	integer
);

CREATE UNIQUE INDEX U_s_idx ON U_sequences (s_id);
