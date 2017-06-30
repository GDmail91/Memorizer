package com.memorizer.memorizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.memorizer.memorizer.backup.CloudLinker;
import com.memorizer.memorizer.backup.CloudManager;
import com.memorizer.memorizer.models.Constants;

/**
 * Created by soo13 on 2017-06-28.
 */

public class CloudLinkerDialog extends AppCompatDialog {

    private static final String TAG = "CloudLinkerDialog";

    private LinearLayout dropboxButton;
    private LinearLayout googleDriveButton;
    private LinearLayout oneDriveButton;
    private TextView dropboxTextView;
    private TextView googleDriveTextView;
    private TextView oneDriveTextView;
    private ImageButton dropboxUnlinkButton;
    private ImageButton googleDriveUnlinkButton;
    private ImageButton oneDriveUnlinkButton;

    private RelativeLayout progressCircle;

    private View.OnClickListener dropboxClickListener;
    private View.OnClickListener googleDriveClickListener;
    private View.OnClickListener oneDriveClickListener;

    CloudManager cm;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.DROPBOX_DONE
            || msg.what == Constants.GOOGLE_DONE
            || msg.what == Constants.ONE_DONE) {
                progressCircle.setVisibility(View.GONE);
                setUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.4f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.cloud_select_layout);

        progressCircle = (RelativeLayout) findViewById(R.id.progresscircle);
        dropboxButton = (LinearLayout) findViewById(R.id.dropbox_btn);
        dropboxTextView = (TextView) findViewById(R.id.dropbox_linked);
        dropboxUnlinkButton = (ImageButton) findViewById(R.id.dropbox_unlink);
        googleDriveButton = (LinearLayout) findViewById(R.id.google_drive_btn);
        googleDriveTextView = (TextView) findViewById(R.id.google_drive_linked);
        googleDriveUnlinkButton = (ImageButton) findViewById(R.id.google_drive_unlink);
        oneDriveButton = (LinearLayout) findViewById(R.id.one_drive_btn);
        oneDriveTextView = (TextView) findViewById(R.id.one_drive_linked);
        oneDriveUnlinkButton = (ImageButton) findViewById(R.id.one_drive_unlink);
        Button okBtn = (Button) findViewById(R.id.ok_btn);

        // 확인 버튼 이벤트
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NOTHING TO DO
                CloudLinkerDialog.this.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        setUI();
        super.onStart();
    }

    private void getUsername() {
        Log.d(TAG, "getUsername");
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                String username = "";

                for (CloudLinker link : cm.getConnectedLinker()) {
                    username += link.storage.get().getUserName() + " ";
                }

                return username;
            }

            @Override
            protected void onPostExecute(String username) {
                Log.d(TAG, "Connect Cloud Storage by : " + username);
            }
        }.execute("");
    }

    private void setUI() {
        // 클릭 이벤트 셋팅 (connected 인경우 등록안됨)
        // Dropbox
        if (cm.getLinker(CloudManager.Linker.DropboxLinker) == null) {
            dropboxUnlinkButton.setVisibility(View.GONE); // Unlink 제거
            dropboxButton.setOnClickListener(dropboxClickListener); // Link 등록
            dropboxTextView.setText(getContext().getString(R.string.not_connected));
        } else {
            dropboxButton.setOnClickListener(null); // Link 초기화
            dropboxUnlinkButton.setOnClickListener(dropboxClickListener); // Unlink 등록
            dropboxUnlinkButton.setVisibility(View.VISIBLE);
            dropboxTextView.setText(getContext().getString(R.string.connected));
        }
        // Google Drive
        if (cm.getLinker(CloudManager.Linker.GoogleDriveLinker) == null) {
            googleDriveUnlinkButton.setVisibility(View.GONE); // Unlink 제거
            googleDriveButton.setOnClickListener(googleDriveClickListener); // Link 등록
            googleDriveTextView.setText(getContext().getString(R.string.not_connected));
        } else {
            googleDriveButton.setOnClickListener(null); // Link 초기화
            googleDriveUnlinkButton.setOnClickListener(googleDriveClickListener); // Unlink 등록
            googleDriveUnlinkButton.setVisibility(View.VISIBLE);
            googleDriveTextView.setText(getContext().getString(R.string.connected));
        }

        // One Drive
        if (cm.getLinker(CloudManager.Linker.OneDriveLinker) == null) {
            oneDriveUnlinkButton.setVisibility(View.GONE); // Unlink 제거
            oneDriveButton.setOnClickListener(oneDriveClickListener); // Link 등록
            oneDriveTextView.setText(getContext().getString(R.string.not_connected));
        } else {
            oneDriveButton.setOnClickListener(null); // Link 초기화
            oneDriveUnlinkButton.setOnClickListener(oneDriveClickListener); // Unlink 등록
            oneDriveUnlinkButton.setVisibility(View.VISIBLE);
            oneDriveTextView.setText(getContext().getString(R.string.connected));
        }
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public CloudLinkerDialog(final Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        cm = CloudManager.getInstance(context);

        this.dropboxClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dropbox 연결
                progressCircle.setVisibility(View.VISIBLE);
                if (cm.getLinker(CloudManager.Linker.DropboxLinker) != null) {
                    showAlert(context, CloudManager.Linker.DropboxLinker);
                } else {
                    cm.setLinker(CloudManager.Linker.DropboxLinker, mHandler);
                }
            }
        };
        this.googleDriveClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google drive 연결
                progressCircle.setVisibility(View.VISIBLE);
                if (cm.getLinker(CloudManager.Linker.GoogleDriveLinker) != null) {
                    showAlert(context, CloudManager.Linker.GoogleDriveLinker);
                } else {
                    cm.setLinker(CloudManager.Linker.GoogleDriveLinker, mHandler);
                }
            }
        };
        this.oneDriveClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO One drive 연결
                progressCircle.setVisibility(View.VISIBLE);
                if (cm.getLinker(CloudManager.Linker.OneDriveLinker) != null) {
                    showAlert(context, CloudManager.Linker.OneDriveLinker);
                } else {
                    cm.setLinker(CloudManager.Linker.OneDriveLinker, mHandler);
                }
            }
        };
    }

    private void showAlert(Context context, final CloudManager.Linker linker) {
        new AlertDialog.Builder(context)
            .setMessage(R.string.cloud_disconnect)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "OK");
                cm.setLinker(linker, mHandler);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "CANCEL");
                progressCircle.setVisibility(View.GONE);
                dialog.cancel();
                }
            })
            .show();
    }

}
