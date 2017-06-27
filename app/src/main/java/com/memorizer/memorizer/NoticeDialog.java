package com.memorizer.memorizer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import us.feras.mdv.MarkdownView;

/**
 * Created by soo13 on 2017-06-28.
 */

public class NoticeDialog extends Dialog {

    private TextView NoticeTitleView;
    private MarkdownView NoticeTextView;
    private Button ConfirmButton;
    private Button CancelButton;
    private String mTitle;
    private String mContent;

    private View.OnClickListener confirmClickListener;
    private View.OnClickListener cancelClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.notice_alarm);

        NoticeTitleView = (TextView) findViewById(R.id.notice_title);
        NoticeTextView = (MarkdownView) findViewById(R.id.notice_text);
        ConfirmButton = (Button) findViewById(R.id.ok_btn);
        CancelButton = (Button) findViewById(R.id.cancel_btn);

        // 제목과 내용을 생성자에서 셋팅한다.
        NoticeTitleView.setText(mTitle);
        NoticeTextView.loadMarkdown(mContent);
        NoticeTextView.setBackgroundColor(getContext().getResources().getColor(R.color.memoPad));
        //NoticeTextView.setBackgroundColor(R.color.memoPad);
        //NoticeTextView.setText(mContent);

        // 클릭 이벤트 셋팅
        if (confirmClickListener != null && cancelClickListener != null) {
            ConfirmButton.setOnClickListener(confirmClickListener);
            CancelButton.setOnClickListener(cancelClickListener);
        } else if (confirmClickListener != null) {
            CancelButton.setVisibility(View.GONE);
            ConfirmButton.setOnClickListener(confirmClickListener);
        }
    }

    // 클릭버튼이 하나일때 생성자 함수로 클릭이벤트를 받는다.
    public NoticeDialog(Context context, String title, String content,
                        View.OnClickListener confirmClickListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mTitle = title;
        this.mContent = content;
        this.confirmClickListener = confirmClickListener;
    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public NoticeDialog(Context context, String title, String content,
                        View.OnClickListener confirmClickListener,
                        View.OnClickListener cancelClickListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mTitle = title;
        this.mContent = content;
        this.confirmClickListener = confirmClickListener;
        this.cancelClickListener = cancelClickListener;
    }

}
