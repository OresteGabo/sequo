package dev.orestegabo.sequo.ui.notifications

import dev.orestegabo.sequo.feature.settings.AppLanguage

internal data class CustomerInboxText(
    val title: String, val refresh: String, val loading: String, val error: String,
    val sessionError: String, val signIn: String, val empty: String, val emptyDetail: String,
    val active: String, val archived: String, val markRead: String, val archive: String,
    val restore: String, val unread: String, val saved: String, val orders: String,
)
internal fun customerInboxText(language: AppLanguage) = when (language) {
    AppLanguage.English -> CustomerInboxText(
        "Inbox", "Refresh", "Reading saved notifications…", "Notifications could not be confirmed. Refresh to read their current state.",
        "Your inbox session has expired. Sign in again to read your saved notifications.", "Sign in", "No notifications yet", "Your latest saved notifications will appear here.",
        "Inbox", "Archived", "Mark read", "Archive", "Restore", "unread", "Latest 100 saved notifications", "View orders",
    )
    AppLanguage.French -> CustomerInboxText(
        "Notifications", "Actualiser", "Lecture des notifications enregistrées…", "Les notifications n’ont pas pu être confirmées. Actualisez pour lire leur état actuel.",
        "Votre session a expiré. Reconnectez-vous pour lire vos notifications enregistrées.", "Se connecter", "Aucune notification", "Vos dernières notifications enregistrées apparaîtront ici.",
        "Boîte de réception", "Archivées", "Marquer comme lu", "Archiver", "Restaurer", "non lues", "100 dernières notifications enregistrées", "Voir les commandes",
    )
}
