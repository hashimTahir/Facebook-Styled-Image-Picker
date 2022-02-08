package com.hashim.filespicker.gallerymodule.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.hashim.filespicker.R
import com.hashim.filespicker.databinding.ActivityGalleryBinding
import com.hashim.filespicker.gallerymodule.FileType
import com.hashim.filespicker.gallerymodule.GalleryStateView.*
import com.hashim.filespicker.gallerymodule.GalleryViewModel
import com.hashim.filespicker.gallerymodule.GalleryVs.*
import com.hashim.filespicker.gallerymodule.PermissionUtils
import com.hashim.filespicker.gallerymodule.data.IntentHolder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(), View.OnClickListener {
    private var hActivityGalleryBinding: ActivityGalleryBinding? = null
    private var hNavHostFragment: NavHostFragment? = null
    private var hNavController: NavController? = null
    private val hGalleryViewModel by viewModels<GalleryViewModel>()


    private val hLaunchSettingsContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (PermissionUtils.hHasReadPermissions(this)) {
            hGalleryViewModel.hFetchFiles(intent.extras)
        } else {
            Toast.makeText(
                this,
                getString(R.string.please_allow_read_permission),
                Toast.LENGTH_SHORT
            ).show()
        }


    }
    private val hTakePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { hIsSuccessFull ->
        hGalleryViewModel.hOnActivityEvents(OnPictureTaken(hIsSuccessFull))
    }

    private val hRequestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hIsPermissionGranted ->
        if (hIsPermissionGranted) {
            hGalleryViewModel.hFetchFiles(intent.extras)
        } else {
            if (PermissionUtils.hRationaileCheck(this).not()) {
                PermissionUtils.hShowSettingsDialog(this, hLaunchSettingsContract)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_allow_read_permission),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityGalleryBinding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(hActivityGalleryBinding!!.root)

        hInitNavView()

        hSetupClickListeners()


        when {
            PermissionUtils.hHasReadPermissions(this) -> {
                hGalleryViewModel.hFetchFiles(intent.extras)
            }
            else -> {
                hRequestPermissions.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }

        hSubscribeObservers()

    }

    private fun hSubscribeObservers() {
        lifecycleScope.launch {
            hGalleryViewModel.hGalleryVsSF.collectLatest { galleryVs ->
                when (galleryVs) {
                    is OnFilesRetrieved -> hUpdateFolderName(galleryVs.hFolderName)
                    is OnViewSetup -> hSetupViews(galleryVs)
                    is OnSelectionDone -> hReturnDataAndFinish(galleryVs.hIntentHolder)
                    is OnUpdateActivity -> hUpdateButtons(galleryVs.hShowNextB)
                    is OnLaunchCamera -> hTakePictureLauncher.launch(galleryVs.hPhotoUri)
                    is OnLoadingOrError -> hShowLoadingOrMessager(galleryVs)
                    else -> Unit
                }
            }
        }
    }

    private fun hShowLoadingOrMessager(galleryVs: OnLoadingOrError) {
        hActivityGalleryBinding?.apply {
            when (galleryVs.hShowLoader) {
                true -> hProgressbar.visibility = View.VISIBLE
                false -> hProgressbar.visibility = View.GONE
            }
        }

        galleryVs.hMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hUpdateButtons(hShowNextB: Boolean) {
        when (hShowNextB) {
            true -> {
                hActivityGalleryBinding?.apply {
                    hNextB.visibility = View.VISIBLE
                    hCamIv.visibility = View.GONE
                }
            }
            else -> {
                hActivityGalleryBinding?.apply {
                    hNextB.visibility = View.GONE
                    hCamIv.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun hReturnDataAndFinish(
        hIntentHolder: IntentHolder
    ) {
        Intent(
            this,
            callingActivity?.className?.javaClass
        ).also {
            when {
                hIntentHolder.hVideosList != null -> {
                    it.putExtra(
                        FileType.Videos.toString(),
                        hIntentHolder
                    )
                }
                hIntentHolder.hImageList != null -> {
                    it.putExtra(
                        FileType.Images.toString(),
                        hIntentHolder
                    )
                }
                hIntentHolder.hAudioList != null -> {
                    it.putExtra(
                        FileType.Audios.toString(),
                        hIntentHolder
                    )
                }
            }

            setResult(RESULT_OK, it)
            finish()
        }
    }

    private fun hSetupViews(onViewSetup: OnViewSetup) {
        hActivityGalleryBinding?.apply {
            hSelectMultipleCL.apply {
                isSelected = onViewSetup.hIsMultipleSelected
            }
            onViewSetup.hToobarTitle?.let {
                hToolbarTitleTv.text = it
            }
            when (onViewSetup.hIsMultipleSelected) {
                true -> {
                    hSelectMultipleTv.setTextColor(
                        ContextCompat.getColor(
                            this@GalleryActivity,
                            R.color.colorDarkBlue
                        )
                    )

                    DrawableCompat.setTint(
                        hSelectMultipleIv.drawable!!,
                        ContextCompat.getColor(
                            this@GalleryActivity,
                            R.color.colorDarkBlue
                        )
                    )
                }
                false -> {
                    hSelectMultipleTv.setTextColor(
                        ContextCompat.getColor(
                            this@GalleryActivity,
                            R.color.black
                        )
                    )
                    DrawableCompat.setTint(
                        hSelectMultipleIv.drawable!!,
                        ContextCompat.getColor(
                            this@GalleryActivity,
                            R.color.black
                        )
                    )
                }
            }
        }

    }

    private fun hUpdateFolderName(hFolderName: String?) {
        hFolderName?.let { folderName ->
            hActivityGalleryBinding?.hFolderTv?.text = folderName

        }
    }

    private fun hSetupClickListeners() {
        hActivityGalleryBinding?.apply {
            hFolderCL.setOnClickListener(this@GalleryActivity)
            hCamIv.setOnClickListener(this@GalleryActivity)
            hSelectMultipleCL.setOnClickListener(this@GalleryActivity)
            hNextB.setOnClickListener(this@GalleryActivity)
            hCloseIv.setOnClickListener(this@GalleryActivity)
        }
    }


    private fun hInitNavView() {
        hNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.hGalleryFragmentContainer)
                as NavHostFragment


        hNavController = hNavHostFragment?.navController
        hNavController?.setGraph(R.navigation.gallery_nav)
    }


    override fun onClick(view: View?) {
        hActivityGalleryBinding?.apply {
            when (view?.id) {
                hFolderCL.id -> {
                    if (hNavController?.currentDestination?.id == R.id.hFolderFragment) {
                        hNavController?.popBackStack()
                    } else {
                        hNavController?.navigate(R.id.action_hGalleryMainFragment_to_folderFragment)
                    }
                    hGalleryViewModel.hOnActivityEvents(OnReloadFiles)
                }
                hCamIv.id -> hGalleryViewModel.hOnActivityEvents(OnCameraSelected)
                hSelectMultipleCL.id -> hGalleryViewModel.hOnActivityEvents(OnMultiSelection)
                hNextB.id -> hGalleryViewModel.hOnActivityEvents(OnNextClicked)
                hCloseIv.id -> finish()
            }
        }
    }

    override fun onBackPressed() {
        if (hNavController?.currentDestination?.id == R.id.hFolderFragment) {
            hNavController?.navigate(R.id.action_hFolderFragment_to_hGalleryMainFragment)
            hGalleryViewModel.hOnActivityEvents(OnReloadFiles)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hActivityGalleryBinding = null
    }
}
