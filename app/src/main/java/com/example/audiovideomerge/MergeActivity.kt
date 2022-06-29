package com.example.audiovideomerge

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.pro.audiotrimmer.AudioTrimmerView
import java.io.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MergeActivity : AppCompatActivity(),AudioTrimmerView.OnSelectedRangeChangedListener {
    var imgPlayPauseAudio: ImageView? = null
    var txtAudioDuration: TextView? = null
//    var audioUri: Uri? = null
    var startTimeMillis: Long = 0;
    var endTimeMillis: Long = 0;
//    var videoUri: Uri? = null
    var mediaPlayer: MediaPlayer? = null
    var runable: Runnable? = null
    var mHandler: Handler? = null
    var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    var longRunningTaskFuture: Future<*>? = null
    var audiview: AudioTrimmerView? = null

        var exoPlayer: SimpleExoPlayer? = null
    var expVideoPlayer: PlayerView? = null
    var currentWindow = 0
    var chosenAudioFile: File? = null
    var chosenVideoFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)
        audiview = findViewById<AudioTrimmerView>(R.id.audioTrimmer);
//        expVideoPlayer = findViewById<PlayerView>(R.id.expVideoPlayer);
        imgPlayPauseAudio = findViewById<ImageView>(R.id.imgPlayPauseAudio);
        txtAudioDuration = findViewById<TextView>(R.id.txtAudioDuration);
        var intens = intent;
        chosenAudioFile =File(intens.extras!!.getString("audioUri"))
        chosenVideoFile =File( intens.extras!!.getString("videoUri"))
        Log.e("audiouri ", chosenAudioFile!!.path.toString())
        Log.e("videoUri ", chosenVideoFile!!.path.toString())
//        initializePlayer(videoUri!!)
//        setWaveform(audioUri!!, videoUri!!)
        savewave()
    }

    override fun onDestroy() {

        runable?.let { mHandler?.removeCallbacks(it) }
        longRunningTaskFuture?.cancel(true)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        runable = null
        mHandler = null
        chosenAudioFile = null
        chosenVideoFile = null
        super.onDestroy()

    }
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
    fun savewave(){
//        val ins: InputStream
//        val FILE_NAM = "mergevideo"
//        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
//        val file = File(directory.toString())
//        val outputFile =
//            file.toString() + File.separator + FILE_NAM + "_audiotmp.mp4"
//        ins = baseContext.getContentResolver().openInputStream(audioUri!!)!!
//        var chosenAudioFile = createFileFromInputStream(ins, outputFile)
        audiview!!.setAudio(chosenAudioFile!!);
        audiview!!.show()
        audiview!!.setTotalAudioLength(extractAudioLength(chosenAudioFile!!.path))
        audiview!!.setExtraDragSpace(80F)
        audiview!!.setMinDuration(10000)
        getDummyWaveSample()
//                        audiview!!.setAudioSamples()
        audiview!!.setOnSelectedRangeChangedListener(this)
        showAudioPreview(true, chosenAudioFile!!)
    }

    fun setupAudioFile(audioUri: Uri) {
        val audioInputStream: InputStream
        val FILE_NAM = "audiovideomerge"
        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        val audioFile = File(directory.toString())
        val outputAudioFilePath = audioFile.toString() + File.separator + FILE_NAM + "_audiotmp.mp4"
        audioInputStream = baseContext.getContentResolver().openInputStream(audioUri!!)!!
        chosenAudioFile = createFileFromInputStream(audioInputStream, outputAudioFilePath)
    }

    fun setupVideoFile(videoUrl: Uri) {
        val videoInputStream: InputStream
        val FILE_NAM = "audiovideomerge"
        val directory: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        val videoFIle = File(directory.toString())
        val outputVideoFilePath = videoFIle.toString() + File.separator + FILE_NAM + "_videotmp.mp4"
        videoInputStream = baseContext.getContentResolver().openInputStream(videoUrl)!!
        chosenVideoFile = createFileFromInputStream(videoInputStream, outputVideoFilePath)
    }

