package com.efurture.glue.loader;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.webkit.URLUtil;

import com.efurture.glue.GLog;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {

	private static final String HYBRID_DIR = "hybrid";

	protected Context context;


	protected Handler handler = new Handler();

	public ResourceLoader(Context context) {
		this.context = context;
	}

	public void load(final Uri uri, final Callback callback){

		//同步及预制文件
		String path = uri.getPath().trim();
		final File file = new File(context.getFilesDir(),  HYBRID_DIR + "/" + path);
		if(file.exists()){
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				callback.onStream(fileInputStream);
			}catch (Exception e){
				GLog.e("illegal file on files dir " + file.getPath());
				file.delete();
			}
		}else {
			if ("true".equals(uri.getQueryParameter("assets"))){
				InputStream inputStream = null;
				try {
					inputStream = context.getAssets().open(path);
					if (inputStream != null){
						callback.onStream(inputStream);
					}
				} catch (IOException e) {}
			}
		}


		if (URLUtil.isAssetUrl(uri.toString())){
			InputStream inputStream = null;
			try {
				String fileName = path.substring(15);
				inputStream = context.getAssets().open(fileName);
				if (inputStream != null){
					callback.onStream(inputStream);
					return;
				}
			} catch (IOException e) {
				throw  new RuntimeException(e);
			}
			return;
		}

		OkHttpClient okHttpClient = Http.getClient();
		Request request = new Request.Builder().url(uri.toString()).build();
		okHttpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				GLog.e("Request url error " + uri.toString(), e);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callback.onStream(null);
					}
                });
			}

			@Override
			public void onResponse(Response response) throws IOException {
				byte[] bts = response.body().bytes();
				final ByteArrayInputStream inputStream =  new ByteArrayInputStream(bts);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callback.onStream(inputStream);
					}
				});
			}
		});

	}

	public interface Callback{
		void onStream(InputStream inputStream);
	}
	
}
