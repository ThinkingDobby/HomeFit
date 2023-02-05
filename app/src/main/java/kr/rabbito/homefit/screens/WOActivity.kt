package kr.rabbito.homefit.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.rabbito.homefit.databinding.ActivityWoBinding
import kr.rabbito.homefit.utils.calc.TimeCalc
import kr.rabbito.homefit.workout.WorkoutCore
import kr.rabbito.homefit.workout.WorkoutData
import kr.rabbito.homefit.workout.WorkoutState
import kr.rabbito.homefit.workout.poseDetection.CameraSource
import kr.rabbito.homefit.workout.poseDetection.PoseDetectorProcessor
import kr.rabbito.homefit.workout.poseDetection.PreferenceUtils
import kr.rabbito.homefit.workout.tts.PoseAdviceTTS
import java.io.IOException
import kotlin.concurrent.timer

class WOActivity : AppCompatActivity() {
    private var mBinding: ActivityWoBinding? = null
    private val binding get() = mBinding!!

    private var cameraSource: CameraSource? = null
    private var selectedModel = POSE_DETECTION
    private lateinit var mat: android.opengl.Matrix

    private var tts: PoseAdviceTTS? = null

    private var workoutIdx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createCameraSource(selectedModel)
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)

        // tts 출력 위한 부분 - 추후 로직 검사 파일로 이동시킬 수도 있음
        tts = PoseAdviceTTS(this)

        /*
        구상: 특정 운동 타일 선택해 넘어오면, intent 이용해서 운동 인덱스 전달됨 -> 해당 인덱스로 initView 호출
         */

        // 운동에 맞게 화면 초기화, 위젯 제시
        initView(workoutIdx)

        startTimer()

        binding.woPvPreviewView.stop()
        startCameraSource()

        // 임시
        binding.woBtnPause.setOnClickListener {
            tts!!.raiseArmTTS() // tts 출력 테스트
        }
        binding.woBtnStop.setOnClickListener {
            startActivity(Intent(this, WOReportActivity::class.java))
        }
    }

    private fun createCameraSource(model: String) {
        Log.d("debug", "createCameraSorce")
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, binding.woGoGraphicOverlay)
        }
        try {

            //POSE_DETECTION ->

            val poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(this)
            Log.i(TAG, "Using Pose Detector with options $poseDetectorOptions")
            val shouldShowInFrameLikelihood =
                PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this)
            val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
            val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
            val runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this)
            cameraSource!!.setMachineLearningFrameProcessor(
                PoseDetectorProcessor(
                    this,
                    poseDetectorOptions,
                    shouldShowInFrameLikelihood,
                    visualizeZ,
                    rescaleZ,
                    runClassification,
                    /* isStreamMode = */
                    true,
                    binding,
                    workoutIdx,
                )
            )

            //}
            /*SELFIE_SEGMENTATION -> {
                cameraSource!!.setMachineLearningFrameProcessor(SegmenterProcessor(this))
            }*/
            //else -> Log.d("debug", "Unknown model")

        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: ", e)
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                binding.woPvPreviewView.start(cameraSource!!, binding.woGoGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    // 최초 화면 초기화
    private fun initView(workoutIdx: Int) {
        /*
        구상: 운동 선택 -> 선택한 운동에 해당하는 위젯들을 생성하는 함수(generateWidgets) 실행
         */

        // 선택한 운동에 맞게 위젯 로드
        val workoutView = WorkoutCore(this, binding).workoutViews[workoutIdx]
        workoutView.generateWidgets()

        // 기본 위젯 로드
        binding.woTvTitle.text = WorkoutData.workoutNamesKOR[workoutIdx]
        binding.woTvSet.text = WorkoutState.set.toString()
        binding.woTvCount.text = WorkoutState.count.toString()
        val elapTime = TimeCalc.secToHourMinSec(WorkoutState.elapSec)
        binding.woTvElapTime.text =
            String.format("%02d:%02d:%02d", elapTime[0], elapTime[1], elapTime[2])
        val remainTime = TimeCalc.secToHourMinSec(WorkoutState.remainSec)
        binding.woTvRemainTime.text =
            String.format("%02d:%02d:%02d", remainTime[0], remainTime[1], remainTime[2])
    }

    private fun startTimer() {
        timer(period = 1000) {
            runOnUiThread {
                WorkoutState.remainSec -= 1
                WorkoutState.elapSec += 1

                val elapTime = TimeCalc.secToHourMinSec(WorkoutState.elapSec)
                binding.woTvElapTime.text =
                    String.format("%02d:%02d:%02d", elapTime[0], elapTime[1], elapTime[2])
                val remainTime = TimeCalc.secToHourMinSec(WorkoutState.remainSec)
                binding.woTvRemainTime.text =
                    String.format("%02d:%02d:%02d", remainTime[0], remainTime[1], remainTime[2])
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createCameraSource(selectedModel)
        startCameraSource()
    }

    /** Stops the camera. */
    override fun onPause() {
        super.onPause()
        binding.woPvPreviewView.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }

    companion object {
        private const val POSE_DETECTION = "Pose Detection"
        private const val TAG = "WOActivity"
    }
}