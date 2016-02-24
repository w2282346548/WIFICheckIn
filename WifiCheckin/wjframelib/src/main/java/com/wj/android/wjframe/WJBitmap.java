package com.wj.android.wjframe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.wj.android.wjframe.bitmap.BitmapCallBack;
import com.wj.android.wjframe.bitmap.BitmapConfig;
import com.wj.android.wjframe.bitmap.BitmapMemoryCache;
import com.wj.android.wjframe.bitmap.DiskImageRequest;
import com.wj.android.wjframe.bitmap.ImageDisplayer;
import com.wj.android.wjframe.http.Cache;
import com.wj.android.wjframe.http.HttpCallBack;
import com.wj.android.wjframe.http.HttpConfig;
import com.wj.android.wjframe.utils.DensityUtils;
import com.wj.android.wjframe.utils.FileUtils;
import com.wj.android.wjframe.utils.StringUtils;
import com.wj.android.wjframe.utils.SystemTool;
import com.wj.android.wjframe.utils.WJLoger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

import javax.crypto.spec.DESKeySpec;

/**
 * Created by wecan-mac on 15/11/30.
 */
public class WJBitmap {
    private ImageDisplayer displayer;
    private DiskImageRequest diskImageRequest;
    private HashSet<String> currentUrls=new HashSet<>(20);

    public WJBitmap(){
        this(null);
    }
    public WJBitmap(BitmapConfig bitmapConfig){
        this((WJHttp)null,bitmapConfig);
    }

    public WJBitmap(HttpConfig httpConfig,BitmapConfig bitmapConfig){
        this(new WJHttp(httpConfig),bitmapConfig);
    }

    public WJBitmap(WJHttp wjHttp, BitmapConfig bitmapConfig) {
        if(wjHttp ==null){
            wjHttp=new WJHttp();
        }
        if(bitmapConfig==null){
            bitmapConfig=new BitmapConfig();
        }
        if(BitmapConfig.mMemoryCache==null){
            BitmapConfig.mMemoryCache=new BitmapMemoryCache();
        }
        displayer=new ImageDisplayer(wjHttp,bitmapConfig);
    }

    public static class Builder{
        private static final int DEF_WIDTH_HEIGHT=-100;
        private View imageView;
        private String imageUrl;
        private int width=DEF_WIDTH_HEIGHT;
        private int height=DEF_WIDTH_HEIGHT;
        private Drawable loadBitmap;
        private Drawable errorBitmap;

        private  int loadBitmapRes;
        private  int errorBitmapRes;

        private BitmapCallBack callBack;
        private BitmapConfig bitmapConfig;
        private HttpConfig httpConfig;

        public Builder bitmapConfig(BitmapConfig bitmapConfig){
            this.bitmapConfig=bitmapConfig;
            return this;
        }

        public Builder httpConfig(HttpConfig httpConfig){
            this.httpConfig=httpConfig;
            return  this;
        }

        public Builder view(View imageView){
            this.imageView=imageView;
            return this;
        }

        public Builder imageUrl(String imageUrl){
            this.imageUrl=imageUrl;
            return this;
        }

        @Deprecated
        public Builder width(int width){
            this.width=width;
            return this;
        }
        @Deprecated
        public Builder height(int height){
            this.height=height;
            return this;
        }


        public  Builder size(int width,int height){
            this.width=width;
            this.height=height;
            return  this;
        }

        public Builder loadBitmapRes(int loadBitmapRes) {
            this.loadBitmapRes = loadBitmapRes;
            return this;
        }

        public Builder errorBitmapRes(int errorBitmapRes) {
            this.errorBitmapRes = errorBitmapRes;
            return this;
        }

        public Builder loadBitmap(Drawable loadBitmap) {
            this.loadBitmap = loadBitmap;
            return this;
        }

        public Builder errorBitmap(Drawable errorBitmap) {
            this.errorBitmap = errorBitmap;
            return this;
        }

        public Builder callback(BitmapCallBack callback) {
            this.callBack = callback;
            return this;
        }

