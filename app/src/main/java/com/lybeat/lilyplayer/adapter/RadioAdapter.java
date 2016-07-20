package com.lybeat.lilyplayer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lybeat.lilyplayer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: lybeat
 * Date: 2016/7/17
 */
public class RadioAdapter extends android.widget.BaseAdapter {

    private Context context;
    private List<String> options;

    public RadioAdapter(Context context, List<String> options) {
        this.context = context;
        this.options = options;
    }

    public RadioAdapter(Context context, String[] options) {
        this.context = context;
        this.options = new ArrayList<>();
        Collections.addAll(this.options, options);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int i) {
        return options.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_radio_list, viewGroup, false);
        }
        TextView optionTxt = (TextView) view.findViewById(R.id.option_txt);
        optionTxt.setText(options.get(i));
        SharedPreferences sp = context.getSharedPreferences("share_data",
                Context.MODE_PRIVATE);
        int actionIndex = sp.getInt("key_action", 2);
        RadioButton optionRadio = (RadioButton) view.findViewById(R.id.option_radio);
        if (i == actionIndex) {
            optionRadio.setChecked(true);
        } else {
            optionRadio.setChecked(false);
        }

        return view;
    }
}
