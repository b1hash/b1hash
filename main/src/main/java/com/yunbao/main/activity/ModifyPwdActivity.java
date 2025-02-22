package com.yunbao.main.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.event.RegSuccessEvent;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2018/10/7.
 * 重置密码
 */

public class ModifyPwdActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditOld;
    private EditText mEditNew;
    private EditText mEditConfirm;
    private View mBtnConfirm;
    private boolean mUseCountryCode;//是否能选择国家代号

    @Override
    protected int getLayoutId() {
        return R.layout.activity_modify_pwd;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.modify_pwd));
        mEditOld = (EditText) findViewById(R.id.edit_old);
        mEditNew = (EditText) findViewById(R.id.edit_new);
        mEditConfirm = (EditText) findViewById(R.id.edit_confirm);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(this);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String oldPwd = mEditOld.getText().toString();
                String newPwd = mEditNew.getText().toString();
                String confirmPwd = mEditConfirm.getText().toString();
                mBtnConfirm.setEnabled(!TextUtils.isEmpty(oldPwd) && !TextUtils.isEmpty(newPwd) && !TextUtils.isEmpty(confirmPwd));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mEditOld.addTextChangedListener(textWatcher);
        mEditNew.addTextChangedListener(textWatcher);
        mEditConfirm.addTextChangedListener(textWatcher);
        TextView btnForgetPwd = findViewById(R.id.btn_forget_pwd);
        btnForgetPwd.setOnClickListener(this);
        btnForgetPwd.setText(StringUtil.contact(WordUtil.getString(R.string.find_pwd_forget), "?"));
        EventBus.getDefault().register(this);
        MainHttpUtil.getLoginInfo(new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mUseCountryCode = obj.getIntValue("sendcode_type") == 1;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_forget_pwd) {
            Intent intent = new Intent(mContext, FindPwdActivity.class);
            intent.putExtra(Constants.FROM_LOGIN, false);
            intent.putExtra(Constants.TIP, mUseCountryCode);
            startActivity(intent);
        } else if (i == R.id.btn_confirm) {
            modify();
        }
    }

    private void modify() {
        String pwdOld = mEditOld.getText().toString().trim();
//        if (TextUtils.isEmpty(pwdOld)) {
//            mEditOld.setError(WordUtil.getString(R.string.modify_pwd_old_1));
//            return;
//        }
        String pwdNew = mEditNew.getText().toString().trim();
//        if (TextUtils.isEmpty(pwdNew)) {
//            mEditNew.setError(WordUtil.getString(R.string.modify_pwd_new_1));
//            return;
//        }
        String pwdConfirm = mEditConfirm.getText().toString().trim();
//        if (TextUtils.isEmpty(pwdConfirm)) {
//            mEditConfirm.setError(WordUtil.getString(R.string.modify_pwd_confirm_1));
//            return;
//        }
//        if (!pwdNew.equals(pwdConfirm)) {
//            mEditConfirm.setError(WordUtil.getString(R.string.reg_pwd_error));
//            return;
//        }
        MainHttpUtil.modifyPwd(pwdOld, pwdNew, pwdConfirm, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                    finish();
                } else {
                    ToastUtil.show(msg);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.MODIFY_PWD);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegSuccessEvent(RegSuccessEvent e) {
        finish();
    }

}
