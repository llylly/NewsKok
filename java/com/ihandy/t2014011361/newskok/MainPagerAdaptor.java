package com.ihandy.t2014011361.newskok;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainPagerAdaptor extends PagerAdapter {

    public ArrayList<NewsListView> viewList;

    MainPagerAdaptor(ArrayList<NewsListView> _viewList) {
        super();
        viewList = _viewList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewList.get(position));
        return viewList.get(position).categoryName;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        if ((view != null) && (view instanceof NewsListView))
            return ((NewsListView) view).categoryName.equals(object);
        else
            return false;
    }

    @Override
    public int getItemPosition(Object object) {
        for (int i = 0; i < viewList.size(); ++i)
            if (viewList.get(i).categoryName.equals(object))
                return i;
        return -1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return viewList.get(position).categoryName;
    }

}
