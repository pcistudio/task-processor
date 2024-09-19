-- contact_manager_prod.address definition

CREATE TABLE `address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `street` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `zip_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- contact_manager_prod.position_available definition

CREATE TABLE `position_available` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `department` varchar(100) DEFAULT NULL,
  `description` varchar(4048) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `number_of_positions` int(11) NOT NULL,
  `requirements` text DEFAULT NULL,
  `responsibilities` text DEFAULT NULL,
  `salary` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- contact_manager_prod.candidate definition

CREATE TABLE `candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attachments` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(4048) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `job_title` varchar(100) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `mark_for_interview` bit(1) NOT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `office_phone` varchar(20) DEFAULT NULL,
  `primary_address` text DEFAULT NULL,
  `secondary_address` text DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `position_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqs9u6u7sfbsnsrss9ydnr1t1p` (`position_id`),
  CONSTRAINT `FKqs9u6u7sfbsnsrss9ydnr1t1p` FOREIGN KEY (`position_id`) REFERENCES `position_available` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- contact_manager_prod.contact definition

CREATE TABLE `contact` (
  `contact_type` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(4048) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `job_title` varchar(100) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `office_phone` varchar(20) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `payment_frequency` tinyint(4) NOT NULL CHECK (`payment_frequency` between 0 and 2),
  `salary` decimal(38,2) NOT NULL,
  `started_date` date NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `primary_address_id` bigint(20) DEFAULT NULL,
  `secondary_address_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoqol80ehjlvw41mq3j1e206ja` (`primary_address_id`),
  UNIQUE KEY `UKshidl9u29kp92jtl72jmwu7dx` (`secondary_address_id`),
  CONSTRAINT `FK8rbe52crsqbs6e1bmvp4aysi6` FOREIGN KEY (`secondary_address_id`) REFERENCES `address` (`id`),
  CONSTRAINT `FKss8y3sx5bumjy6sj5x0fwkbmm` FOREIGN KEY (`primary_address_id`) REFERENCES `address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- contact_manager_prod.note definition

CREATE TABLE `note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contact_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9rvtkjx5gbkgfygg78w5nsmhy` (`contact_id`),
  CONSTRAINT `FK9rvtkjx5gbkgfygg78w5nsmhy` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- contact_manager_prod.attachment definition

CREATE TABLE `attachment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `file_name` varchar(100) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contact_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKscq8gdkq0hgkfs83r0x5txegp` (`contact_id`),
  CONSTRAINT `FKscq8gdkq0hgkfs83r0x5txegp` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

