# Altimeter App
A simple Android app that calculates altitude based on pressure sensor readings and displays an altimeter UI.
Features

## Realtime altitude calculation from pressure sensor
Simulated pressure changes for testing
Background color changes based on altitude (darker = higher)
Live updating display

## How It Works
The app uses the barometric pressure formula to calculate altitude:
h = 44330 * (1 - (P/P0)^0.1903)

## Testing
Use the pressure simulator to test altitude changes without a physical pressure sensor. The UI updates in real-time as simulated pressure values change.
Requirements

## Setup
- Clone the repository
- Open in Android Studio
- Build and run on device or emulator
