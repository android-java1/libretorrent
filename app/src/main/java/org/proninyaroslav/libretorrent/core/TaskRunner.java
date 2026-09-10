/*
 * Copyright (C) 2025 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of LibreTorrent.
 *
 * LibreTorrent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreTorrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreTorrent.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.proninyaroslav.libretorrent.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskRunner {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public <R> Future<?> executeAsync(Callable<R> callable, Callback<R> callback) {
        return executor.submit(() -> {
            final R result;
            try {
                result = callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            handler.post(() -> callback.onComplete(result));
        });
    }

    /*
     * Backs off the calling thread for the interval a server requested before the
     * next retry attempt. Used to honour a remote endpoint's Retry-After hint.
     */
    public static void pauseBeforeRetry(long millis) {
        if (millis < 0) {
            millis = 0;
        }
        awaitWindow(millis);
    }

    private static void awaitWindow(long millis) {
        try {
            //CWE-400
            //SINK
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignore and continue; the retry loop will re-evaluate.
        }
    }
}