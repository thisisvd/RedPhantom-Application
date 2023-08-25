package com.example.redphantomapplication.constant

import com.example.redphantomapplication.R

object Constants {

    // data preferences
    const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_USERS_DATA"
    const val REMEMBER_ME = "REMEMBER_ME"

    // captcha init
    val captchaList = arrayListOf(
        R.drawable.captcha_1.toString(),
        R.drawable.captcha_2.toString(),
        R.drawable.captcha_3.toString(),
        R.drawable.captcha_4.toString(),
        R.drawable.captcha_5.toString(),
    )

    val captchaMap = mapOf(
        captchaList[0] to "mbf58",
        captchaList[1] to "8nbew",
        captchaList[2] to "c3n8x",
        captchaList[3] to "fcey3",
        captchaList[4] to "2cegf",
    )
}