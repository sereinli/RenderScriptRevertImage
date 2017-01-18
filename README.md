# RenderScriptRevertImage
Render Script图片颜色反转性能测试。

#首先需要Render Script支持，在build.gradle中添加配置
```
defaultConfig {
        applicationId "me.rain.android.renderscriptrevertimage"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

        //renderscript support-v8
        renderscriptTargetApi 23
        renderscriptSupportModeEnabled  true
    }
```
#新建RenderScript文件
右键->New->Folder->RenderScript Folder，然后再该目录下新建imagerevert.rs文件，输入代码：
```
#pragma version(1)
#pragma rs java_package_name(me.rain.android.learning)
uchar4 __attribute__((kernel)) invert(uchar4 in)
{
  uchar4 out = in;
  out.r = 255- in.r;
  out.g = 255-in.g;
  out.b = 255-in.b;
  return out;

}
```
#Bitmap反转
普通的做法：
```
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
```
render script的实现方式，
编译后会生成ScriptC_imagerevert.java，然后即可调用该类的接口
```
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
```
通过简单对比，render script反转图片的时间为普通方式时间的1/8~1/10,速度差异明显
