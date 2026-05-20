## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Core Features](#core-features)
- [Configuration](#configuration)
- [Building & Running](#building--running)
- [API & Services](#api--services)
- [Machine Learning](#machine-learning)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

**Genex** is a sophisticated management platform designed for esports organizations, training centers, and tournament organizers. It provides a complete ecosystem for:

- 🎮 **Tournament Management** - Create, organize, and manage esports tournaments with Challonge integration
- 👥 **Team Management** - Build and manage esports teams with real-time rankings
- 🏋️ **Training Programs** - Schedule and track player training sessions
- 🎥 **Video Content** - Tutorial videos and player progress tracking
- 💰 **Payment Integration** - Stripe-based payment processing
- 🔍 **Content Moderation** - AI-powered toxicity detection
- 📊 **Analytics Dashboard** - Real-time statistics and reporting
- 👤 **User Authentication** - Google OAuth 2.0 integration
- 💬 **Communication** - Team messaging and notifications

---

## ✨ Features

### 🏆 Tournament System
- Create and manage multiple tournaments
- Challonge Integration - Automatic bracket generation and tournament hosting
- Public Tournament URLs - Share tournaments via Challonge links and embedded modules
- Sponsor integration and partnerships
- Participant ranking and statistics
- Real-time tournament updates

### 👥 Team Management
- Team creation and member management
- Hierarchical team structure
- Team messaging and collaboration
- Real-time rankings

### 🎓 Training & Development
- Training session scheduling
- Tutorial video library
- Player progress tracking
- Training notifications and attendance

### 💳 Payment Processing
- Stripe integration for secure payments
- Order management and marketplace
- Payment history and receipts
- Multiple payment methods

### 🤖 Content Moderation
- Weka ML-based toxicity detection
- Automatic content filtering
- Post moderation and approval workflow
- Configurable sensitivity levels

### 🔐 Authentication
- Google OAuth 2.0 login
- Secure token management
- Session handling
- User profile management

### 🎙️ Multimedia
- Tutorial video management
- Player video progress tracking
- QR code generation and scanning
- Voice integration (Vosk)

---

## 🛠️ Tech Stack

### Backend
- **Language**: Java 21
- **UI Framework**: JavaFX 23.0.2
- **Build Tool**: Maven
- **Database**: MySQL 8.0.33
- **ORM**: Hibernate/JPA

### Libraries & Services
- **Machine Learning**: Weka 3.8.6 (Toxicity Detection)
- **Tournament Management**: Challonge API
- **Payments**: Stripe Java SDK 32.1.0
- **Email**: SendGrid 4.10.1
- **Authentication**: Google OAuth 2.0
- **AI**: Google Cloud AI Platform (Gemini)
- **Calendar**: Google Calendar API
- **QR Codes**: ZXing 3.5.3
- **Voice**: Vosk 0.3.45
- **PDF**: OpenPDF 2.0.3

---

## 📦 Installation

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- MySQL 8.0+
- Git
- Challonge account (for tournament management)

### Setup Steps

1. **Clone the repository**
```bash
git clone https://github.com/LavanderBe/Genex.git
cd Genex
