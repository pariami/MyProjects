Android Currency Exchange App

In this application we will be using the Currency API to fetch the currency list and exchange rate and display them in a list. The user can click on a exchange button and exchange the wallet deposit. we will be using the following API:

    https://developers.paysera.com

Technologies

    Using Kotlin.
    Using Clean Architecture principles.
    Using MVVM.
    Using Modular structure.
    Using Jetpack Compose for the UI.
    Using Hilt for dependency injection.
    Using Retrofit for networking.
    Using Kotlin Coroutine for asynchronous programming.
    Using Unit tests.
    Using Ui tests.
    Using Single activity, zero fragments.

Architecture

The app is built using the Clean Architecture principles. The app is divided into 3 modules:

    app - The main module that contains the UI and the DI setup.
    data - The module that contains the data layer. It contains the repository and the data sources with api and mock data sources.
    domain - The module that contains the business logic. It contains the use cases and the models.

Every module is independent and can be tested separately. also they work together to achieve the final result. this architecture makes the app more maintainable and testable and also makes it easier to add new features. each layer use their own models and mappers to make the app more flexible and easy to maintain
