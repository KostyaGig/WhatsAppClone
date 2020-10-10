package ru.kostya.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.kostya.whatsapp.model.CallList;
import ru.kostya.whatsapp.R;

import static android.R.color.holo_green_dark;
import static android.R.color.holo_red_dark;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.ViewHolder> {

    private List<CallList> callList;
    private Context context;

    public CallListAdapter(List<CallList> callList, Context context) {
        this.callList = callList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView userName,date;
        public ImageView profileImage,arrowImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);

            date = itemView.findViewById(R.id.date);

            profileImage = itemView.findViewById(R.id.image_profile);
            arrowImage = itemView.findViewById(R.id.img_arrow);
        }
    }

    @NonNull
    @Override
    public CallListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_call,parent,false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CallListAdapter.ViewHolder holder, int position) {
        CallList currentList = callList.get(position);

        holder.userName.setText(currentList.getUserName());

        holder.date.setText(currentList.getDate());

        //Если тип вызова входящий...
        if (currentList.getCallType().equals("missed")){
            holder.arrowImage.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_arrow_downward_24));
            holder.arrowImage.getDrawable().setTint(context.getColor(holo_red_dark));
        } else if(currentList.getCallType().equals("income")){
            holder.arrowImage.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_arrow_downward_24));
            holder.arrowImage.getDrawable().setTint(context.getColor(holo_green_dark));
        } else {
            holder.arrowImage.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_arrow_upward_24));
            holder.arrowImage.getDrawable().setTint(context.getColor(holo_green_dark));
        }
        Glide.with(context).load(currentList.getUrlImage()).placeholder(R.mipmap.ic_launcher).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

}
