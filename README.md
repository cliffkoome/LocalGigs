# LocalGigs

LocalGigs is a platform that connects clients with verified local professionals for various services. It addresses the challenges of finding reliable professionals and provides visibility for skilled workers while ensuring trust and transparency. This repository contains the Android application code for LocalGigs, developed using Android Studio with Jetpack Compose.

## Features

- **User Authentication:**
  - Signup and Login functionalities.
  - Firebase Authentication for secure access.

- **Professional Listings:**
  - View and search for professionals based on categories and location.

- **Chat System: {Not Complete}**
  - Real-time messaging between clients and professionals.
  - Firebase Firestore for storing messages.

- **Job Management:**
  - Post job requests.
  - Accept and manage job offers.
  - View ongoing and completed jobs.

- **Review System: {Not Complete}**
  - Rate and review professionals after service completion.

- **Revenue Streams: {Not Complete}**
  - Subscription plans for professionals.
  - Transaction fees and in-app advertisements.

- **Dashboard:**
  - Personalized client and professional dashboards.
  - Access to job history and performance statistics.

- **Notifications: {Not Complete}**
  - Real-time push notifications for job updates, messages, and reviews.

- **Geo-targeted Listings: {Not Complete}**
  - Match clients with professionals in their vicinity.

- **Profile Verification:**
  - Ensure authenticity and trustworthiness of professionals.

## Project Structure

```
LocalGigs/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/localgigs/
│   │   │   │   ├── auth/                # Authentication logic
│   │   │   │   ├── model/               # Data models (User, Job, Message, etc.)
│   │   │   │   ├── pages/               # Composable pages (Login, Home, etc.)
│   │   │   │   ├── repository/          # Firebase interaction logic
│   │   │   │   ├── ui/                  # UI components
│   │   │   │   ├── utils/               # Utility functions
│   │   │   │   ├── MainActivity.kt      # Entry point of the app
│   │   │   │   ├── MyAppNavigation.kt   # Navigation setup
│   │   │   │   ├── notifications/       # Push notifications logic
│   │   │   │   ├── dashboard/           # Client and professional dashboards
│   │   │   │   ├── jobs/                # Job management logic
├── README.md
├── build.gradle
└── ...
```

## Prerequisites

- Android Studio (Arctic Fox or later)
- Java 11
- Firebase Project with Firestore, Authentication, and Cloud Messaging enabled

## Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/localgigs.git
   cd localgigs
   ```

2. **Open in Android Studio:**
   - Launch Android Studio.
   - Open the project directory.

3. **Configure Firebase:**
   - Download the `google-services.json` file from your Firebase console.
   - Place it in the `app/` directory.

4. **Build and Run:**
   - Sync Gradle files.
   - Build the project.
   - Run on an emulator or physical device.

## Navigation Flow

- **LoginPage:** User login functionality.
- **SignupPage:** New user registration.
- **HomePage:** Main dashboard with options to navigate to professional listings, chat, and settings.
- **UsersListPage:** Displays a list of professionals for the user to initiate chats.
- **ChatScreen:** Real-time messaging functionality between users.
- **JobManagementPage:** Manage posted jobs, view ongoing and completed jobs.
- **NotificationPage:** Access and manage real-time push notifications.
- **ProfilePage:** User profile management and verification.

## Contribution Guidelines

1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature-name
   ```
3. Make your changes and commit them:
   ```bash
   git commit -m "Add detailed description of changes"
   ```
4. Push to the branch:
   ```bash
   git push origin feature-name
   ```
5. Create a pull request on the main repository.


## Acknowledgements

- Firebase for backend services.
- Android Studio and Jetpack Compose for development.
- Open-source libraries and community contributions.

---

For any issues or feature requests, please open an issue on the [GitHub repository](https://github.com/your-username/localgigs).
