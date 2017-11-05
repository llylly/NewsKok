package com.ihandy.t2014011361.newskok;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Handler;

import me.maxwin.view.XListView;

public class SettingFragment extends Fragment implements XListView.IXListViewListener {

    static public final int TAB_SETTING = 1;
    static public final int SOURCE_SETTING = 2;
    static public final int LIKE_SETTING = 3;
    static public final int ABOUT_SETTING = 4;

    static public final int ENABLE_CLICKED = -1;
    static public final int UNABLE_CLICKED = -2;
    static public final int UP_CLICKED = -3;
    static public final int DOWN_CLICKED = -4;

    static public final int LIKELIST_REMOVE  = 1001;
    static public final int LIKELIST_ENTER = 1002;

    public int type;
    public boolean changed = false;

    private View rootView;
    private TextView titleView;
    private LinearLayout settingLayout;
    private View innerLayout;

    private ListView tabListView, sourceListView;
    private XListView likeListView;

    private ArrayList<Pair<String, Integer>> tabList, sourceList;
    private ArrayList<Pair<News, String>> likeList;

    private TabListViewAdapter tabAdapter;
    private SourceListViewAdapter sourceAdapter;
    private LikeListViewAdapter likeListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        type = getArguments().getInt("type");
        View view = inflater.inflate(R.layout.setting_base, container, false);

