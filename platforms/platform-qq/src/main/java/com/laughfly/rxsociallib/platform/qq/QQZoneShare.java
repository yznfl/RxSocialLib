package com.laughfly.rxsociallib.platform.qq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.laughfly.rxsociallib.SocialConstants;
import com.laughfly.rxsociallib.SocialThreads;
import com.laughfly.rxsociallib.SocialUriUtils;
import com.laughfly.rxsociallib.delegate.SocialDelegateActivity;
import com.laughfly.rxsociallib.exception.SocialShareException;
import com.laughfly.rxsociallib.share.ShareAction;
import com.laughfly.rxsociallib.share.ShareFeature;
import com.laughfly.rxsociallib.share.ShareFeatures;
import com.laughfly.rxsociallib.share.ShareResult;
import com.laughfly.rxsociallib.share.ShareType;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;
import java.util.ListIterator;

import static com.tencent.connect.share.QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD;
import static com.tencent.connect.share.QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO;
import static com.tencent.connect.share.QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QQ_APP_NAME;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QQ_IMAGE_URL;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QQ_SUMMARY;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QQ_TARGET_URL;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QQ_TITLE;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QZONE_KEY_TYPE;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
import static com.tencent.connect.share.QzoneShare.SHARE_TO_QZONE_TYPE_NO_TYPE;


/**
 * QQZone分享
 * author:caowy
 * date:2018-05-11
 */
@ShareFeatures({
    @ShareFeature(platform = QQConstants.QQZONE, supportFeatures = QQConstants.QQZONE_SHARE_SUPPORT)
})
public class QQZoneShare extends ShareAction implements IUiListener {

    private Tencent mTencent;

    @Override
    protected void check() throws Exception {
        if (!QQUtils.isQQInstalled(mParams.getContext()) && !QQUtils.isTimInstalled(mParams.getContext())) {
            throw new SocialShareException(getPlatform(), SocialConstants.ERR_APP_NOT_INSTALL);
        }
    }

    @Override
    protected void init() throws Exception {
        mTencent = Tencent.createInstance(mParams.getAppId(), mParams.getContext());
    }

