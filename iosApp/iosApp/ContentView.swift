import UIKit
import SwiftUI
import Shared
import UserNotifications

final class SequoNotificationPresenter: NSObject, UNUserNotificationCenterDelegate {
    static let shared = SequoNotificationPresenter()
    private var homeNotificationShown = false
    var onOpenNotifications: (() -> Void)?

    private override init() {
        super.init()
    }

    func notifyHomeReached() {
        guard !homeNotificationShown else { return }
        homeNotificationShown = true

        let center = UNUserNotificationCenter.current()
        center.delegate = self
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }

            let content = UNMutableNotificationContent()
            content.title = "Welcome to Sequo"
            content.body = "You can browse products as a guest. Sign in when you are ready to save, order, or track."
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
            onHomeEntered: {
                SequoNotificationPresenter.shared.notifyHomeReached()
            },
            openNotificationsRequest: Int32(openNotificationsRequest)
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {
        SequoNotificationPresenter.shared.onOpenNotifications = onOpenNotifications
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
