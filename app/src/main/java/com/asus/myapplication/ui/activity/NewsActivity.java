package com.asus.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.asus.myapplication.Imageloader.MainActivity;
import com.asus.myapplication.R;
import com.asus.myapplication.adapter.NewsAdapter;
import com.asus.myapplication.api.INewsService;
import com.asus.myapplication.model.NewItem;
import com.asus.myapplication.model.News;
import com.asus.myapplication.util.BaseResp;
import com.asus.myapplication.util.HRetrofitNetHelper;
import java.util.ArrayList;
import java.util.List;


import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;

public class NewsActivity extends BaseActivity implements HRetrofitNetHelper.RetrofitCallBack<News> {
    @Bind(R.id.id_news_recycler_view)
    RecyclerView idNewsRecyclerView;
    @Bind(R.id.swipeRefreshLayout)
    android.support.v4.widget.SwipeRefreshLayout swipeRefreshLayout;
    boolean isLoading;
    private String mUserId;
    private List<NewItem> mDataList;
    private NewsAdapter mNewsAdapter;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);


        mUserId = getIntent().getStringExtra("intent_user_id");
        mDataList = new ArrayList<>();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.clear();
                        getData();

                    }
                }, 2000);
            }
        });

        final LinearLayoutManager manager = new LinearLayoutManager(NewsActivity.this);
        idNewsRecyclerView.setLayoutManager(manager);
        mNewsAdapter = new NewsAdapter(NewsActivity.this, mDataList);
        idNewsRecyclerView.setAdapter(mNewsAdapter);
        loadData();
        idNewsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("test", "StateChanged = " + newState);


            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("test", "onScrolled");
                int lastVisibleItemPosition = manager.findLastVisibleItemPosition();
                if (lastVisibleItemPosition + 1 == mNewsAdapter.getItemCount()) {
                    Log.d("test", "loading executed");

                    boolean isRefreshing = swipeRefreshLayout.isRefreshing();
                    if (isRefreshing) {
                        mNewsAdapter.notifyItemRemoved(mNewsAdapter.getItemCount());
                        return;
                    }
                    if (!isLoading) {
                        isLoading = true;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getData();
                                Log.d("test", "load more completed");
                                isLoading = false;
                            }
                        }, 1000);
                    }
                }
            }
        });
        mNewsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Log.d("test", "item position = " + position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }


    private void getData() {
        for (int i = 0; i < 3; i++) {
            NewItem map = new NewItem();
            mDataList.add(map);
        }
        mNewsAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        mNewsAdapter.notifyItemRemoved( mNewsAdapter.getItemCount());

    }
    private void loadData() {
        mDialog.setMessage("正在加载中，请稍后...");
        mDialog.show();
        INewsService newService = retrofitNetHelper.getAPIService(INewsService.class);
        Log.d("zgx", "mUserId=====" + mUserId);
        final Call<BaseResp<News>> repos = newService.userNews(mUserId);
        retrofitNetHelper.enqueueCall(repos, this);
    }

    @Override
    public void onSuccess(BaseResp<News> baseResp) {
        mDialog.dismiss();
        mDataList.clear();
        mDataList.addAll(baseResp.getData().getNewsItem());
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(String error) {
        mDialog.dismiss();
        Toast.makeText(NewsActivity.this, "请求出现异常" + error, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(0, 1, 1, "加载图片");
        menu.add(0,2,2,"退出");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == 1) {//重新登入
            Intent intent = new Intent();
            intent.setClass(NewsActivity.this,
                    MainActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == 2) {//退出
            System.exit(0);
        }
        return true;
    }
}
