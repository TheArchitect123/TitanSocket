package com.architect.titansocket.native

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.darwin.NSObject
import platform.Security.SecTrustRef
import platform.posix.memcpy
import kotlin.concurrent.Volatile

class SocketEvents {
    private var onOpen: ((Unit) -> Unit)? = null
    private var onClose: ((code: Int, reason: String?, clean: Boolean) -> Unit)? = null
    private var onError: ((NSError?) -> Unit)? = null
    private var onMessage: ((Any) -> Unit)? = null

    fun setOpen(block: (Unit) -> Unit) {
        onOpen = block
    }

    fun setClose(block: (Int, String?, Boolean) -> Unit) {
        onClose = block
    }

    fun setError(block: (NSError?) -> Unit) {
        onError = block
    }

    fun setMessage(block: (Any) -> Unit) {
        onMessage = block
    }

    internal fun fireOpen() = onOpen?.invoke(Unit)
    internal fun fireClose(code: Int, reason: String?, clean: Boolean) =
        onClose?.invoke(code, reason, clean)

    internal fun fireError(err: NSError?) = onError?.invoke(err)
    internal fun fireText(s: String) = onMessage?.invoke(s)
    internal fun fireBinary(b: ByteArray) = onMessage?.invoke(b)
}

class NativeWebSocket(
    private val urlString: String
) {
    private val events = SocketEvents()

    private var session: NSURLSession? = null
    private var task: NSURLSessionWebSocketTask? = null

    var allowSelfSignedSSL: Boolean = true

    fun event(): SocketEvents = events

    fun setAllowSelfSignedSSL(allow: Boolean) {
        allowSelfSignedSSL = allow
    }

    @Volatile
    private var isOpen = false
    fun isConnected(): Boolean = isOpen

    // ---- Open connection ----
    fun open(endpoint: String = urlString) {
        val url = NSURL.URLWithString(endpoint) ?: run {
            events.fireError(NSError.errorWithDomain("NativeWebSocket", -1, null))
            return
        }

        val cfg = NSURLSessionConfiguration.defaultSessionConfiguration()

        // Delegate handles trust-all and close/error callbacks
        val delegate = TrustAllDelegate(allowSelfSignedSSL, events)
        session = NSURLSession.sessionWithConfiguration(cfg, delegate, null)
        task = session?.webSocketTaskWithURL(url)

        //send a ping to confirm the connection; when pong arrives, fire open.
        task?.sendPingWithPongReceiveHandler { error ->
            if (error == null) {
                // Mark open (if not already) and notify
                markOpen()

                // Start receive loop first so initial frames are not lost
                startReceiveLoop()
            } else {
                events.fireError(error)
            }
        }

        task?.resume()
    }

    // ---- Send text ----
    fun send(text: String) {
        val t = task ?: return
        val message = NSURLSessionWebSocketMessage(text)
        t.sendMessage(message) { error ->
            if (error != null) events.fireError(error)
        }
    }

    // ---- Send binary ----
    fun send(bytes: ByteArray) {
        val t = task ?: return
        val data = bytes.useNSData()
        val message = NSURLSessionWebSocketMessage(data)
        t.sendMessage(message) { error ->
            if (error != null) events.fireError(error)
        }
    }

    // ---- Close connection ----
    fun close(
        code: Int = NSURLSessionWebSocketCloseCodeNormalClosure.toInt(),
        reason: String? = "Client close"
    ) {
        val t = task ?: return
        val reasonData = reason?.encodeToNSData()
        t.cancelWithCloseCode(code.toLong(), reasonData)

        // NSURLSession will call didCompleteWithError or we get no more messages; emit a close event proactively:
        isOpen = false
        events.fireClose(code, reason, true)
    }

    // ---- Internals ----
    private fun startReceiveLoop() {
        val t = task ?: return
        t.receiveMessageWithCompletionHandler { msg, err ->
            when {
                err != null -> {
                    isOpen = false
                    events.fireError(err)
                    // emit close with unknown code if we can’t map an error
                    events.fireClose(
                        NSURLSessionWebSocketCloseCodeAbnormalClosure.toInt(),
                        err.localizedDescription,
                        false
                    )
                }

                msg != null -> {
                    val text = msg.string
                    val data = msg.data
                    when {
                        text != null -> events.fireText(text)
                        data != null -> events.fireBinary(data.toByteArray())
                    }
                    // continue loop
                    startReceiveLoop()
                }

                else -> {
                    // no message and no error -> treat as closed
                    isOpen = false
                    events.fireClose(NSURLSessionWebSocketCloseCodeGoingAway.toInt(), "EOF", false)
                }
            }
        }
    }

    private fun markOpen() {
        if (!isOpen) {
            isOpen = true
            events.fireOpen()
        }
    }
}

/** NSURLSession delegate that trusts all certs (dev) and forwards lifecycle to events. */
@OptIn(ExperimentalForeignApi::class)
private class TrustAllDelegate(
    private val trustAll: Boolean,
    private val events: SocketEvents,
) : NSObject(), NSURLSessionDelegateProtocol, NSURLSessionTaskDelegateProtocol {

    // TLS trust override
    override fun URLSession(
        session: NSURLSession,
        didReceiveChallenge: NSURLAuthenticationChallenge,
        completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
    ) {
        if (trustAll) {
            val trust = didReceiveChallenge.protectionSpace.serverTrust
            completionHandler(NSURLSessionAuthChallengeUseCredential, NSURLCredential.credentialForTrust(trust))
        } else {
            completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
        }
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?
    ) {
        if (didCompleteWithError != null) {
            events.fireError(didCompleteWithError)
            events.fireClose(
                NSURLSessionWebSocketCloseCodeAbnormalClosure.toInt(),
                didCompleteWithError.localizedDescription,
                false
            )
        } else {
            events.fireClose(NSURLSessionWebSocketCloseCodeNormalClosure.toInt(), "Closed", true)
        }
    }
}

// ---- Small utilities ----
private fun String.encodeToNSData(): NSData =
    (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.useNSData(): NSData = memScoped {
    val cArray = allocArrayOf(this@useNSData)
    NSData.create(bytes = cArray, length = this@useNSData.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    out.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    return out
}
