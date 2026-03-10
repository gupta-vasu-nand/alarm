# Alarm App – Modern Android Alarm Manager

A modern alarm scheduling application built using **Jetpack Compose** and **Material 3**.
This app provides a clean user experience, precise alarm triggering, customizable tones, and reliable background execution.

---

## Features

### Alarm Management

* Create, edit, and delete alarms
* Recurring alarms with day selection
* Enable or disable alarms instantly
* Multiple alarms support

### Alarm Behavior

* Exact alarm scheduling (Android 12+ compliant)
* Snooze with configurable duration
* Vibration support
* Works in Doze mode
* Automatically restores alarms after device reboot

### Ringtone System

* System and custom ringtone selection
* Default ringtone configuration
* Media playback using foreground services

### Modern UI

* Built entirely with Jetpack Compose
* Material 3 design system
* Dynamic light/dark theme
* Clean and responsive layouts
* Lock screen alarm interface

### Architecture

* MVVM Architecture
* Hilt Dependency Injection
* StateFlow and Compose State
* Modular and scalable structure

---

## Screenshots

### Splash Screen

![Splash](res/drawable/splash_screen.png)

### Home Screen

![Home](res/drawable/home_screen.png)

### Add / Edit Alarm

![Add Alarm](res/drawable/add_alarm_screen.png)

### Alarm Ringing Screen

![Ringing](res/drawable/ringing_screen.png)

### Ringtone Picker

![Ringtone Picker](res/drawable/ringtone_picker_screen.png)

### Settings Screen

![Settings](res/drawable/settings_screen.png)

---

## Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose
* **Design:** Material 3
* **Architecture:** MVVM
* **Dependency Injection:** Hilt
* **Concurrency:** Coroutines & Flow
* **Local Storage:** DataStore
* **Background Tasks:** Foreground Service & AlarmManager

---

## Permissions Used

* Exact alarm scheduling
* Foreground service (media playback)
* Notifications
* Vibration
* Boot completed receiver
* Wake lock
* Media audio access

---

## How to Run

1. Clone the repository
2. Open in Android Studio (latest stable)
3. Sync Gradle
4. Run on Android 12+ device or emulator
5. Grant required permissions when prompted

---

## Project Structure

```
app/
 ├── alarm/              # Alarm scheduling and services
 ├── ui/
 │    ├── screens/       # All app screens
 │    ├── components/    # Reusable UI components
 │    ├── theme/         # Material theme setup
 │    └── navigation/    # Navigation graph
 ├── data/               # DataStore and repositories
 └── di/                 # Dependency injection modules
```

---

## Future Improvements

* Gradual alarm volume increase
* Weather-aware alarms
* Spotify/custom streaming alarms
* Wear OS companion support
* Home screen widgets
* Cloud backup & sync

---

## License

This project is for educational and portfolio purposes.
