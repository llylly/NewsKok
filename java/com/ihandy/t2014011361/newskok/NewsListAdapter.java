package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewsListAdapter extends ArrayAdapter<News> {

    final public static int EMPTY_LAYOUT = 0;
    final public static int ENG_LAYOUT = 1;
    final public static int CHN_LAYOUT = 2;

    MainActivity context;
    int listItemResourceId;
    ArrayList<News> newsList;

    public NewsListAdapter(MainActivity context, int listItemResourceId, ArrayList<News> newsList) {
        super(context, listItemResourceId, newsList);
        this.context = context;
        this.listItemResourceId = listItemResourceId;
        this.newsList = newsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final News news = getItem(position);
        View view;
        ViewHolder viewHolder;
        int nowType = -1;
        if (news instanceof EmptyNews) {
            nowType = EMPTY_LAYOUT;
        } else if (news.source_id == Config.ENGSOURCEID) {
            nowType = ENG_LAYOUT;
        } else if (news.source_id == Config.CHNSOURCEID) {
            nowType = CHN_LAYOUT;
        }
        if ((convertView == null) || (((ViewHolder)convertView.getTag()).type != nowType)) {
            view = LayoutInflater.from(context).inflate(listItemResourceId, null);
            viewHolder = new ViewHolder();
            switch (nowType) {
                case EMPTY_LAYOUT:
                    viewHolder.type = EMPTY_LAYOUT;

                    view.findViewById(R.id.noNews).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.engLayout).setVisibility(View.GONE);
                    view.findViewById(R.id.chnLayout).setVisibility(View.GONE);
                    break;
                case ENG_LAYOUT:
                    viewHolder.type = ENG_LAYOUT;
                    viewHolder.rootLayout = (LinearLayout)view.findViewById(R.id.engLayout);
                    View eng0Layout = viewHolder.rootLayout.findViewById(R.id.eng0Layout);
                    View eng1Layout = eng0Layout.findViewById(R.id.eng1Layout);
                    viewHolder.star = (ImageView)viewHolder.rootLayout.findViewById(R.id.engStar);
                    viewHolder.title = (TextView)eng1Layout.findViewById(R.id.engTitle);
                    viewHolder.time = (TextView)eng1Layout.findViewById(R.id.engTime);
                    viewHolder.img = (ImageView)eng0Layout.findViewById(R.id.engImg);
                    viewHolder.intro = null;
                    viewHolder.source = (TextView)viewHolder.rootLayout.findViewById(R.id.engSource);

                    view.findViewById(R.id.noNews).setVisibility(View.GONE);
                    view.findViewById(R.id.engLayout).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.chnLayout).setVisibility(View.GONE);
                    break;
                case CHN_LAYOUT:
                    viewHolder.type = CHN_LAYOUT;
                    viewHolder.rootLayout = (LinearLayout)view.findViewById(R.id.chnLayout);
                    View chn0Layout = viewHolder.rootLayout.findViewById(R.id.chn0Layout);
                    View chn1Layout = chn0Layout.findViewById(R.id.chn1Layout);
                    viewHolder.star = (ImageView)viewHolder.rootLayout.findViewById(R.id.chnStar);
                    viewHolder.title = (TextView)viewHolder.rootLayout.findViewById(R.id.chnTitle);
                    viewHolder.time = (TextView)viewHolder.rootLayout.findViewById(R.id.chnTime);
                    viewHolder.img = (ImageView)chn0Layout.findViewById(R.id.chnImg);
                    viewHolder.intro = (TextView)chn1Layout.findViewById(R.id.chnIntro);
                    viewHolder.source = (TextView)chn1Layout.findViewById(R.id.chnSource);

                    view.findViewById(R.id.noNews).setVisibility(View.GONE);
                    view.findViewById(R.id.engLayout).setVisibility(View.GONE);
                    view.findViewById(R.id.chnLayout).setVisibility(View.VISIBLE);
                    break;
            }
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        if (news instanceof EmptyNews) {
            return view;
        }

        if (news.liked == 0) {
            viewHolder.rootLayout.setBackground(context.getResources().getDrawable(R.drawable.item_unlike_bg));
            viewHolder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.star_uncheck));
            viewHolder.star.setVisibility(View.GONE);
        } else {
            viewHolder.rootLayout.setBackground(context.getResources().getDrawable(R.drawable.item_like_bg));
            viewHolder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.star_check));
            viewHolder.star.setVisibility(View.VISIBLE);
        }
        viewHolder.title.setText(news.title);
        viewHolder.time.setText(new SimpleDateFormat().format(new Date(news.time)));
        if ((news.img != null) && (!news.img.equals(""))) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                Bitmap bmp = null;
                bmp = BitmapFactory.decodeStream(context.openFileInput(news.img), null, opts);
                viewHolder.img.setImageBitmap(bmp);
                viewHolder.img.setVisibility(View.VISIBLE);
                viewHolder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.showImageView(news);
                    }
                });
            } catch (FileNotFoundException e) {
                viewHolder.img.setVisibility(View.GONE);
            }
        } else {
            viewHolder.img.setVisibility(View.GONE);
        }
        if ((news.intro != null) && (!news.intro.equals(""))) {
            viewHolder.intro.setText(news.intro);
        } else {
        }
        viewHolder.source.setText(news.region + " " + news.origin);

        return view;
    }

    class ViewHolder {
        int type;
        LinearLayout rootLayout;
        ImageView star;
        TextView title;
        TextView time;
        ImageView img;
        TextView intro;
        TextView source;
    }

}
