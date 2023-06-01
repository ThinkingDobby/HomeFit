package kr.rabbito.homefit.screens

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kr.rabbito.homefit.client.HomeFitClient
import kr.rabbito.homefit.databinding.ActivityDaddBinding
import kr.rabbito.homefit.databinding.ActivityDcameraBinding
import kr.rabbito.homefit.databinding.ActivityDimageBinding
import kr.rabbito.homefit.utils.calc.Converter
import kotlin.math.atan

class DImageActivity : AppCompatActivity() {
    private var mBinding: ActivityDimageBinding? = null
    private val binding get() = mBinding!!

    // 통신
    private var client: HomeFitClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDimageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        for (cameraId in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                // 후면 카메라
                val focalLength =
                    characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) // 렌즈 초점 거리
                val physicalSize =
                    characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!! // 카메라 센서의 물리적인 크기
                val pixelArraySize =
                    characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!! // 센서에서 사용되는 픽셀 배열의 크기
                val horizontalAngle = 2 * atan(physicalSize.width / (2 * focalLength!![0]))
                val verticalAngle =
                    2 * atan(physicalSize.height / (2 * focalLength[0])) * pixelArraySize.width / pixelArraySize.height
                val spoon_size = (296f * resources.displayMetrics.density + 0.5f).toInt()
                Log.e(
                    "camera",
                    "후면 카메라\nid : ${cameraId}\n렌즈 초점 거리 : ${focalLength[0]}\n센서 크기 : $physicalSize\n 카메라 픽셀 사이즈 : $pixelArraySize\n 수직 화각 : $verticalAngle\n 수평 화각 : $horizontalAngle\n 숟가락 사이즈 : $spoon_size"
                )
                Thread {
                    client = HomeFitClient()

                    try {
                        client!!.sendRequest()
                        client!!.sendUserName("User")
                    } catch (e: NullPointerException) {
                        Log.d("connection", "socket not initialized")
                    }

                    val imageUriString = intent.getStringExtra("SELECTED_IMAGE")
                    val imageUri = Uri.parse(imageUriString)

                    val bitmap = Converter.imageUriToBitmap(contentResolver, imageUri)
                    runOnUiThread {
                        binding.dimageIvBackground.setImageURI(imageUri)
                    }
                    val cameraInfo = "${focalLength[0]} ${physicalSize.width} ${pixelArraySize.width} ${pixelArraySize.height} $verticalAngle $horizontalAngle $spoon_size"
                    client!!.sendCameraInfo(cameraInfo)

                    client!!.sendImage(bitmap)
                    val data = client!!.getData()!!

                    val intent = Intent(this@DImageActivity, MainActivity::class.java)
                    intent.putExtra("VIEW_PAGER_INDEX", 1)
                    intent.putExtra("DATA", data)
                    startActivity(intent)
                }.start()
            }
        }

    }
    override fun onDestroy() {
        client?.closeSocket()
        super.onDestroy()
    }

}