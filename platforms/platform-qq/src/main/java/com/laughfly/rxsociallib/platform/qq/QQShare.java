package com.laughfly.rxsociallib.platform.qq;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.laughfly.rxsociallib.SocialConstants;
import com.laughfly.rxsociallib.SocialIntentUtils;
import com.laughfly.rxsociallib.SocialThreads;
import com.laughfly.rxsociallib.SocialUriUtils;
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
import java.util.List;

import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_APP_NAME;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_ARK_INFO;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_AUDIO_URL;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_IMAGE_URL;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_KEY_TYPE;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_SUMMARY;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TARGET_URL;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TITLE;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_APP;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_AUDIO;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
import static com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_IMAGE;

/**
 * QQ分享
 * author:caowy
 * date:2018-05-11
 */
@ShareFeatures({
    @ShareFeature(platform = QQConstants.QQ, supportFeatures = QQConstants.QQ_SHARE_SUPPORT)
})
public class QQShare extends ShareAction implements IUiListener {

    private Tencent mTencent;

    private boolean mShareByIntent;

    @Override
    protected void check() throws Exception {
//        if (!QQUtils.isQQInstalled(mParams.getContext()) && !QQUtils.isTimInstalled(mParams.getContext())) {
//            throw new SocialShareException(getPlatform(), SocialConstants.ERR_APP_NOT_INSTALL);
//        }
    }

