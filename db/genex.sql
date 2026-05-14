-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 14, 2026 at 11:52 AM
-- Server version: 8.4.7
-- PHP Version: 8.3.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `genex`
--

-- --------------------------------------------------------

--
-- Table structure for table `budget`
--

DROP TABLE IF EXISTS `budget`;
CREATE TABLE IF NOT EXISTS `budget` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sponsor_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fiscal_year` year NOT NULL,
  `allocated_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `spent_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `fk_budget_sponsor` (`sponsor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `centers`
--

DROP TABLE IF EXISTS `centers`;
CREATE TABLE IF NOT EXISTS `centers` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `map_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_centers_email` (`contact_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `centers`
--

INSERT INTO `centers` (`id`, `name`, `address`, `city`, `contact_email`, `map_url`, `created_at`, `latitude`, `longitude`) VALUES
('center-bordeaux-001', 'Bordeaux Gaming Hub', '12 Cours de l\'Intendance', 'Bordeaux', 'info@bordeauxgaming.fr', 'https://maps.google.com/?q=44.8378,-0.5792', '2026-05-09 13:10:52', 44.8378, -0.5792),
('center-lille-001', 'Lille Gaming Center', '5 Place de la République', 'Lille', 'contact@lillegaming.fr', 'https://maps.google.com/?q=50.6292,3.0573', '2026-05-09 13:10:52', 50.6292, 3.0573),
('center-lyon-001', 'Lyon Esports Hub', '25 Avenue Jean Jaurès', 'Lyon', 'info@lyonesports.fr', 'https://maps.google.com/?q=45.7640,4.8357', '2026-05-09 13:10:52', 45.764, 4.8357),
('center-marseille-001', 'Marseille Gaming Center', '10 Boulevard de la Libération', 'Marseille', 'contact@marseillegaming.fr', 'https://maps.google.com/?q=43.2965,5.3698', '2026-05-09 13:10:52', 43.2965, 5.3698),
('center-nantes-001', 'Nantes Esports Arena', '18 Rue Crébillon', 'Nantes', 'hello@nantesesports.fr', 'https://maps.google.com/?q=47.2184,-1.5536', '2026-05-09 13:10:53', 47.2184, -1.5536),
('center-nice-001', 'Nice Esports Center', '30 Promenade des Anglais', 'Nice', 'contact@niceesports.fr', 'https://maps.google.com/?q=43.7102,7.2620', '2026-05-09 13:10:52', 43.7102, 7.262),
('center-paris-001', 'Paris Gaming Arena', '15 Rue de la République', 'Paris', 'contact@parisgaming.fr', 'https://maps.google.com/?q=48.8566,2.3522', '2026-05-09 13:10:52', 48.8566, 2.3522),
('center-toulouse-001', 'Toulouse Gaming Arena', '8 Rue des Arts', 'Toulouse', 'hello@toulousegaming.fr', 'https://maps.google.com/?q=43.6047,1.4442', '2026-05-09 13:10:52', 43.6047, 1.4442);

-- --------------------------------------------------------

--
-- Table structure for table `forums`
--

DROP TABLE IF EXISTS `forums`;
CREATE TABLE IF NOT EXISTS `forums` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_by` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'General',
  `topic_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'open',
  `moderation_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'visible',
  `is_pinned` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_forums_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `forums`
--

INSERT INTO `forums` (`id`, `title`, `description`, `created_by`, `created_at`, `category`, `topic_status`, `moderation_status`, `is_pinned`) VALUES
('f956c447-4f06-11f1-a50c-88d7f63043f1', 'mounir', 'MOUNIRAT DALLEJIET BARCHA BARCHET', 'user', '2026-05-13 20:04:40', 'General', 'open', 'visible', 0);

-- --------------------------------------------------------

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
CREATE TABLE IF NOT EXISTS `games` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `genre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `platform` enum('PC','Console','Mobile','Cross-Platform') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PC',
  `team_mode` enum('solo','team') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'team',
  `max_players_per_match` int NOT NULL,
  `icon_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_games_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `games`
--

INSERT INTO `games` (`id`, `name`, `genre`, `platform`, `team_mode`, `max_players_per_match`, `icon_url`) VALUES
('cb337297-4c04-11f1-acf9-88d7f63043f1', 'League Of Legends', 'MOBA', 'PC', 'team', 10, 'file:/C:/Users/moham/Downloads/Gemini_Generated_Image_6zvhua6zvhua6zvh-removebg-preview.png'),
('ee087b9f-4c60-11f1-acf9-88d7f63043f1', 'Valorant', 'FPS', 'PC', 'team', 10, 'file:/C:/Users/moham/Downloads/Gemini_Generated_Image_6zvhua6zvhua6zvh-removebg-preview.png');

-- --------------------------------------------------------

--
-- Table structure for table `marketplace_cart`
--

DROP TABLE IF EXISTS `marketplace_cart`;
CREATE TABLE IF NOT EXISTS `marketplace_cart` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `added_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cart` (`user_id`,`item_id`),
  KEY `idx_cart_user` (`user_id`),
  KEY `fk_cart_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `marketplace_items`
--

DROP TABLE IF EXISTS `marketplace_items`;
CREATE TABLE IF NOT EXISTS `marketplace_items` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `seller_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `seller_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `seller_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `product_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int DEFAULT NULL,
  `has_delivery` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE',
  `image_paths` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `listed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `item_condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'NEUF',
  PRIMARY KEY (`id`),
  KEY `idx_mi_seller` (`seller_id`),
  KEY `idx_mi_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `marketplace_orders`
--

DROP TABLE IF EXISTS `marketplace_orders`;
CREATE TABLE IF NOT EXISTS `marketplace_orders` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyer_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `buyer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity_bought` int NOT NULL DEFAULT '1',
  `payment_method` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `payment_ref` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ordered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `idx_mo_item` (`item_id`),
  KEY `idx_mo_buyer` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `marketplace_reviews`
--

DROP TABLE IF EXISTS `marketplace_reviews`;
CREATE TABLE IF NOT EXISTS `marketplace_reviews` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reviewer_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `seller_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `rating` int NOT NULL,
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rev_order` (`order_id`),
  KEY `idx_rev_item` (`item_id`),
  KEY `idx_rev_seller` (`seller_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `marketplace_wishlist`
--

DROP TABLE IF EXISTS `marketplace_wishlist`;
CREATE TABLE IF NOT EXISTS `marketplace_wishlist` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `added_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wishlist` (`user_id`,`item_id`),
  KEY `idx_wish_user` (`user_id`),
  KEY `fk_wish_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `players`
--

DROP TABLE IF EXISTS `players`;
CREATE TABLE IF NOT EXISTS `players` (
  `user_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cin` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` text COLLATE utf8mb4_unicode_ci,
  `tactical_xp` int DEFAULT NULL,
  `total_attempts` int DEFAULT NULL,
  `correct_answers` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_players_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `players`
--

INSERT INTO `players` (`user_id`, `first_name`, `last_name`, `nickname`, `cin`, `date_of_birth`, `nationality`, `city`, `avatar_url`, `tactical_xp`, `total_attempts`, `correct_answers`) VALUES
('1c4c4771-4f27-11f1-a50c-88d7f63043f1', 'ahmed', 'ben ahmed', '7amouda', '65365465', '2011-05-05', 'ALGERIE', 'alger', 'https://api.dicebear.com/7.x/micah/png?seed=GENEX_USER_148440&backgroundColor=000000', 0, 0, 0),
('90d1a9a1-4f02-11f1-a50c-88d7f63043f1', 'Player', 'PlayerNamed', 'SuperPlayer', '12341211', '2012-05-11', 'Playirien', 'Playercity', 'https://api.dicebear.com/7.x/bottts/png?seed=GENEX_USER_6539&backgroundColor=5c7cfa', 50, 1, 1),
('9f2f2ed3-4f75-11f1-a50c-88d7f63043f1', 'abc', 'abc', 'abc', '12355669', '2011-05-06', 'abc', 'abc', NULL, 0, 0, 0),
('dffd610a-4f2e-11f1-a50c-88d7f63043f1', 'Aziz', 'Jarraya', 'LavanderBeast', '14533305', '2004-11-29', 'Tunisien', 'Ariana', 'file:/C:/Users/moham/Downloads/background.jpg', 0, 0, 0),
('ec0eae27-4f26-11f1-a50c-88d7f63043f1', 'mounir', 'dalleji', 'LOSDALLEJINOS', '67676767', '2010-05-12', 'tunisian', 'JANDOUBA', 'https://api.dicebear.com/7.x/notionists/png?seed=GENEX_USER_926222&backgroundColor=5c7cfa', 0, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `player_games`
--

DROP TABLE IF EXISTS `player_games`;
CREATE TABLE IF NOT EXISTS `player_games` (
  `player_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname_game` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rank` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hours_played` int DEFAULT '0',
  `region` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `game_uid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`player_id`,`game_id`),
  KEY `fk_pg_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_games`
--

INSERT INTO `player_games` (`player_id`, `game_id`, `nickname_game`, `rank`, `hours_played`, `region`, `game_uid`) VALUES
('1c4c4771-4f27-11f1-a50c-88d7f63043f1', 'ee087b9f-4c60-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL),
('90d1a9a1-4f02-11f1-a50c-88d7f63043f1', 'cb337297-4c04-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL),
('90d1a9a1-4f02-11f1-a50c-88d7f63043f1', 'ee087b9f-4c60-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL),
('dffd610a-4f2e-11f1-a50c-88d7f63043f1', 'cb337297-4c04-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL),
('dffd610a-4f2e-11f1-a50c-88d7f63043f1', 'ee087b9f-4c60-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL),
('ec0eae27-4f26-11f1-a50c-88d7f63043f1', 'cb337297-4c04-11f1-acf9-88d7f63043f1', NULL, NULL, 0, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `player_video_progress`
--

DROP TABLE IF EXISTS `player_video_progress`;
CREATE TABLE IF NOT EXISTS `player_video_progress` (
  `player_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tutorial_video_id` int NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  `completed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`player_id`,`tutorial_video_id`),
  KEY `idx_pvp_video` (`tutorial_video_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_video_progress`
--

INSERT INTO `player_video_progress` (`player_id`, `tutorial_video_id`, `completed`, `completed_at`) VALUES
('24f8f9df-4a18-11f1-9cd9-c85acfb0ea7a', 6, 1, '2026-05-11 10:19:44'),
('24f8f9df-4a18-11f1-9cd9-c85acfb0ea7a', 7, 1, '2026-05-11 10:19:51');

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
CREATE TABLE IF NOT EXISTS `posts` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `forum_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `media_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `post_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text',
  `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `post_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'published',
  `moderation_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'visible',
  `views` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_posts_forum` (`forum_id`),
  KEY `idx_posts_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `posts`
--

INSERT INTO `posts` (`id`, `forum_id`, `author_id`, `title`, `body`, `created_at`, `updated_at`, `media_type`, `media_url`, `post_type`, `tag`, `post_status`, `moderation_status`, `views`) VALUES
('0b5ab9ac-4f75-11f1-a50c-88d7f63043f1', 'f956c447-4f06-11f1-a50c-88d7f63043f1', '90d1a9a1-4f02-11f1-a50c-88d7f63043f1', 'yuio', 'rrrrrrrrrrrrrrrrrrrrrrrrr', '2026-05-14 09:12:34', '2026-05-14 09:12:34', 'image', NULL, 'DISCUSSION', NULL, 'ACTIF', 'flagged', 0),
('cbc67b9b-447f-11f1-a02f-a8934ae87a23', '122', 'raed', 'raaaed', 'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd', '2026-04-30 09:31:49', '2026-04-30 09:31:49', 'video', '/home/medraedboukari/Videos/Screencasts/video.mp4', 'video', '', 'published', 'visible', 0),
('ee29d37e-4adc-11f1-acf9-88d7f63043f1', 'ba090e3c-4adc-11f1-acf9-88d7f63043f1', 'd5a6f942-4a05-11f1-acf9-88d7f63043f1', 'aaaa', 'azeazeaze', '2026-05-08 12:53:37', '2026-05-08 12:53:37', 'image', NULL, 'DISCUSSION', NULL, 'ACTIF', 'visible', 0);

-- --------------------------------------------------------

--
-- Table structure for table `post_versions`
--

DROP TABLE IF EXISTS `post_versions`;
CREATE TABLE IF NOT EXISTS `post_versions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `media_url` varchar(1024) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `promotion_requests`
--

DROP TABLE IF EXISTS `promotion_requests`;
CREATE TABLE IF NOT EXISTS `promotion_requests` (
  `player_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  PRIMARY KEY (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `promotion_requests`
--

INSERT INTO `promotion_requests` (`player_id`, `date`, `status`) VALUES
('90d1a9a1-4f02-11f1-a50c-88d7f63043f1', '2026-05-14 00:39:51', 'approved');

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
CREATE TABLE IF NOT EXISTS `quiz` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tutorial_id` int DEFAULT NULL,
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `option_a` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_b` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_c` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `option_d` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `correct_answer` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `tutorial_id` (`tutorial_id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`id`, `tutorial_id`, `question`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `created_at`) VALUES
(12, 14, 'earraear', 'right', 'erarae', 'aerar', 'zeaera', 'A', '2026-05-06 09:56:41'),
(13, 13, 'earaaer', 'a', 'aerea', 'aerea', 'aerear', 'A', '2026-05-06 10:01:42'),
(14, 16, 'earaerer', 'a', 'aerae', 'aerera', 'aeraer', 'A', '2026-05-06 10:22:41'),
(16, 18, 'What you do when you play as Invisible Woman.', 'heal', 'damage', 'support', 'tank', 'B', '2026-05-11 10:14:32');

-- --------------------------------------------------------

--
-- Table structure for table `sponsors`
--

DROP TABLE IF EXISTS `sponsors`;
CREATE TABLE IF NOT EXISTS `sponsors` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `logo_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sponsor_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sponsors_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sponsor_notes`
--

DROP TABLE IF EXISTS `sponsor_notes`;
CREATE TABLE IF NOT EXISTS `sponsor_notes` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sponsor_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_note_sponsor` (`sponsor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sponsor_team`
--

DROP TABLE IF EXISTS `sponsor_team`;
CREATE TABLE IF NOT EXISTS `sponsor_team` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `sponsor_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `team_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `methode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `budget_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `sponsor_tournament`
--

DROP TABLE IF EXISTS `sponsor_tournament`;
CREATE TABLE IF NOT EXISTS `sponsor_tournament` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `sponsor_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `tournament_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `methode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `budget_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
CREATE TABLE IF NOT EXISTS `teams` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','DISBANDED') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `created_by` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `jersey_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_team_game` (`game_id`),
  KEY `fk_team_creator` (`created_by`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `teams`
--

INSERT INTO `teams` (`id`, `name`, `game_id`, `contact`, `logo_image`, `status`, `created_by`, `created_at`, `updated_at`, `jersey_image`) VALUES
('c07051d5-dd80-47c5-8f95-31711e891f97', 'SKT T1', 'cb337297-4c04-11f1-acf9-88d7f63043f1', 'skt@league.com', 'uploads/team-logos/1778703352089_skt_t1_logo.png', 'ACTIVE', NULL, '2026-05-13 20:23:06', '2026-05-13 20:23:05', 'uploads/team-jerseys/1778703441570_skt_t1_jersey.png'),
('649d4ac5-b9f6-40f0-8578-1eb495acf5a1', 'SSG', 'ee087b9f-4c60-11f1-acf9-88d7f63043f1', 'SSG@contact.com', 'uploads/team-logos/1778703921829_ssg_logo.png', 'ACTIVE', NULL, '2026-05-13 20:26:21', '2026-05-13 20:28:47', 'uploads/team-jerseys/1778704101547_ssg_jersey.png'),
('fd97d933-28de-49f1-ba58-3ffdbf8ca809', 'Vitality', 'cb337297-4c04-11f1-acf9-88d7f63043f1', 'vitality@mail.com', 'uploads/team-logos/1778747798124_background.jpg', 'ACTIVE', '7758df35-4f03-11f1-a50c-88d7f63043f1', '2026-05-14 08:36:25', '2026-05-14 08:36:55', 'uploads/team-jerseys/1778747813433_background.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `team_members`
--

DROP TABLE IF EXISTS `team_members`;
CREATE TABLE IF NOT EXISTS `team_members` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `player_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `role` enum('MEMBER','CAPTAIN','COACH') COLLATE utf8mb4_unicode_ci DEFAULT 'MEMBER',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_player_team` (`player_id`),
  KEY `fk_member_team` (`team_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `team_messages`
--

DROP TABLE IF EXISTS `team_messages`;
CREATE TABLE IF NOT EXISTS `team_messages` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_type` enum('USER','SYSTEM') COLLATE utf8mb4_unicode_ci DEFAULT 'USER',
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_edited` tinyint(1) DEFAULT '0',
  `edited_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_message_team` (`team_id`),
  KEY `fk_message_sender` (`sender_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tournaments`
--

DROP TABLE IF EXISTS `tournaments`;
CREATE TABLE IF NOT EXISTS `tournaments` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `tournament_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `game_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `center_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `participant_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `starts_at` datetime NOT NULL,
  `ends_at` datetime NOT NULL,
  `prize_pool` decimal(10,2) NOT NULL DEFAULT '0.00',
  `state` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'REGISTRATION_OPEN',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `max_players` int NOT NULL DEFAULT '32',
  `challonge_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Challonge tournament ID',
  `challonge_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Public bracket URL',
  `challonge_url_slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_synced` tinyint(1) DEFAULT '0' COMMENT 'Is synced to Challonge',
  `is_started` tinyint(1) DEFAULT '0' COMMENT 'Is tournament started',
  PRIMARY KEY (`id`),
  KEY `idx_tournament_name` (`tournament_name`),
  KEY `idx_starts_at` (`starts_at`),
  KEY `idx_center_id` (`center_id`),
  KEY `idx_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tournaments`
--

INSERT INTO `tournaments` (`id`, `tournament_name`, `game_id`, `center_id`, `format`, `participant_type`, `starts_at`, `ends_at`, `prize_pool`, `state`, `created_at`, `max_players`, `challonge_id`, `challonge_url`, `challonge_url_slug`, `is_synced`, `is_started`) VALUES
('b0085a75-4f81-11f1-a50c-88d7f63043f1', 'Smash Summit', 'ee087b9f-4c60-11f1-acf9-88d7f63043f1', 'center-lille-001', 'Double Elimination', 'Team', '2026-05-08 00:00:00', '2026-05-30 23:59:00', 5000.00, 'REGISTRATION_OPEN', '2026-05-14 10:43:04', 8, NULL, NULL, NULL, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `tournament_matches`
--

DROP TABLE IF EXISTS `tournament_matches`;
CREATE TABLE IF NOT EXISTS `tournament_matches` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `tournament_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `challonge_match_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `round` int NOT NULL,
  `match_number` int NOT NULL,
  `player1_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `player2_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `challonge_player1_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Challonge participant ID for player 1',
  `challonge_player2_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Challonge participant ID for player 2',
  `winner_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `player1_score` int NOT NULL DEFAULT '0',
  `player2_score` int NOT NULL DEFAULT '0',
  `status` enum('PENDING','IN_PROGRESS','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `scheduled_time` datetime DEFAULT NULL,
  `completed_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_tm_tournament` (`tournament_id`),
  KEY `idx_tm_player1` (`player1_id`),
  KEY `idx_tm_player2` (`player2_id`),
  KEY `idx_tm_winner` (`winner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tournament_participants`
--

DROP TABLE IF EXISTS `tournament_participants`;
CREATE TABLE IF NOT EXISTS `tournament_participants` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `tournament_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `participant_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tournament_type` enum('SOLO','TEAM') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `seed` int NOT NULL,
  `status` enum('ACTIVE','ELIMINATED','WINNER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `challonge_participant_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Challonge participant ID',
  `elimination_reason` enum('LOST','WITHDREW') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Why player was eliminated',
  `final_placement` int DEFAULT NULL COMMENT 'Final ranking position (1st, 2nd, etc.)',
  `eliminated_at_round` int DEFAULT NULL COMMENT 'Which round they were eliminated',
  PRIMARY KEY (`id`),
  KEY `fk_tp_tournament` (`tournament_id`),
  KEY `idx_tp_participant` (`participant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tournament_participants`
--

INSERT INTO `tournament_participants` (`id`, `tournament_id`, `participant_id`, `tournament_type`, `seed`, `status`, `challonge_participant_id`, `elimination_reason`, `final_placement`, `eliminated_at_round`) VALUES
('48f98cab-4f59-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', '03820c24-4708-11f1-a197-0a0027000009', 'SOLO', 1, 'ELIMINATED', '293697565', 'LOST', NULL, 1),
('6046a5ee-4f5d-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', 'a4ea6356-4707-11f1-a197-0a0027000009', 'SOLO', 3, 'ACTIVE', '293697567', NULL, NULL, NULL),
('6f3672ff-4f5e-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', 'fad9e9fa-4706-11f1-a197-0a0027000009', 'SOLO', 5, 'ELIMINATED', '293697569', 'LOST', NULL, 1),
('766edfa9-4f59-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', '79b0b910-4707-11f1-a197-0a0027000009', 'SOLO', 2, 'ACTIVE', '293697566', NULL, NULL, NULL),
('c06002f0-4f60-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', 'd8c79354-4707-11f1-a197-0a0027000009', 'SOLO', 8, 'ACTIVE', '293697572', NULL, NULL, NULL),
('ddbc1c8b-4f5e-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', '4019939e-4707-11f1-a197-0a0027000009', 'SOLO', 6, 'ACTIVE', '293697570', NULL, NULL, NULL),
('f50f3abd-4f5d-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', 'a133e931-467b-11f1-a197-0a0027000009', 'SOLO', 4, 'ACTIVE', '293697568', NULL, NULL, NULL),
('f5d75e38-4f5e-11f1-8072-0a002700000a', '1b532931-4f59-11f1-8072-0a002700000a', '26662043-46ec-11f1-a197-0a0027000009', 'SOLO', 7, 'ACTIVE', '293697571', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `training_attendance`
--

DROP TABLE IF EXISTS `training_attendance`;
CREATE TABLE IF NOT EXISTS `training_attendance` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `player_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PRESENT','ABSENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recorded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_session_player` (`session_id`,`player_id`),
  KEY `fk_attendance_team` (`team_id`),
  KEY `fk_attendance_player` (`player_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `training_notifications`
--

DROP TABLE IF EXISTS `training_notifications`;
CREATE TABLE IF NOT EXISTS `training_notifications` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `player_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `notification_type` enum('SESSION_CREATED','SESSION_UPDATED','SESSION_CANCELLED','SESSION_REMINDER','ATTENDANCE_REQUEST') COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `read_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_notification_session` (`session_id`),
  KEY `fk_notification_team` (`team_id`),
  KEY `fk_notification_player` (`player_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `training_sessions`
--

DROP TABLE IF EXISTS `training_sessions`;
CREATE TABLE IF NOT EXISTS `training_sessions` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `team_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('TEAM_PRACTICE','SCRIMMAGE','STRATEGY','REVIEW','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT 'TEAM_PRACTICE',
  `session_datetime` timestamp NOT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PLANNED','IN_PROGRESS','COMPLETED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'PLANNED',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_session_team` (`team_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `training_sessions`
--

INSERT INTO `training_sessions` (`id`, `team_id`, `title`, `type`, `session_datetime`, `start_time`, `end_time`, `location`, `status`, `notes`, `created_at`, `updated_at`) VALUES
('431f93f6-6023-43e7-acaf-4424d9098011', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Pratique en Équipe', 'TEAM_PRACTICE', '2026-05-14 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Mise en pratique des stratégies apprises', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('f270ddf5-f74a-4874-a703-478c18a970fa', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Stratégie & Théorie', 'STRATEGY', '2026-05-19 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Apprentissage des bases et des stratégies fondamentales', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('2f54da8d-5efa-49fa-a773-e9550ebbb2c1', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Pratique en Équipe', 'TEAM_PRACTICE', '2026-05-21 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Mise en pratique des stratégies apprises', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('719d3ed4-60e1-4e84-b934-54e9dd1daeed', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Stratégie & Théorie', 'STRATEGY', '2026-05-26 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Apprentissage des bases et des stratégies fondamentales', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('100ea455-3f84-444b-8537-ad009a103dbe', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Pratique en Équipe', 'TEAM_PRACTICE', '2026-05-28 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Mise en pratique des stratégies apprises', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('ba492608-144c-4011-ae65-4f41c6c3c710', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Stratégie & Théorie', 'STRATEGY', '2026-06-02 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Apprentissage des bases et des stratégies fondamentales', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('2605adee-e09e-414b-b5fb-bba3485cdb8f', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Pratique en Équipe', 'TEAM_PRACTICE', '2026-06-04 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Mise en pratique des stratégies apprises', '2026-05-13 20:24:28', '2026-05-13 20:24:28'),
('8d096414-8b54-43b0-8b31-6f74ab7d498d', 'c07051d5-dd80-47c5-8f95-31711e891f97', 'Stratégie & Théorie', 'STRATEGY', '2026-06-09 17:00:00', '18:00:00', '20:00:00', NULL, 'PLANNED', 'Apprentissage des bases et des stratégies fondamentales', '2026-05-13 20:24:28', '2026-05-13 20:24:28');

-- --------------------------------------------------------

--
-- Table structure for table `tutorial`
--

DROP TABLE IF EXISTS `tutorial`;
CREATE TABLE IF NOT EXISTS `tutorial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tutorial`
--

INSERT INTO `tutorial` (`id`, `title`, `description`, `video_url`, `category`, `difficulty`, `created_at`) VALUES
(18, 'Guide : How to get to Celestial - Marvel Rivals', 'Get to celestial in one week', 'https://www.youtube.com/watch?v=PfnLGIS7NLI', 'OBJECTIVE', 'EXPERT', '2026-05-11'),
(13, 'aerearaer', 'aerear', 'youtube.com', 'MACRO', 'INTERMEDIATE', '2026-05-05'),
(14, 'tuto', 'aeraerrae', 'youtube.com', 'MACRO', 'INTERMEDIATE', '2026-05-05');

-- --------------------------------------------------------

--
-- Table structure for table `tutorial_rating`
--

DROP TABLE IF EXISTS `tutorial_rating`;
CREATE TABLE IF NOT EXISTS `tutorial_rating` (
  `player_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tutorial_id` int NOT NULL,
  `stars` tinyint NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`tutorial_id`),
  KEY `idx_rating_tutorial` (`tutorial_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tutorial_rating`
--

INSERT INTO `tutorial_rating` (`player_id`, `tutorial_id`, `stars`, `comment`, `created_at`) VALUES
('24f8f9df-4a18-11f1-9cd9-c85acfb0ea7a', 18, 5, 'WoW !', '2026-05-11 10:20:13');

-- --------------------------------------------------------

--
-- Table structure for table `tutorial_video`
--

DROP TABLE IF EXISTS `tutorial_video`;
CREATE TABLE IF NOT EXISTS `tutorial_video` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tutorial_id` int NOT NULL,
  `position` int NOT NULL DEFAULT '1',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tv_tutorial` (`tutorial_id`),
  KEY `idx_tv_tutorial_position` (`tutorial_id`,`position`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tutorial_video`
--

INSERT INTO `tutorial_video` (`id`, `tutorial_id`, `position`, `title`, `video_url`, `created_at`) VALUES
(1, 16, 1, 'Video 1 - test2', 'youtube.com', '2026-05-11 10:07:18'),
(2, 17, 1, 'Video 1 - VIDEOTEST', 'https://www.youtube.com/watch?v=QX8FUu-s0C0&pp=ugUHEgVlbi1VU9IHCQkDCwGHKiGM7w%3D%3D', '2026-05-11 10:07:18'),
(3, 15, 1, 'Video 1 - test', 'youtube.com', '2026-05-11 10:07:18'),
(4, 13, 1, 'Video 1 - aerearaer', 'youtube.com', '2026-05-11 10:07:18'),
(5, 14, 1, 'Video 1 - tuto', 'youtube.com', '2026-05-11 10:07:18'),
(6, 18, 1, 'Day 1', 'https://www.youtube.com/watch?v=PfnLGIS7NLI', '2026-05-11 10:13:15'),
(7, 18, 2, 'Day 2', 'https://www.youtube.com/watch?v=vd1LtoDiU80', '2026-05-11 10:13:15');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','coach','player') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'player',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `salt` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_username` (`username`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `role`, `created_at`, `salt`) VALUES
('1c4c4771-4f27-11f1-a50c-88d7f63043f1', 'black7med', 'mouhamed@mail.com', '6BosJ6z28YiD45w5ejHvQAQNleXyeS61clHLiTP6BpM=', 'player', '2026-05-13 23:54:42', '47MQqetQx2nJAqTvXN3YbQ=='),
('7758df35-4f03-11f1-a50c-88d7f63043f1', 'user', 'mail@mail.com', '6qz/ZbBOtuyLXOJjcuoYGddM7MTArUAevWkz6FpfL6g=', 'admin', '2026-05-13 19:39:33', '/BRI/tvGWBWy8mPtyoIZZg=='),
('90d1a9a1-4f02-11f1-a50c-88d7f63043f1', 'player', 'mail@player.gg', 'lRdgyyq2ZivLMxM5UJO0GIV48V2kq5n9oSnRTL0dpCM=', 'coach', '2026-05-13 19:33:06', 'b5h2RazuO5YLrnZCXOjxBw=='),
('9f2f2ed3-4f75-11f1-a50c-88d7f63043f1', 'aaaa', 'abc@abc.abc', 'SEbpVXKs5hwn/G7lR7qj5OBcqqFpHLYenYshogIee44=', 'player', '2026-05-14 09:16:43', '2XZ0sf9UICWpksewTrkXHQ=='),
('dffd610a-4f2e-11f1-a50c-88d7f63043f1', 'LavanderBeast', 'mohamedazizjarraya@gmail.com', 'YrK7Kvx0DDv72cKOoOpZ1TCignkXPMsX2tLfjTsmYGE=', 'player', '2026-05-14 00:50:17', 'IUoOEZLJpU6Yc8k2djTr9Q=='),
('ec0eae27-4f26-11f1-a50c-88d7f63043f1', 'ESMI DALLEJI', 'mounirdalleji@7alouf.fr', '5i3mwM9hA+ENA33YbKQRbhr5EiSPS6EKfd7KfENWsfc=', 'coach', '2026-05-13 23:53:21', 'Z4AmeYkKysAmetMWaiCRMA==');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `marketplace_cart`
--
ALTER TABLE `marketplace_cart`
  ADD CONSTRAINT `fk_cart_item` FOREIGN KEY (`item_id`) REFERENCES `marketplace_items` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `marketplace_items`
--
ALTER TABLE `marketplace_items`
  ADD CONSTRAINT `fk_mi_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `players`
--
ALTER TABLE `players`
  ADD CONSTRAINT `fk_players_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `player_games`
--
ALTER TABLE `player_games`
  ADD CONSTRAINT `fk_pg_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_pg_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `promotion_requests`
--
ALTER TABLE `promotion_requests`
  ADD CONSTRAINT `fk_nr_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `tournaments`
--
ALTER TABLE `tournaments`
  ADD CONSTRAINT `fk_tournaments_center` FOREIGN KEY (`center_id`) REFERENCES `centers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_tournaments_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
