package com.yunbao.main.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.main.R;

/**
 * Created by 云豹科技 on 2022/6/6.
 */
public class TeenagerTipActivity extends AbsActivity implements View.OnClickListener {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_teenager_tip;
    }

    @Override
    protected void main() {
        String tip = getIntent().getStringExtra(Constants.TIP);
        TextView tvContent = findViewById(R.id.content);
        tvContent.setText(tip);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_cancel) {
            finishApp();
        } else if (i == R.id.btn_confirm) {
            TeenagerActivity.forward(mContext);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    public void finishApp() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(Constants.EXIT, true);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
