# Camth Projects Android App

## Overview
The *Camth Projects Android App* is a mobile application that allows users to submit and track electrical service requests, and for administrators to manage those requests. The app uses *internal storage* (SQLite database) to store user information and service requests locally on the device.

---

## Features

### User
- Sign up and log in  
- Submit service requests  
- View submitted requests and their status  

### Administrator
- Log in as admin  
- View all submitted service requests  
- Update the status of requests (In Progress, Completed)  

---

## Tech Stack
- *Frontend:* Android (Kotlin)  
- *Database:* SQLite (local storage)  
- *IDE:* Android Studio  
- *Build System:* Gradle  

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/YourUsername/CamthProjectsApp.git

	2.	Open the project in Android Studio
	3.	Sync Gradle and build the project
	4.	Run the app on an emulator or physical Android device

⸻

Folder Structure

CamthProjectsApp/
├─ app/
│  ├─ src/main/java/com/example/camthprojects/
│  │  ├─ MainActivity.kt
│  │  ├─ LoginActivity.kt
│  │  ├─ SignupActivity.kt
│  │  ├─ UserDashboardActivity.kt
│  │  ├─ AdminDashboardActivity.kt
│  │  └─ DatabaseHelper.kt
│  └─ src/main/res/
│     ├─ layout/
│     │  ├─ activity_main.xml
│     │  ├─ activity_login.xml
│     │  ├─ activity_signup.xml
│     │  ├─ activity_user_dashboard.xml
│     │  └─ activity_admin_dashboard.xml
│     ├─ drawable/
│     └─ values/
└─ build.gradle


⸻

Usage
	1.	Launch the app
	2.	Sign up as a user or log in as an admin
	3.	Users can submit and view service requests
	4.	Admins can view all requests and update their status
	5.	All data is stored locally in the app

⸻

Contributing
	1.	Fork the repository
	2.	Create a new branch (git checkout -b feature/YourFeature)
	3.	Make your changes and commit (git commit -m 'Add some feature')
	4.	Push to the branch (git push origin feature/YourFeature)
	5.	Open a Pull Request

⸻

License

This project is licensed under the MIT License – see the LICENSE file for details.
