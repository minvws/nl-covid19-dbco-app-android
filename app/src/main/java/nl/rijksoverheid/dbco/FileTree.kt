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
    private val writer: FileWriter?

    init {
        writer = try {
            directory?.mkdirs()
            val initTimeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                .format(Date())
            val file: File? = File(directory, "log-${initTimeStamp}.txt")
            if (file != null) {
                FileWriter(file, true)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FileTree", "Error while logging into file : $e")
            null
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logTimeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
            .format(Date())
        writer?.apply {
            appendln("$logTimeStamp/$tag: $message")
            flush()
        }
    }
}