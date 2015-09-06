# --- !Ups
TRUNCATE TABLE Country;

ALTER TABLE Country ADD COLUMN nativeName VARCHAR(128) NOT NULL;

# --- !Downs
