package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LikeListViewAdapter extends ArrayAdapter<Pair<News, String>> {

    SettingFragment fragment;
    Context context;
    int listItemResourceId;
    ArrayList<Pair<News, String>> newsList;

    public LikeListViewAdapter(SettingFragment fragment, Context context, int listItemResourceId, ArrayList<Pair<News, String>> newsList) {
        super(context, listItemResourceId, newsList);
        this.fragment = fragment;
        this.context = context;
        this.listItemResourceId = listItemResourceId;
        this.newsList = newsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pair<News, String> nowPair = newsList.get(position);
        final News nowNews = nowPair.first;
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(listItemResourceId, null);

        if (nowNews == null) {
            view.findViewById(R.id.likeListItemMain).setVisibility(View.GONE);
            view.findViewById(R.id.emptyLike).setVisibility(View.VISIBLE);
            return view;
        }

        ((TextView)view.findViewById(R.id.likeListItemMain).findViewById(R.id.likeListInfoMain).findViewById(R.id.title)).setText(nowNews.title);
        ((TextView)view.findViewById(R.id.likeListItemMain).findViewById(R.id.likeListInfoMain).findViewById(R.id.category)).setText(nowPair.second);

        view.findViewById(R.id.likeListItemMain).findViewById(R.id.removeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleLikeListClicked(nowPair, SettingFragment.LIKELIST_REMOVE);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.handleLikeListClicked(nowPair, SettingFragment.LIKELIST_ENTER);
            }
        });
        return view;
    }

}
