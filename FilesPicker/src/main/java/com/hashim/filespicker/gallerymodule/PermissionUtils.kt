package com.hashim.filespicker.gallerymodule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hashim.filespicker.R

class PermissionUtils {
    companion object {
        fun hHasReadPermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun hRationaileCheck(context: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        fun hShowSettingsDialog(
            galleryActivity: Activity,
            hLaunchSettingsContract: ActivityResultLauncher<Intent>,
        ) {
            AlertDialog.Builder(galleryActivity)
                .setTitle(galleryActivity.getString(R.string.permission_needed))
                .setMessage(galleryActivity.getString(R.string.read_storage_permission))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    hLaunchSettingsContract.launch(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).also { hIntent ->
                            hIntent.data = Uri.fromParts(
                                "package",
                                galleryActivity.packageName,
                                null
                            )
                        }
                    )
                }
                .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    galleryActivity.finish()
                }.create().show()
        }

    }


}