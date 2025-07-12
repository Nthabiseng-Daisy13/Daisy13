package com.example.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewTimer;
    private long startMillis;
    private long sessionStartMillis;


    private ObjectAnimator progressAnimator;
    private boolean isPaused = false;
    private ProgressBar circleProgress;

    private Button buttonStart, buttonPause, buttonReset;
    private EditText editTextMinutes;

    private long initialTimeInMillis;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    private long timeLeftInMillis = 1500000; // 25 minutes default
    private int currentProgress = 100; // Track current progress for resume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Focus");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home3);
        }

        circleProgress = findViewById(R.id.circleProgress);
        textViewTimer = findViewById(R.id.textViewTimer);
        buttonStart = findViewById(R.id.buttonStart);
        buttonPause = findViewById(R.id.buttonPause);
        buttonReset = findViewById(R.id.buttonReset);
        editTextMinutes = findViewById(R.id.editTextMinutes);

        updateTimerText();





        buttonStart.setOnClickListener(v -> {
            if (!isTimerRunning && !isPaused) {
                startMillis = System.currentTimeMillis(); // capture when timer starts

                String input = editTextMinutes.getText().toString();
                if (!input.isEmpty()) {
                    int minutes = Integer.parseInt(input);
                    if (minutes <= 0) minutes = 25;
                    initialTimeInMillis = minutes * 60 * 1000;
                    timeLeftInMillis = initialTimeInMillis;
                } else {
                    initialTimeInMillis = 25 * 60 * 1000;
                    timeLeftInMillis = initialTimeInMillis;
                }
                animateCircularProgressBar(timeLeftInMillis); // fresh animation
                startTimer();
            } else if (isPaused) {
                if (progressAnimator != null) {
                    progressAnimator.resume(); // resume animation
                }
                startTimer();
                isPaused = false;
            }
        });

        buttonPause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            }
        });

        buttonReset.setOnClickListener(v -> {
            resetTimer();
        });

        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "timer_channel",
                    "Study Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void startTimer() {
        sessionStartMillis = System.currentTimeMillis(); // ✅ store actual start time

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                isPaused = false;
                editTextMinutes.setEnabled(true);
                editTextMinutes.setVisibility(View.VISIBLE);
                playSoundAndVibrate();
                showNotification();
                circleProgress.setProgress(100); // reset to full

                int sessionMinutes = (int) (initialTimeInMillis / 60000);
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                String startTime = timeFormat.format(sessionStartMillis);                     // ✅ correct
                String endTime = timeFormat.format(System.currentTimeMillis());              // ✅ correct

                StudySession session = new StudySession(currentDate, sessionMinutes, startTime, endTime);

                new Thread(() -> {
                    SessionDatabase.getInstance(getApplicationContext()).sessionDao().insert(session);
                }).start();
            }
        }.start();

        isTimerRunning = true;
        editTextMinutes.setEnabled(false);
        editTextMinutes.setVisibility(View.INVISIBLE);
    }


    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isPaused = true;
            isTimerRunning = false;
        }

        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.pause();
            currentProgress = circleProgress.getProgress(); // store current progress
        }

        editTextMinutes.setVisibility(View.GONE);
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timeLeftInMillis = initialTimeInMillis;
        updateTimerText();
        isTimerRunning = false;
        isPaused = false;
        editTextMinutes.setEnabled(true);
        editTextMinutes.setVisibility(View.VISIBLE);

        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        circleProgress.setProgress(100);
        currentProgress = 100;
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeFormatted);
    }

    private void playSoundAndVibrate() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.timer_done);
        mediaPlayer.start();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(500);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_channel")
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("Time's up!")
                .setContentText("Your study session is complete.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void animateCircularProgressBar(long durationInMillis) {
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }

        int startProgress = circleProgress.getProgress();
        currentProgress = startProgress;

        progressAnimator = ObjectAnimator.ofInt(circleProgress, "progress", startProgress, 0);
        progressAnimator.setDuration(durationInMillis);
        progressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        progressAnimator.start();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(MainActivity.this, SessionHistoryActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
