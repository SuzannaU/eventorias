# Eventorias

Eventorias is a modern Android application designed for discovering and sharing events. Built with Jetpack Compose and following clean architecture principles, it provides a seamless experience for users to browse, search, and manage events.

## 🚀 Features

- **Event Browsing**: View a curated list of events with detailed information.
- **Search & Filtering**: Easily find events by title and sort them by date or category.
- **User Authentication**: Secure sign-in and profile management powered by Firebase Auth.
- **Real-time Data**: Event data is synchronized in real-time using Cloud Firestore.
- **Cloud Storage**: Event images and user profile pictures are hosted on Firebase Storage.
- **Modern UI**: A responsive and beautiful interface built entirely with Jetpack Compose and Material 3.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Backend Services**: [Firebase](https://firebase.google.com/) (Firestore, Auth, Storage, Cloud Messaging)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture layers.

## 🏗 Project Structure

The project is organized into several layers to ensure maintainability and testability:

- **`data`**: Implements repositories and data sources (Firebase, local cache).
- **`domain`**: Contains business logic, models (`Event`, `User`), and repository interfaces.
- **`ui`**: Jetpack Compose screens, ViewModels, and navigation logic.

## ⚙️ Setup

To get the project running locally:

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/Eventorias.git
    ```
2.  **Add Firebase**:
    - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android app with the package name `parcours.android.eventorias`.
    - Download the `google-services.json` file and place it in the `app/` directory.
    - Enable **Email/Password** authentication, **Firestore**, and **Storage** in the Firebase console.
3.  **Build and Run**:
    - Open the project in Android Studio.
    - Sync Gradle and run the app on an emulator or physical device.

## 📸 Screenshots

*(Add screenshots here once available)*

---
Developed as part of the Android learning path.