    @Override
    protected void execute() throws Exception {
        SocialDelegateActivity delegate = getDelegate();
        @ShareType.Def int type = getShareType();
        switch (type) {
            case ShareType.SHARE_WEB:
                shareWeb(delegate);
                break;
            case ShareType.SHARE_TEXT:
            case ShareType.SHARE_IMAGE:
            case ShareType.SHARE_MULTI_IMAGE:
                publishMood(delegate, type);
                break;
            case ShareType.SHARE_LOCAL_VIDEO:
                publishVideo(delegate);
                break;
            default:
                throw new SocialShareException(getPlatform(), SocialConstants.ERR_SHARETYPE_NOT_SUPPORT);
        }
    }


    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        //分享成功但停留在QQ，并直接通过任务管理切换回APP
        if (requestCode != Constants.REQUEST_QZONE_SHARE) {
            finishWithNoResult();
        } else {
            Tencent.handleResultData(data, QQZoneShare.this);
        }
    }


    /**
     * 分享图文链接
     *
     * @param activity
     */
    private void shareWeb(Activity activity) throws SocialShareException {
        Bundle params = createParams(ShareType.SHARE_WEB);

        ArrayList<String> imageList = new ArrayList<>();
        String thumbUri = getThumbPath(QQConstants.THUMB_SIZE_LIMIT);
        if (!TextUtils.isEmpty(thumbUri)) {
            imageList.add(thumbUri);
        }

        params.putStringArrayList(SHARE_TO_QQ_IMAGE_URL, imageList);
        params.putString(SHARE_TO_QQ_TITLE, mParams.getTitle());
        params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
        params.putString(SHARE_TO_QQ_TARGET_URL, mParams.getWebUrl());

        shareBySDK(activity, params);
    }

    /**
     * 分享说说
     *
     * @param activity
     */
    private void publishMood(Activity activity, int type) throws SocialShareException {
        Bundle params = createParams(type);

        params.putString(SHARE_TO_QQ_TITLE, mParams.getTitle());
        params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
        params.putString(SHARE_TO_QQ_TARGET_URL, mParams.getWebUrl());
        ArrayList<String> imageList = new ArrayList<>();
        if (mParams.hasImage()) {
            imageList.add(getImagePath(QQConstants.IMAGE_SIZE_LIMIT));
        }
        if (mParams.hasImageList()) {
            imageList.addAll(mParams.getImageList());
        }

        ListIterator<String> iterator = imageList.listIterator();
        while (iterator.hasNext()) {
            String imageUri = iterator.next();
            String _imageUri = transformUri(imageUri, URI_TYPES_ALL, SocialUriUtils.TYPE_FILE_PATH);
            if (_imageUri != null && !_imageUri.equalsIgnoreCase(imageUri)) {
                iterator.set(_imageUri);
            }
        }

        params.putStringArrayList(SHARE_TO_QQ_IMAGE_URL, imageList);

        publishBySDK(activity, params);
    }

    private void publishVideo(Activity activity) throws SocialShareException {
        Bundle params = createParams(ShareType.SHARE_LOCAL_VIDEO);

        params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
        params.putString(PUBLISH_TO_QZONE_VIDEO_PATH, transformUri(mParams.getVideoUri(), URI_TYPES_LOCAL, SocialUriUtils.TYPE_FILE_PATH));

        publishBySDK(activity, params);
    }

    private void shareBySDK(final Activity activity, final Bundle params) {
        SocialThreads.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTencent.shareToQzone(activity, params, QQZoneShare.this);
            }
        });
    }

    private void publishBySDK(final Activity activity, final Bundle params) {
        SocialThreads.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTencent.publishToQzone(activity, params, QQZoneShare.this);
            }
        });
    }

    private Bundle createParams(@ShareType.Def int shareType) {
        Bundle params = new Bundle();

        params.putInt(SHARE_TO_QZONE_KEY_TYPE, toQQShareType(shareType));

        if (mParams.getShareAppName() != null) {
            params.putString(SHARE_TO_QQ_APP_NAME, mParams.getShareAppName());
        }

        return params;
    }

    private int toQQShareType(@ShareType.Def int shareType) {
        switch (shareType) {
            case ShareType.SHARE_IMAGE:
            case ShareType.SHARE_MULTI_IMAGE:
            case ShareType.SHARE_TEXT:
                return PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD;
            case ShareType.SHARE_LOCAL_VIDEO:
                return PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO;
            case ShareType.SHARE_WEB:
                return SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
            default:
                return SHARE_TO_QZONE_TYPE_NO_TYPE;
        }
    }

    @Override
    public void onComplete(Object o) {
        ShareResult result = new ShareResult(getPlatform());
        finishWithSuccess(result);
    }

    @Override
    public void onError(UiError uiError) {
        @SocialConstants.ErrCode
        int errorCode;
        String msg = uiError != null ? uiError.errorMessage : "";
        int platformErrCode = uiError != null ? uiError.errorCode : -1;
        if (uiError == null) {
            errorCode = SocialConstants.ERR_OTHER;
        } else {
            switch (platformErrCode) {
                case 110404://没有传入AppId
                case 110407://应用授权已下架
                case 110406://授权未通过
                case 100044://签名错误
                case 110503://获取授权码失败
                    errorCode = SocialConstants.ERR_AUTH_DENIED;
                    break;
                case 110401://应用未安装
                    errorCode = SocialConstants.ERR_APP_NOT_INSTALL;
                    break;
                default://其他错误
                    errorCode = SocialConstants.ERR_OTHER;
                    break;
            }
        }
        finishWithError(new SocialShareException(getPlatform(), errorCode, platformErrCode, msg, uiError));
    }

    @Override
    public void onCancel() {
        finishWithCancel();
    }
}
