package com.omnitech.cryptune.MessagesPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omnitech.cryptune.R;

import java.util.ArrayList;
import java.util.Calendar;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageHolder> {

    public static final String phNo = "phNo";
    Context mContext;
    int mResource;
    ArrayList<MessageObject> list;
    String myPhone;
    private OnItemClickListener listener;

    public MessageListAdapter(Context mContext, int mResource, ArrayList<MessageObject> aList) {
        this.mContext = mContext;
        this.mResource = mResource;
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        myPhone = sharedPreferences.getString(phNo, "");
        list = aList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        return new MessageHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageHolder holder, final int position) {
        String timeStamp = list.get(position).getTimestamp();
        String text = list.get(position).getText();
        String phoneOfText = list.get(position).getPhone();
        if (TextUtils.equals(phoneOfText, myPhone)) {
            holder.setSent(text, timeStamp);
        } else {
            holder.setReceived(text, timeStamp);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemCLick(int position);
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {
        TextView Sent, Received, DateSent, DateReceived;

        public MessageHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            Sent = itemView.findViewById(R.id.Sent);
            Received = itemView.findViewById(R.id.Received);
            DateReceived = itemView.findViewById(R.id.recDate);
            DateSent = itemView.findViewById(R.id.sentDate);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    int pos = getAdapterPosition();
                    listener.onItemCLick(pos);
                }
            });

        }

        public void setSent(String text, String time) {
            Sent.setText(text);
            Received.setVisibility(View.GONE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.decode(time));
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mHour = calendar.get(Calendar.HOUR);
            int mMinute = calendar.get(Calendar.MINUTE);
            int mSecond = calendar.get(Calendar.SECOND);
            String date = mYear + "-" + mMonth + "-" + mDay + "|" + mHour + ":" + mMinute + ":" + mSecond;
            DateSent.setVisibility(View.VISIBLE);
            DateSent.setText(date);
        }

        public void setReceived(String text, String time) {
            Received.setText(text);
            Sent.setVisibility(View.GONE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.decode(time));
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mHour = calendar.get(Calendar.HOUR);
            int mMinute = calendar.get(Calendar.MINUTE);
            int mSecond = calendar.get(Calendar.SECOND);
            String date = mYear + "-" + mMonth + "-" + mDay + "|" + mHour + ":" + mMinute + ":" + mSecond;
            DateReceived.setVisibility(View.VISIBLE);
            DateReceived.setText(date);
        }
    }

}
