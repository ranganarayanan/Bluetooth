package com.example.bluetooth.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetooth.R;
import com.example.bluetooth.model.MessageModel;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<MessageModel> messageModels;
    private Context context;

    public MessageAdapter(List<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        holder.setData(position);

    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView msg;
        private LinearLayout ll;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msg=itemView.findViewById(R.id.msg_txt);
            ll=itemView.findViewById(R.id.ll);
        }
        public void setData(int position){
            MessageModel message=messageModels.get(position);
            msg.setText(message.getMsg());
            if(message.getMsgType().equals("w")){
                msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }
        }
    }
}
