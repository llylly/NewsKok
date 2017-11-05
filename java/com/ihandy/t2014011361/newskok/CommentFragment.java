package com.ihandy.t2014011361.newskok;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.maxwin.view.XListView;

public class CommentFragment extends Fragment implements XListView.IXListViewListener {

    public static final int FINISH_LOADING = 1;
    public static final int FAIL_LOADING = 2;
    public static final int FINISH_ADD = 3;
    public static final int FAIL_ADD = 4;

    private News news;

    private View view;
    private XListView listView;
    private String refreshTimeStr;

    private CommentListAdapter adapter;
    private CommentListManager manager;
    private ArrayList<Comment> commentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        news = (News)getArguments().getSerializable("news");

        view = inflater.inflate(R.layout.comment_fragment, container, false);
        view.findViewById(R.id.sideLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit);
                fragmentTransaction.remove(CommentFragment.this);
                fragmentTransaction.commit();
            }
        });
        // button to open comment dialog
        view.findViewById(R.id.addCommentBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                view.findViewById(R.id.addCommentFrame).setVisibility(View.VISIBLE);
            }
        });
        // comment dialog close
        view.findViewById(R.id.addCommentFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                view.findViewById(R.id.addCommentFrame).setVisibility(View.GONE);
            }
        });
        // button to add comment
        view.findViewById(R.id.commentPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                addComment(
                        ((EditText)view.findViewById(R.id.nickNameEdit)).getText().toString(),
                        ((EditText)view.findViewById(R.id.commentEdit)).getText().toString());
            }
        });

        refreshTimeStr = getActivity().getResources().getString(R.string.not_refresh_yet);
        listView = (XListView)view.findViewById(R.id.commentList);
        listView.setRefreshTime(refreshTimeStr);
        listView.setPullLoadEnable(true);
        listView.setXListViewListener(this);

        commentList = new ArrayList<>();
        manager = new CommentListManager(handler, commentList, news.news_id);
        adapter = new CommentListAdapter(getActivity(), R.layout.comment_item, commentList);
        listView.setAdapter(adapter);

        manager.refresh();

        return view;
    }

    @Override
    public void onRefresh() {
        refreshTimeStr = new SimpleDateFormat().format(new Date());
        adapter.notifyDataSetInvalidated();
        manager.refresh();
    }

    @Override
    public void onLoadMore() {
        adapter.notifyDataSetChanged();
        manager.getMore();
    }

    private void addComment(String nickName, String comment) {
        if ((nickName == null) || (nickName.equals(""))) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.nickname_empty), Toast.LENGTH_SHORT).show();
        }
        if ((comment == null) || (comment.equals(""))) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.comment_empty), Toast.LENGTH_SHORT).show();
        }
        manager.addComment(nickName, comment);
        view.findViewById(R.id.addCommentFrame).setVisibility(View.GONE);

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.findViewById(R.id.commentEdit).getWindowToken(), 0);

        ((EditText)view.findViewById(R.id.nickNameEdit)).setText("");
        ((EditText)view.findViewById(R.id.commentEdit)).setText("");
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CommentFragment.FAIL_LOADING) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.comment_failed), Toast.LENGTH_SHORT).show();
            }
            if (msg.what == CommentFragment.FINISH_LOADING) {
                commentList = manager.arr;
                adapter.clear();
                adapter.addAll(commentList);
                adapter.notifyDataSetChanged();
                listView.stopRefresh();
                listView.stopLoadMore();
            }
            if (msg.what == CommentFragment.FINISH_ADD) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.comment_add_success), Toast.LENGTH_SHORT).show();
            }
            if (msg.what == CommentFragment.FAIL_ADD) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.comment_add_fail), Toast.LENGTH_SHORT).show();
            }
        }
    };

}
