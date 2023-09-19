package kr.rabbito.homefit.workout

import android.content.Context
import kr.rabbito.homefit.R

class WorkoutData(val context: Context){
    companion object {
        // 운동 이름
        val workoutNamesENG = arrayListOf("Push Up", "Pull Up", "Squat", "Side Lateral Raise", "Dumbbell Curl", "Leg Raise")
        val workoutNamesKOR = arrayListOf("팔굽혀펴기", "턱걸이", "스쿼트", "사이드 레터럴 레이즈", "덤벨 컬", "레그 레이즈")

        // 운동 사진
        val workoutImages =  arrayListOf(R.drawable.wodetail_iv_push_up, R.drawable.wodetail_iv_pull_up, R.drawable.wodetail_iv_squat,
            R.drawable.wodetail_iv_side_lateral_raise, R.drawable.wodetail_iv_dumbbell_curl, R.drawable.wodetail_iv_leg_raise)

        // 운동 설명
        val workoutExplain = arrayListOf(
            "엎드린 상태에서 팔을 굽혔다 펴는 가슴 운동입니다.\n" +
                    "양손을 어깨너비로 벌려 땅을 짚고 팔 이외의 몸은 일직선으로 유지합니다.",
            "철봉을 손으로 당겨 턱을 철봉 위까지 끌어 올리는 등 운동입니다.\n" +
                    "손 등이 보이는 방향으로, 양손을 어깨너비보다 조금 넓게 철봉을 잡습니다.",
            "일어선 상태에서 앉았다 일어나는 하체 운동입니다.\n" +
                    "양발을 어깨보다 조금 넓게 벌리고 양 발끝과 무릎을 바깥쪽으로 향하도록 합니다.",
            "양손에 덤벨을 들고 팔 전체를 양쪽으로 올렸다 내리는 어깨 운동입니다.\n" +
                    "손을 어깨높이까지만 올려야 하며 승모근을 최대한 사용하지 않도록 합니다.",
            "양손에 덤벨을 들고 팔을 앞으로 굽혔다 펴는 팔 운동입니다.\n" +
                    "팔을 허리 앞쪽에 고정하고 손바닥이 전면을 향하게 합니다. ",
            "누운 상태에서 다리를 올렸다 내리는 복근 운동입니다.\n" +
                    "다리를 상체 가까이 올렸다 내립니다.\n" +
                    "내릴 때 다리가 바닥에 닿지 않도록 합니다."
        )
    }
}