package me.rain.android.renderscriptrevertimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.rain.android.learning.ScriptC_imagerevert;

/**
 * Created by CBS on 2017/1/18.
 * 通过对比，使用render script进行同一个图片反转，所需时间差不多是相差10倍
 */

public class RenderScriptActivity extends Activity implements View.OnClickListener {
    private ImageView mSourceImage;
    private ImageView mRevertImage;
    private ImageView mRSRevertImage;
    private TextView mNormalTime;
    private TextView mRSTime;
    private Button mBtnClear;
    private Button mBtnRevert;
    private Button mBtnRenderScript;

    private ScriptC_imagerevert mRenderScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_script);
        initViews();
    }

    private void initViews() {
        mSourceImage = (ImageView)findViewById(R.id.image_original);
        mSourceImage.setImageResource(R.mipmap.artboard);

        mRevertImage = (ImageView)findViewById(R.id.image_normal_revert);
        mNormalTime = (TextView)findViewById(R.id.tv_normal_revert_time);

        mRSRevertImage = (ImageView)findViewById(R.id.image_render_script_revert);
        mRSTime = (TextView)findViewById(R.id.tv_rs_revert_time);

        mBtnClear = (Button)findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);

        mBtnRevert = (Button)findViewById(R.id.btn_normal_revert);
        mBtnRevert.setOnClickListener(this);

        mBtnRenderScript = (Button)findViewById(R.id.btn_render_script_revert);
        mBtnRenderScript.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_clear) {
            clearImageView();
        }else if(view.getId() == R.id.btn_normal_revert) {
            normalRevert();
        }else if(view.getId() == R.id.btn_render_script_revert) {
            renderScriptRevert();
        }
    }

    private void clearImageView() {
        mRevertImage.setImageDrawable(null);
        mRSRevertImage.setImageDrawable(null);

        mNormalTime.setText("");
        mRSTime.setText("");
    }

    /**
     * 遍历图片逐个像素反转
     */
    private void normalRevert() {
        long start = System.currentTimeMillis();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.artboard);
        if(bitmap != null) {
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), bitmap.getConfig());

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = bitmap.getPixel(x, y);
                    int r=  255-(Color.red(color));
                    int g=  255-(Color.green(color));
                    int b=  255-(Color.blue(color));
                    int c = Color.rgb(r, g, b);
                    outBitmap.setPixel(x, y, c);
                }
            }
            mRevertImage.setImageBitmap(outBitmap);
            bitmap.recycle();
        }

        long end = System.currentTimeMillis();
        Log.d("rain", "normal convert time is:"+(end - start));

        mNormalTime.setText("共计耗时" + (end - start) + "ms");
    }

    /**
     * 使用render script进行图片反转
     */
    private void renderScriptRevert() {
        long start = System.currentTimeMillis();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.artboard);
        if(bitmap != null) {
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), bitmap.getConfig());
            RenderScript rs = RenderScript.create(this);
            mRenderScript = new ScriptC_imagerevert(rs);

            Allocation aIn = Allocation.createFromBitmap(rs, bitmap);
            Allocation aOut = Allocation.createFromBitmap(rs, bitmap);


            mRenderScript.forEach_invert(aIn, aOut);
            aOut.copyTo(outBitmap);
            mRSRevertImage.setImageBitmap(outBitmap);
            rs.destroy();

            bitmap.recycle();
        }

        long end = System.currentTimeMillis();
        Log.d("rain", "render script convert time is:"+(end - start));
        mRSTime.setText("共计耗时" + (end - start) + "ms");
    }
}