//    fun setWaveform(audioUri: Uri, videoUrl: Uri) {
//        setupAudioFile(audioUri)
//        setupVideoFile(videoUrl)
//        audiview!!.setAudio(chosenAudioFile!!);
//        setDummyWaveSample()
//        audiview!!.show()
//        var audioLength = extractAudioLength(chosenAudioFile!!.path)
//        var videoLength = extractAudioLength(chosenVideoFile!!.path)
//        Log.e("setWaveform", "audio " + audioLength + " video " + videoLength)
//        audiview!!.setTotalAudioLength(audioLength)
////        endTimeMillis = audioLength
//        audiview!!.setExtraDragSpace(80F)
//        audiview!!.setMinDuration(5000)
//
////                        audiview!!.setAudioSamples()
//        audiview!!.setOnSelectedRangeChangedListener(object :
//            AudioTrimmerView.OnSelectedRangeChangedListener {
//
//            override fun onSelectRangeStart() {
//                Log.e("onSelectRangeStart", "");
////                                TODO("Not yet implemented")
//            }
//
//            override fun onSelectRange(startMillis: Long, endMillis: Long) {
//                Log.e(
//                    "onSelectedRange",
//                    "statrt " + startMillis.toString() + " end " + endMillis.toString()
//                )
////                                TODO("Not yet implemented")
//            }
//
//            override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
//                Log.e(
//                    "onSelectRangeEnd",
//                    "statrt " + startMillis.toString() + " end " + endMillis.toString()
//                )
//                startTimeMillis = startMillis;
//                endTimeMillis = endMillis;
////                exoPlayer!!.seekTo(currentWindow, startTimeMillis)
//                mediaPlayer?.seekTo(startTimeMillis.toInt())
//
////                                TODO("Not yet implemented")
//            }
//
//            override fun onProgressStart() {
//                Log.e("onProgressStart", "");
////                                TODO("Not yet implemented")
//            }
//
//            override fun onProgressEnd(millis: Long) {
//                mediaPlayer?.seekTo(millis.toInt())
////                exoPlayer!!.seekTo(currentWindow, millis)
//                Log.e("onProgressEnd", "statrt " + millis.toString())
////                                TODO("Not yet implemented")
//            }
//
//            override fun onDragProgressBar(millis: Long) {
//                Log.e("onDragProgressBar", "statrt " + millis.toString())
////                                TODO("Not yet implemented")
//            }
//
//        })
//        showAudioPreview(true, chosenAudioFile!!)
//    }

//    private fun initializePlayer(vidioUri: Uri) {
//        exoPlayer = SimpleExoPlayer.Builder(baseContext).build()
//        expVideoPlayer!!.setPlayer(exoPlayer)
//        expVideoPlayer!!.hideController()
//        val mediaSource: MediaSource = buildMediaSource(vidioUri, baseContext)
//        exoPlayer!!.setPlayWhenReady(false)
////        exoPlayer!!.seekTo(currentWindow, playbackPosition)
//        exoPlayer!!.setMediaSource(mediaSource)
//        exoPlayer!!.prepare()
//        exoPlayer!!.volume=0f
//
//    }

    private fun buildMediaSource(uri: Uri, context: Context): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer-codelab")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }
    private fun getDummyWaveSample() {

        val data = ShortArray(70)
        for (i in data.indices) {
            data[i] = Random().nextInt(data.size).toShort()
        }
        audiview!!.setAudioSamples(data)
//        return data
    }
    //    fun showAudioPreview(show: Boolean, chosendFile: File) {
