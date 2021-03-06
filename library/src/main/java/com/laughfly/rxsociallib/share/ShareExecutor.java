package com.laughfly.rxsociallib.share;

import com.laughfly.rxsociallib.SocialActionFactory;
import com.laughfly.rxsociallib.internal.SocialExecutor;

/**
 * Created by caowy on 2019/4/29.
 * email:cwy.fly2@gmail.com
 */

public class ShareExecutor extends SocialExecutor<ShareAction, ShareParams, ShareResult>{
    private ShareParams mShareParams;

    public ShareExecutor(ShareParams shareParams) {
        mShareParams = shareParams;
    }

    protected ShareAction createAction() {
        return SocialActionFactory.createShareAction(mShareParams);
    }

}
