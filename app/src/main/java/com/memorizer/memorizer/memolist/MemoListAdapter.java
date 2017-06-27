package com.memorizer.memorizer.memolist;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.ScheduleData;
import com.memorizer.memorizer.models.ScheduleModel;

import java.util.ArrayList;

import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoListAdapter extends RecyclerView.Adapter<MemoListAdapter.ViewHolder> {
    private static final String TAG = "MemoListAdapter";
    private Context mContext;
    private ArrayList<MemoData> memoDatas;

    public MemoListAdapter(Context context, ArrayList<MemoData> memoData) {
        this.memoDatas = memoData;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_item_adapter, parent, false);
        return new ViewHolder(v);
    }

    // 출력 될 아이템 관리
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        // 각 뷰에 값넣기

        // TextView에 현재 position의 문자열 추가
        if (memoDatas.get(position).getLabel().equals("") && memoDatas.get(position).getLabelPos() == 0) {
            holder.label.setVisibility(View.GONE);
        } else {
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setText(memoDatas.get(pos).getLabel());
            holder.label.setBackground(ContextCompat.getDrawable(mContext, memoDatas.get(pos).getLabelPosDraw()));
        }

        if (memoDatas.get(pos).getContent().length() >= 24) {
            holder.memo_content.setText(memoDatas.get(pos).getContent()+"...");
        } else {
            holder.memo_content.setText(memoDatas.get(pos).getContent());
        }

        holder.memo_time.setText(memoDatas.get(pos).getTime());
        holder.memo_posted.setText(memoDatas.get(pos).getPosted().split(" ")[0]); // 시간 제거

        ScheduleModel scheduleModel = new ScheduleModel(mContext);
        ScheduleData nextSchedule = scheduleModel.getMemoSchedule(memoDatas.get(pos).get_id());
        if (nextSchedule != null) {
            holder.next_schedule.setVisibility(View.VISIBLE);
            holder.memo_term.setText(""+nextSchedule.getDaysNext());
        } else {
            holder.next_schedule.setVisibility(View.GONE);
        }
        scheduleModel.close();

        holder.memo_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뷰 누를경우 상세 보기로 이동
                Intent intent = new Intent(mContext, MemoCreate.class);
                intent.putExtra("is_edit", true);
                intent.putExtra("memo_id", memoDatas.get(pos).get_id());

                ((AppCompatActivity) mContext).startActivityForResult(intent, ITEM_DELETE);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (this.memoDatas == null)
            return 0;
        return this.memoDatas.size();
    }

    // 아이템 position의 ID 값 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void swap(ArrayList<MemoData> memoDatas){
        this.memoDatas.clear();
        this.memoDatas.addAll(memoDatas);
        notifyDataSetChanged();
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(MemoData memoData) {
        this.memoDatas.add(memoData);
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        this.memoDatas.remove(_position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout memo_item;
        TextView label;
        TextView memo_content;
        TextView memo_time;
        TextView memo_posted;
        TextView memo_term;
        RelativeLayout next_schedule;

        public ViewHolder(View view) {
            super(view);
            memo_item = (RelativeLayout) view.findViewById(R.id.memo_item);
            label = (TextView) view.findViewById(R.id.label);
            memo_content = (TextView) view.findViewById(R.id.memo_content);
            memo_term = (TextView) view.findViewById(R.id.memo_term);
            memo_time = (TextView) view.findViewById(R.id.memo_time);
            memo_posted = (TextView) view.findViewById(R.id.memo_posted);
            next_schedule = (RelativeLayout) view.findViewById(R.id.next_schedule);
        }
    }

}

