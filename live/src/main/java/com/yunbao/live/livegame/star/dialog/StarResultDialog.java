package com.yunbao.live.livegame.star.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.live.R;
import com.yunbao.live.livegame.star.adapter.StarResultAdapter;
import com.yunbao.live.livegame.star.bean.StarResultBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class StarResultDialog extends AbsDialogFragment implements View.OnClickListener {

    private List<StarResultBean> mResultList;
    private StarResultAdapter mAdapter;
    private ActionListener mActionListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_game_star_result;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.animCenter);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.y = DpUtil.dp2px(80);
        window.setAttributes(params);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 15, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        recyclerView.addItemDecoration(decoration);
        if (mResultList == null) {
            mResultList = new ArrayList<>();
        }
        mAdapter = new StarResultAdapter(mContext, mResultList);
        recyclerView.setAdapter(mAdapter);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_again).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        } else if (i == R.id.btn_again) {
            dismiss();
            if (mActionListener != null) {
                mActionListener.againGame();
            }
        }
    }

    public void setResultList(List<StarResultBean> resultList) {
        mResultList = resultList;
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void onDestroy() {
        mActionListener = null;
        super.onDestroy();
    }

    public interface ActionListener {
        void againGame();
    }
}
