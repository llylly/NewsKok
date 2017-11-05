package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    Context context;
    int listItemResourceId;
    ArrayList<Comment> commentList;

    public CommentListAdapter(Context context, int listItemResourceId, ArrayList<Comment> commentList) {
        super(context, listItemResourceId, commentList);
        this.context = context;
        this.listItemResourceId = listItemResourceId;
        this.commentList = commentList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.comment_item, null);
        } else
            view = convertView;
        Comment comment = commentList.get(position);
        ((TextView)view.findViewById(R.id.floorNum)).setText(comment.floor);
        ((TextView)view.findViewById(R.id.nickName)).setText(comment.nickName);
        ((TextView)view.findViewById(R.id.comment)).setText(comment.comment);
        return view;
    }

}
