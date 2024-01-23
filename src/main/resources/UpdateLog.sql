DROP TABLE TimeTracer.Log;

CREATE TABLE `TimeTracer`.`Log` (
  `IDLog` int NOT NULL AUTO_INCREMENT,
  `Query` text NOT NULL,
  `PreviousState` text NOT NULL,
  `Timestamp` timestamp NOT NULL,
  PRIMARY KEY (`IDLog`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;