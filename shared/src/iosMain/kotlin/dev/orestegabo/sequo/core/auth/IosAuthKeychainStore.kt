package dev.orestegabo.sequo.core.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

/** Checked device-only storage shared by the session and biometric refresh cache. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosAuthKeychainStore {
    fun save(account: String, value: String) {
        val data = checkNotNull(NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding)) {
            "Protected sign-in could not be saved."
        }
        val attributes = mapOf<Any?, Any?>(kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        fun update() = query(account).useCFDictionary { key -> attributes.useCFDictionary { SecItemUpdate(key, it) } }
        var status = update()
        if (status == errSecItemNotFound) {
            status = (query(account) + attributes).useCFDictionary { SecItemAdd(it, null) }
            if (status == errSecDuplicateItem) status = update()
        }
        check(status == errSecSuccess) { "Protected sign-in could not be saved. Unlock this device and retry." }
    }

    fun get(account: String): String? = memScoped {
        val result = alloc<CFTypeRefVar>(); result.value = null
        try {
            val status = (query(account) + mapOf<Any?, Any?>(kSecReturnData to true,
                kSecMatchLimit to kSecMatchLimitOne)).useCFDictionary { SecItemCopyMatching(it, result.ptr) }
            when (status) {
                errSecItemNotFound -> null
                errSecSuccess -> {
                    val raw = checkNotNull(result.value) { "Protected sign-in could not be read." }
                    result.value = null // CFBridgingRelease transfers ownership; never release twice.
                    val data = checkNotNull(CFBridgingRelease(raw) as? NSData) { "Protected sign-in could not be read." }
                    checkNotNull(NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()) {
                        "Protected sign-in could not be decoded."
                    }
                }
                else -> error("Protected sign-in is unavailable. Unlock this device and retry.")
            }
        } finally { result.value?.let { CFRelease(it) } }
    }

    fun clear(account: String) {
        val status = query(account).useCFDictionary { SecItemDelete(it) }
        check(status == errSecSuccess || status == errSecItemNotFound) { "Protected sign-in could not be cleared. Retry." }
    }

    private fun query(account: String): Map<Any?, Any?> = mapOf(kSecClass to kSecClassGenericPassword,
        kSecAttrService to "dev.orestegabo.sequo.auth", kSecAttrAccount to account)

}

@OptIn(ExperimentalForeignApi::class)
private inline fun <T> Map<Any?, Any?>.useCFDictionary(block: (CFDictionaryRef) -> T): T {
    val dictionary = toRetainedCFDictionary()
    return try {
        block(dictionary)
    } finally {
        CFRelease(dictionary)
    }
}

/** Preserve native CF constants; boxing their pointers into NSDictionary produces errSecParam. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun Map<Any?, Any?>.toRetainedCFDictionary(): CFDictionaryRef {
    val dictionary = checkNotNull(CFDictionaryCreateMutable(null, 0,
        kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr))
    try {
        forEach { (key, value) ->
            if (key != null && value != null) {
                val (nativeKey, releaseKey) = key.asCFValue()
                val (nativeValue, releaseValue) = value.asCFValue()
                try { CFDictionarySetValue(dictionary, nativeKey, nativeValue) }
                finally {
                    if (releaseKey) CFRelease(nativeKey)
                    if (releaseValue) CFRelease(nativeValue)
                }
            }
        }
        return dictionary
    } catch (error: Throwable) { CFRelease(dictionary); throw error }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun Any.asCFValue(): Pair<CFTypeRef?, Boolean> = when (this) {
    is CPointer<*> -> this to false
    is Boolean -> (if (this) kCFBooleanTrue else kCFBooleanFalse) to false
    else -> CFBridgingRetain(if (this is String) NSString.create(string = this) else this) to true
}

