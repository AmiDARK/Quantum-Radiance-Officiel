-- phpMyAdmin SQL Dump
-- version 4.9.0.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le :  sam. 23 mai 2020 à 21:36
-- Version du serveur :  10.4.6-MariaDB
-- Version de PHP :  7.3.9

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `qradiance`
--

-- --------------------------------------------------------

--
-- Structure de la table `devices`
--

CREATE TABLE `devices` (
  `deviceName` text NOT NULL,
  `deviceOS` text NOT NULL,
  `deviceOSVersion` text NOT NULL,
  `deviceUDID` text NOT NULL,
  `deviceManufacturer` text NOT NULL,
  `deviceModel` text NOT NULL,
  `deviceStatus` int(11) NOT NULL,
  `expirationDate` datetime NOT NULL,
  `onServerName` text NOT NULL,
  `reservedByUserID` text NOT NULL,
  `deviceID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Déchargement des données de la table `devices`
--

INSERT INTO `devices` (`deviceName`, `deviceOS`, `deviceOSVersion`, `deviceUDID`, `deviceManufacturer`, `deviceModel`, `deviceStatus`, `expirationDate`, `onServerName`, `reservedByUserID`, `deviceID`) VALUES
('Huawei P8 Lite 2017', 'android', '8.0', 'UpdateUDID', 'Huawei', 'PRA-LX1', 0, '2019-10-10 23:37:50', 'local', '', 1),
('Huawei P20', 'android', '9.0', 'UpdateUDID', 'Huawei', 'EML-L29', 0, '2019-09-28 23:59:59', 'local', '', 2);

-- --------------------------------------------------------

--
-- Structure de la table `servers`
--

CREATE TABLE `servers` (
  `serverName` text NOT NULL,
  `serverIP` text NOT NULL,
  `serverPort` int(11) NOT NULL,
  `serverState` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Déchargement des données de la table `servers`
--

INSERT INTO `servers` (`serverName`, `serverIP`, `serverPort`, `serverState`) VALUES
('local', 'localhost', 17000, 0);

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `userName` mediumtext NOT NULL,
  `userKey` mediumtext NOT NULL,
  `expirationDate` datetime NOT NULL,
  `isConnected` tinyint(1) NOT NULL,
  `userID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`userName`, `userKey`, `expirationDate`, `isConnected`, `userID`) VALUES
('Ernest Schrodinger (Expired Account)', '9F7458650340F0A0DB0C758', '2019-09-24 23:59:59', 0, 1),
('Albert Einstein (Active Account)', 'F8546934ED89AB89ED09435', '2020-06-25 23:59:59', 1, 2);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `devices`
--
ALTER TABLE `devices`
  ADD UNIQUE KEY `deviceID` (`deviceID`) USING BTREE;

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD UNIQUE KEY `userID` (`userID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
