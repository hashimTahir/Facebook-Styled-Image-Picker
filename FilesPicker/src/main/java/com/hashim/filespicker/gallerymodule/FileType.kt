package com.hashim.filespicker.gallerymodule

sealed class FileType {
    object Images : FileType() {
        override fun toString(): String {
            return "FileType:Images"
        }
    }

    object Videos : FileType() {
        override fun toString(): String {
            return "FileType:Videos"
        }
    }

    object Audios : FileType() {
        override fun toString(): String {
            return "FileType:Audios"
        }
    }
}
