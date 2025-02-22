package com.yunbao.main.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.custom.SplitEditText;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

/**
 * Created by 云豹科技 on 2022/6/6.
 * 青少年模式修改密码
 */
public class TeenagerModifyPwdActivity extends AbsActivity implements SplitEditText.OnInputListener {

    public static void forward(Context context) {
        Intent intent = new Intent(context, TeenagerModifyPwdActivity.class);
        context.startActivity(intent);
    }

    private InputMethodManager imm;
    private SplitEditText mEditText1;
    private SplitEditText mEditText2;
    private SplitEditText mEditText3;
    private View mBtnUse;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_teenager_modify_pwd;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.a_119));
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        mEditText1 = findViewById(R.id.edit_1);
        mEditText1.setOnInputListener(this);


        mEditText2 = findViewById(R.id.edit_2);
        mEditText2.setOnInputListener(this);

        mEditText3 = findViewById(R.id.edit_3);
        mEditText3.setOnInputListener(this);

        bindView(mEditText1, R.id.wrap_1);
        bindView(mEditText2, R.id.wrap_2);
        bindView(mEditText3, R.id.wrap_3);

        mBtnUse = findViewById(R.id.btn_use);
        mBtnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyPwd();
            }
        });

    }


    private void bindView(final SplitEditText editText, int wrapId) {
        View wrap = findViewById(wrapId);
        wrap.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }


    @Override
    public void onInputFinished(String content) {

    }

    @Override
    public void onInputChanged(String text) {
        String originPwd = mEditText1.getText().toString();
        String newPwd1 = mEditText2.getText().toString();
        String newPwd2 = mEditText3.getText().toString();
        mBtnUse.setEnabled(originPwd.length() == 4 && newPwd1.length() == 4 && newPwd2.length() == 4);

    }



    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.MODIFY_TEENAGER_PWD);
        super.onDestroy();
    }



    private void modifyPwd(){
        String originPwd = mEditText1.getText().toString();
        String newPwd1 = mEditText2.getText().toString();
        String newPwd2 = mEditText3.getText().toString();

        if (!newPwd1.equals(newPwd2)) {
            ToastUtil.show(R.string.reg_pwd_error);
            return;
        }
        MainHttpUtil.modifyTeenagerPwd(originPwd, newPwd1, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(findViewById(R.id.edit_1).getWindowToken(), 0);
                    }
                    finish();
                }
                ToastUtil.show(msg);
            }
        });

    }
}
