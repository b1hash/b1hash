package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LiveClassBean;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.MainHomeLiveClassAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/6/1.
 * 全部分类
 */
public class AllLiveClassActivity extends AbsActivity {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, AllLiveClassActivity.class));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_all_live_class;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.a_106));
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 5, GridLayoutManager.VERTICAL, false));
        List<LiveClassBean> classList = null;
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            classList = configBean.getLiveClass();
        }
        if (classList == null) {
            classList = new ArrayList<>();
        }
        MainHomeLiveClassAdapter classAdapter = new MainHomeLiveClassAdapter(mContext, classList);
        classAdapter.setOnItemClickListener(new OnItemClickListener<LiveClassBean>() {
            @Override
            public void onItemClick(LiveClassBean bean, int position) {
                if (!canClick()) {
                    return;
                }
                LiveClassActivity.forward(mContext, bean.getId(), bean.getName());
            }
        });
        recyclerView.setAdapter(classAdapter);
    }


}
