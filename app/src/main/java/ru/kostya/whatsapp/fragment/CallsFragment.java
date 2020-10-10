package ru.kostya.whatsapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.kostya.whatsapp.adapter.CallListAdapter;
import ru.kostya.whatsapp.model.CallList;
import ru.kostya.whatsapp.R;

public class CallsFragment extends Fragment {

    private RecyclerView callsRecyclerView;
    private CallListAdapter adapter;
    private List<CallList> callsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calls_fragment, container, false);

        callsRecyclerView= view.findViewById(R.id.callRecView);
        callsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        callsList = new ArrayList<>();
        callsList.add(new CallList("100","Polina","25.09.2020","https://i.pinimg.com/originals/59/70/5f/59705f38c873e5f729bc4a83963c259f.png","missed"));

        adapter = new CallListAdapter(callsList,getContext());

        callsRecyclerView.setAdapter(adapter);
        return view;
    }
}
