package com.example.musicloud

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import com.example.musicloud.database.Song


fun formatSongs (songs: List<Song>, resources: Resources): Spanned {
    val sb = StringBuilder ()

    sb.apply {
        append (resources.getString (R.string.title))
        songs.forEach {
            append ("<br>")
            append(resources.getString(R.string.created_at))
            append(it.createdAt)
        }
    }
    return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
}