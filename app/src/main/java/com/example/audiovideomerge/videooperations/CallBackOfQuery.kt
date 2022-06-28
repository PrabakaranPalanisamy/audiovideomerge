package com.example.audiovideomerge.videooperations

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ait.qliq.ui.fragment.FFmpegCallBack
import com.arthenica.mobileffmpeg.FFmpeg
import com.simform.videooperations.LogMessage
import com.simform.videooperations.Statistics

import java.util.concurrent.CyclicBarrier
import com.arthenica.mobileffmpeg.Config

/**
 * Created by Ashvin Vavaliya on 22,January,2021
 * Simform Solutions Pvt Ltd.
 */
class CallBackOfQuery {
    fun callQuery(query: Array<String>, fFmpegCallBack: FFmpegCallBack) {
        val gate = CyclicBarrier(2)
        object : Thread() {
            override fun run() {
                gate.await()
                process(query, fFmpegCallBack)
            }
        }.start()
        gate.await()
    }

    fun cancelProcess(executionId: Long) {
        if (!executionId.equals(0)) {
            FFmpeg.cancel(executionId)
        } else {
            FFmpeg.cancel()
        }
    }

    fun cancelProcess() {
        FFmpeg.cancel()
    }

    private fun process(query: Array<String>, ffmpegCallBack: FFmpegCallBack) {
        val processHandler = Handler(Looper.getMainLooper())
        Config.enableLogCallback { logMessage ->
            val logs = LogMessage(logMessage.executionId, logMessage.level, logMessage.text)
            processHandler.post {
                ffmpegCallBack.process(logs)
            }
        }
        Config.enableStatisticsCallback { statistics ->
            val statisticsLog =
                Statistics(
                    statistics.executionId,
                    statistics.videoFrameNumber,
                    statistics.videoFps,
                    statistics.videoQuality,
                    statistics.size,
                    statistics.time,
                    statistics.bitrate,
                    statistics.speed
                )
            processHandler.post {
                ffmpegCallBack.statisticsProcess(statisticsLog)
            }
        }
        when (FFmpeg.execute(query)) {
            Config.RETURN_CODE_SUCCESS -> {
                processHandler.post {
                    ffmpegCallBack.success()
                }
            }
            Config.RETURN_CODE_CANCEL -> {
                processHandler.post {
                    ffmpegCallBack.cancel()
                    FFmpeg.cancel()
                }
            }
            else -> {
                processHandler.post {
                    ffmpegCallBack.failed()
                    Config.printLastCommandOutput(Log.INFO)
                }
            }
        }
    }
}