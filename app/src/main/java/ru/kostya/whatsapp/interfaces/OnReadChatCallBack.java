package ru.kostya.whatsapp.interfaces;

import java.util.List;

import ru.kostya.whatsapp.model.Chats;

public interface OnReadChatCallBack {
    void onReadSuccess(List<Chats> chatsList);
    void onReadFailed();
}
