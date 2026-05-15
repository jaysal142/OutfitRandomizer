[README.md](https://github.com/user-attachments/files/27818802/README.md)
# Closet Genie — Comprehensive Code Reference

## Project Overview

Closet Genie is an Android wardrobe application built in Java with Android Studio. Users photograph clothing items using the in-app camera, which automatically removes the background using ML Kit's Selfie Segmenter, then crops the result with uCrop. Items are saved to a local Room database, categorized by type, and used to generate randomized outfits. Individual items in a generated outfit can be locked in place before re-randomizing. Outfits can be named, saved, favorited, and browsed. User accounts are authenticated against Firebase Realtime Database, with session state persisted locally via SharedPreferences and Room.

Built as the final project for App Development, Fall 2025.

---

## File Structure

```
OutfitRandomizer/
    app/
        src/main/
            java/com/codeblooded/outfitrandomizer/
                MainActivity.java           — Splash screen with logo animations
                Login.java                  — Firebase auth login
                SignUp.java                 — Firebase user registration
                HomePage.java               — Home screen: favorites feed + search
                Wardrobe.java               — Wardrobe item list with category filters
                WardrobeDetails.java        — Item detail: rename, recategorize, retake photo, delete
                AddItem.java                — Add item form: camera, name, category spinner
                Camera.java                 — CameraX capture → uCrop → ML Kit BG removal
                Generator.java              — Outfit randomizer with per-category lock toggles
                Outfits.java                — All saved outfits list with search
                OutfitDetails.java          — Outfit detail: favorite, rename, delete
                UserProfile.java            — Profile: username, email, outfit/favorite counts, logout, clear storage

                data/local/
                    AppDatabase.java        — Room singleton (version 5), hosts all 3 DAOs
                    WardrobeEntity.java     — Room entity: wardrobe_items table
                    WardrobeDAO.java        — Wardrobe queries: insert, update, delete, getAll, getByCategory, getRandomByCategory
                    OutfitEntity.java       — Room entity: outfits table
                    OutfitDao.java          — Outfit queries: insert, upsert, delete, getAllOutfits (LiveData), getFavoriteOutfits (LiveData), countAll, countFavorites
                    OutfitRepository.java   — Repository wrapping OutfitDao for ViewModel access
                    UserEntity.java         — Room entity: users table (local cache)
                    UserDao.java            — User queries: upsert, getUser, clearAll

                ui/
                    WardrobeAdapter.java    — RecyclerView adapter for wardrobe items, uses Glide for image loading
                    OutfitCardAdapter.java  — ListAdapter for outfit cards, DiffUtil-backed, uses Glide
                    OutfitViewModel.java    — AndroidViewModel exposing OutfitRepository LiveData

                util/
                    BGRemover.java          — ML Kit Selfie Segmenter wrapper: processes Bitmap, returns ARGB_8888 cutout
                    AppExecutors.java       — Single-thread IO executor singleton
                    UserHelperClass.java    — POJO for Firebase user node serialization

            res/
                layout/                     — XML layouts for each activity + wardrobe_card, outfit_card
                anim/                       — top_animation.xml, bottom_animation.xml (splash transitions)
                drawable/                   — Icons, button backgrounds, lock/favorite state drawables
                drawable-night/             — Dark mode logo variants
                font/                       — Poppins variants, Bungee, Bangers, Anton, Synthemesc
                menu/bottom_nav_menu.xml    — 5-tab bottom navigation (Home, Wardrobe, Generator, Outfits, Profile)
                values/                     — colors, strings, themes, styles
                values-night/               — Dark mode color/theme overrides
                xml/file_paths.xml          — FileProvider paths for internal camera output

        google-services.json                — Firebase project config
        build.gradle.kts                    — App-level Gradle build

    gradle/libs.versions.toml              — Version catalog for all dependencies
    settings.gradle.kts
```

---

## Activities

### MainActivity
Animated splash screen. Displays logo and taglines with `top_animation` / `bottom_animation`. After a 5-second delay, transitions to `Login` using shared-element animation (logo + header).

### Login
Authenticates against Firebase Realtime Database (`users` node). On success, caches the user locally via `UserDao.upsert()` and writes `current_username` to `SharedPreferences("session")`. If a session already exists on launch, skips directly to `HomePage`.

### SignUp
Registers a new user. Validates username (no spaces, max 15 chars), phone number, email format, and password (min 6 chars, no spaces, confirmation match). Writes the user as a `UserHelperClass` POJO to Firebase under `users/{username}`.

### HomePage
Displays the logged-in username and a `RecyclerView` of favorited outfits (observed via `OutfitDao.getFavoriteOutfits()` LiveData). Supports live search filtering by outfit name. Tapping a card navigates to `OutfitDetails`. Bottom navigation links to all main sections.

### Wardrobe
Lists all wardrobe items from Room via `WardrobeDAO`. Category filter buttons (All / Jacket / Shirt / Pants / Shoes) reload the list using `getAll()` or `getByCategory()`. Tapping an item opens `WardrobeDetails`. Add button opens `AddItem`. Reloads on `onResume`.

### WardrobeDetails
Detail view for a single `WardrobeEntity`. Edit dialog offers three actions:
- **Rename** — inline `AlertDialog` with `EditText`
- **Change Category** — single-choice `AlertDialog` (Jacket / Shirt / Pants / Shoes)
- **Retake / Recrop Image** — re-launches `Camera` via `startActivityForResult`, replaces `imageUri` on return

Delete triggers `WardrobeDAO.delete()` and attempts to clean up the stored image file.

### AddItem
Form screen: opens `Camera` via `ActivityResultLauncher`, previews the returned image URI, collects item name and category from a spinner. On save, inserts a `WardrobeEntity` into Room on a background thread.

### Camera
Full in-app camera using **CameraX**:
1. Captures photo via `ImageCapture.OnImageCapturedCallback`, converts `ImageProxy` to `Bitmap` with correct rotation
2. Saves raw capture to internal storage, creates a `FileProvider` URI
3. Launches **uCrop** for crop/resize (max 1080×1080)
4. On crop result, passes the cropped `Bitmap` to `BGRemover`
5. On BG removal success, saves the ARGB_8888 cutout PNG to internal storage and returns the `FileProvider` URI to the calling activity via `setResult(RESULT_OK)`

### Generator
Core feature. Displays one slot per category (Jacket, Shirt, Pants, Shoes) each with a lock toggle button. On randomize:
- Unlocked slots: `WardrobeDAO.getRandomByCategory()` — SQL `ORDER BY RANDOM() LIMIT 1`
- Locked slots: retain current selection

Outfit name defaults to `jacket.name / shirt.name / pants.name / shoes.name` if left blank. A save overlay panel collects an optional name before inserting as an `OutfitEntity` into Room.

### Outfits
Full list of all saved outfits via `OutfitViewModel` → `OutfitRepository` → `OutfitDao.getAllOutfits()` LiveData. Live search filters by name. Tapping opens `OutfitDetails`.

### OutfitDetails
Shows all four clothing item images, outfit name, and formatted save date. Actions:
- **Favorite toggle** — flips `isFavorite`, updates via `OutfitDao.upsert()`
- **Rename** — `AlertDialog` with `EditText`, updates via `OutfitDao.upsert()`
- **Delete** — confirmation dialog, `OutfitDao.delete()`, then `finish()`

### UserProfile
Shows username and email (from local Room `UserEntity`), plus live counts of total outfits and favorites (`OutfitDao.countAll()`, `OutfitDao.countFavorites()`). Clear Storage deletes all wardrobe items and outfits from Room. Logout clears `SharedPreferences("session")` and navigates to `Login` with `FLAG_ACTIVITY_CLEAR_TASK`.

---

## Data Layer

### Room Database — `AppDatabase`
Single `@Database` instance (version 5), singleton via double-checked locking.

```
outfit_db (SQLite)
├── wardrobe_items    — WardrobeEntity
├── outfits           — OutfitEntity
└── users             — UserEntity (local session cache)
```

Migration 4→5 adds `isFavorite INTEGER NOT NULL DEFAULT 0` to the `outfits` table.

### Entities

**WardrobeEntity** (`wardrobe_items`)
| Field | Type | Description |
|---|---|---|
| `id` | int (autoGenerate) | Primary key |
| `name` | String | Item name |
| `category` | String | Jacket / Shirt / Pants / Shoes |
| `imageUri` | String | FileProvider URI string for background-removed PNG |

**OutfitEntity** (`outfits`)
| Field | Type | Description |
|---|---|---|
| `id` | long (autoGenerate) | Primary key |
| `name` | String | Outfit name |
| `jacketImageUri` | String | URI for jacket image |
| `shirtImageUri` | String | URI for shirt image |
| `pantsImageUri` | String | URI for pants image |
| `shoesImageUri` | String | URI for shoes image |
| `createdAt` | long | Unix timestamp (ms) |
| `isFavorite` | boolean | Favorited flag (added in migration 4→5) |

**UserEntity** (`users`)
| Field | Type | Description |
|---|---|---|
| `username` | String (PK) | Firebase username, used as local cache key |
| `phoneNo` | String | Phone number |
| `email` | String | Email address |

### Firebase Realtime Database
Used exclusively for user account storage and login lookup. Schema under `users/{username}`:
```json
{
  "username": "string",
  "phoneNo": "string",
  "email": "string",
  "password": "string"
}
```
Login is a manual `Query` against the `users` node — no Firebase Auth SDK is used. Passwords are stored in plaintext.

---

## Key Utilities

### BGRemover
Wraps **ML Kit Selfie Segmentation** (`SINGLE_IMAGE_MODE`). Runs the segmenter on a `Bitmap`, reads the `SegmentationMask` float buffer, and computes per-pixel alpha from the foreground probability. Returns a transparent-background `ARGB_8888` `Bitmap`.

```java
// Alpha assignment per pixel
float foregroundProb = buffer.getFloat();
int alpha = (int) (foregroundProb * 255);
outPixels[i] = (alpha << 24) | (srcPixels[i] & 0x00FFFFFF);
```

### WardrobeAdapter
`RecyclerView.Adapter` for wardrobe item cards. Uses **Glide** with `centerCrop()` to load images from FileProvider URIs. Item click starts `WardrobeDetails` with the `WardrobeEntity` as a `Serializable` extra.

### OutfitCardAdapter
`ListAdapter<OutfitEntity>` with a `DiffUtil.ItemCallback` for efficient diff updates. Uses **Glide** with `fitCenter()` for outfit images. Exposes an `OnItemClick` callback interface.

### OutfitViewModel / OutfitRepository
Standard MVVM chain: `OutfitViewModel` (AndroidViewModel) → `OutfitRepository` → `OutfitDao`. Exposes `LiveData<List<OutfitEntity>>` from `getAllOutfits()`. Repository uses a single-thread `ExecutorService` for insert/update/delete operations.

---

## Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| Room | 2.6.1 | Local SQLite ORM |
| Firebase Realtime Database | 22.0.1 | User account storage and login |
| CameraX (camera2, lifecycle, view, extensions) | 1.5.1 | In-app camera capture |
| ML Kit Selfie Segmentation | 16.0.0-beta3 | Automatic background removal |
| uCrop | 2.2.11 | Image crop and resize |
| Glide | 4.16.0 | Image loading in RecyclerViews |
| Material Components | 1.13.0 | Bottom navigation, TextInputLayout |
| Lifecycle ViewModel + LiveData | 2.8.6 | MVVM architecture for outfit list |

---

## Setup

1. Clone the repo and open in Android Studio (Hedgehog or later)
2. Add your own `google-services.json` to `app/` (requires a Firebase project with Realtime Database enabled)
3. In the Firebase Console, configure Realtime Database rules to allow read/write
4. Let Gradle sync — all dependencies resolve from Maven Central and JitPack
5. Connect an Android device (API 26+) or launch an emulator with camera support
6. Run via the Play button — no additional config required

---

## Requirements

| Requirement | Value |
|---|---|
| Android Studio | Hedgehog or later |
| Min SDK | API 26 (Android 8.0) |
| Target / Compile SDK | API 36 |
| Language | Java 11 |
| Build System | Gradle 8 (Kotlin DSL) |

---

## Permissions

| Permission | Reason |
|---|---|
| `CAMERA` | CameraX photo capture |

All images are served through `FileProvider` (`${applicationId}.provider`) from the app's internal files directory. No external storage permissions are needed.
