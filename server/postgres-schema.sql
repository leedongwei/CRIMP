CREATE TABLE crimp_data (
	c_id VARCHAR(5) PRIMARY KEY,
	c_name TEXT NOT NULL,
	c_category VARCHAR(3) NOT NULL,
	c_qualified BOOLEAN,

	q01_judge TEXT,
	q01_raw TEXT,
	q01_top SMALLINT,
	q01_bonus SMALLINT,

	q02_judge TEXT,
	q02_raw TEXT,
	q02_top SMALLINT,
	q02_bonus SMALLINT,

	q03_judge TEXT,
	q03_raw TEXT,
	q03_top SMALLINT,
	q03_bonus SMALLINT,

	q04_judge TEXT,
	q04_raw TEXT,
	q04_top SMALLINT,
	q04_bonus SMALLINT,

	q05_judge TEXT,
	q05_raw TEXT,
	q05_top SMALLINT,
	q05_bonus SMALLINT,

	q06_judge TEXT,
	q06_raw TEXT,
	q06_top SMALLINT,
	q06_bonus SMALLINT,

	f01_judge TEXT,
	f01_raw TEXT,
	f01_top SMALLINT,
	f01_bonus SMALLINT,

	f02_judge TEXT,
	f02_raw TEXT,
	f02_top SMALLINT,
	f02_bonus SMALLINT,

	f03_judge TEXT,
	f03_raw TEXT,
	f03_top SMALLINT,
	f03_bonus SMALLINT,

	f04_judge TEXT,
	f04_raw TEXT,
	f04_top SMALLINT,
	f04_bonus SMALLINT
);

INSERT INTO crimp_data (c_id, c_name, c_category, c_qualified) VALUES
('NM001', 'Andy', 'NM', '0'),
('NM002', 'Ben', 'NM', '0'),
('NM003', 'Charlie', 'NM', '0'),
('NM004', 'David', 'NM', '0'),
('NM005', 'Ethan', 'NM', '0'),
('NM006', 'Florine', 'NM', '0'),
('NM007', 'Glen', 'NM', '0'),
('NW001', 'Anna', 'NW', '0'),
('NW002', 'Brenda', 'NW', '0'),
('NW003', 'Cathy', 'NW', '0'),
('NW004', 'Dorothy', 'NW', '0'),
('NW005', 'Erica', 'NW', '0'),
('NW006', 'Felicia', 'NW', '0'),
('NW007', 'Ginny', 'NW', '0');