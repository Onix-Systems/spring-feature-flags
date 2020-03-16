--liquibase formatted sql
--changeset alexey.novikov:001

CREATE TABLE `properties` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `application` VARCHAR(255),
  `profile` VARCHAR(255),
  `label` VARCHAR(255),
  `key` VARCHAR(255),
  `value` VARCHAR(255),
  `updatedOn` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `properties` (`application`, `profile`, `label`, `key`, `value`) VALUES
('local', 'default', 'latest', 'feature-flags.is-new-papers-service-enabled', 'true');