        public void display(){
            display(new WJBitmap(httpConfig,bitmapConfig));
        }

        public void display(WJBitmap wjBitmap){
            if(imageView==null){
                showLogIfOpen("imageview is null T-T");
                return;
            }
            if (StringUtils.isEmpty(imageUrl)){
                showLogIfOpen("image url is empty");
                doFailure(imageView, errorBitmap, errorBitmapRes);
                if(callBack !=null){
                    callBack.onFailure(new RuntimeException("image url is empty"));
                }
                return;
            }

            if(width ==DEF_WIDTH_HEIGHT && height==DEF_WIDTH_HEIGHT){
                width=imageView.getWidth();
                height=imageView.getHeight();
                if(width==0) {
                    width = DensityUtils.getScreenW(imageView.getContext()) / 2;
                }
                if(height==0){
                    height=DensityUtils.getScreenH(imageView.getContext())/2;
                }
            }
            else if(width==DEF_WIDTH_HEIGHT){
                width =DensityUtils.getScreenW(imageView.getContext());
            }
            else if(height==DEF_WIDTH_HEIGHT){
                height= DensityUtils.getScreenH(imageView.getContext());
            }

            if(loadBitmapRes==0&&loadBitmap==null){
                loadBitmap=new ColorDrawable(0xFFCFCFCF);
            }

            wjBitmap.doDisplay(imageView,imageUrl,width,height,loadBitmap,loadBitmapRes,errorBitmap,errorBitmapRes,callBack);
        }

    }

    ///
    /**
    *真真正加载一个图片
    */
    private void doDisplay(final View imageView,final String imageUrl, int width, int height,
                           final Drawable loadBitmap,final int loadBitmapRes,final Drawable errorBitmap,
                           final int errorBitmapRes,final BitmapCallBack callBack) {

        imageView.setTag(imageUrl);
        BitmapCallBack bitmapCallBack =new BitmapCallBack() {
            @Override
            public void onPreLoad() {
                super.onPreLoad();
                if (getMemoryCache(imageUrl)==null)
                    setImageWithResource(imageView,loadBitmap,loadBitmapRes);
                if(callBack!=null){
                    callBack.onPreLoad();
                }
            }

            @Override
            public void onSuccess(Bitmap bitmap) {
                super.onSuccess(bitmap);
                if(imageUrl.equals(imageView.getTag())){
                    doSuccess(imageView,bitmap,errorBitmap,errorBitmapRes);
                    if (callBack!=null){
                        callBack.onSuccess(bitmap);
                    }
                    currentUrls.add(imageUrl);
                }
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                doFailure(imageView, errorBitmap, errorBitmapRes);
                if (callBack != null) {
                    callBack.onFailure(e);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (callBack != null) {
                    callBack.onFinish();
                }
            }

            @Override
            public void onDoHttp() {
                super.onDoHttp();
                if (callBack != null) {
                    callBack.onDoHttp();
                }
            }
        };
        if(imageUrl.startsWith("http")){
            displayer.get(imageUrl,width,height,bitmapCallBack);
        }else {
            if(diskImageRequest==null){
                diskImageRequest=new DiskImageRequest();
            }
            diskImageRequest.load(imageUrl,width,height,bitmapCallBack);
        }
    }

    /**
     * 如果内存缓存有图片，则显示内存缓存的图片，否则显示默认图片
     *
     * @param imageView    要显示的View
     * @param imageUrl     网络图片地址
     * @param defaultImage 如果没有内存缓存，则显示默认图片
     */
    public void displayCacheOrDefult(View imageView,String imageUrl,int defaultImage){
        Bitmap cache=getMemoryCache(imageUrl);
        doSuccess(imageView,cache,null,defaultImage);
    }

    /**
     * 如果内存缓存有图片，则显示内存缓存的图片，否则显示默认图片
     *
     * @param imageView    要显示的View
     * @param imageUrl     网络图片地址
     * @param defaultImage 如果没有内存缓存，则显示默认图片
     */
    public void displayCacheOrDefult(View imageView, String imageUrl,
                                     Drawable defaultImage) {
        Bitmap cache = getMemoryCache(imageUrl);
        doSuccess(imageView, cache, defaultImage, 0);
    }

    /**
     * 移除一个缓存
     *
     * @param url 哪条url的缓存
     */
    public  void  removeCache(String url){
        BitmapConfig.mMemoryCache.remove(url);
        HttpConfig.mCache.remove(url);
    }

    public void finish(){
        for(String url :currentUrls){
            BitmapConfig.mMemoryCache.remove(url);
        }
        currentUrls=null;
        displayer=null;
        diskImageRequest=null;
    }

    /**
     * 取消一个加载请求
     *
     * @param url 要取消的url
     */
    public  void cancel(String url){
        displayer.cancel(url);
    }

    /**
     * 清空缓存
     */
    public void cleanCache(){
        BitmapConfig.mMemoryCache.clean();
        HttpConfig.mCache.clean();
    }

    /**
     * 获取缓存数据
     *
     * @param url 哪条url的缓存
     * @return 缓存数据的二进制数组
     */
    public byte[] getCache(String url) {
        Cache cache = HttpConfig.mCache;
        cache.initialize();
        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            return entry.data;
        } else {
            return new byte[0];
        }
    }

