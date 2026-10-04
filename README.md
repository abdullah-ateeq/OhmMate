# ⚡ OhmMate - Electrical & Ohm's Law Calculator

**OhmMate** is an intuitive, modern, and high-precision Android application built with **Kotlin** and **Jetpack Compose**. It simplifies electrical calculations by computing Ohm's Law parameters ($V, I, R, P$) instantaneously given any two known parameters.

---

## ✨ Features

- 🧮 **Complete Ohm's Law Calculations**:
  - Input any **two** known parameters (**Voltage**, **Current**, **Resistance**, or **Power**) to automatically compute the remaining two.
  - Supports all 6 parameter combinations ($V+I$, $V+R$, $V+P$, $I+R$, $I+P$, $R+P$).
- 📐 **Formula & Calculation Details**:
  - Step-by-step mathematical formula explanations rendered dynamically with each calculation.
  - Safety advisory banners for high-power or sensitive scenarios.
- 🕒 **History Log**:
  - Save calculation snapshots locally to Jetpack DataStore.
  - Quickly reload previous calculations back into the interactive calculator.
  - Delete individual entries or clear history on demand.
- ⚙️ **Customization & Settings**:
  - **Theme Modes**: System Default, Light Mode, Dark Mode.
  - **Material You Dynamic Colors**: Adapts seamlessly to your device wallpaper on Android 12+.
  - **Precision Control**: Select between 1 and 3 decimal places.
  - **Auto-Scale Units**: Displays values automatically in $mV$, $kV$, $mA$, $kA$, $m\Omega$, $k\Omega$, $M\Omega$, $mW$, $kW$.
  - **Default Parameter Pairs**: Configure your most frequently used starting pair (e.g. $V + R$).
  - **Haptic Feedback**: Subtle tactile response when making adjustments.
- 📱 **Modern UI**: Clean, responsive layout adhering strictly to Material Design 3 guidelines.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 components
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **Asynchronous Operations**: Kotlin Coroutines & `StateFlow`
- **Data Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Navigation**: Navigation Compose with bottom navigation bar
- **Gradle**: Kotlin DSL with Version Catalog (`libs.versions.toml`)
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 37 (Android 16)

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Ladybug / Meerkat or newer recommended)
- JDK 11 or higher
- Android SDK 37 (installed via SDK Manager in Android Studio)

### Building and Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/OhmMate.git
   ```
2. Open the project in Android Studio.
3. Sync project with Gradle files (`File` > `Sync Project with Gradle Files`).
4. Select your target target device or emulator and click **Run** (`Shift + F10`).

---

## 🧪 Testing

Run unit tests locally using Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
