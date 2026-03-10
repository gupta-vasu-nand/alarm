package com.vng.alarm.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class RingtonePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playRingtone(uriString: String?) {
        stopPlayback()

        if (uriString.isNullOrEmpty()) return

        try {
            val uri = Uri.parse(uriString)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setVolume(0.5f, 0.5f) // Lower volume for preview
                isLooping = false
                prepare()
                start()

                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}