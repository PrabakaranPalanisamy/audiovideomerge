package com.example.audiovideomerge

import Videocompress.Companion.compressProcess
import Videocompress.Companion.mergeAudioVideo
import android.Manifest.permission
import android.content.ContentProvider
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.audiovideomerge.videooperations.Switchfile
import com.pro.audiotrimmer.AudioTrimmerView
import java.io.*
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    var txtMergeLocation: TextView? = null
    var txtAudioDuration: TextView? = null
    var audiview: AudioTrimmerView? = null
    var imgPlayPauseAudio: ImageView? = null
    var audioUri: Uri? = null
    var startTimeMillis: Long = 0;
    var endTimeMillis: Long = 0;
    var videoUri: Uri? = null
    var mediaPlayer: MediaPlayer? = null
    var runable: Runnable? = null
    var mHandler: Handler? = null
    var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    var longRunningTaskFuture: Future<*>? = null
    override fun onDestroy() {
        super.onDestroy()
        runable?.let { mHandler?.removeCallbacks(it) }
        longRunningTaskFuture?.cancel(true)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        runable = null
        mHandler = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var btnchooseAudio = findViewById<Button>(R.id.btnchooseAudio);
        var btnchooseVideo = findViewById<Button>(R.id.btnchooseVideo);
        var btnMerge = findViewById<Button>(R.id.btnMerge);
        txtMergeLocation = findViewById<TextView>(R.id.txtMergeLocation);
        audiview = findViewById<AudioTrimmerView>(R.id.audioTrimmerView);
        imgPlayPauseAudio = findViewById<ImageView>(R.id.imgPlayPauseAudio);
        txtAudioDuration = findViewById<TextView>(R.id.txtAudioDuration);
        val onActivityResultLauncher = registerForActivityResult(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == RESULT_OK) {

                        val data = result.data
                        val uri = data!!.data
                        audioUri = uri
                        btnchooseAudio.setText(uri!!.path)
                        val ins: InputStream
                        val FILE_NAM = "mergevideo"
                        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
                        val file = File(directory.toString())
                        val outputFile =
                            file.toString() + File.separator + FILE_NAM + "_audiotmp.mp4"
                        ins = baseContext.getContentResolver().openInputStream(audioUri!!)!!
                        var chosenAudioFile = createFileFromInputStream(ins, outputFile)
                        audiview!!.setAudio(chosenAudioFile!!);
                        audiview!!.show()
                        audiview!!.setTotalAudioLength(extractAudioLength(chosenAudioFile.path))
                        audiview!!.setExtraDragSpace(80F)
                        audiview!!.setAudioSamples(getDummyWaveSample())
                        audiview!!.setOnSelectedRangeChangedListener(object :
                            AudioTrimmerView.OnSelectedRangeChangedListener {
                            override fun onSelectRangeStart() {
                                Log.e("onSelectRangeStart", "");
//                                TODO("Not yet implemented")
                            }

                            override fun onSelectRange(startMillis: Long, endMillis: Long) {
                                Log.e(
                                    "onSelectedRange",
                                    "statrt " + startMillis.toString() + " end " + endMillis.toString()
                                )
//                                TODO("Not yet implemented")
                            }

                            override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
                                Log.e(
                                    "onSelectRangeEnd",
                                    "statrt " + startMillis.toString() + " end " + endMillis.toString()
                                )
                                startTimeMillis = startMillis;
                                endTimeMillis = endMillis;
                                mediaPlayer?.seekTo(startTimeMillis.toInt())
//                                TODO("Not yet implemented")
                            }

                            override fun onProgressStart() {
                                Log.e("onProgressStart", "");
//                                TODO("Not yet implemented")
                            }

                            override fun onProgressEnd(millis: Long) {
                                mediaPlayer?.seekTo(millis.toInt())
                                Log.e("onProgressEnd", "statrt " + millis.toString())
//                                TODO("Not yet implemented")
                            }

                            override fun onDragProgressBar(millis: Long) {
                                Log.e("onDragProgressBar", "statrt " + millis.toString())
//                                TODO("Not yet implemented")
                            }

                        })
                        showAudioPreview(true, chosenAudioFile)
                    }
                }

                private val contentResolver: ContentProvider?
                    private get() = null
            })
        val onActivityVideoResultLauncher = registerForActivityResult(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == RESULT_OK) {

                        val data = result.data
                        val uri = data!!.data
                        videoUri = uri
                        btnchooseVideo.setText(uri!!.path)
                    }
                }

                private val contentResolver: ContentProvider?
                    private get() = null
            })

        btnchooseAudio.setOnClickListener {
            if (checkPermission()) {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "*/*"
                intent.type = "*/*"
                onActivityResultLauncher.launch(intent)
            }
        }
        btnMerge.setOnClickListener {
            if (audioUri != null && videoUri != null)
                mergeaudioVideo(audioUri!!, videoUri!!);
        }
        btnchooseVideo.setOnClickListener {
            if (checkPermission()) {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "video/mp4"
                onActivityVideoResultLauncher.launch(intent)
            }
        }
    }

    fun showAudioPreview(show: Boolean, chosendFile: File) {

        if (show) {
            mediaPlayer = MediaPlayer()
            prepareMediaPlayer(chosendFile)
            if (mediaPlayer!!.isPlaying) {
                imgPlayPauseAudio!!.setImageDrawable(
                    baseContext.resources.getDrawable(
                        R.drawable.ic_pause
                    )
                )
            } else {
                imgPlayPauseAudio!!.setImageDrawable(
                    baseContext.resources.getDrawable(
                        R.drawable.ic_play
                    )
                )
            }
            mediaPlayer!!.setOnCompletionListener {
                imgPlayPauseAudio!!.setImageDrawable(
                    baseContext.resources.getDrawable(
                        R.drawable.ic_play
                    )
                )
            }
            mHandler = Handler(Looper.getMainLooper())
            audiview!!.setMaxDuration(mediaPlayer!!.duration.toLong());
//           previewSeekbar.setOnSeekBarChangeListener(object :
//                SeekBar.OnSeekBarChangeListener {
//                override fun onStopTrackingTouch(seekBar: SeekBar) {}
//                override fun onStartTrackingTouch(seekBar: SeekBar) {}
//                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                    if (fromUser) {
//                       mediaPlayer?.seekTo(progress)
//                    }
//                }
//            })
            txtAudioDuration!!.setText(
                "0:00/" + calculateDuration(
                    mediaPlayer!!.getDuration().toLong()
                )
            )
            audiview!!.setAudioProgress(0)
            mediaPlayer!!.seekTo(0)
            imgPlayPauseAudio!!.setOnClickListener {

                if (!mediaPlayer!!.isPlaying) {
                    imgPlayPauseAudio!!.setImageDrawable(
                        baseContext.resources.getDrawable(
                            R.drawable.ic_pause
                        )
                    )
                    mediaPlayer!!.start()

                    runable = object : Runnable {
                        override fun run() {
                            // Updateing SeekBar every 100 miliseconds

                            mHandler?.postDelayed(this, 50)
                            audiview!!.setAudioProgress(
                                mediaPlayer?.getCurrentPosition()!!.toLong()
                            )
                            Log.e("mediaplayer ","cutrrentpos "+mediaPlayer!!.currentPosition.toLong()+" endtimemilis "+endTimeMillis)
                            if (mediaPlayer!!.currentPosition.toLong() >= endTimeMillis) {
                                mediaPlayer!!.seekTo(startTimeMillis.toInt())
                                return
                            }
                            //For Showing time of audio(inside runnable)
                            val miliSeconds: Long =
                                mediaPlayer?.getCurrentPosition()!!.toLong()
                            if (miliSeconds != 0L) {
                                //if audio is playing, showing current time;
                                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(miliSeconds)
                                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
                                if (minutes == 0L) {
                                    txtAudioDuration!!.setText(
                                        "0:" + seconds + "/" + calculateDuration(
                                            mediaPlayer?.getDuration()!!.toLong()
                                        )
                                    )
                                } else {
                                    if (seconds >= 60) {
                                        val sec = seconds - minutes * 60
                                        txtAudioDuration!!.setText(
                                            minutes.toString() + ":" + sec + "/" + calculateDuration(
                                                mediaPlayer?.getDuration()!!.toLong()
                                            )
                                        )
                                    }
                                }
                            } else {
                                //Displaying total time if audio not playing
                                val totalTime: Long = mediaPlayer?.getDuration()!!.toLong()
                                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(totalTime)
                                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(totalTime)
                                if (minutes == 0L) {
                                    txtAudioDuration!!.setText("0:$seconds")
                                } else {
                                    if (seconds >= 60) {
                                        val sec = seconds - minutes * 60
                                        txtAudioDuration!!.setText("$minutes:$sec")
                                    }
                                }
                            }
                        }
                    }
                    longRunningTaskFuture = executorService.submit(runable)
                    runable!!.run()
                } else {
                    mediaPlayer?.pause();
                    imgPlayPauseAudio!!.setImageDrawable(
                        baseContext.resources.getDrawable(
                            R.drawable.ic_play
                        )
                    )
                }
            }
        }
    }

    private fun prepareMediaPlayer(
        chosendFile: File
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                mediaPlayer!!.setDataSource(
                    baseContext,
                    Uri.parse((chosendFile.absolutePath))
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                mediaPlayer!!.setDataSource(
                    baseContext,
                    Uri.parse((chosendFile.absolutePath))
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun calculateDuration(duration: Long): String? {
        var finalDuration = ""
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
        if (minutes == 0L) {
            finalDuration = "0:$seconds"
        } else {
            if (seconds >= 60) {
                val sec = seconds - minutes * 60
                finalDuration = "$minutes:$sec"
            }
        }
        return finalDuration
    }

    internal fun extractAudioLength(audioPath: String): Long {
        val retriever = try {
            MediaMetadataRetriever()
                .apply { setDataSource(audioPath) }
        } catch (e: IllegalArgumentException) {
            return 0L
        }

        val length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()

        return length?.toLong() ?: 0L
    }

    private fun getDummyWaveSample(): ShortArray {
        val data = ShortArray(50)
        for (i in data.indices)
            data[i] = Random().nextInt(data.size).toShort()

        return data
    }

    private fun mergeaudioVideo(inputuri: Uri, videoInputUri: Uri) {
        val FILE_NAM = "mergevideo"
        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        val file = File(directory.toString())
        val outputAudioFile = file.toString() + File.separator + FILE_NAM + "_audiotmp.mp3"
        val outputFile = file.toString() + File.separator + FILE_NAM + "_tmp.mp4"
        val ins: InputStream
        val videosIns: InputStream
        try {
            ins = baseContext.getContentResolver().openInputStream(inputuri)!!
            var chosenAudioFile = createFileFromInputStream(ins, outputAudioFile)

            videosIns = baseContext.contentResolver.openInputStream(videoInputUri)!!
            var chosenVideoFile = createFileFromInputStream(videosIns, outputFile)


            val filePath =
                mergeAudioVideo(
                    chosenAudioFile!!.getPath(),
                    chosenVideoFile!!.getPath(), "-00:00:20",
                    this,
                    object : Switchfile {
                        @Throws(URISyntaxException::class)
                        override fun getFile(file: String?) {
                            Log.e("filesize", "after::" + File(file).length() / 1024f)
                            runOnUiThread {
                                txtMergeLocation!!.text = file
                            }
                        }
                    })
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun compressVideo(videoUri: Uri) {
        val FILE_NAM = "video"
        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        val file = File(directory.toString())
        val outputFile = file.toString() + File.separator + FILE_NAM + "_tmp.mp4"
        val ins: InputStream
        try {
            ins = baseContext.getContentResolver().openInputStream(videoUri)!!
            var choosendFile = createFileFromInputStream(ins, outputFile)
            val filePath =
                compressProcess(choosendFile!!.getPath(), this, object : Switchfile {
                    @Throws(URISyntaxException::class)
                    override fun getFile(file: String?) {
                        Log.e("filesize", "after::" + File(file).length() / 1024f)
                        runOnUiThread {
                            txtMergeLocation!!.text = file
                        }
                    }
                })
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun createFileFromInputStream(inputStream: InputStream, fileName: String): File? {
        try {
            val f = File(fileName)
            f.setWritable(true, false)
            val outputStream: OutputStream = FileOutputStream(f)
            val buffer = ByteArray(1024)
            var length = 0
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream.close()
            return f
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun checkPermission(): Boolean {

        return (ContextCompat.checkSelfPermission(
            baseContext,
            permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)

    }

}