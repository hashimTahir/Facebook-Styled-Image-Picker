
[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-Facebook--Styled--Image--Picker-green.svg?style=flat )]( https://android-arsenal.com/details/1/8383 )

# Facebook-Styled-File-Picker

- Facebook Styled Gallery Files picker.
- One or multiple files can be selected.
- Keeps track of selected files count exactly like Facebook.
- Selected folders are also shown.
- Media Store api is used to fetch the image files. So its backward compatible and only read permission is required.
- Both image Uris and ImagePaths can be retrieved..

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.hashimTahir:Facebook-Styled-Image-Picker:1.1'
	}

Step 3. Use the launcher to start the image picker with GalleryActivity like so:

      hGalleryActivityLauncher.launch(
                    Intent(
                        requireContext(),
                        GalleryActivity::class.java
                    ).also {
                        it.putExtra(Constants.H_GET_VIDEOS, "")
                    }
                )

Use the "H_GET_VIDEOS" and "H_GET_IMAGES" to get videos and images from the device respectively.

Step 4. Recieve the result from launcher in IntentHolder class as:

	  private val hGalleryActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val hIntentHolder1 = result.data?.extras?.getParcelable<IntentHolder>(H_GET_VIDEOS)
            val hIntentHolder2 = result.data?.extras?.getParcelable<IntentHolder>(H_GET_IMAGES)

            val hBundle = Bundle()
            if (hIntentHolder1 != null) {
                hBundle.putParcelable(H_DATA_IC, hIntentHolder1)
            }
            if (hIntentHolder2 != null) {
                hBundle.putParcelable(H_DATA_IC, hIntentHolder2)
            }

            findNavController().navigate(R.id.action_hHomeFragment_to_hDisplayFragment, hBundle)
        }
    }

where the IntentHolder data class holds list of selected Images/Videos details.

      @Parcelize
    data class IntentHolder(
    var hVideosList: List<Folder.VideoItem>? = null,
    var hImageList: List<Folder.ImageItem>? = null  
        ) : Parcelable


VideoItem data class holds these details for each video.

      @Parcelize
    data class VideoItem(
        var hFilePath: String? = null,
        val hUri: String? = null,
        var hFileName: String? = null,
        var hFileSize: String? = null,
        var hFileDuaration: String? = null,
        var hModifiedDate: Long? = null,
        var hFileSizeForOrder: String? = null,
        var hFileDateTime: String? = null
    ) : Parcelable


ImageItem data class holds these details for each video.

       @Parcelize
    data class ImageItem(
        val hItemName: String? = null,
        val hSize: String? = null,
        var hImagePath: String? = null,
        var hImageUri: String? = null,
    ) : Parcelable








