/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("LogNotTimber")
class FileTree(directory: File?) : Timber.DebugTree() {

    private val writer: FileWriter? = try {
        directory?.mkdirs()
        val initTimestamp = SimpleDateFormat(TIMESTAMP_FORMAT_INIT, Locale.ROOT).format(Date())
        FileWriter(File(directory, "log-${initTimestamp}.txt"), true)
    } catch (e: Exception) {
        Log.e("FileTree", "Error while logging into file : $e")
        null
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logTimestamp = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.ROOT).format(Date())
        writer?.apply {
            appendLine("$logTimestamp/$tag: $message")
            flush()
        }
    }

    companion object {

        private const val TIMESTAMP_FORMAT_INIT = "yyyy-MM-dd HH:mm:ss"
        private const val TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
    }
}