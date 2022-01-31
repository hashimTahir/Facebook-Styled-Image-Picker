# Facebook-Styled-Image-Picker

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
	        implementation 'com.github.hashimTahir:Facebook-Styled-Image-Picker:v1.0'
	}

Step 3. Use the launcher to start the image picker with GalleryActivity like so:

      hGalleryActivityLauncher.launch(
                Intent(
                    this,
                    GalleryActivity::class.java
                )
            )

	  private val hGalleryActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val hRecieviedImagesList =
                result.data?.extras?.getParcelableArrayList<IntentHolder>("hImageList")
            hDisplayAdapter.hSetData(hRecieviedImagesList)
        }

    }

Use the Constants.H_IMAGE_LIST_IC to retrieve the data from picker. Which returns it As a list of IntentHolder data
class, which contains both uri and real path.

      @Parcelize
      data class IntentHolder(
      val hImagePath: String? = null,
      val hImageUri: String? = null
      ) : Parcelable