    /**
     * 获取内存缓存
     *
     * @param url key
     * @return 缓存的bitmap或null
     */
    public static Bitmap getMemoryCache(String url) {
        return BitmapConfig.mMemoryCache.getBitmap(url);
    }

    /**
     * 保存一张图片到本地，并自动通知图库刷新
     *
     * @param url  网络图片链接
     * @param path 保存到本地的绝对路径
     */
    public void saveImage(Context cxt, String url, String path) {
        saveImage(cxt, url, path, true, null);
    }

    /**
     * 保存一张图片到本地
     *
     * @param url  网络图片链接
     * @param path 保存到本地的绝对路径
     * @param cb   保存过程监听器
     */
    public void saveImage(final Context cxt, String url, final String path,
                          final boolean isRefresh, HttpCallBack cb) {
        if (cb == null) {
            cb = new HttpCallBack() {
                @Override
                public void onSuccess(byte[] t) {
                    super.onSuccess(t);
                    if (isRefresh) {
                        refresh(cxt, path);
                    }
                }
            };
        }
        byte[] data = getCache(url);
        if (data.length == 0) {
            new WJHttp().download(path, url, cb);
        } else {
            File file = new File(path);
            cb.onPreStart();
            File folder = file.getParentFile();
            if (folder != null) {
                folder.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    cb.onFailure(-1, e1.getMessage());
                    return;
                }
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(data);
                cb.onSuccess(data);
                if (isRefresh) {
                    refresh(cxt, path);
                }
            } catch (IOException e) {
                cb.onFailure(-1, e.getMessage());
            } finally {
                FileUtils.closeIO(os);
                cb.onFinish();
            }
        }
    }

    /**
     * 刷新图库
     *
     * @param path 要刷新的文件的绝对路径
     */
    public void refresh(Context cxt, String path) {
        String name = path.substring(path.lastIndexOf('/'));
        try {
            MediaStore.Images.Media.insertImage(cxt.getContentResolver(), path,
                    name, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        cxt.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                .parse("file://" + path)));
    }


    /*********************
     * private method
     *********************/

    /**
     * 按照优先级为View设置图片资源
     * 优先使用drawable，仅当drawable无效时使用bitmapRes，若两值均无效，则不作处理
     *
     * @param view          要设置图片的控件(View设置bg，ImageView设置src)
     * @param errorImage    优先使用项
     * @param errorImageRes 次级使用项
     */
    private static void doFailure(View view, Drawable errorImage, int errorImageRes) {
        setImageWithResource(view, errorImage, errorImageRes);
    }

    /**
     * 按照优先级为View设置图片资源
     *
     * @param view          要设置图片的控件(View设置bg，ImageView设置src)
     * @param bitmap        优先使用项
     * @param errorImage    二级使用项
     * @param errorImageRes 三级使用项
     */
    private static void doSuccess(View view, Bitmap bitmap, Drawable errorImage,
                                  int errorImageRes) {
        if (bitmap != null) {
            setViewImage(view, bitmap);
        } else {
            setImageWithResource(view, errorImage, errorImageRes);
        }
    }

    /**
     * 按照优先级为View设置图片资源
     * 优先使用drawable，仅当drawable无效时使用bitmapRes，若两值均无效，则不作处理
     *
     * @param imageView 要设置图片的控件(View设置bg，ImageView设置src)
     * @param drawable  优先使用项
     * @param bitmapRes 次级使用项
     */
    private static void setImageWithResource(View imageView, Drawable drawable,
                                             int bitmapRes) {
        if (drawable != null) {
            setViewImage(imageView, drawable);
        } else if (bitmapRes > 0) { //大于0视为有效ImageResource
            setViewImage(imageView, bitmapRes);
        }
    }

    private static void setViewImage(View view, int background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(background);
        } else {
            view.setBackgroundResource(background);
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private static void setViewImage(View view, Bitmap background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageBitmap(background);
        } else {
            if (SystemTool.getSDKVersion() >= 16) {
                view.setBackground(new BitmapDrawable(view.getResources(),
                        background));
            } else {
                view.setBackgroundDrawable(new BitmapDrawable(view
                        .getResources(), background));
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private static void setViewImage(View view, Drawable background) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(background);
        } else {
            if (SystemTool.getSDKVersion() >= 16) {
                view.setBackground(background);
            } else {
                view.setBackgroundDrawable(background);
            }
        }
    }

    private static void showLogIfOpen(String msg) {
        if (BitmapConfig.isDEBUG) {
            WJLoger.debugLog(WJBitmap.class.getSimpleName(), msg);
        }
    }



    /*********************
     * Deprecated method
     *********************/

    @Deprecated
    public void display(View imageView, String imageUrl) {
        new Builder().view(imageView).imageUrl(imageUrl).display(this);
    }

    @Deprecated
    public void display(View imageView, String imageUrl, int width, int height) {
        new Builder().view(imageView).imageUrl(imageUrl).width(width).height(height).display(this);
    }

    @Deprecated
    public void display(View imageView, String imageUrl, BitmapCallBack callback) {
        new Builder().view(imageView).imageUrl(imageUrl).callback(callback).display(this);
    }

    @Deprecated
    public void display(View imageView, String imageUrl, int width, int height, int loadBitmap) {
        new Builder().view(imageView).imageUrl(imageUrl).width(width).height(height)
                .loadBitmapRes(loadBitmap).display(this);
    }

    @Deprecated
    public void displayWithLoadBitmap(View imageView, String imageUrl, int loadBitmap) {
        new Builder().view(imageView).imageUrl(imageUrl).loadBitmapRes(loadBitmap).display(this);
    }

    @Deprecated
    public void displayWithErrorBitmap(View imageView, String imageUrl, int errorBitmap) {
        new Builder().view(imageView).imageUrl(imageUrl).errorBitmapRes(errorBitmap).display(this);
    }

    @Deprecated
    public void displayLoadAndErrorBitmap(View imageView, String imageUrl,
                                          int loadBitmap, int errorBitmap) {
        new Builder().view(imageView).imageUrl(imageUrl).loadBitmapRes(loadBitmap)
                .errorBitmapRes(errorBitmap).display(this);
    }

    @Deprecated
    public void displayWithDefWH(View imageView, String imageUrl, Drawable loadBitmap,
                                 Drawable errorBitmap, BitmapCallBack callback) {
        new Builder().view(imageView).imageUrl(imageUrl).loadBitmap(loadBitmap)
                .errorBitmap(errorBitmap).callback(callback).display(this);
    }

    @Deprecated
    public void display(View imageView, String imageUrl, int loadAndErrorRes,
                        int width, int height, BitmapCallBack callback) {
        new Builder().view(imageView).imageUrl(imageUrl).loadBitmapRes(loadAndErrorRes)
                .errorBitmapRes(loadAndErrorRes).width(width).height(height).callback(callback)
                .display(this);
    }
}
