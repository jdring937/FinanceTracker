-- phpMyAdmin SQL Dump
-- version 4.5.1
-- http://www.phpmyadmin.net
--
-- Host: 
-- Generation Time: Apr 25, 2016 at 12:03 AM
-- Server version: 10.1.19-MariaDB
-- PHP Version: 5.6.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `finances`
--

-- --------------------------------------------------------

--
-- Table structure for table `monthly_finances`
--

CREATE TABLE `monthly_finances` (
  `Date` date NOT NULL,
  `Amount` double NOT NULL,
  `Location` varchar(256) NOT NULL,
  `CategoryID` int(11) NOT NULL,
  `TransactionID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `monthly_finances`
--

INSERT INTO `monthly_finances` (`Date`, `Amount`, `Location`, `CategoryID`, `TransactionID`) VALUES
('2017-04-23', 25.23, 'Kroger', 0, 1),
('2017-04-24', 75.87, 'Electric', 5, 2),
('2017-04-16', 36.12, 'Home Depot', 2, 3),
('2017-04-16', 30, 'Speedway', 12, 4),
('2017-04-21', 40, 'Club', 2, 5),
('2017-04-22', 20, 'Jungle Jim''s', 0, 6),
('2017-04-24', 5.55, 'Bank', 4, 7),
('2017-03-31', 750, 'Apartment', 5, 8),
('2017-04-09', 36.74, 'Kroger', 0, 9),
('2017-04-02', 15, 'Bookstore', 11, 10);

-- --------------------------------------------------------

--
-- Table structure for table `tcategory`
--

CREATE TABLE `tcategory` (
  `CategoryID` int(11) NOT NULL,
  `Category` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tcategory`
--

INSERT INTO `tcategory` (`CategoryID`, `Category`) VALUES
(0, 'Groceries'),
(1, 'Restaurants & Dining'),
(2, 'Shopping & Entertainment'),
(3, 'Cash, Checks,& Misc.'),
(4, 'Finance'),
(5, 'Home & Utilities'),
(6, 'Personal & Family'),
(7, 'Health'),
(8, 'Travel'),
(9, 'Giving & Charity'),
(10, 'Business Expenses'),
(11, 'Education'),
(12, 'Transportation'),
(13, 'Uncategorized');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `monthly_finances`
--
ALTER TABLE `monthly_finances`
  ADD PRIMARY KEY (`TransactionID`);

--
-- Indexes for table `tcategory`
--
ALTER TABLE `tcategory`
  ADD PRIMARY KEY (`CategoryID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `monthly_finances`
--
ALTER TABLE `monthly_finances`
  MODIFY `TransactionID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
