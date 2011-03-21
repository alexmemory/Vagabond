DROP SCHEMA IF EXISTS source CASCADE;
CREATE SCHEMA source;

CREATE TABLE source.person(
tid INT8 NOT NULL,
name TEXT,
address INT8,
PRIMARY KEY (name)
) WITH OIDS;

CREATE TABLE source.address(
tid INT8 NOT NULL,
id INT8,
city TEXT,
PRIMARY KEY (id)
) WITH OIDS;


DROP SCHEMA IF EXISTS target CASCADE;
CREATE SCHEMA target;

CREATE VIEW target.employee AS (
SELECT COALESCE(p.tid::text,'') || '|' || COALESCE(a.tid::text,'') AS tid, p.name AS name, a.city AS city
FROM
	source.person ANNOT('M1','M2') p LEFT OUTER JOIN
	source.address ANNOT('M2') a ON (p.address = a.id)
);

INSERT INTO source.person VALUES ('1','Peter','1');
INSERT INTO source.person VALUES ('2','Heinz','2');
INSERT INTO source.person VALUES ('3','Gert',NULL);
INSERT INTO source.person VALUES ('4','Gertrud','2');

INSERT INTO source.address VALUES ('1','1','Toronto');
INSERT INTO source.address VALUES ('2','2','Montreal');
INSERT INTO source.address VALUES ('3','3','Quebec');


ALTER TABLE source.person ADD FOREIGN KEY (address) REFERENCES source.address (id);
