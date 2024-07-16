package com.venturessoft.human.main.data.models

data class FaceDetectionResponce(
    val data:FaceDetection = FaceDetection(),
    val status:String = ""
)

data class FaceDetection(
    val angles:Angles=Angles(),
    val box:Box = Box(),
    val result:String = "",
    val score:Double = 0.0
)

data class Angles(
    val pitch:Double= 0.0,
    val roll:Double= 0.0,
    val yaw:Double= 0.0
)

data class Box(
    val h:Long = 0,
    val w:Long= 0,
    val x:Long= 0,
    val y:Long= 0
)