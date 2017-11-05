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

public class TabListViewAdapter extends ArrayAdapter<Pair<String, Integer>> {

    public static final int SHOWED_ITEM = 1;
    public static final int HIDE_ITEM = 2;
    public static final int SHOWED_LABEL = 3;
    public static final int HIDE_LABEL = 4;

    private SettingFragment fragment;
    private Context context;
    private int listItemResourceId;
    private ArrayList<Pair<String, Integer>> tabList;

    public TabListViewAdapter(SettingFragment fragment, Context context, int listItemResourceId, ArrayList<Pair<String, Integer>> tabList) {
        super(context, listItemResourceId, tabList);
        this.fragment = fragment;
        this.context = context;
        this.listItemResourceId = listItemResourceId;
        this.tabList = tabList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pair<String, Integer> nowItem = tabList.get(position);
        View view = LayoutInflater.from(context).inflate(listItemResourceId, null);
        ((TextView)view.findViewById(R.id.itemText)).setText(nowItem.first);
        switch (nowItem.second) {
            case SHOWED_LABEL:
                view.findViewById(R.id.showedTabs).setVisibility(View.VISIBLE);
                view.findViewById(R.id.itemMain).setVisibility(View.GONE);
                break;
            case HIDE_LABEL:
                view.findViewById(R.id.hideTabs).setVisibility(View.VISIBLE);
                view.findViewById(R.id.itemMain).setVisibility(View.GONE);
                break;
            case SHOWED_ITEM:
                view.findViewById(R.id.enableBtn).setVisibility(View.GONE);
                if ((position < (tabList.size() - 1)) && (tabList.get(position + 1).second == TabListViewAdapter.HIDE_LABEL))
                    view.findViewById(R.id.downBtn).setVisibility(View.GONE);
                if (position == 1)
                    view.findViewById(R.id.upBtn).setVisibility(View.GONE);
                break;
            case HIDE_ITEM:
                view.findViewById(R.id.upBtn).setVisibility(View.GONE);
                view.findViewById(R.id.downBtn).setVisibility(View.GONE);
                view.findViewById(R.id.unableBtn).setVisibility(View.GONE);
                break;
        }
        view.findViewById(R.id.enableBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleTabClicked(nowItem.first, SettingFragment.ENABLE_CLICKED);
            }
        });
        view.findViewById(R.id.unableBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleTabClicked(nowItem.first, SettingFragment.UNABLE_CLICKED);
            }
        });
        view.findViewById(R.id.upBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleTabClicked(nowItem.first, SettingFragment.UP_CLICKED);
            }
        });
        view.findViewById(R.id.downBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleTabClicked(nowItem.first, SettingFragment.DOWN_CLICKED);
            }
        });
        return view;
    }

}
