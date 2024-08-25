# WeatherApp

This project was for Mobile and Ubiquitous Computing (CS 4518). The goal was to create an Android app that read weather API data from www.weatherapi.com using Android Studio, Kotlin, and the Model, Controller, View design model. The data collected from the API was stored in the [Models](app/src/main/java/wpics/weather/models). The data was interpreted and stored by the [WeatherFragment](app/src/main/java/wpics/weather/fragments/WeatherFragment.kt). The visible display was made with an [XML](app/src/main/res/layout/fragment_weather.xml). 

Before running, the API key needs to be updated. Start a free trial at www.weatherapi.com and replace the API key in the [apikey.properties](apikey.properties). This app will work for Android API 34+.
