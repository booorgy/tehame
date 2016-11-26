CREATE SCHEMA `tehame` DEFAULT CHARACTER SET utf8;

USE `tehame`;

CREATE TABLE `user` (
  `uuid` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `passwort` varchar(255) NOT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `role` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Kaskadiere alles, außer beim Löschen von Rollen
CREATE TABLE `userrole` (
  `uuiduser` varchar(255) NOT NULL,
  `idrole` int(11) NOT NULL,
  PRIMARY KEY (`uuiduser`,`idrole`),
  KEY `role_idx` (`idrole`),
  CONSTRAINT `role` FOREIGN KEY (`idrole`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `user` FOREIGN KEY (`uuiduser`) REFERENCES `user` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Passwort: a
INSERT INTO `user` (`uuid`, `email`, `passwort`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79ed', 'admin@tehame.de', 'ypeBEsobvcr6wjGzmiPcTaeG7/gUfE5yuYB3ha/uSLs=');
INSERT INTO `role` (`id`, `name`) VALUES ('1', 'admin');
INSERT INTO `role` (`id`, `name`) VALUES ('2', 'user');
INSERT INTO `userrole` (`uuiduser`, `idrole`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79ed', '1');
