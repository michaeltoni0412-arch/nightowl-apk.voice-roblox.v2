package com.nightowl.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var panelView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null

    private val audioEngine = AudioEngine()
    private val handler = Handler(Looper.getMainLooper())
    private var previewRunnable: Runnable? = null

    private val channelId = "night_owl_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(notificationId, buildNotification())
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
        bubbleView?.let { runCatching { windowManager.removeView(it) } }
        panelView?.let { runCatching { windowManager.removeView(it) } }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Night Owl Voice", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Night Owl Voice is active")
            .setContentText("Mic voice-changer bubble is running")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun showBubble() {
        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.overlay_bubble, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 200
        }

        var lastTouchX = 0f
        var lastTouchY = 0f
        var lastX = 0
        var lastY = 0
        var isDrag = false

        bubbleView?.setOnTouchListener { v, event ->
            val params = bubbleParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    lastX = params.x
                    lastY = params.y
                    isDrag = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - lastTouchX).toInt()
                    val dy = (event.rawY - lastTouchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) isDrag = true
                    params.x = lastX + dx
                    params.y = lastY + dy
                    windowManager.updateViewLayout(bubbleView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDrag) toggleBubbleAndPanel()
                    true
                }
                else -> false
            }
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun toggleBubbleAndPanel() {
        bubbleView?.visibility = View.GONE
        if (panelView == null) showPanel() else panelView?.visibility = View.VISIBLE
    }

    private fun showPanel() {
        val inflater = LayoutInflater.from(this)
        panelView = inflater.inflate(R.layout.overlay_panel, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val closeButton: TextView = panelView!!.findViewById(R.id.closeButton)
        val onOffSwitch: Switch = panelView!!.findViewById(R.id.onOffSwitch)
        val onOffLabel: TextView = panelView!!.findViewById(R.id.onOffLabel)
        val listenButton: Button = panelView!!.findViewById(R.id.listenButton)
        val voiceList: RecyclerView = panelView!!.findViewById(R.id.voiceList)

        voiceList.layoutManager = LinearLayoutManager(this)
        voiceList.adapter = VoiceAdapter(VoiceLibrary.presets) { preset ->
            audioEngine.currentPreset = preset
            previewVoice()
        }

        onOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                audioEngine.start()
                onOffLabel.text = "Voice changer: ON"
            } else {
                audioEngine.stop()
                onOffLabel.text = "Voice changer: OFF"
            }
        }

        listenButton.setOnClickListener { previewVoice(forceStartStop = true) }

        closeButton.setOnClickListener {
            panelView?.visibility = View.GONE
            bubbleView?.visibility = View.VISIBLE
        }

        windowManager.addView(panelView, panelParams)
    }

    private fun previewVoice(forceStartStop: Boolean = false) {
        val wasRunning = audioEngine.isRunning()
        if (!wasRunning) audioEngine.start()

        previewRunnable?.let { handler.removeCallbacks(it) }
        if (!wasRunning || forceStartStop) {
            previewRunnable = Runnable {
                if (!wasRunning) audioEngine.stop()
            }
            handler.postDelayed(previewRunnable!!, 2500)
        }
    }
}
