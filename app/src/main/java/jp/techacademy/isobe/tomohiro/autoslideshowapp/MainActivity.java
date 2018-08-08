package jp.techacademy.isobe.tomohiro.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    ImageView mImageView;
    Button mPrevButton;
    Button mStartButton;
    Button mNextButton;
    TextView mTextView;

    List<Uri> UriList = new ArrayList<Uri>();
    int UriIndex = 0;

    boolean auto = false;

    Timer mTimer;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);

        // Android6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // パーミッションの許可状態
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可
                getContentsInfo();
            } else {
                // 不許可のためダイアログ表示
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            getContentsInfo();
        }


        mPrevButton = (Button) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prev();
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                next();
            }
        });

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 再生、停止の場合分け
                if (auto) {

                    autoStop();
                } else {

                    autoStart();
                }

            }
        });
    }


    // requestPermissionsからのコールバック
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        try {
            switch (requestCode) {
                case PERMISSION_REQUEST_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getContentsInfo();
                    } else {
                        Toast.makeText(this,"アクセスすることができません", Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

            e.getMessage();
        }

    }


    private void getContentsInfo() {

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
     if (cursor.moveToFirst()) {

      do {
         int index = cursor.getColumnIndex(MediaStore.Images.Media._ID);
         Long id = cursor.getLong(index);
         Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
         mImageView = (ImageView) findViewById(R.id.imageView);
         UriList.add(imageUri);

        } while (cursor.moveToNext());

    }
        cursor.close();

        // 画像の有る無しでボタンの有効、無効の場合分け & エラーが出たので例外処理
        try {
            if (UriList.size() != 0) {
                mNextButton.setEnabled(true);
                mPrevButton.setEnabled(true);
                mStartButton.setEnabled(true);
                mImageView.setImageURI(UriList.get(UriIndex));
            } else {
                mNextButton.setEnabled(false);
                mPrevButton.setEnabled(false);
                mStartButton.setEnabled(false);
            }
        } catch (NullPointerException e) {

            e.getMessage();
        }
            setImageNumber();
    }

    private void setImageNumber() {
        if (UriList.size() == 0) {
            mTextView.setText("0 / 0");
        } else {
            mTextView.setText("<" + (UriIndex + 1) + "枚目" + "/" + UriList.size() + "枚中>");
        }
    }



    // 次の画像を表示
    private void next() {
        if (UriIndex + 1 != UriList.size()) {
            UriIndex++;

        } else {
            // 最初の画像を表示
            UriIndex = 0;
        }
        mImageView.setImageURI(UriList.get(UriIndex));

        setImageNumber();
    }

    private void prev() {
        if (UriIndex == 0) {
            // 最後の画像を表示
            UriIndex = UriList.size() - 1;
        } else {
            // 前の画像を表示
            UriIndex--;
        }
        mImageView.setImageURI(UriList.get(UriIndex));

        setImageNumber();
    }

    // 再生
    private void autoStart() {
        // ボタンのテキスト書き換え、他の２つのボタンを無効化
        mStartButton.setText("停止");
        mNextButton.setEnabled(false);
        mPrevButton.setEnabled(false);
        auto = true;

        // nullチェック、これをしないとエラーになる
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            next();
                        }
                    });
                }
            }, 2000, 2000);
        }

    }

    // 停止
    private void autoStop() {

        mStartButton.setText("再生");
        mNextButton.setEnabled(true);
        mPrevButton.setEnabled(true);
        auto = false;

        if (mTimer != null) {

            mTimer.cancel();
            mTimer = null;
        }

    }


}
