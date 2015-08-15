# --- !Ups
CREATE TABLE Country (
    id     SMALLINT PRIMARY KEY,
    name   VARCHAR(128) NOT NULL,
    trName VARCHAR(128) NOT NULL
) DEFAULT CHARSET=utf8;

CREATE TABLE City (
    id        SMALLINT PRIMARY KEY,
    countryId SMALLINT NOT NULL,
    name      VARCHAR(128) NOT NULL
) DEFAULT CHARSET=utf8;

CREATE TABLE District (
    id     SMALLINT PRIMARY KEY,
    cityId SMALLINT NOT NULL,
    name   VARCHAR(128) NOT NULL
) DEFAULT CHARSET=utf8;

CREATE TABLE PrayerTimes (
    countryId  SMALLINT NOT NULL,
    cityId     SMALLINT NOT NULL,
    districtId SMALLINT,
    dayDate    BIGINT NOT NULL,
    fajr       BIGINT NOT NULL,
    shuruq     BIGINT NOT NULL,
    dhuhr      BIGINT NOT NULL,
    asr        BIGINT NOT NULL,
    maghrib    BIGINT NOT NULL,
    isha       BIGINT NOT NULL,
    qibla      BIGINT NOT NULL,
    UNIQUE (countryId, cityId, districtId, dayDate)
) DEFAULT CHARSET=utf8;

# --- !Downs
