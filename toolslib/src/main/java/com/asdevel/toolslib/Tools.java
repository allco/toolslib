package com.asdevel.toolslib;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.text.TextUtils.isEmpty;
import static com.asdevel.toolslib.Logger.log;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Tools {

	public static final String UTF_8 = "UTF-8";
	public static final String PATCH = "PATCH";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String GET = "GET";

	public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_X_DEVICE_ID = "X-Device-Id";
	public static final String HEADER_X_API_VERSION = "X-Api-Version";
	public static final String HEADER_X_CLIENT_VERSION = "X-Client-Version";

	public static boolean DEBUG;

	public static class ToolsImpl {

		private final Context ctx;
		private final Handler handler = new Handler();

		private ToolsImpl(Context ctx) {
			this.ctx = ctx;
		}

		static ToolsImpl createToolsImpl(Context m_sCtx) {return new ToolsImpl(m_sCtx);}



		public Handler getMainHandler() {
			return handler;
		}

		public void post(Runnable runnable) {
			postDelayed(runnable, 0);
		}

		public void postDelayed(Runnable runnable, long delayMillis) {
			checkArgument(runnable != null);
			checkState(handler != null);
			handler.postDelayed(runnable, delayMillis);
		}

		public boolean isUiThread() {
			return Looper.getMainLooper().getThread() == Thread.currentThread();
		}

		public void runOnUiThread(Runnable runnable) // clone of Activity.runOnUiThread()
		{
			if (isUiThread()) {
				runnable.run();
			}
			else {
				getMainHandler().post(runnable);
			}
		}

		@NonNull
		private Gson createGson() {

			GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
			if (DEBUG) {
				gsonBuilder.setPrettyPrinting();
			}


			return gsonBuilder.create();
		}

		public void closeQuietly(Closeable obj) {
			if (obj == null) return;
			try {
				obj.close();
			} catch (IOException e) {
				log(e);
			}
		}

		public void closeQuietly(HttpURLConnection conn) {
			if (conn != null) {
				conn.disconnect();
			}
		}



		public String toJson(Object obj) {
			return createGson().toJson(obj);
		}

		public <T> T fromJson(InputStream is, Class<T> clazz) throws UnsupportedEncodingException {
			return createGson().fromJson(new InputStreamReader(new BufferedInputStream(is), UTF_8), clazz);
		}

		public <T> T fromJson(Reader reader, Class<T> clazz) {
			return createGson().fromJson(reader, clazz);
		}

		public <T> T fromJson(String json, Class<T> clazz)  {
			return createGson().fromJson(json, clazz);
		}


		@NonNull
		public HttpURLConnection openConnection(Context context, String url, String method) throws IOException {

			log("openConnection: url[ " + url + " ] ");

			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr == null ? null : connMgr.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected()) {throw new IOException("Internet connection in unavailable");}
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
			checkNotNull(conn).setRequestMethod(method);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(10000);
			return conn;
		}

		public void downloadFile(Context context, Uri uri, String accessToken, String filename) {

			checkArgument(context != null);
			checkArgument(uri != null);

			String lastPathSegment = uri.getLastPathSegment();

			DownloadManager.Request r = new DownloadManager.Request(uri);

			if (!isEmpty(accessToken)) {
				r.addRequestHeader(HEADER_AUTHORIZATION, accessToken);
			}

			r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

			if (Build.VERSION.SDK_INT >= 11) {
				r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}

			DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			dm.enqueue(r);
		}

		@NonNull
		public Context getContext() {
			checkState(ctx != null);
			return ctx;
		}

		public int getColor(int resourceId) {
			checkState(ctx != null);

			if (Build.VERSION.SDK_INT < 23) {
				return ctx.getResources().getColor(resourceId);
			}
			else {
				return ctx.getResources().getColor(resourceId, null);
			}
		}
	}

	public static int getColor(int resourceId) {return tools.getColor(resourceId);}

	public static void downloadFile(Context context, Uri uri, String accessToken, String fileName) {tools.downloadFile(context, uri, accessToken, fileName);}

	public static ToolsImpl createToolsImpl(Context m_sCtx) {return ToolsImpl.createToolsImpl(m_sCtx);}

	@NonNull
	public static HttpURLConnection openConnection(Context context, String url, String method) throws IOException {return tools.openConnection(context, url, method);}

	public static ToolsImpl tools;

	public static void initialize(Context context, boolean debug) {

		DEBUG = debug;

		if (tools == null) {
			tools = ToolsImpl.createToolsImpl(context);
		}
	}

	public static void installMock(ToolsImpl tools) {
		checkArgument(tools != null);
		Tools.tools = tools;
	}

	public static void post(Runnable runnable) {tools.post(runnable);}

	public static Handler getMainHandler() {return tools.getMainHandler();}

	@NonNull
	public static Gson createGson() {return tools.createGson();}

	public static <T> T fromJson(InputStream is, Class<T> clazz) throws UnsupportedEncodingException {return tools.fromJson(is, clazz);}

	public static <T> T fromJson(String json, Class<T> clazz) {return tools.fromJson(json, clazz);}

	public static String toJson(Object obj) {return tools.toJson(obj);}

	public static void closeQuietly(Closeable obj) {tools.closeQuietly(obj);}

	public static void runOnUiThread(Runnable runnable) {tools.runOnUiThread(runnable);}

	public static <T> T fromJson(Reader reader, Class<T> clazz) {return tools.fromJson(reader, clazz);}

	public static void postDelayed(Runnable runnable, long delayMillis) {tools.postDelayed(runnable, delayMillis);}

	public static boolean isUiThread() {return tools.isUiThread();}

	public static void closeQuietly(HttpURLConnection conn) {tools.closeQuietly(conn);}


	@NonNull
	public static Context getContext() {return tools.getContext();}
}
