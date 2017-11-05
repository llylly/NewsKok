package com.ihandy.t2014011361.newskok;

import android.os.Handler;
import android.os.Message;

abstract public class CategoryRequester {

    final public static int CATEGORY_FINISH = 1;
    final public static int CATEGORY_FAILED = 2;

    abstract public Message refreshCategories();

}
