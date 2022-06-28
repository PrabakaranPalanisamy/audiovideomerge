import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.ait.qliq.ui.fragment.FFmpegCallBack
import com.example.audiovideomerge.Common
import com.example.audiovideomerge.Common.getFilePath
import com.example.audiovideomerge.videooperations.CallBackOfQuery
import com.example.audiovideomerge.videooperations.Switchfile
import com.simform.videooperations.LogMessage
import java.io.File

class Videocompress() {
    private var isInputVideoSelected: Boolean = false

    companion object {
        val ffmpegQueryExtension = FFmpegQueryExtension()
        var height: Int? = 720
        var width: Int? = 1280

        fun compressProcess(
            input: String,
            mcontext: Activity,
            switchfile: Switchfile
        ): String {
            val outputPath = getFilePath(mcontext, Common.VIDEO)
            var result = ""
            val query = ffmpegQueryExtension.compressor(input, width, height, outputPath)
            CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
                override fun process(logMessage: LogMessage) {
                    Log.e("videocompresscalling", "processing");
                }

                @SuppressLint("SetTextI18n")
                override fun success() {
                    val ouput: String =
                        String.format(outputPath, Common.getFileSize(File(outputPath)))
                    result = ouput
                    switchfile.getFile(result)
                }

                override fun cancel() {
                    Log.e("videocompresscalling", "cancel")
                }

                override fun failed() {
                    result = ""
                    Log.e("videocompresscalling", "failure");
                }
            })
            return result
        }

        fun mergeAudioVideo(
            inputAudio: String,
            inputVideo: String,
            offset:String,
            mcontext: Activity,
            switchfile: Switchfile
        ): String {

            val outputPath = getFilePath(mcontext, Common.VIDEO)
            var result = ""
            Log.e("videocompress ",inputAudio)
            Log.e("videocompress ",inputVideo)
            Log.e("videocompress ",outputPath)
            val query = ffmpegQueryExtension.mergeAudioVideo2(inputVideo,inputAudio , outputPath,offset)
            CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
                override fun process(logMessage: LogMessage) {
                    Log.e("videocompresscalling", "processing");
                }

                @SuppressLint("SetTextI18n")
                override fun success() {
                    val ouput: String =
                        String.format(outputPath, Common.getFileSize(File(outputPath)))
                    result = ouput
                    switchfile.getFile(result)
                }

                override fun cancel() {
                    Log.e("videocompresscalling", "cancel")
                }

                override fun failed() {
                    result = ""
                    Log.e("videocompresscalling", "failure");
                }
            })
            return result
        }
    }



}