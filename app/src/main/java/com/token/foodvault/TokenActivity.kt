package com.token.foodvault

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TokenActivity : AppCompatActivity() {
    override fun onBackPressed() {

    }

    lateinit var time: TextView
    var countdown = 14

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_token)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val generated = findViewById<TextView>(R.id.generated)
        val token_id = findViewById<TextView>(R.id.token_id)
        time = findViewById<TextView>(R.id.time)

        val dateFormat =
            SimpleDateFormat("hh:mm a | dd-MM-yyyy", Locale.getDefault()).format(Date()).uppercase()

        generated.text = dateFormat

        token_id.text = UUID.randomUUID().toString().substring(0, 8).uppercase()

        val media_player : MediaPlayer = MediaPlayer.create(this, R.raw.token_gen)
        media_player.start()

        media_player.setOnCompletionListener {
            it.release()
        }

        startCountdown()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startCountdown() {
        // Post a delayed update every 1000 milliseconds (1 second)
        time.postDelayed(object : Runnable {
            override fun run() {
                // Update the TextView with the current countdown value
                time.text = "Token expires in: " + countdown.toString()
                countdown--

                // If the countdown is not finished, continue the countdown
                if (countdown >= 0) {
                    time.postDelayed(this, 1000)
                }

                else {
                    finish()
                }
            }
        }, 1000)
    }
}