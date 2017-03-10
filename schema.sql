#DROP DATABASE IF EXISTS soccerElo;
CREATE DATABASE soccerElo DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

#CREATE USER 'usr'@'localhost' IDENTIFIED BY 'psswrd';
#GRANT ALL ON soccerElo.* TO 'usr'@'localhost' IDENTIFIED BY 'psswrd';

USE soccerElo;

CREATE TABLE Club (
    name VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    PRIMARY KEY(name, country)
);

CREATE TABLE ClubEloEntry (
    entryId INT AUTO_INCREMENT,
    rank VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    level INTEGER NOT NULL,
    elo DOUBLE NOT NULL,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    PRIMARY KEY (entryId),
    FOREIGN KEY (name, country) REFERENCES Club (name, country)
);
