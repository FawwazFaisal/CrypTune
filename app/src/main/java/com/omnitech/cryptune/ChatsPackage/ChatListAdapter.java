package com.omnitech.cryptune.ChatsPackage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omnitech.cryptune.R;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MessageHolder> {

    Context mContext;
    int mResource;
    ArrayList<ChatObject> list;
    private OnItemClickListener listener;

    public ChatListAdapter(Context mContext, int mResource, ArrayList<ChatObject> aList) {
        this.mContext = mContext;
        this.mResource = mResource;
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
        String Name = list.get(position).getName();
        String Phone = list.get(position).getPhone();
        holder.setPhone(Phone);
        holder.setName(Name);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemCLick(int position);
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        View item;
        TextView Name;
        TextView Phone;

        public MessageHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            item = itemView;
            Name = item.findViewById(R.id.title);
            Phone = item.findViewById(R.id.phone);

            item.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    int pos = getAdapterPosition();
                    listener.onItemCLick(pos);
                }
            });

        }

        public void setName(String title) {
            Name.setText(title);
        }

        public void setPhone(String phone) {
            Phone.setText(phone);
        }
    }

}
