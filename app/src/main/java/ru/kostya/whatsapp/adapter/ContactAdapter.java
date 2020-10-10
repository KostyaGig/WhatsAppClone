package ru.kostya.whatsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.kostya.whatsapp.model.User;
import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.views.chats.ChatActivity;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String IMAGE_PROFILE = "imageProfile";

    private List<User> userList;
    private Context mContext;

    public ContactAdapter(List<User> userList, Context mContext) {
        this.userList = userList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_contact,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User currentUser = userList.get(position);

        //placeholder - этот метод нужен,чтобы если у юзера нет фото показывалась фото по умолчанию,это в своем роде заглушка
        Glide.with(mContext).load(currentUser.getImageProfile()).placeholder(R.mipmap.ic_launcher).into(holder.profileImage);

        holder.userName.setText(currentUser.getUserName());
        holder.description.setText(currentUser.getBio());

        //По нажатию на контакта
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Когда мы кликаем на какойто контакт,то передаем через put id этого контакта,его имя,чтобы отображать в toolbar chatActivity,
                // а также фото,чтобы соответственно тоже ее отображать
                mContext.startActivity(new Intent(mContext, ChatActivity.class)
                        .putExtra(USER_ID,currentUser.getUserID())
                        .putExtra(USER_NAME,currentUser.getUserName())
                        .putExtra(IMAGE_PROFILE,currentUser.getImageProfile())
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView profileImage;
        public TextView userName,description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.image_profile);
            userName = itemView.findViewById(R.id.tv_username);
            description = itemView.findViewById(R.id.tv_desc);
        }

    }
}
