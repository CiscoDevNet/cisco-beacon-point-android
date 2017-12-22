package com.android.ble.sample.helper;

import android.content.Context;

import com.android.ble.sample.R;
import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.cache.plus.SimpleImageLoader;


public class AppImageLoader {
    public static final float IMAGE_MEMORY_CACHE_PERCENT = 0.50f;

    /**
     * Method return the SimpleImageLoader instance for loading the image with cache and single default resource
     */
    public static SimpleImageLoader getImageLoaderInstance(Context context, int defaultResId, String cacheDir) {
        SimpleImageLoader loader;
        DiskLruBasedCache.ImageCacheParams cacheParams = new DiskLruBasedCache.ImageCacheParams(context, cacheDir);
        cacheParams.setMemCacheSizePercent(IMAGE_MEMORY_CACHE_PERCENT);
        loader = new SimpleImageLoader(context, cacheParams);
        loader.setDefaultDrawable(defaultResId);
        return loader;
    }

    public static void ClearCache(Context context) {
        SimpleImageLoader modelLoader  = AppImageLoader.getImageLoaderInstance(context, R.mipmap.ic_launcher, "FLOOR_PLAN_IMAGES");
        modelLoader.clearCache();
    }
}
