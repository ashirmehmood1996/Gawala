package com.android.example.gawala.Consumer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.gawala.R;

import java.util.Timer;
import java.util.TimerTask;

public class AggressiveNotificationAlertActivity extends AppCompatActivity {
    public static final String IS_DEMO = "isDemo";
    public static final String MESSAGE = "message";

    private ImageView alertIconImageView;
    private Button dismissButton;
    private TextView messageTextView;

    private Timer mTimer;
    private Ringtone mRingtone = null;
    private boolean isDemo;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggressive_notification_alert);
        initFields();
        startalertIconAnimation();
        playTone();
        attachLiosteners();
    }


    private void initFields() {
        isDemo = getIntent().getBooleanExtra(IS_DEMO, false);
        if (isDemo) {
            message = "Demo Aggressive Alert Activty";
        } else {
            message = getIntent().getStringExtra(MESSAGE);
        }
        messageTextView = findViewById(R.id.tv_alert_message);
        messageTextView.setText(message);
        alertIconImageView = findViewById(R.id.iv_alert_activity_alert_icon);
        dismissButton = findViewById(R.id.bt_alert_activity_call_child);
        mTimer = new Timer();
        Uri alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(getApplicationContext(), alertUri);
    }

    private void startalertIconAnimation() {
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_animation);
        alertIconImageView.startAnimation(bounceAnimation);
    }

    private void playTone() {
        mRingtone.play();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!mRingtone.isPlaying()) {
                    mRingtone.play();
                }
            }
        }, 1000 * 1, 1000 * 4);

    }

    private void attachLiosteners() {
        dismissButton.setOnClickListener(v -> {
            if (mRingtone != null) {
                mRingtone.stop();
            }
            mTimer.cancel();
            v.setEnabled(false);
            finish();

        });

    }

    @Override
    protected void onDestroy() {
        mRingtone.stop();
        mTimer.cancel();
        super.onDestroy();
    }
}
