package com.example.videoapp

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private var captured: String? = null
    private var referer: String = ""
    private var player: ExoPlayer? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_main)
        val web = findViewById<WebView>(R.id.web)
        val url = findViewById<EditText>(R.id.url)
        val play = findViewById<Button>(R.id.play)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest): WebResourceResponse? {
                val u = r.url.toString()
                if (Regex("\\.(m3u8|mp4|mpd)(\\?|$)").containsMatchIn(u)) {
                    captured = u
                    referer = v.url ?: ""
                    runOnUiThread { play.isEnabled = true }
                }
                return null
            }
        }

        url.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_GO) {
                var t = url.text.toString().trim()
                if (!t.startsWith("http")) t = "https://$t"
                captured = null; play.isEnabled = false
                web.loadUrl(t); true
            } else false
        }

        play.setOnClickListener { captured?.let { startPlayer(it) } }
    }

    private fun startPlayer(link: String) {
        val cookies = CookieManager.getInstance().getCookie(link) ?: ""
        val ds = DefaultHttpDataSource.Factory()
            .setUserAgent(WebSettings.getDefaultUserAgent(this))
            .setDefaultRequestProperties(mapOf("Referer" to referer, "Cookie" to cookies))
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(ds)).build()
        val pv = PlayerView(this).also { it.player = player }
        setContentView(pv)
        player!!.setMediaItem(MediaItem.fromUri(Uri.parse(link)))
        player!!.prepare(); player!!.play()
    }

    override fun onDestroy() { player?.release(); super.onDestroy() }
}