package com.lybeat.lilyplayer.widget.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.adapter.RadioAdapter;

/**
 * Author: lybeat
 * Date: 2016/7/18
 */
public class RadioDialog {

    private static final String TAG = "RadioDialog";

    private AlertDialog dialog;
    private ListView listView;
    private RadioAdapter adapter;

    private Context context;
    private String[] options;
    private final TextView titleTxt;

    public RadioDialog(Context context, String[] options) {
        this.context = context;
        this.options = options;

        this.dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setContentView(R.layout.dialog_radio);

        adapter = new RadioAdapter(context, options);
        listView = (ListView) window.findViewById(R.id.option_list);
        listView.setAdapter(adapter);
        titleTxt = (TextView) window.findViewById(R.id.dialog_title_txt);
        Button cancelBtn = (Button) window.findViewById(R.id.dialog_cancel_tbn);
        cancelBtn.setOnClickListener(onCancelListener);
    }

    private View.OnClickListener onCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };

    public void setDialogTitle(String title) {
        titleTxt.setText(title);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
