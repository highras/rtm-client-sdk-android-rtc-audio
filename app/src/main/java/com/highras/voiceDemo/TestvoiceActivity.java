package com.highras.voiceDemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.fpnn.sdk.ErrorRecorder;
import com.livedata.rtc.RTCEngine;
import com.rtcvoice.RTMClient;
import com.rtcvoice.RTMPushProcessor;
import com.rtcvoice.RTMStruct;
import com.rtcvoice.UserInterface;

import com.rtcvoice.RTMPushProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

public class TestvoiceActivity extends AppCompatActivity {
    RTMPushProcessor voicepush = new RTMVoiceProcessor();
    public TestErrorRecorderVoice voicerecoder = new TestErrorRecorderVoice();
    ConstraintLayout alllayout;
    //    private int viewHeght;
//    private int viewWidth;
    public static boolean running = false;
    boolean micStatus = false;
    boolean usespeaker = true;
    LinearLayout leave;
    LinearLayout mic;
    LinearLayout speaker;
    ImageView speakerImageView;
    ImageView muteImageView;
    TextView muteTextView;
    long activityRoom = 0;
    Activity myactivity = this;
    TextView roomdshow;
    RTMClient client;
    Utils utils = Utils.INSTANCE;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    class RTMVoiceProcessor extends RTMPushProcessor {
        String msg = "";

        public boolean reloginWillStart(long uid, int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            return true;
        }


        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0)
                    return;
                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode == 0) {
                            startVoice(activityRoom, true);
                        } else {
                            mylog.log("??????????????????");
                        }
                    }
                }, activityRoom);
            } else {
                mylog.log("??????????????????");
                mic.setBackgroundResource(R.drawable.micclose);
                micStatus = false;
            }
        }

        public void rtmConnectClose(long uid) {
            TestvoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        public void kickout() {
            mylog.log("receive kickout");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }

    private void leaveRoom() {
        if (client == null)
            return;
        utils.client.leaveRTCRoom(activityRoom, new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                client.closeRTM();
                client = null;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)

    @Override
    public Resources getResources() {
        // ???????????????????????????
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    int getLatencyTime() {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Method m = null;
        try {
            m = am.getClass().getMethod("getOutputLatency", int.class);
            return (Integer) m.invoke(am, AudioManager.STREAM_MUSIC);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return 0;
    }

    AudioManager am;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testvideo);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        customToolbarAndStatusBarBackgroundColor(true);
        mic = $(R.id.mic);
        muteImageView = $(R.id.muteImageView);
        speaker = $(R.id.speaker);
        speakerImageView = $(R.id.speakerImageView);
        leave = $(R.id.leave);
        muteTextView = $(R.id.muteTextView);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        roomdshow = $(R.id.roomnum);

        activityRoom = getIntent().getIntExtra("roomid", 0);
        speakerImageView.setSelected(true);

        utils.login(new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    client = utils.client;
                    RTMStruct.RTMAnswer jj = client.initRTMVoice(false);
                    if (jj.errorCode != 0) {
                        mylog.log("????????? ???????????? " + jj.getErrInfo());
                        return;
                    }
                    realEnterRoom(activityRoom);
                }
//                client = utils.client;
            }
        }, myactivity, voicepush);

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveRoom();
                finish();
            }
        });


        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client == null || !client.isOnline())
                    return;
                setSpeakerStatus();
            }
        });


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!client.isOnline() || activityRoom == 0)
                    return;
                setMicStatus(!micStatus);
            }
        });
    }


    public class TestErrorRecorderVoice extends ErrorRecorder {
        public TestErrorRecorderVoice() {
            super.setErrorRecorder(this);
        }

        public void recordError(Exception e) {
            String msg = "Exception:" + e;
            mylog.log(msg);
        }

        public void recordError(String message) {
            mylog.log(message);
        }

        public void recordError(String message, Exception e) {
            String msg = String.format("Error: %s, exception: %s", message, e);
            mylog.log(msg);
        }
    }

    void closeInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
    }

    void setMicStatus(boolean status) {
        if (!status) {
            client.closeMic();
            muteTextView.setText("???????????????");
            muteImageView.setSelected(true);
        } else {
            client.openMic();
            muteTextView.setText("???????????????");
            muteImageView.setSelected(false);
        }
        micStatus = status;
    }

    void setSpeakerStatus() {
        usespeaker = !usespeaker;
        if (!usespeaker) {
            speakerImageView.setSelected(false);
        } else {
            speakerImageView.setSelected(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (usespeaker)
                    client.switchOutput(true);
                else
                    client.switchOutput(false);
            }
        }).start();
    }


    void startVoice(long roomId) {
        startVoice(roomId,false);
    }
        void startVoice(long roomId, boolean relogin) {
        activityRoom = roomId;
        client.setActivityRoom(activityRoom);
        RTMStruct.RTMAnswer ret = client.setVoiceStat(true);
        if (ret.errorCode != 0) {
            return;
        }
        if (relogin){
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usespeaker = true;
                    speakerImageView.setSelected(true);
                }
            });
            return;
        }
        client.openMic();
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mic.setBackgroundResource(R.drawable.micopen);
//                speaker.setBackgroundResource(R.drawable.speakon);
                micStatus = true;
                usespeaker = true;
                roomdshow.setText("??????id-" + roomId);
//                speakerImageView.setSelected(true);
            }
        });
    }

    void realEnterRoom(final long roomId) {
        client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    startVoice(roomId);
                } else {
                    client.createRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            if (answer.errorCode == 0) {
                                startVoice(roomId);
                            } else {
                                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                    @Override
                                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                        if (answer.errorCode == 0) {
                                            startVoice(roomId);
                                        }
                                    }
                                }, roomId);
                            }
                        }
                    }, roomId);
                }
            }
        }, roomId);
    }

    protected void customToolbarAndStatusBarBackgroundColor(boolean darkTheme) {
        int toolbarBackgroundColorResId = darkTheme ? R.color.purple_500 : R.color.white;
        setTitleBackgroundResource(toolbarBackgroundColorResId, darkTheme);
    }

    /**
     * ????????????????????????????????????
     *
     * @param resId ????????????id
     */
    protected void setTitleBackgroundResource(int resId, boolean dark) {
        toolbar.setBackgroundResource(resId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, resId));
        }
        setStatusBarTheme(this, dark);
    }

    /**
     * Changes the System Bar Theme.
     */
    public static void setStatusBarTheme(final Activity pActivity, final boolean pIsDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Fetch the current flags.
            final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
            // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
            pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
        }
    }
}
