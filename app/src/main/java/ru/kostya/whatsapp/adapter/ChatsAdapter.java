package ru.kostya.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.model.Chats;
import ru.kostya.whatsapp.tools.AudioService;


public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context mContext;
    private List<Chats> chatsList;
    private FirebaseUser currentUser;
    private ImageButton tmpBtnPlayer;
    //Наша класс,лежит в пакете tools
    private AudioService audioService;

    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;


    public ChatsAdapter(Context mContext, List<Chats> chatsList) {
        this.audioService = new AudioService(mContext);
        this.mContext = mContext;
        this.chatsList = chatsList;
    }

    public void setChatsList(List<Chats> chatsList){
        this.chatsList = chatsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Если viewType = 0,то отображаем recycler view с помощью layout chat_tem_left,тоесть слева
        //Есои наобото,то используем лаяут для отображения сообщения справа

        View view = null;

        if (viewType == MESSAGE_TYPE_LEFT){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_tem_left,parent,false);
        } else {
             view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chats currentChat = chatsList.get(position);

        holder.bind(currentChat);
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        public TextView textMessage;
        private LinearLayout layoutText,layoutImage,layoutVoice;
        private ImageView imageMessage;


        private ImageButton btnPlay;
        private ViewHolder viewHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.tv_text_message);
            imageMessage = itemView.findViewById(R.id.image_message);
            layoutText = itemView.findViewById(R.id.layout_text);
            layoutImage = itemView.findViewById(R.id.layout_image);
            layoutVoice = itemView.findViewById(R.id.layout_voice);

            btnPlay = itemView.findViewById(R.id.btn_play_chat);

        }
        //Тоже самое,что мы делали в onBindView holder,но так код красивее
        void bind(final Chats currentChat){
            //Проверяем тип сообщения
            switch(currentChat.getType()){
                //Если это текст
                case "TEXT":

                    //коменты неактуальны,меняьб лень,тут ничего сложного,вруби мозг
                    //Если это обычное сообщение,скрываем layout  с фото и показываем с текстом,а также загружаем сообщение
                    layoutText.setVisibility(View.VISIBLE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    textMessage.setText(currentChat.getTextMessage());
                    break;
                    //Если это фото
                case "IMAGE":
                    //коменты неактуальны,меняьб лень,тут ничего сложного,вруби мозг
                    //Если это обычное сообщение,скрываем layout  с текстом и показываем с фото,а также загружаем фото
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutText.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    Glide.with(mContext).load(currentChat.getUrl()).placeholder(R.drawable.add_document).into(imageMessage);
                    break;
                    //Если это голосовое сообщение
                case "VOICE":
                    //коменты неактуальны,меняьб лень,тут ничего сложного,вруби мозг
                    //Если это обычное сообщение,скрываем layout  с текстом и показываем с фото,а также загружаем фото
                    layoutImage.setVisibility(View.GONE);
                    layoutText.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.VISIBLE);

                    //По клику на голосовой layout
                    layoutVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //хз зач такой мув
                            if (tmpBtnPlayer != null){
                                tmpBtnPlayer.setImageDrawable(mContext.getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                            }
                            //По нажатию на этот лэяут устанавливаем иконку паузы
                            btnPlay.setImageDrawable(mContext.getDrawable(R.drawable.ic_baseline_pause_24));

                            audioService.playAudioFromUrl(currentChat.getUrl(), new AudioService.OnPlayCallBack() {
                                @Override
                                public void onFinish() {
                                    //Этот метод нам пригодился для того чтобы сменить иконку в нашем голосовом Layout
                                    btnPlay.setImageDrawable(mContext.getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                }
                            });
                            //хз зач такой мув
                            tmpBtnPlayer = btnPlay;
                        }
                    });
                    break;
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        int myConst = 0;
        //Если отправитель это текущий юзер то юзаем layout right иначе left
            if (String.valueOf(chatsList.get(position).getSender()).equals(String.valueOf(currentUser.getUid()))) {
                myConst = MESSAGE_TYPE_RIGHT;
            } else {
                myConst =  MESSAGE_TYPE_LEFT;
            }

        return myConst;
    }
}
