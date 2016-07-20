package com.lybeat.lilyplayer.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.db.DBManager;
import com.lybeat.lilyplayer.entity.PlayRecord;

import java.util.List;

/**
 * Author: lybeat
 * Date: 2016/7/20
 */
public class PlayRecordFragment extends BaseFragment {

    private RecyclerView recyclerView;

    private List<PlayRecord> playRecords;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_record, container, false);
        initViews(view);
        initData();

        return view;
    }

    @Override
    protected void initViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.record_recycler);
    }

    @Override
    protected void initData() {
        DBManager dbManager = DBManager.getInstance(getActivity());
        playRecords =  dbManager.queryPlayRecordList();
    }
}
