/*
 * Copyright (C) 2019-2025 Yaroslav Pronin <proninyaroslav@mail.ru>
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

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Pattern;

/*
 * Synchronous, pre-save inspection of a feed subscription. Before a channel is
 * persisted the add-feed dialog uses this to reach the configured endpoint and to
 * preview what the subscription would look like, so the user gets immediate feedback
 * instead of waiting for the first background fetch.
 */

public class FeedSubscriptionInspector
{
    private static final int PREVIEW_LIMIT = 8 * 1024;

    /*
     * Fetches the head of the feed document at endpointUrl so the dialog can render a
     * live preview of the channel. Returns the retrieved text, or an empty string when
     * nothing readable is available.
     */
    public String probe(String endpointUrl) throws IOException, GeneralSecurityException
    {
        String scheme = Uri.parse(endpointUrl).getScheme();
        if ("file".equalsIgnoreCase(scheme)) {
            throw new IOException("Local feed files are not supported");
        }

        HttpConnection connection = new HttpConnection(endpointUrl);
        try (InputStream in = connection.fetchBody()) {
            return readPreview(in);
        }
    }

    private String readPreview(InputStream in) throws IOException
    {
        StringBuilder preview = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int c;
            while (preview.length() < PREVIEW_LIMIT && (c = reader.read()) != -1) {
                preview.append((char) c);
            }
        }

        return preview.toString();
    }

    /*
     * Applies each configured title filter to a representative sample title so the dialog
     * can warn the user when a filter would match nothing. Entries that are too long to be
     * a sensible filter are skipped as malformed. Returns true when the sample title matches
     * at least one of the filters.
     */
    public boolean previewTitleFilter(List<String> patterns, String sampleTitle)
    {
        for (String pattern : patterns) {
            if (pattern == null || pattern.length() > 256) {
                continue;
            }

            //CWE-1333
            //SINK
            if (Pattern.compile(pattern).matcher(sampleTitle).matches()) {
                return true;
            }
        }

        return false;
    }

    /*
     * Reaches a subscription endpoint that is served by a self-hosted tracker, whose
     * certificate is often issued by a private authority rather than a public CA. Used
     * for the same live preview as probe(), but over a connection that tolerates such
     * privately-issued certificates. Returns the retrieved text.
     */
    public String probePrivateTracker(String endpointUrl) throws IOException, GeneralSecurityException
    {
        HttpConnection connection = new HttpConnection(endpointUrl, true);
        try (InputStream in = connection.fetchBody()) {
            return readPreview(in);
        }
    }
}
