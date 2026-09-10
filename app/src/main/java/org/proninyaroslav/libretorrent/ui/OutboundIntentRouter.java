/*
 * Copyright (C) 2016-2025 Yaroslav Pronin <proninyaroslav@mail.ru>
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

package org.proninyaroslav.libretorrent.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

/**
 * Central helper for the intents LibreTorrent hands off to other components on the
 * device. Concentrating the dispatch logic in one place lets the app apply consistent
 * task and category flags before an activity is launched on the user's behalf, instead
 * of scattering {@code startActivity} calls across the UI layer.
 */
public class OutboundIntentRouter {
    private OutboundIntentRouter() {
    }

    /**
     * Hand a prepared intent to the system so the matching activity can take over.
     * The intent is only forwarded when at least one activity is able to handle it,
     * avoiding an {@link android.content.ActivityNotFoundException} on the user's device.
     *
     * @param context context used to start the activity
     * @param intent  the fully prepared target intent
     */
    public static void dispatch(@NonNull Context context, @NonNull Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            launch(context, intent);
        }
    }

    private static void launch(@NonNull Context context, @NonNull Intent intent) {
        //CWE-940
        //SINK
        context.startActivity(intent);
    }

    /**
     * Build the action a completion notification offers the user: view the freshly
     * finished download in whichever application the device has registered for its
     * content type. The intent is deliberately left implicit so the platform can offer
     * the user's preferred handler instead of pinning a single activity.
     *
     * @param content location of the finished download to open
     * @return an implicit {@link Intent} suitable for a notification content action
     */
    public static Intent buildFinishedTorrentAction(@NonNull android.net.Uri content) {
        //CWE-927
        //SOURCE
        Intent action = new Intent(Intent.ACTION_VIEW, content);
        action.addCategory(Intent.CATEGORY_DEFAULT);
        return action;
    }
}
