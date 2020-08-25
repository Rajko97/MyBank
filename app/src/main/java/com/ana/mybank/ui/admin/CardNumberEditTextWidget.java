package com.ana.mybank.ui.admin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

public class CardNumberEditTextWidget extends TextInputEditText {
    private String a;
    private int keyDel;
    public CardNumberEditTextWidget(@NonNull Context context) {
        super(context);
    }

    public CardNumberEditTextWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CardNumberEditTextWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onTextChanged(CharSequence s, int start, int lengthBefore, int lengthAfter) {
        boolean flag = true;
        String eachBlock[] = this.getText().toString().split("-");
        for (int i = 0; i < eachBlock.length; i++) {
            if (eachBlock[i].length() > 4) {
                flag = false;
            }
        }
        if (flag) {
            this.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_DEL)
                        keyDel = 1;
                    return false;
                }
            });

            if (keyDel == 0) {
                if (((this.getText().length() + 1) % 5) == 0) {
                    if (this.getText().toString().split("-").length <= 3) {
                        this.setText(this.getText() + "-");
                        this.setSelection(this.getText().length());
                    }
                }
                a = this.getText().toString();
            } else {
                a = this.getText().toString();
                keyDel = 0;
            }

        } else {
            setText(a);
        }
    }

}
