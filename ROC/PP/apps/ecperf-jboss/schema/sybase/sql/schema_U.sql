DROP TABLE U_sequences
go
CREATE TABLE U_sequences
(
	s_id		varchar(50) not null,
	s_nextnum	int,
	s_blocksize	int
)
go

CREATE UNIQUE INDEX U_s_idx ON U_sequences (s_id)
go
