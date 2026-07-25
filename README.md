# 🦉 Night Owl Voice

A real-time mic voice-changer for Android with a small floating bubble control.
Tap the bubble to open a panel with 60+ voice presets, tap any voice to hear a
live preview, flip the switch to keep it running, and use **Listen** to hear
exactly what your mic is outputting.

## What it does

- Captures your microphone in real time
- Applies a pitch/robot/echo/radio/distortion/reverb/whisper effect
- Plays the processed voice back out your speaker/earpiece — so it works with
  any app that picks your mic up (games, calls, etc.) without needing special
  access to that app
- Runs as a small floating round button (drag it anywhere) that expands into
  a control panel and collapses back down with the ✕ button

## Permissions this app uses (and why)

- **Microphone** — required to hear and process your voice
- **Display over other apps** — lets the floating bubble/control panel show
  up while you're inside another app
- A **persistent notification** is shown any time the voice changer is
  actively running, so it's never processing your mic silently in the
  background without you knowing.

## Getting it onto GitHub and building the APK

1. Create a new empty repo on GitHub.
2. Push this whole folder to it:
   ```bash
   git init
   git add .
   git commit -m "Night Owl Voice"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```
3. On push, the workflow in `.github/workflows/build.yml` automatically runs
   and builds a debug APK.
4. Go to the **Actions** tab on your repo → click the latest run → download
   the `NightOwlVoice-debug-apk` artifact (it's a zip containing
   `app-debug.apk`).
5. Copy that APK to your phone and install it (you'll need to allow installs
   from unknown sources for the app you use to open it).

## Using the app

1. Open the app, tap **Allow Microphone**, then **Allow Floating Bubble**
   (this opens Android's overlay settings — enable it there and go back).
2. Tap **Start Night Owl Bubble** — the app minimizes and a round 🦉 bubble
   appears on screen.
3. Drag the bubble anywhere. Tap it to open the panel.
4. Tap any voice in the list to hear a quick live preview.
5. Flip **Voice changer: ON** to keep that effect running continuously.
6. Tap **Listen** any time to check what your processed voice sounds like.
7. Tap **✕** to collapse the panel back to just the bubble.

## Notes on the DSP

The effects (pitch shift, ring-modulation "robot", delay-based echo, a cheap
band-pass "radio" filter, clipping distortion, comb-filter reverb, and a
noise-mixed whisper) are lightweight, low-latency implementations meant for
real-time use on a phone — not studio-quality. The 60+ presets are built by
running these same effect engines with different parameters (e.g. "Chipmunk"
vs. "Extreme Helium" are both the pitch effect at different speeds).
