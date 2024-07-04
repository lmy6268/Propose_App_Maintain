package com.hanadulset.pro_poseapp.utils.common

import net.lingala.zip4j.ZipFile
import java.io.File

fun unZip(srcZip: File, dstPath: String) {
    ZipFile(srcZip).use {
        it.extractAll(dstPath)
    }
}