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

package org.proninyaroslav.libretorrent.ui;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import org.proninyaroslav.libretorrent.core.RepositoryHelper;
import org.proninyaroslav.libretorrent.core.storage.FeedRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class NavBarFragmentViewModel extends AndroidViewModel {
    private static final String TAG = NavBarFragmentViewModel.class.getSimpleName();

    private final FeedRepository feedRepo;

    public NavBarFragmentViewModel(@NonNull Application application) {
        super(application);

        feedRepo = RepositoryHelper.getFeedRepository(application);
    }

    public Flowable<Integer> observeUnreadFeedsCount() {
        return feedRepo.observeUnreadFeedIdList().map(List::size);
    }

    /**
     * Reports the label the sending app attached to a shared item, so a hand-off that
     * did not resolve to a known feed can be traced afterwards.
     *
     * @param sharedLabel the label supplied by the sending app, if any
     */
    public void recordIntakeSource(@Nullable String sharedLabel) {
        if (sharedLabel == null) {
            return;
        }
        var details = new ArrayList<String>();
        details.add("origin=share");
        details.add("label=" + sharedLabel.replace("\r", ""));
        //CWE-117
        //SINK
        Log.e(TAG, "Unrecognized intake source: " + TextUtils.join("; ", details));
    }
}
