package ru.kostya.whatsapp.tools;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;


public class AudioService {
    private Context context;
    private MediaPlayer tmpMediaPlayer;


    public AudioService(Context context) {
        this.context = context;
    }
    //Будем проигрывать наше голосовое сообщение по url из FIREBASEDATABASE
    public void playAudioFromUrl(String url, final OnPlayCallBack onPlayCallBack){
       if (tmpMediaPlayer != null){
           tmpMediaPlayer.stop();
       }
        //try обязателен
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {

            //Устанавливаем Url который будем проигрывать
            mediaPlayer.setDataSource(url);
            //Посомогает прослушивать синхронно
            mediaPlayer.prepare();
            //Запускаем
            mediaPlayer.start();

            tmpMediaPlayer = mediaPlayer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //При выполнении данного Listener'A БУДЕТ вызван наш метод onFinish,с помощью которого мы сможем отследить
        //Когда mediaPlayer перестанет прослушивать наше гол. сообщение
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
                onPlayCallBack.onFinish();
            }
        });
    }

    public interface OnPlayCallBack{
        void onFinish();
    }
}
