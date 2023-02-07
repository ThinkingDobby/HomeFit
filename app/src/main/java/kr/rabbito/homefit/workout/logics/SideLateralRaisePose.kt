package kr.rabbito.homefit.workout.logics

import android.content.Context
import android.util.Log
import kotlin.math.abs
import com.google.mlkit.vision.pose.Pose
import kr.rabbito.homefit.workout.WorkoutState
import kr.rabbito.homefit.workout.poseDetection.PoseGraphic
import kr.rabbito.homefit.workout.poseDetection.PoseGraphic.Companion.redPaint
import kr.rabbito.homefit.workout.poseDetection.PoseGraphic.Companion.whitePaint
import kotlin.math.absoluteValue

class SideLateralRaisePose: WorkoutPose() {
    lateinit var pose: Pose
    lateinit var context: Context

    override fun calculate(pose: Pose) {
        val coordinate = PoseCoordinate(pose)

        // 자세 검사
        guidePose(coordinate)

        // 횟수 검사
        checkCount(coordinate)
    }

    private fun guidePose(c: PoseCoordinate) {
        try {   //팔을 굽히면 빨간색 표시
            if (getAngle(c.rightHand, c.rightElbow, c.rightShoulder) < 160) {
                PoseGraphic.rightShoulderToRightElbowPaint = redPaint
                PoseGraphic.rightElbowToRightWristPaint = redPaint
            } else {
                PoseGraphic.rightShoulderToRightElbowPaint = whitePaint
                PoseGraphic.rightElbowToRightWristPaint = whitePaint
            }

            if (getAngle(c.leftHand, c.leftElbow, c.leftShoulder) < 160) {
                PoseGraphic.leftShoulderToLeftElbowPaint = redPaint
                PoseGraphic.leftElbowToLeftWristPaint = redPaint
            } else {
                PoseGraphic.leftShoulderToLeftElbowPaint = whitePaint
                PoseGraphic.leftElbowToLeftWristPaint = whitePaint
            }

            if (WorkoutState.isUp) { // 올라가는 시점
                if (
                    getAngle(c.rightElbow, c.rightShoulder, c.leftShoulder) < 140
                ) {   // 팔을 너무 높게 들면 안내선 빨갛게
                    PoseGraphic.rightShoulderToRightElbowPaint = redPaint
                    PoseGraphic.rightElbowToRightWristPaint = redPaint
                } else {
                    PoseGraphic.rightShoulderToRightElbowPaint = whitePaint
                    PoseGraphic.rightElbowToRightWristPaint = whitePaint
                }

                if (
                    getAngle(c.leftElbow, c.leftShoulder, c.rightShoulder) < 140
                ) {
                    PoseGraphic.leftShoulderToLeftElbowPaint = redPaint
                    PoseGraphic.leftElbowToLeftWristPaint = redPaint
                } else {
                    PoseGraphic.leftShoulderToLeftElbowPaint = whitePaint
                    PoseGraphic.leftElbowToLeftWristPaint = whitePaint
                }
            }
        } catch (_: NullPointerException) {
            // 추후 로그 작성
        }
    }

    private fun checkCount(c: PoseCoordinate) {
        try {
            Log.d("side lateral raise","L:${getXDistance(c.leftShoulder, c.leftHand).absoluteValue.toInt()}, R:${getXDistance(c.rightShoulder, c.rightHand).absoluteValue.toInt()}")
            if (
                (getAngle(c.leftElbow, c.leftShoulder, c.leftHip) > 100)
                && (getAngle(c.rightElbow, c.rightShoulder,c. rightHip) > 100)
                && getXDistance(c.rightShoulder, c.rightHand).absoluteValue > 90  // 한 번에 여러번 검사되는 것 방지, 정확도 향상
                && getXDistance(c.leftShoulder, c.leftHand).absoluteValue > 90
                && !WorkoutState.isUp
            ) {
                Log.d("side lateral raise","up")
                WorkoutState.count += 1
                WorkoutState.isUp = true

                checkSetCondition()
                checkEnd()
            } else if (
                (getAngle(c.leftElbow, c.leftShoulder, c.leftHip) <= 30)
                && (getAngle(c.rightElbow, c.rightShoulder,c. rightHip) <= 30)
                && getXDistance(c.rightShoulder, c.rightHand).absoluteValue <= 40
                && getXDistance(c.leftShoulder, c.leftHand).absoluteValue <= 40
                && WorkoutState.isUp
            ) {
                Log.d("side lateral raise","down")
                WorkoutState.isUp = false
            }
        } catch (_: NullPointerException) {
        }
    }

    // 세트가 끝났는지 확인
    private fun checkSetCondition() {
        if (WorkoutState.count == WorkoutState.setCondition) {
            WorkoutState.count = 0
            WorkoutState.set += 1
        }
    }

    // 운동이 끝났는지 확인
    private fun checkEnd() {
        if (WorkoutState.set == WorkoutState.setTotal + 1) {
            // 운동 종료
            Log.d("side lateral raise pose", "운동 종료")
        }
    }
}