    @Override
    protected void init() throws Exception {
        SocialThreads.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTencent = Tencent.createInstance(mParams.getAppId(), mParams.getContext());
            }
        });
    }

    @Override
    protected void execute() throws Exception {
        shareToQQ(getDelegate());
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        //分享成功但停留在QQ，并直接通过任务管理切换回APP
        if (requestCode != Constants.REQUEST_QQ_SHARE) {
            finishWithNoResult();
        } else {
            Tencent.handleResultData(data, QQShare.this);
        }
    }

    @Override
    public void handleNoResult() {
        if (mShareByIntent) {
            finishWithSuccess(new ShareResult(getPlatform()));
        } else {
            super.handleNoResult();
        }
    }

    private void shareToQQ(Activity activity) throws Exception {
        @ShareType.Def int type = getShareType();
        switch (type) {
            case ShareType.SHARE_APP:
                shareApp(activity);
                break;
            case ShareType.SHARE_AUDIO:
                shareAudio(activity);
                break;
            case ShareType.SHARE_FILE:
                shareFile(activity, mParams.getFileUri());
                break;
            case ShareType.SHARE_IMAGE:
                shareImage(activity);
                break;
            case ShareType.SHARE_LOCAL_VIDEO:
            case ShareType.SHARE_NETWORK_VIDEO:
                shareVideo(activity);
                break;
            case ShareType.SHARE_TEXT:
                shareText(activity, mParams.getTitle(), mParams.getText());
                break;
            case ShareType.SHARE_WEB:
                shareWeb(activity);
                break;
            case ShareType.SHARE_MULTI_FILE:
                shareFileList(activity);
                break;
            case ShareType.SHARE_MINI_PROGRAM:
            case ShareType.SHARE_MULTI_IMAGE:
            case ShareType.SHARE_NONE:
            default:
                throw new SocialShareException(getPlatform(), SocialConstants.ERR_SHARETYPE_NOT_SUPPORT);
        }
    }

    private void shareApp(Activity activity) throws SocialShareException {
        Bundle params = createParams(ShareType.SHARE_APP);
        params.putString(SHARE_TO_QQ_TITLE, mParams.getTitle());
        params.putString(SHARE_TO_QQ_TARGET_URL, mParams.getWebUrl());
        params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
        params.putString(SHARE_TO_QQ_IMAGE_URL, getThumbPath(QQConstants.THUMB_SIZE_LIMIT));
        params.putString(SHARE_TO_QQ_ARK_INFO, mParams.getAppInfo());
        shareBySDK(activity, params);
    }

    private void shareAudio(Activity activity) throws SocialShareException {
        String audioUri = mParams.getAudioUri();
        if (SocialUriUtils.isHttpUrl(audioUri)) {
            Bundle params = createParams(ShareType.SHARE_AUDIO);
            params.putString(SHARE_TO_QQ_TITLE, mParams.getTitle());
            params.putString(SHARE_TO_QQ_TARGET_URL, mParams.getWebUrl());
            params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
            params.putString(SHARE_TO_QQ_IMAGE_URL, getThumbPath(QQConstants.THUMB_SIZE_LIMIT));
            params.putString(SHARE_TO_QQ_AUDIO_URL, mParams.getAudioUri());
            shareBySDK(activity, params);
        } else {
            shareFile(activity, audioUri);
        }
    }

    private void shareWeb(Activity activity) throws SocialShareException {
        Bundle params = createParams(ShareType.SHARE_WEB);
        params.putString(SHARE_TO_QQ_TITLE, mParams.getTitle());
        params.putString(SHARE_TO_QQ_SUMMARY, mParams.getText());
        params.putString(SHARE_TO_QQ_TARGET_URL, mParams.getWebUrl());
        params.putString(SHARE_TO_QQ_IMAGE_URL, getThumbPath(QQConstants.THUMB_SIZE_LIMIT));
        shareBySDK(activity, params);
    }

    private void shareImage(Activity activity) throws Exception {
        Bundle params = createParams(ShareType.SHARE_IMAGE);
        params.putString(SHARE_TO_QQ_IMAGE_URL, getThumbPath(QQConstants.THUMB_SIZE_LIMIT));
        params.putString(SHARE_TO_QQ_IMAGE_LOCAL_URL, getImagePath(QQConstants.IMAGE_SIZE_LIMIT));
        shareBySDK(activity, params);
    }

    private void shareFile(Activity activity, String fileUri) {
        Intent shareFile = SocialIntentUtils.createFileShare(Uri.parse(fileUri));
        shareByIntent(activity, shareFile);
    }

    private void shareFileList(Activity activity) throws SocialShareException {
        List<String> fileList = mParams.getFileList();
        ArrayList<Uri> fileUriList = new ArrayList<>();
        for (String file : fileList) {
            file = transformUri(file, URI_TYPES_LOCAL, SocialUriUtils.TYPE_FILE_PATH);
            fileUriList.add(Uri.parse(file));
        }
        Intent shareFileList = SocialIntentUtils.createFileListShare(fileUriList);
        shareByIntent(activity, shareFileList);
    }

    private void shareVideo(Activity activity) {
        String videoUri = mParams.getVideoUri();
        if (SocialUriUtils.isHttpUrl(videoUri)) {
            shareText(activity, mParams.getTitle(), videoUri);
        } else {
            shareByIntent(activity, SocialIntentUtils.createFileShare(Uri.parse(mParams.getVideoUri())));
        }
    }

    private void shareText(Activity activity, String title, String text) {
        shareByIntent(activity, SocialIntentUtils.createTextShare(title, text));
    }

    private void shareByIntent(Activity activity, Intent originalIntent) {
        ArrayList<Intent> intents = new ArrayList<>();
        if (QQUtils.isQQInstalled(activity)) {
            intents.add(new Intent(originalIntent).setClassName(QQConstants.QQ_PACKAGE_NAME, QQConstants.QQ_SHARE_TARGET_CLASS));
        }
        if (QQUtils.isTimInstalled(activity)) {
            intents.add(new Intent(originalIntent).setClassName(QQConstants.TIM_PACKAGE_NAME, QQConstants.TIM_SHARE_TARGET_CLASS));
        }
        try {
            if (intents.size() == 2) {
                Intent chooserIntent = Intent.createChooser(intents.get(0), "请选择");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intents.get(1)});
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(chooserIntent);
            } else {
                activity.startActivity(intents.get(0));
            }
            mShareByIntent = true;
        } catch (Exception e) {
            e.printStackTrace();
            finishWithError(e);
        }
    }

    private void shareBySDK(Activity activity, Bundle params) {
        SocialThreads.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mTencent.shareToQQ(activity, params, QQShare.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    finishWithError(e);
                }
            }
        });
    }

    private Bundle createParams(@ShareType.Def int shareType) {
        Bundle params = new Bundle();

        params.putInt(SHARE_TO_QQ_KEY_TYPE, toQQShareType(shareType));

        if (mParams.getShareAppName() != null) {
            params.putString(SHARE_TO_QQ_APP_NAME, mParams.getShareAppName());
        }

        return params;
    }

    private int toQQShareType(@ShareType.Def int shareType) {
        switch (shareType) {
            case ShareType.SHARE_APP:
                return SHARE_TO_QQ_TYPE_APP;
            case ShareType.SHARE_AUDIO:
                return SHARE_TO_QQ_TYPE_AUDIO;
            case ShareType.SHARE_IMAGE:
                return SHARE_TO_QQ_TYPE_IMAGE;
            case ShareType.SHARE_TEXT:
            case ShareType.SHARE_WEB:
                return SHARE_TO_QQ_TYPE_DEFAULT;
            default:
                return 0;
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
