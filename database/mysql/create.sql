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
INSERT INTO `user` (`uuid`, `email`, `passwort`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79ea', 'admin_a@tehame.de', 'ypeBEsobvcr6wjGzmiPcTaeG7/gUfE5yuYB3ha/uSLs=');
INSERT INTO `user` (`uuid`, `email`, `passwort`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79eb', 'admin_b@tehame.de', 'ypeBEsobvcr6wjGzmiPcTaeG7/gUfE5yuYB3ha/uSLs=');
INSERT INTO `role` (`id`, `name`) VALUES ('1', 'admin');
INSERT INTO `role` (`id`, `name`) VALUES ('2', 'user');
INSERT INTO `userrole` (`uuiduser`, `idrole`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79ea', '1');
INSERT INTO `userrole` (`uuiduser`, `idrole`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79eb', '1');

CREATE TABLE `relation` (
  `uuidusera` varchar(255) NOT NULL,
  `uuiduserb` varchar(255) NOT NULL,
  `type` int(11) NOT NULL COMMENT '0 = Privat, 1 = Familie, 2 = Freunde, 3 = Öffentlich',
  PRIMARY KEY (`uuidusera`,`uuiduserb`,`type`),
  KEY `userb_idx` (`uuiduserb`),
  CONSTRAINT `usera` FOREIGN KEY (`uuidusera`) REFERENCES `user` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userb` FOREIGN KEY (`uuiduserb`) REFERENCES `user` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Relationen werden bidirektional doppelt gespeichert: A -> B und B -> A';

-- Admin_A und Admin_B sind Freunde
INSERT INTO `tehame`.`relation` (`uuidusera`, `uuiduserb`, `type`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79ea', 'e26fc393-9219-44b5-b681-f08f054a79eb', '1');
INSERT INTO `tehame`.`relation` (`uuidusera`, `uuiduserb`, `type`) VALUES ('e26fc393-9219-44b5-b681-f08f054a79eb', 'e26fc393-9219-44b5-b681-f08f054a79ea', '1');
