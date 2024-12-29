# Android Currency Exchange App

## Overview
The Android Currency Exchange App is a feature-rich application that uses the [Currency API](https://developers.paysera.com) to fetch a list of currencies and exchange rates. Users can view exchange rates in a list and perform currency exchanges for their wallet deposits through an intuitive UI.

---

## Features
- Fetches currency list and exchange rates.
- Displays exchange rates in a user-friendly list.
- Enables users to exchange wallet deposits with the tap of a button.

---

## Technologies Used
- **Kotlin** for programming.
- **Clean Architecture** principles for maintainability and scalability.
- **MVVM (Model-View-ViewModel)** for state management.
- **Modular Structure** for better organization.
- **Jetpack Compose** for building a modern and reactive UI.
- **Hilt** for dependency injection.
- **Retrofit** for networking and API communication.
- **Kotlin Coroutines** for asynchronous programming.
- **Unit Tests** for testing individual components.
- **UI Tests** for testing the user interface.
- **Single Activity Architecture** with zero fragments for simplicity and performance.

---

## Architecture
The app is built using **Clean Architecture** principles, ensuring separation of concerns and testability. It is divided into three main modules:

### 1. **App Module**
   - Contains the UI components.
   - Manages dependency injection setup using Hilt.

### 2. **Data Module**
   - Implements the data layer of the application.
   - Contains repositories and data sources.
   - Handles API calls and provides mock data for testing.

### 3. **Domain Module**
   - Encapsulates the business logic of the application.
   - Includes use cases and models for data processing.

Each module operates independently and can be tested separately while working together seamlessly to deliver the app's functionalities. The layered structure promotes flexibility, easier maintenance, and straightforward feature addition.

---

## Benefits of Clean Architecture
- **Maintainability:** Separation of concerns makes the app easier to manage and extend.
- **Testability:** Independent modules allow for thorough and isolated testing.
- **Flexibility:** Each layer uses its own models and mappers, ensuring adaptability to changing requirements.

---

## Getting Started
To set up and run the project:
1. Clone the repository.
2. Set up your API key for the Currency API in the data module.
3. Build and run the app in Android Studio.

---

## Contribution
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request for review.

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

---

## Acknowledgments
This app leverages the [Currency API by Paysera](https://developers.paysera.com) for real-time exchange rates and currency information.

