CREATE DATABASE `TimeTracer`;

CREATE TABLE `TimeTracer`.`Times` (
  `IDTime` int NOT NULL AUTO_INCREMENT,
  `EmployeeID` int NOT NULL,
  `ProjectID` int NOT NULL,
  `PunchedTime` datetime NOT NULL,
  `In` tinyint(1) NOT NULL,
  `InsertUser` varchar(100) NOT NULL,
  `InsertTimestamp` timestamp NOT NULL,
  `ModifyUser` varchar(100) DEFAULT NULL,
  `ModifyTimestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`IDTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TimeTracer`.`Employees` (
  `IDEmployee` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(100) NOT NULL,
  `Surname` varchar(100) NOT NULL,
  `BirthDate` date NOT NULL,
  `BirthPlace` varchar(100) NOT NULL,
  `SocialSecurityNum` varchar(100) NOT NULL,
  `Residence` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`IDEmployee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TimeTracer`.`Users` (
  `UserName` varchar(100) NOT NULL,
  `Password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `RoleID` int NOT NULL,
  `EmployeeID` int DEFAULT NULL,
  PRIMARY KEY (`UserName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TimeTracer`.`Projects` (
  `IDProject` int NOT NULL AUTO_INCREMENT,
  `ProjectName` varchar(100) NOT NULL,
  PRIMARY KEY (`IDProject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TimeTracer`.`Roles` (
  `IDRole` int NOT NULL AUTO_INCREMENT,
  `Role` varchar(100) DEFAULT NULL,
  `IsEmployee` tinyint(1) NOT NULL DEFAULT '0',
  `ProjectID` int DEFAULT NULL,
  PRIMARY KEY (`IDRole`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TimeTracer`.`Log` (
  `IDLog` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `OpID` int NOT NULL,
  PRIMARY KEY (`IDLog`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `TimeTracer`.`Roles` (`IDRole`, `Role`, `IsEmployee`) VALUES (1, 'Admin', 0);
INSERT INTO `TimeTracer`.`Roles` (`IDRole`, `Role`, `IsEmployee`) VALUES (2, 'Employee', 1);
INSERT INTO `TimeTracer`.`Users` (`UserName`, `RoleID`) VALUES ('Admin', 1);
