package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iflytek.cloud.Setting;

import java.util.ArrayList;

public class SourceListViewAdapter extends ArrayAdapter<Pair<String, Integer>> {

    public static final int SHOWED_ITEM = 1;
    public static final int HIDE_ITEM = 2;
    public static final int SHOWED_LABEL = 3;
    public static final int HIDE_LABEL = 4;

    private SettingFragment fragment;
    private Context context;
    private int listItemResourceId;
    private ArrayList<Pair<String, Integer>> sourceList;

    public SourceListViewAdapter(SettingFragment fragment, Context context, int listItemResourceId, ArrayList<Pair<String, Integer>> sourceList) {
        super(context, listItemResourceId, sourceList);
        this.fragment = fragment;
        this.context = context;
        this.listItemResourceId = listItemResourceId;
        this.sourceList = sourceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pair<String, Integer> nowItem = sourceList.get(position);
        View view = LayoutInflater.from(context).inflate(listItemResourceId, null);
        ((TextView)view.findViewById(R.id.itemText)).setText(nowItem.first);
        switch (nowItem.second) {
            case SHOWED_LABEL:
                view.findViewById(R.id.showedSources).setVisibility(View.VISIBLE);
                view.findViewById(R.id.itemMain).setVisibility(View.GONE);
                break;
            case HIDE_LABEL:
                view.findViewById(R.id.hideSources).setVisibility(View.VISIBLE);
                view.findViewById(R.id.itemMain).setVisibility(View.GONE);
                break;
            case SHOWED_ITEM:
                view.findViewById(R.id.enableBtn).setVisibility(View.GONE);
                view.findViewById(R.id.upBtn).setVisibility(View.GONE);
                view.findViewById(R.id.downBtn).setVisibility(View.GONE);
                break;
            case HIDE_ITEM:
                view.findViewById(R.id.unableBtn).setVisibility(View.GONE);
                view.findViewById(R.id.upBtn).setVisibility(View.GONE);
                view.findViewById(R.id.downBtn).setVisibility(View.GONE);
                break;
        }
        view.findViewById(R.id.enableBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleSourceClicked(nowItem.first, SettingFragment.ENABLE_CLICKED);
            }
        });
        view.findViewById(R.id.unableBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleSourceClicked(nowItem.first, SettingFragment.UNABLE_CLICKED);
            }
        });
        return view;
    }

}
