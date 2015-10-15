package com.pkuhelper.lib;

//import java.io.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.pkuhelper.R;
//import com.pkuhelper.lib.view.CustomToast;
/*
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennExecutor.CallBack;
import com.renn.rennsdk.RennResponse;
import com.renn.rennsdk.RennClient.LoginListener;
import com.renn.rennsdk.exception.RennException;
import com.renn.rennsdk.param.*;
*/
import com.tencent.mm.sdk.openapi.*;
import com.tencent.mm.sdk.modelmsg.*;

public class Share {
	// We do not provide any share to renren now
	private static final String[] sharesTo={"发送给微信好友","分享到朋友圈"/*,"分享到人人"*/};
	private static final String WX_APP_ID="wxe410b2da07aaa0e4";
	/*
	private static final String RENREN_APP_ID="475741";
	private static final String RENREN_API_KEY="83469c9420c4408088221971b64fefde";
	private static final String RENREN_SECRET_KEY="be71e223026945d2b2db18bf15d2f5ac";
	private static final String RENREN_PERMISSIONS="publish_feed photo_upload status_update read_user_photo publish_share";
	
	private static final int RENREN_SHARE_URL=0;
	private static final int RENREN_SHARE_IMAGE=1;
	private static final int RENREN_SHARE_TEXT=2;
	private static final String addString="(分享自PKU Helper)";
	*/

	private static IWXAPI api;
	//private static RennClient rennClient;
	
	public static void readyToShareURL(final Context context, final String dialogTitle,
			final String url, final String title, final String content, final Bitmap bitmap) {
		new AlertDialog.Builder(context).setTitle(dialogTitle)
		.setItems(sharesTo, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0)
					urlToWx(context, url, title, content, bitmap, false);
				else if (which==1)
					urlToWx(context, url, title, content, bitmap, true);
//				else if (which==2)
//					shareToRenren(context, RENREN_SHARE_URL, title, url, null);
			}
		}).setCancelable(true).show();
	}
	public static void readyToShareImage(final Context context, final String dialogTitle,
			final Bitmap bitmap) {
		if (bitmap==null) return;
		new AlertDialog.Builder(context).setTitle(dialogTitle)
		.setItems(sharesTo, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0)
					imgToWx(context, bitmap, false);
				else if (which==1)
					imgToWx(context, bitmap, true);
//				else if (which==2)
//					shareToRenren(context, RENREN_SHARE_IMAGE, dialogTitle, "renren_photo", bitmap);
			}
		}).setCancelable(true).show();
	}
	public static void readyToShareText(final Context context, final String dialogTitle,
			final String text) {
		new AlertDialog.Builder(context).setTitle(dialogTitle)
		.setItems(sharesTo, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0)
					textToWx(context, text, false);
				else if (which==1)
					textToWx(context, text, true);
//				else if (which==2)
//					shareToRenren(activity, RENREN_SHARE_TEXT, text, "", null);
			}
		}).setCancelable(true).show();
	}
	
	public static void urlToWx(Context context, String url, String title, String content,
			Bitmap bitmap, boolean isToTimeline) {
		api=regAPI(context);
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		String _con=new String(content);
		if (_con.length()>=40) _con=_con.substring(0, 38)+"...";
		msg.description = _con;
		if (bitmap==null)
			bitmap=((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_share_default)).getBitmap();
		byte[] bts=MyBitmapFactory.bitmapToArray(bitmap, 31);
		if (bts.length>=31*1024) {
			bitmap=((BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_share_default)).getBitmap();
			bts=MyBitmapFactory.bitmapToArray(bitmap, 31);
		}
		msg.thumbData = bts;
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = Util.getHash("webpage"+System.currentTimeMillis());
		req.message = msg;
		req.scene = isToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		Log.w("success?", api.sendReq(req)+"");
	}
	public static void textToWx(Context context, String text, boolean isToTimeline) {
		api=regAPI(context);
		WXTextObject textObj = new WXTextObject();
		textObj.text = text;
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = Util.getHash("text"+System.currentTimeMillis());
		req.message = msg;
		req.scene = isToTimeline?SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		Log.w("success?", api.sendReq(req)+"");
	}
	public static void imgToWx(Context context, Bitmap bitmap, boolean isToTimeline) {
		api=regAPI(context);
		
		WXImageObject imgObj = new WXImageObject(bitmap);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
		
		msg.thumbData = MyBitmapFactory.bitmapToArray(thumbBmp, 31);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = Util.getHash("image"+System.currentTimeMillis());
		req.message = msg;
		req.scene = isToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}	
	private static IWXAPI regAPI(Context context) {
		if (api==null) {
			api=WXAPIFactory.createWXAPI(context, WX_APP_ID, false);
			api.registerApp(WX_APP_ID);
		}
		
		return api;
	}
