package ru.kostya.whatsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.kostya.whatsapp.dialog.DialogViewUser;
import ru.kostya.whatsapp.model.ChatList;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.views.chats.ChatActivity;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<ChatList> chatList;
    private Context context;

    public ChatListAdapter(List<ChatList> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView userName,description,date;
        public ImageView profileImage;
        private LinearLayout layoutChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            description = itemView.findViewById(R.id.description);
            date = itemView.findViewById(R.id.date);

            profileImage = itemView.findViewById(R.id.profileImage);
            layoutChat = itemView.findViewById(R.id.layout_chat);
        }
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat,parent,false);

        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {
        final ChatList currentList = chatList.get(position);

        holder.userName.setText(currentList.getUserName());
        holder.description.setText(currentList.getDescription());
        holder.date.setText(currentList.getDate());

        //Если фотография юзера пустая,то
        if (currentList.getUrlImage().equals("")){
            holder.profileImage.setImageDrawable(context.getDrawable(R.drawable.profile));
        } else {
            Glide.with(context).load(currentList.getUrlImage()).placeholder(R.mipmap.ic_launcher).into(holder.profileImage);
        }

        holder.layoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivity = new Intent(context, ChatActivity.class);
                chatActivity.putExtra(ContactAdapter.USER_ID,currentList.getUserId());
                chatActivity.putExtra(ContactAdapter.USER_NAME,currentList.getUserName());
                chatActivity.putExtra(ContactAdapter.IMAGE_PROFILE,currentList.getUrlImage());

                context.startActivity(chatActivity);
            }
        });

        //По нажатию на фото юзера
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogViewUser(context,currentList);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

}
