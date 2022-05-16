package com.example.btmm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Page2 extends Fragment {

    private String rawData;
    private static TextView val;

    public Page2(String value){
        this.rawData = value;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("Page2", "CreateView");
        View view = (View)inflater.inflate(R.layout.fragment_page2, container, false);
        TextView textView = (TextView) view.findViewById(R.id.rawData);
        textView.setText(rawData);
        this.val = textView;
        return view;
    }

    static public void updateVal(float newVal) {
        val.setText(String.valueOf(Math.round(newVal*1000.0)/1000.0));
        Log.i("Page2","x");
    }
}