/*
	public static void shareToRenren(final Context context,
			final int type, final String text, final String url, final Bitmap bitmap) {
		if (rennClient==null) {
			rennClient=RennClient.getInstance(context);
			rennClient.init(RENREN_APP_ID, RENREN_API_KEY, RENREN_SECRET_KEY);
			rennClient.setScope(RENREN_PERMISSIONS);
			//rennClient.setTokenType("bearer");
			rennClient.setLoginListener(new LoginListener() {
				@Override
				public void onLoginSuccess() {
					realShareToRenren(context, type, text, url, bitmap);
				}
				@Override
				public void onLoginCanceled() {				
					rennClient=null;
				}
			});
			rennClient.login(context);
		}
		else realShareToRenren(context, type, text, url, bitmap);
	}
	private static void realShareToRenren(final Context context,
			int type, String text, String url, Bitmap bitmap) {
		if (rennClient==null) return;
		if (!rennClient.isLogin())  {
			rennClient=null; shareToRenren(context, type, text, url, bitmap);
			return;
		}
		if (type==RENREN_SHARE_URL) {
			String string=text+" "+addString;
			PutShareUrlParam putShareUrlParam=new PutShareUrlParam();
			putShareUrlParam.setUrl(url);
			putShareUrlParam.setComment(string);
			try {
			rennClient.getRennService().sendAsynRequest(putShareUrlParam, new CallBack() {
				
				@Override
				public void onSuccess(RennResponse arg0) {
					//Toast.makeText(activity, "分享成功！", Toast.LENGTH_SHORT).show();
					CustomToast.showInfoToast(context, "分享成功！");
				}
				
				@Override
				public void onFailed(String arg0, String arg1) {
					String text="分享失败";
					if (arg1!=null && !"".equals(arg1)) text=arg1;
					CustomToast.showErrorToast(context, text);

				}
			});
			} catch (RennException e) {
				e.printStackTrace();
				CustomToast.showErrorToast(context, "分享失败");
			}
		}
		else if (type==RENREN_SHARE_IMAGE) {
			if (bitmap==null) return;
			File file=MyFile.getFile(context, "cache", Util.getHash(url));
			try {
				file.createNewFile();
				FileOutputStream fileOutputStream=new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				CustomToast.showErrorToast(context, "分享失败");
				return;
			}
			if (text==null || "".equals(text)) text=addString;
			else text+=" "+addString;
			
			UploadPhotoParam uploadPhotoParam=new UploadPhotoParam();
			uploadPhotoParam.setFile(file);
			uploadPhotoParam.setDescription(text);
			try {
				rennClient.getRennService().sendAsynRequest(uploadPhotoParam, new CallBack() {
					
					@Override
					public void onSuccess(RennResponse arg0) {
						CustomToast.showSuccessToast(context, "分享成功！");
					}
					
					@Override
					public void onFailed(String arg0, String arg1) {
						//if (arg1!=null && !"".equals(arg1)) 
						//	Toast.makeText(activity, arg1, Toast.LENGTH_SHORT).show();
						//else Toast.makeText(activity, "分享失败", Toast.LENGTH_SHORT).show();
						String text="分享失败";
						if (arg1!=null && !"".equals(arg1)) text=arg1;
						CustomToast.showErrorToast(context, text);
					}
				});
			} catch (RennException e) {
				e.printStackTrace();
				//Toast.makeText(activity, "分享失败", Toast.LENGTH_SHORT).show();
				CustomToast.showErrorToast(context, "分享失败");
			}
		}
		else if (type==RENREN_SHARE_TEXT) {
			String string=text+" "+addString;
			PutStatusParam putStatusParam=new PutStatusParam();
			putStatusParam.setContent(string);
			try {
				rennClient.getRennService().sendAsynRequest(putStatusParam, new CallBack() {
					
					@Override
					public void onSuccess(RennResponse arg0) {
						//Toast.makeText(activity, "发布成功！", Toast.LENGTH_SHORT).show();
						CustomToast.showSuccessToast(context, "发布成功！");
					}
					
					@Override
					public void onFailed(String arg0, String arg1) {
						//if (arg1!=null && !"".equals(arg1)) 
						//	Toast.makeText(activity, arg1, Toast.LENGTH_SHORT).show();
						//else Toast.makeText(activity, "发布失败", Toast.LENGTH_SHORT).show();
						String text="发布失败";
						if (arg1!=null && !"".equals(arg1)) text=arg1;
						CustomToast.showErrorToast(context, text);
					}
				});
			} catch (RennException e) {
				e.printStackTrace();
				//Toast.makeText(activity, "发布失败", Toast.LENGTH_SHORT).show();
				CustomToast.showErrorToast(context, "发布失败");
			}
		}
	}
*/
}

