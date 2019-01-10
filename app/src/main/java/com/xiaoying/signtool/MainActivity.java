package com.xiaoying.signtool;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    private static final int RC_HISTORY = 1000;
    private static final int RC_SELECT_APP = 1001;

    private EditText mEtInput;
    private TextView mTvMsg;
    private TextView mTvKeyHash;
    private TextView mTvMD5;
    private TextView mTvSHA1;
    private TextView mTvSHA256;

    private String mLineSeparator = System.getProperty("line.separator", "\n");

    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSp = getSharedPreferences("app_data", Context.MODE_PRIVATE);

        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RC_HISTORY == requestCode || RC_SELECT_APP == requestCode) {
            if(RESULT_OK == resultCode) {
                final String packageName = data.getStringExtra(EXTRA_PACKAGE_NAME);
                if(TextUtils.isEmpty(packageName)) {
                    return;
                }
                if(null != mEtInput) {
                    mEtInput.setText(packageName);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_signature_key_hash:
                hideSoftKeyboard(MainActivity.this, v);
                getSignatureInfo();
                break;
            case R.id.tv_key_hash:
            case R.id.tv_md5_fingerprint:
            case R.id.tv_sha1_fingerprint:
            case R.id.tv_sha256_fingerprint:
                if(v instanceof TextView) {
                    copyToClipboard(((TextView) v).getText().toString());
                }
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_history:
                // 历史记录
                hideSoftKeyboard(MainActivity.this, mEtInput);
                startActivityForResult(new Intent(MainActivity.this, HistoryActivity.class), RC_HISTORY);
                return true;
            case R.id.action_main_select_app:
                hideSoftKeyboard(MainActivity.this, mEtInput);
                startActivityForResult(new Intent(MainActivity.this, SelectAppActivity.class), RC_HISTORY);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        mEtInput = findViewById(R.id.et_input);
        mTvMsg = findViewById(R.id.tv_msg);
        mTvKeyHash = findViewById(R.id.tv_key_hash);
        mTvMD5 = findViewById(R.id.tv_md5_fingerprint);
        mTvSHA1 = findViewById(R.id.tv_sha1_fingerprint);
        mTvSHA256 = findViewById(R.id.tv_sha256_fingerprint);

        rippleView(mTvKeyHash);
        rippleView(mTvMD5);
        rippleView(mTvSHA1);
        rippleView(mTvSHA256);
    }

    /**
     * 获取签名信息
     */
    private void getSignatureInfo() {
        final String packageName = mEtInput.getText().toString().trim();
        if(TextUtils.isEmpty(packageName)) {
            mTvMsg.setText(R.string.package_name_hint);
            return;
        }
        mTvMsg.setText(String.format(getResources().getString(R.string.package_name_format), packageName));
        mTvMsg.append(mLineSeparator);

        // save to history
        save(packageName);

        mTvKeyHash.setText("");
        mTvMD5.setText("");
        mTvSHA1.setText("");
        mTvSHA256.setText("");

        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            String keyHash;
            String md5FingerPrint;
            String sha1FingerPrint;
            String sha256FingerPrint;
            MessageDigest md;
            for (Signature signature : info.signatures) {
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash:", keyHash);
                mTvKeyHash.setText(keyHash);

                md = MessageDigest.getInstance("MD5");
                md.update(signature.toByteArray());
                md5FingerPrint = convert2HexFormatted(md.digest());
                Log.e("MD5:", md5FingerPrint);
                mTvMD5.setText(md5FingerPrint);

                md = MessageDigest.getInstance("SHA-1");
                md.update(signature.toByteArray());
                sha1FingerPrint = convert2HexFormatted(md.digest());
                Log.e("SHA-1:", sha1FingerPrint);
                mTvSHA1.setText(sha1FingerPrint);

                md = MessageDigest.getInstance("SHA-256");
                md.update(signature.toByteArray());
                sha256FingerPrint = convert2HexFormatted(md.digest());
                Log.e("SHA-256:", sha256FingerPrint);
                mTvSHA256.setText(sha256FingerPrint);

                mTvMsg.append(mLineSeparator);
                mTvMsg.append(getResources().getString(R.string.tips_click_to_copy_to_clipboard));

            }
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing
            e.printStackTrace();
            mTvMsg.append(getResources().getString(R.string.error_package_not_found));
        } catch (NoSuchAlgorithmException e) {
            // do nothing
            e.printStackTrace();
            mTvMsg.setText(getResources().getString(R.string.error_get_signature_failed));
        }

    }


    /**
     * 将签名信息的byte数组转换成16进制字符串
     * @param array 签名byte数组
     * @return 十六进制字符串，每两位用":"分隔开
     */
    private static String convert2HexFormatted(byte[] array) {
        if(null == array) {
            return null;
        }

        StringBuilder str = new StringBuilder();
        String hex;
        int hexLength;
        for(byte b : array) {
            hex = Integer.toHexString(b);
            hexLength = hex.length();
            if(hexLength < 2) {
                // 位数小于2，左边加0补足成两位
                hex = "0" + hex;
            }
            if(hexLength > 2) {
                // 位数大于2，截取最后两位
                hex = hex.substring(hexLength - 2, hexLength);
            }
            str.append(hex.toUpperCase())
                    .append(":"); // 添加":"分隔符
        }
        str.replace(str.length() - 1, str.length(), ""); // 去掉最后一个多余的":"分割符
        return str.toString();
    }

    /**
     * 复制到剪切板
     * @param content 复制的内容
     */
    private void copyToClipboard(CharSequence content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(null == cm) {
            Toast.makeText(MainActivity.this, R.string.fail_to_copy_to_clipboard, Toast.LENGTH_SHORT).show();
            return;
        }
        // 将文本内容放到系统剪贴板里。
        cm.setPrimaryClip(ClipData.newPlainText("text", content));
        Toast.makeText(MainActivity.this, R.string.already_copy_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void rippleView(View view) {
        MaterialRippleLayout.on(view)
                .rippleOverlay(true)
                .rippleHover(true)
                .rippleAlpha(0.2f)
                .create();
    }

    private void save(String packageName) {
        long time = System.currentTimeMillis();
        mSp.edit().putLong(packageName, time).apply();
    }



    /**
     * 隐藏输入软键盘
     * @param context Context对象
     * @param view 当前页面有效的View控件对象
     */
    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm && imm.isActive()) {
            if (view.getWindowToken() != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
