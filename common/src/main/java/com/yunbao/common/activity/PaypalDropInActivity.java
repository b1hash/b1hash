package com.yunbao.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalPaymentIntent;

public class PaypalDropInActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String braintreeToken = intent.getStringExtra("braintreeToken");
        String money = intent.getStringExtra("money");
        String goodsName = intent.getStringExtra("goodsName");

        PayPalCheckoutRequest payPalRequest = new PayPalCheckoutRequest(money);
        payPalRequest.setLocaleCode("en_US");
        payPalRequest.setCurrencyCode("USD");
        payPalRequest.setDisplayName(goodsName);
        payPalRequest.setIntent(PayPalPaymentIntent.SALE);

        DropInRequest dropInRequest = new DropInRequest();

        dropInRequest.setPayPalDisabled(false);
        dropInRequest.setGooglePayDisabled(true);
        dropInRequest.setVenmoDisabled(true);
        dropInRequest.setCardDisabled(true);

        DropInClient dropInClient = new DropInClient(this, braintreeToken, dropInRequest);
        dropInClient.launchDropInForResult(this, 1000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String nonce = null;
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            if (data != null) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                if (result != null && result.getPaymentMethodNonce() != null) {
                    nonce = result.getPaymentMethodNonce().getString();
                }
            }
        }
        if (!TextUtils.isEmpty(nonce)) {
            Intent intent = new Intent();
            intent.putExtra("nonce", nonce);
            setResult(RESULT_OK, intent);
        }
        finish();
    }


}