//
//        if (show) {
//            mediaPlayer = MediaPlayer()
//            prepareMediaPlayer(chosendFile)
//            if (mediaPlayer!!.isPlaying) {
//                imgPlayPauseAudio!!.setImageDrawable(
//                    baseContext.resources.getDrawable(
//                        R.drawable.ic_pause
//                    )
//                )
//            } else {
//                imgPlayPauseAudio!!.setImageDrawable(
//                    baseContext.resources.getDrawable(
//                        R.drawable.ic_play
//                    )
//                )
//            }
//            mediaPlayer!!.setOnCompletionListener {
//                imgPlayPauseAudio!!.setImageDrawable(
//                    baseContext.resources.getDrawable(
//                        R.drawable.ic_play
//                    )
//                )
////                exoPlayer!!.stop()
//            }
//            mHandler = Handler(Looper.getMainLooper())
//            audiview!!.setMaxDuration(mediaPlayer!!.duration.toLong());
////            endTimeMillis = mediaPlayer!!.duration.toLong();
////           previewSeekbar.setOnSeekBarChangeListener(object :
////                SeekBar.OnSeekBarChangeListener {
////                override fun onStopTrackingTouch(seekBar: SeekBar) {}
////                override fun onStartTrackingTouch(seekBar: SeekBar) {}
////                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
////                    if (fromUser) {
////                       mediaPlayer?.seekTo(progress)
////                    }
////                }
////            })
//            txtAudioDuration!!.setText(
//                "0:00/" + calculateDuration(
//                    mediaPlayer!!.getDuration().toLong()
//                )
//            )
//            audiview!!.setAudioProgress(0)
//            mediaPlayer!!.seekTo(0)
////            exoPlayer!!.seekTo(currentWindow, 0)
//            imgPlayPauseAudio!!.setOnClickListener {
//
//                if (!mediaPlayer!!.isPlaying) {
//                    imgPlayPauseAudio!!.setImageDrawable(
//                        baseContext.resources.getDrawable(
//                            R.drawable.ic_pause
//                        )
//                    )
//                    mediaPlayer!!.start()
////                    exoPlayer!!.play()
//
//                    runable = object : Runnable {
//                        override fun run() {
//                            // Updateing SeekBar every 100 miliseconds
//
//                            mHandler?.postDelayed(this, 50)
//                            audiview!!.setAudioProgress(
//                                mediaPlayer?.getCurrentPosition()!!.toLong()
//                            )
////                            Log.e("mediaplayer ","cutrrentpos "+mediaPlayer!!.currentPosition.toLong()+" endtimemilis "+endTimeMillis)
//                            if (mediaPlayer!!.currentPosition.toLong() >= endTimeMillis) {
//                                print("meidaplyaerended ")
//                                mediaPlayer!!.seekTo(startTimeMillis.toInt())
////                                exoPlayer!!.seekTo(currentWindow, startTimeMillis)
//                                return
//                            }
//                            //For Showing time of audio(inside runnable)
//                            val miliSeconds: Long =
//                                mediaPlayer?.getCurrentPosition()!!.toLong()
//                            if (miliSeconds != 0L) {
//                                //if audio is playing, showing current time;
//                                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(miliSeconds)
//                                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
//                                if (minutes == 0L) {
//                                    txtAudioDuration!!.setText(
//                                        "0:" + seconds + "/" + calculateDuration(
//                                            mediaPlayer?.getDuration()!!.toLong()
//                                        )
//                                    )
//                                } else {
//                                    if (seconds >= 60) {
//                                        val sec = seconds - minutes * 60
//                                        txtAudioDuration!!.setText(
//                                            minutes.toString() + ":" + sec + "/" + calculateDuration(
//                                                mediaPlayer?.getDuration()!!.toLong()
//                                            )
//                                        )
//                                    }
//                                }
//                            } else {
//                                //Displaying total time if audio not playing
//                                val totalTime: Long = mediaPlayer?.getDuration()!!.toLong()
//                                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(totalTime)
//                                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(totalTime)
//                                if (minutes == 0L) {
//                                    txtAudioDuration!!.setText("0:$seconds")
//                                } else {
//                                    if (seconds >= 60) {
//                                        val sec = seconds - minutes * 60
//                                        txtAudioDuration!!.setText("$minutes:$sec")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    longRunningTaskFuture = executorService.submit(runable)
//                    runable!!.run()
//                } else {
//                    mediaPlayer?.pause();
////                    exoPlayer!!.pause()
//                    imgPlayPauseAudio!!.setImageDrawable(
//                        baseContext.resources.getDrawable(
//                            R.drawable.ic_play
//                        )
//                    )
//                }
//            }
//        }
//    }
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
            endTimeMillis=mediaPlayer!!.duration.toLong()
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
//                            Log.e("mediaplayer ","cutrrentpos "+mediaPlayer!!.currentPosition.toLong()+" endtimemilis "+endTimeMillis)
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

    private fun setDummyWaveSample() {
        val data = ShortArray(50)
        for (i in data.indices)
            data[i] = Random().nextInt(data.size).toShort()
        audiview!!.setAudioSamples(data)

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

}