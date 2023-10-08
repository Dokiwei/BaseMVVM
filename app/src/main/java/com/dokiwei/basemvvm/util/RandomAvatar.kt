package com.dokiwei.basemvvm.util

import com.dokiwei.basemvvm.R

/**
 * @author DokiWei
 * @date 2023/8/20 21:00
 */
fun randomAvatar(): Int {
    val imageIds = intArrayOf(
        R.drawable.img_user,
        R.drawable.img_user1,
        R.drawable.img_user2,
        R.drawable.img_user3,
        R.drawable.img_user4,
        R.drawable.img_user5,
        R.drawable.img_user6,
        R.drawable.img_user7,
        R.drawable.img_user8
    )

    return imageIds[(0..8).random()]
}