        view.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).viewPager.setVisibility(View.VISIBLE);
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit);
                fragmentTransaction.remove(SettingFragment.this);
                fragmentTransaction.commit();
            }
        });

        titleView = (TextView)view.findViewById(R.id.settingTitle);
        settingLayout = (LinearLayout)view.findViewById(R.id.settingLayout);
        switch (type) {
            case TAB_SETTING:
                titleView.setText(TApplication.context.getString(R.string.tab_setting));
                innerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.tabsource_setting_view, settingLayout);
                tabSettingInit();
                break;
            case SOURCE_SETTING:
                titleView.setText(TApplication.context.getString(R.string.source_setting));
                innerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.tabsource_setting_view, settingLayout);
                sourceSettingInit();
                break;
            case LIKE_SETTING:
                titleView.setText(TApplication.context.getString(R.string.like_list));
                innerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.like_list_view, settingLayout);
                likeListInit();
                break;
            case ABOUT_SETTING:
                titleView.setText(TApplication.context.getString(R.string.about_setting));
                innerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.about_view, settingLayout);
                break;
        }

        changed = false;
        Log.e(SettingFragment.class.getName(), "Setting fragment start");

        return rootView = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ((type == TAB_SETTING) || (type == SOURCE_SETTING) || (type == LIKE_SETTING))
            if (changed) {
                Log.e(SettingFragment.class.getName(), "Reload category toggled");
                rootView.findViewById(R.id.loadingView).setVisibility(View.VISIBLE);
                ((MainActivity)getActivity()).reloadCategory();
            }
    }

    private void tabSettingInit() {
        tabListView = (ListView)innerLayout.findViewById(R.id.listView);
        tabList = new ArrayList<>();
        tabAdapter = new TabListViewAdapter(this, getActivity(), R.layout.setting_list_item, tabList);
        tabListView.setAdapter(tabAdapter);
        tabRefresh();
    }

    private void tabRefresh() {
        ArrayList<String> showedTabNameList = TApplication.config.categoryNameListShowed;
        ArrayList<String> unabledTabNameList = TApplication.config.unabledCategories;

        tabAdapter.notifyDataSetInvalidated();
        tabList = new ArrayList<>();
        tabList.add(new Pair<>("", TabListViewAdapter.SHOWED_LABEL));
        for (String s : showedTabNameList)
            tabList.add(new Pair<>(s, TabListViewAdapter.SHOWED_ITEM));
        tabList.add(new Pair<>("", TabListViewAdapter.HIDE_LABEL));
        for (String s : unabledTabNameList)
            tabList.add(new Pair<>(s, TabListViewAdapter.HIDE_ITEM));
        tabAdapter.clear();
        tabAdapter.addAll(tabList);
        tabAdapter.notifyDataSetChanged();
    }

    private void sourceSettingInit() {
        sourceListView = (ListView) innerLayout.findViewById(R.id.listView);
        sourceList = new ArrayList<>();
        sourceAdapter = new SourceListViewAdapter(this, getActivity(), R.layout.setting_list_item, sourceList);
        sourceListView.setAdapter(sourceAdapter);
        sourceRefresh();
    }

    private void sourceRefresh() {
        Map<Integer, Source> grossSourceList = TApplication.config.sources;
        ArrayList<String> unabledSource = TApplication.config.unabledSources;

        sourceAdapter.notifyDataSetInvalidated();
        sourceList = new ArrayList<>();
        sourceList.add(new Pair<>("", SourceListViewAdapter.SHOWED_LABEL));
        for (Source s : grossSourceList.values())
            if (!unabledSource.contains(s.name))
                sourceList.add(new Pair<>(s.name, SourceListViewAdapter.SHOWED_ITEM));
        sourceList.add(new Pair<>("", SourceListViewAdapter.HIDE_LABEL));
        for (String s : unabledSource)
            sourceList.add(new Pair<>(s, SourceListViewAdapter.HIDE_ITEM));
        sourceAdapter.clear();
        sourceAdapter.addAll(sourceList);
        sourceAdapter.notifyDataSetChanged();
    }

    private void likeListInit() {
        likeListView = (XListView) innerLayout.findViewById(R.id.likeListView);
        likeList = new ArrayList<>();
        likeListAdapter = new LikeListViewAdapter(this, getActivity(), R.layout.like_list_item, likeList);
        likeListView.setAdapter(likeListAdapter);
        likeListView.setPullLoadEnable(false);
        likeListView.setPullRefreshEnable(true);
        likeListView.setXListViewListener(this);
        likeListRefresh();
    }

    private void likeListRefresh() {
        SQLiteDatabase db = TApplication.sqlHelper.getReadableDatabase();
        Cursor cursor = db.query("LIKES", null, null, null, null, null, null);
        likeListAdapter.notifyDataSetInvalidated();
        likeListAdapter.clear();
        if (cursor.moveToFirst())
            do {
                Cursor c2 =
                        db.query("NEWS", null, "news_id=?", new String[] {cursor.getString(cursor.getColumnIndex("news_id"))}, null, null, null);
                if (c2.moveToFirst()) {
                    News nowNews = News.fromCursor(c2);
                    String categoryName = "";
                    for (Category i : TApplication.config.categories.values())
                        if (i.category.equals(nowNews.category))
                            categoryName = i.category_name;
                    likeList.add(new Pair<>(nowNews, categoryName));
                }

            } while (cursor.moveToNext());
        if (likeList.isEmpty())
            likeList.add(new Pair<News, String>(null, null));
        likeListAdapter.notifyDataSetChanged();
    }

    public void handleTabClicked(String categoryName, int type) {
        changed = true;
        if (type == ENABLE_CLICKED)
            TApplication.config.setCategoryEnablity(categoryName, Config.CATEGORY_ENABLE);
        if (type == UNABLE_CLICKED)
            TApplication.config.setCategoryEnablity(categoryName, Config.CATEGORY_UNABLE);
        if (type == UP_CLICKED)
            TApplication.config.adjustCategoryOrder(categoryName, Config.UPORDER);
        if (type == DOWN_CLICKED)
            TApplication.config.adjustCategoryOrder(categoryName, Config.DOWNORDER);
        tabRefresh();
    }

    public void handleSourceClicked(String sourceName, int type) {
        changed = true;
        if (type == ENABLE_CLICKED)
            TApplication.config.setSourceEnablity(sourceName, Config.SOURCE_ENABLE);
        if (type == UNABLE_CLICKED)
            TApplication.config.setSourceEnablity(sourceName, Config.SOURCE_UNABLE);
        sourceRefresh();
    }

    public void handleLikeListClicked(Pair<News, String> news, int type) {
        if (type == LIKELIST_REMOVE) {
            changed = true;
            news.first.changeLiked();
            likeListRefresh();
        }
        if (type == LIKELIST_ENTER) {
            changed = true;
            ((MainActivity)getActivity()).startWeb(news.first, news.second);
        }
    }

    @Override
    public void onRefresh() {
        likeListRefresh();
        likeListView.setRefreshTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        likeListView.stopRefresh();
    }

    @Override
    public void onLoadMore() {}

}
