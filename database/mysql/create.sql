CREATE SCHEMA `tehame` DEFAULT CHARACTER SET utf8;

USE `tehame`;

CREATE TABLE `user` (
  `uuid` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `passwort` varchar(255) NOT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
