import UIKit
import SwiftUI
import Shared
import UserNotifications
import GoogleSignIn
import AuthenticationServices

private let googleIosClientId = "92804430098-q92ufo08vjdsu285cek8993os6tgv2gl.apps.googleusercontent.com"

final class SequoNotificationPresenter: NSObject, UNUserNotificationCenterDelegate {
    static let shared = SequoNotificationPresenter()
    private let homeWelcomeNotificationShownKey = "home_welcome_notification_shown"
    var onOpenNotifications: (() -> Void)?

    private override init() {
        super.init()
    }

    func notifyHomeReached(language: AppLanguage) {
        guard !UserDefaults.standard.bool(forKey: homeWelcomeNotificationShownKey) else { return }
        UserDefaults.standard.set(true, forKey: homeWelcomeNotificationShownKey)

        let center = UNUserNotificationCenter.current()
        center.delegate = self
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }

            let content = UNMutableNotificationContent()
            let french = language == AppLanguage.french
            content.title = french ? "Bienvenue sur Sequo" : "Welcome to Sequo"
            content.body = french
                ? "Vous pouvez parcourir les produits en invite. Connectez-vous quand vous voulez enregistrer, commander ou suivre."
                : "You can browse products as a guest. Sign in when you are ready to save, order, or track."
            content.sound = .default

            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
            let request = UNNotificationRequest(
                identifier: "sequo-home-welcome",
                content: content,
                trigger: trigger
            )
            center.add(request)
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .sound, .list])
        } else {
            completionHandler([.alert, .sound])
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        onOpenNotifications?()
        completionHandler()
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let openNotificationsRequest: Int
    let onOpenNotifications: () -> Void

    func makeUIViewController(context: Self.Context) -> UIViewController {
        SequoNotificationPresenter.shared.onOpenNotifications = onOpenNotifications
        return MainViewControllerKt.MainViewController(
            onGoogleSignIn: { complete in
                GoogleSignInPresenter.signIn { result in
                    _ = complete(result)
                }
            },
            onAppleSignIn: { nonce, complete in
                AppleSignInPresenter.shared.signIn(nonce: nonce) { result in
                    _ = complete(result)
                }
            },
            onGoogleSignOut: {
                GoogleSignInPresenter.signOut()
            },
            onHomeEntered: { language in
                SequoNotificationPresenter.shared.notifyHomeReached(language: language)
            },
            openNotificationsRequest: Int32(openNotificationsRequest)
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {
        SequoNotificationPresenter.shared.onOpenNotifications = onOpenNotifications
    }
}

private final class AppleSignInPresenter: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {
    static let shared = AppleSignInPresenter()
    private var completion: ((any AppleSignInResult) -> Void)?
    private var controller: ASAuthorizationController?
    private var anchor: UIWindow?
    private var expectedState: String?

    func signIn(nonce: String, complete: @escaping (any AppleSignInResult) -> Void) {
        guard completion == nil,
              let window = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .filter({ $0.activationState == .foregroundActive })
                .flatMap({ $0.windows }).first(where: { $0.isKeyWindow }) else {
            complete(AppleSignInResultUnavailable.shared)
            return
        }
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.email]
        request.nonce = nonce
        let state = UUID().uuidString
        request.state = state
        expectedState = state
        completion = complete
        anchor = window
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        self.controller = controller
        controller.performRequests()
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        // Retained for the lifetime of the authorization request.
        return anchor!
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              credential.state == expectedState,
              let data = credential.identityToken,
              let token = String(data: data, encoding: .utf8), !token.isEmpty else {
            finish(AppleSignInResultFailed.shared)
            return
        }
        finish(AppleSignInResultSuccess(idToken: token))
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let cancelled = (error as? ASAuthorizationError)?.code == .canceled
        finish(cancelled ? AppleSignInResultCancelled.shared : AppleSignInResultFailed.shared)
    }

    private func finish(_ result: any AppleSignInResult) {
        let callback = completion
        completion = nil
        controller = nil
        anchor = nil
        expectedState = nil
        callback?(result)
    }
}

private enum GoogleSignInPresenter {
    static func signOut() {
        GIDSignIn.sharedInstance.signOut()
    }

    static func signIn(complete: @escaping (any GoogleSignInResult) -> Void) {
        guard let presenter = topViewController() else {
            complete(GoogleSignInResultFailure(reason: GoogleSignInFailureReason.unavailable))
            return
        }

        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: googleIosClientId)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as NSError? {
                if error.domain == kGIDSignInErrorDomain && error.code == GIDSignInError.canceled.rawValue {
                    complete(GoogleSignInResultCancelled.shared)
                } else {
                    complete(GoogleSignInResultFailure(reason: GoogleSignInFailureReason.failed))
                }
                return
            }

            guard let user = result?.user, let idToken = user.idToken?.tokenString, !idToken.isEmpty else {
                complete(GoogleSignInResultFailure(reason: GoogleSignInFailureReason.unavailable))
                return
            }

            complete(
                GoogleSignInResultSuccess(
                    idToken: idToken,
                    displayName: user.profile?.name,
                    email: user.profile?.email,
                    profilePictureUri: user.profile?.imageURL(withDimension: 160)?.absoluteString
                )
            )
        }
    }

    private static func topViewController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let root = scenes
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .rootViewController
        return topViewController(from: root)
    }

    private static func topViewController(from root: UIViewController?) -> UIViewController? {
        if let navigation = root as? UINavigationController {
            return topViewController(from: navigation.visibleViewController)
        }
        if let tab = root as? UITabBarController {
            return topViewController(from: tab.selectedViewController)
        }
        if let presented = root?.presentedViewController {
            return topViewController(from: presented)
        }
        return root
    }
}

struct ContentView: View {
    @State private var openNotificationsRequest = 0

    var body: some View {
        ComposeView(
            openNotificationsRequest: openNotificationsRequest,
            onOpenNotifications: {
                openNotificationsRequest += 1
            }
        )
            .id(openNotificationsRequest)
            .ignoresSafeArea()
    }
}
