package com.memorizer.memorizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.memorizer.memorizer.create.MemoData;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoListAdapter extends BaseAdapter {
    private static final String TAG = "MemoListAdapter";
    private ArrayList<MemoData> memoDatas;

    public MemoListAdapter(ArrayList<MemoData> memoData) {
        this.memoDatas = memoData;
    }


    // 현재 아이템의 수를 리턴
    @Override
    public int getCount() {
        return memoDatas.size();
    }

    // 현재 아이템의 오브젝트를 리턴, Object를 상황에 맞게 변경하거나 리턴받은 오브젝트를 캐스팅해서 사용
    @Override
    public Object getItem(int position) {
        return memoDatas.get(position);
    }

    // 아이템 position의 ID 값 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 출력 될 아이템 관리
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        View view = convertView;
        ViewHolder holder;

        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
        if ( view == null ) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.memo_item_adapter, parent, false);

            holder = new ViewHolder();

            holder.memo_content = (TextView) view.findViewById(R.id.memo_content);
            holder.memo_during = (TextView) view.findViewById(R.id.memo_during);
            holder.memo_term = (TextView) view.findViewById(R.id.memo_term);
            holder.memo_time = (TextView) view.findViewById(R.id.memo_time);
            holder.memo_posted = (TextView) view.findViewById(R.id.memo_posted);
            holder.setting_button = (ImageButton) view.findViewById(R.id.setting_button);

            view.setTag(holder);
        } else {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            holder = (ViewHolder)view.getTag();

        }

        // 각 뷰에 값넣기

        // TextView에 현재 position의 문자열 추가
        holder.memo_content.setText(memoDatas.get(position).getContent());
        String during;
        if (memoDatas.get(position).getWhileDate() == null
         || memoDatas.get(position).getWhileDate().getTimeInMillis() == 0)
            during = "무제한";
        else {
            during = memoDatas.get(position).getWhileDate().get(Calendar.YEAR) + "."
                    + memoDatas.get(position).getWhileDate().get(Calendar.MONTH) + "."
                    + memoDatas.get(position).getWhileDate().get(Calendar.DAY_OF_MONTH);
        }
        holder.memo_during.setText(during);
        holder.memo_term.setText(""+memoDatas.get(position).getTerm());

        holder.memo_time.setText(makeTime(memoDatas.get(position).getTimeOfHour(), memoDatas.get(position).getTimeOfMinute()));
        holder.memo_posted.setText(memoDatas.get(position).getPosted().split(" ")[0]); // 시간 제거

        holder.setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.showContextMenuForChild(v);
            }
        });

        return view;
    }

    static class ViewHolder {
        TextView memo_content;
        TextView memo_during;
        TextView memo_term;
        TextView memo_time;
        TextView memo_posted;
        ImageButton setting_button;
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(MemoData memoData) {
        this.memoDatas.add(memoData);
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        this.memoDatas.remove(_position);
    }


    protected String makeTime(int hourOfDay, int minute) {
        String hourStr, minStr, ampm = "am";

        if (hourOfDay >= 22) {
            hourStr = String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay > 12) {
            hourStr = "0" + String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay == 0) {
            hourStr = "12";
        } else if (hourOfDay < 10) {
            hourStr = "0" + String.valueOf(hourOfDay);
        } else if (hourOfDay == 12) {
            hourStr = String.valueOf(hourOfDay);
            ampm = "pm";
        } else
            hourStr = String.valueOf(hourOfDay);

        if (minute < 10) {
            minStr = "0" + String.valueOf(minute);
        } else
            minStr = String.valueOf(minute);

        return hourStr +" : "+minStr+ " "+ampm;
    }

}

