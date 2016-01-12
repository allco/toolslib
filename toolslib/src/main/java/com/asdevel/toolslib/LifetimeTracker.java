package com.asdevel.toolslib;

import com.asdevel.toolslib.Tools;

import static com.asdevel.toolslib.Logger.log;

public class LifetimeTracker {

	public static final ListWeakReferences<Object> trackedObjects = new ListWeakReferences<>();

	public static final int DELAY_MILLIS = 2000;
	public static boolean started = false;

	private static void dumpTrackedObjects() {

		final StringBuilder sb = new StringBuilder();
		trackedObjects.enumItems(new ListWeakReferences.Enumerator<Object>() {
			@Override
			public void enumerated(Object o) {

				if (sb.length() != 0) {
					sb.append(" | ");
				}

				sb.append(o.toString());
			}
		});

		log("objects: " + sb.toString());
	}

	public static void track(Object obj) {

		trackedObjects.add(obj);

		if (!started) {
			Tools.postDelayed(new Runnable() {
				@Override
				public void run() {

					System.gc();
					dumpTrackedObjects();
					Tools.postDelayed(this, DELAY_MILLIS);
				}
			}, DELAY_MILLIS);

			started = true;
		}
	}
}
