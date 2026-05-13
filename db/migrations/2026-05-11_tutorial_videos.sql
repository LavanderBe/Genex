/* =============================================================
   GENEX - Migration : tutoriels multi-videos + progression + notation
   Date : 2026-05-11
   Rend les tutoriels composables de plusieurs videos YouTube,
   trace la progression par video pour chaque joueur, et permet
   au joueur de noter le tutoriel apres completion totale.
   ============================================================= */

START TRANSACTION;

/* -----------------------------------------------------------
   Table tutorial_video : liste ordonnee de videos par tutoriel
   ----------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS `tutorial_video` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tutorial_id` INT NOT NULL,
  `position` INT NOT NULL DEFAULT 1,
  `title` VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_url` VARCHAR(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tv_tutorial` (`tutorial_id`),
  KEY `idx_tv_tutorial_position` (`tutorial_id`, `position`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* -----------------------------------------------------------
   Table player_video_progress : completion par video pour un joueur
   PK composee (player_id, tutorial_video_id) : un joueur ne peut
   completer une video qu'une seule fois.
   ----------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS `player_video_progress` (
  `player_id` CHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tutorial_video_id` INT NOT NULL,
  `completed` TINYINT(1) NOT NULL DEFAULT 0,
  `completed_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`player_id`, `tutorial_video_id`),
  KEY `idx_pvp_video` (`tutorial_video_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* -----------------------------------------------------------
   Table tutorial_rating : une note par joueur et par tutoriel.
   Le widget cote application n'autorise la note qu'a 100% de
   completion, cf. TutorialDetailController.
   ----------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS `tutorial_rating` (
  `player_id` CHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tutorial_id` INT NOT NULL,
  `stars` TINYINT NOT NULL,
  `comment` TEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`, `tutorial_id`),
  KEY `idx_rating_tutorial` (`tutorial_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* -----------------------------------------------------------
   Migration des donnees existantes : chaque tutoriel ayant
   une video_url non vide devient un tutoriel mono-video dans
   la nouvelle table. La colonne tutorial.video_url est laissee
   en place pour retrocompatibilite mais n'est plus alimentee
   par les nouveaux ajouts.
   ----------------------------------------------------------- */
INSERT INTO `tutorial_video` (`tutorial_id`, `position`, `title`, `video_url`)
SELECT
  t.`id`,
  1,
  CONCAT('Video 1 - ', t.`title`),
  t.`video_url`
FROM `tutorial` t
WHERE t.`video_url` IS NOT NULL
  AND t.`video_url` <> ''
  AND NOT EXISTS (
    SELECT 1 FROM `tutorial_video` tv WHERE tv.`tutorial_id` = t.`id`
  );

COMMIT;
