# CamthProjects - Project Management Android App

## Overview

CamthProjects is a demonstration project management application for Android. It provides a simple, local-only interface for managing users, projects, and service requests. The app features separate dashboards for administrators and standard users, allowing for different levels of access and functionality.

All data is stored in-memory, meaning it is reset every time the application is closed. This makes it a lightweight and easy-to-run demonstration app without any external database or API dependencies.

## Features

*   **User and Admin Roles:** Simple role-based access control.
    *   Sign up as `lethabo.hlalele01@gmail.com` to gain admin privileges.
*   **Admin Dashboard:**
    *   View all users, projects, and service requests.
    *   Create, edit, and delete projects.
    *   Assign projects to users.
    *   Update project statuses.
    *   Approve or deny service requests submitted by users.
*   **User Dashboard:**
    *   View projects assigned to the logged-in user.
    *   Submit new service requests.
*   **Project Schedule:**
    *   A calendar view to visualize project start and end dates.
*   **Document Management:**
    *   Upload and view documents associated with a project (local storage only).

## How to Build and Run

This project is a standard Android Studio project and has no special requirements.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Tokoloho17/CamthProjects.git
    ```
2.  **Open in Android Studio:**
    *   Open Android Studio.
    *   Select "Open an Existing Project".
    *   Navigate to the cloned `CamthProjects` directory and open it.
3.  **Build the Project:**
    *   Allow Gradle to sync and download any necessary dependencies.
    *   Click on **Build -> Rebuild Project** from the top menu to ensure a clean build.
4.  **Run the App:**
    *   Select an emulator or a physical device from the dropdown menu in the toolbar.
    *   Click the **Run 'app'** button (the green play icon).

