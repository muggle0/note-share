package com.muggle.jetcahetest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;

public interface Server {

    @Cached(cacheType = CacheType.REMOTE)
    int test (String message);
}
