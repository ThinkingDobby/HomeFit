package kr.rabbito.homefit.screens

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kr.rabbito.homefit.client.HomeFitClient
import kr.rabbito.homefit.databinding.ActivityDcameraBinding
import kr.rabbito.homefit.screens.navigatorBar.DReportFragment
import kr.rabbito.homefit.utils.calc.Converter
import kr.rabbito.homefit.utils.calc.PermissionChecker
import java.util.concurrent.ExecutorService
import kotlin.math.atan

class DCameraActivity : AppCompatActivity() {
    private var mBinding: ActivityDcameraBinding? = null
    private val binding get() = mBinding!!

    // 통신
    private var client: HomeFitClient? = null

    // 카메라
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraAnimationListener: Animation.AnimationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDcameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 확인
        permissionCheck()

        // 서버 연결
        client = HomeFitClient()

        // 연결, 사용자명 전송
        Thread {
            try {
                client!!.sendRequest()
                client!!.sendUserName("User")
            } catch (e: NullPointerException) {
                Log.d("connection", "socket not initialized")
            }
        }.start()

        // 촬영 및 사진 전송
        binding.dcameraBtnShot.setOnClickListener {
            takeAndSendPhoto()
        }

        // 임시
        binding.dcameraBtnCancel.setOnClickListener {
//            Thread {
//                val image = BitmapFactory.decodeResource(this.resources, R.drawable.connection_test)
//                client!!.sendImage(image)
//            }.start()
//            startActivity(Intent(this, DReportActivity::class.java))
        }
    }

    private fun permissionCheck() {
        val permissionList = listOf(Manifest.permission.CAMERA)

        if (!PermissionChecker.checkPermission(this, permissionList)) {
            PermissionChecker.requestPermission(this, permissionList)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    // 카메라 실행
    private fun openCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.dcameraPvCameraView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (_: Exception) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takeAndSendPhoto() {
        imageCapture = imageCapture ?: return

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        for (cameraId in cameraIdList){
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                // 후면 카메라
                val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) // 렌즈 초점 거리
                val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!! // 카메라 센서의 물리적인 크기
                val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!! // 센서에서 사용되는 픽셀 배열의 크기
                val horizontalAngle = 2 * atan(physicalSize.width / (2 * focalLength!![0]))
                val verticalAngle = 2 * atan(physicalSize.height / (2 * focalLength[0])) * pixelArraySize.width / pixelArraySize.height
                Log.e("camera", "후면 카메라\nid : ${cameraId}\n렌즈 초점 거리 : ${focalLength[0]}\n센서 크기 : $physicalSize\n 카메라 픽셀 사이즈 : $pixelArraySize\n 수직 화각 : $verticalAngle\n 수평 화각 : $horizontalAngle")
                Thread {
                    try {
                        val cameraInfo = "${focalLength[0]} $verticalAngle $horizontalAngle"
                        client!!.sendCameraInfo(cameraInfo)
                        Log.e("connection", "send cameraInfo")
                    } catch (e: NullPointerException) {
                        Log.e("connection", "$e \tsocket not initialized")
                    }
                }.start()
                break
            }
//            else if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                // 전면 카메라
//                val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) // 렌즈 초점 거리
//                val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!! // 카메라 센서의 물리적인 크기
//                val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!! // 센서에서 사용되는 픽셀 배열의 크기
//                val horizontalAngle = 2 * atan(physicalSize.width / (2 * focalLength!![0]))
//                val verticalAngle = 2 * atan(physicalSize.height / (2 * focalLength[0])) * pixelArraySize.width / pixelArraySize.height
//                Log.e("camera", "전면 카메라\nid : ${cameraId}\n렌즈 초점 거리 : ${focalLength[0]}\n센서 크기 : $physicalSize\n 카메라 픽셀 사이즈 : $pixelArraySize\n 수직 화각 : $verticalAngle\n 수평 화각 : $horizontalAngle")
//            }
        }

        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val bitmap = Converter.imageProxyToBitmap(image)

                    Thread {
                        runOnUiThread {
                            binding.dcameraClLoading.visibility = View.VISIBLE
                        }

                        client!!.sendImage(bitmap)
                        val data = client!!.getData()!!
                        Log.d("connection", "${data[0]}: ${data[1]}")

                        runOnUiThread {
                            binding.dcameraClLoading.visibility = View.INVISIBLE
                        }

                        val intent = Intent(this@DCameraActivity, MainActivity::class.java)
                        intent.putExtra("VIEW_PAGER_INDEX",1)
                        intent.putExtra("FOOD_NAME", data[0].toString())
                        intent.putExtra("FOOD_QUANTITY", data[1].toString())
                        startActivity(intent)
                    }.start()
                }
            }
        )
    }

    override fun onDestroy() {
        client?.closeSocket()
        super.onDestroy()
    }
}