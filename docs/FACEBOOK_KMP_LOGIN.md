# Facebook Login For The KMP App

Sequo is a Kotlin Multiplatform mobile app. Facebook Login should be implemented with the native Facebook SDKs on Android and iOS, then handed to shared KMP code as a Facebook user access token.

Do not use Facebook's web JavaScript SDK, XFBML Login Button, or web-only callback examples in this app.

## Shared KMP Contract

After native Facebook Login succeeds, call:

```kotlin
authRepository.loginWithFacebook(accessToken)
```

The shared client posts to:

```http
POST /api/auth/social/FACEBOOK
```

with:

```json
{
  "token": "<facebook-user-access-token>",
  "device": {
    "deviceId": "<stable-per-install-id>",
    "appSource": "SEQUO_APP"
  }
}
```

The app already keeps a stable per-install device id in shared auth code:

- Android: `SharedPreferences` key `auth_device_id`
- iOS: `NSUserDefaults` key `auth_device_id`

## Android Native Setup

Use the Facebook Android Login SDK in the Android app layer. Request only the permissions Sequo needs:

```kotlin
LoginManager.getInstance().logInWithReadPermissions(
    activityOrFragment,
    listOf("public_profile", "email")
)
```

`email` is optional from Facebook's side, so UI and backend code must still work when it is missing.

Android setup checklist:

- Add dependency: `com.facebook.android:facebook-login`.
- Add string resources:
  - `facebook_app_id`
  - `fb_login_protocol_scheme` as `fb<facebook_app_id>`
  - `facebook_client_token`
- Add manifest metadata:
  - `com.facebook.sdk.ApplicationId`
  - `com.facebook.sdk.ClientToken`
- Add `FacebookActivity`.
- Add `CustomTabActivity` with the Facebook protocol scheme.
- Add `android.permission.INTERNET`.
- Add package name, default activity, debug key hash, and release key hash in the Facebook developer dashboard.

Do not put the Facebook app secret or backend app access token in the Android app.

## iOS Native Setup

Use the Facebook iOS SDK in the iOS app layer. A custom Compose button can trigger native login through a small iOS bridge that returns the access token string to shared code.

Recommended initial permissions:

```swift
["public_profile", "email"]
```

iOS setup checklist:

- Add the Facebook iOS SDK to the iOS target.
- Configure the Facebook app id/client token in `Info.plist` according to the SDK setup.
- Add URL schemes required by the SDK.
- Add `fbauth2` to `LSApplicationQueriesSchemes` so classic Facebook Login can fast-switch to the Facebook app when available.
- Use `LoginManager` for the custom Sequo button flow.

Fast App Switch applies only to classic Facebook Login. Limited Login uses the browser path.

## Permissions And Review

Keep the login permission set small. `public_profile` and `email` are the expected first-pass permissions for Sequo.

If future features need more profile data, request the extra permission at the moment that feature needs it, not during initial sign-in. Permissions beyond `public_profile` and `email` may require Facebook Login Review.

Do not request publish permissions during sign-in.

## Current Implementation State

Shared KMP now has:

- `AuthApiClient.loginWithFacebook(accessToken)`
- `AuthRepository.loginWithFacebook(accessToken)`
- Backend-compatible `/api/auth/social/FACEBOOK` request shape
- Stable device id handoff for auth sessions

Still needed:

- Android native Facebook SDK login bridge that returns an access token.
- iOS native Facebook SDK login bridge that returns an access token.
- Button handler update from the current placeholder message to the native bridge plus `authRepository.loginWithFacebook(...)`.
