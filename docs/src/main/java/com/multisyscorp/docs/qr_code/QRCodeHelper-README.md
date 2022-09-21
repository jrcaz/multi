# QR Code Helper

Nowadays we need to scan many barcodes and QR codes for scanning purposes, we need a barcode scanner or QR code.

## Requirements to test in your local machine

First of all, we need to add camerax, barcode scanner, and accompanist permissions dependency in the build.Gradle(:app) file.

```
// Add CameraX Preview  
implementation "androidx.camera:camera-camera2:1.1.0-beta02"  
implementation "androidx.camera:camera-lifecycle:1.1.0-beta02"  
implementation "androidx.camera:camera-view:1.1.0-beta02"  
  
// Add ZXing (Barcode Scanner)
implementation "com.google.zxing:core:3.3.3"  

// Add Accompanist Permissions (Compose)
implementation "com.google.accompanist:accompanist-permissions:0.25.0"
```

Now we need to add camera permission in the manifest file. We will add a user feature in the manifest file for accessing the camera hardware.

```
<uses-permission android:name="android.permission.CAMERA" />  

<uses-feature 
	android:name="android.hardware.camera"  
	android:required="true" 
/>
```

Create a class `QRCodeAnalyzer` to get the result of QR Image raw value.

```
class QRCodeAnalyzer(
	private val supportedImageFormats: List<Int>,
	private val onQRCodeScanned: (String?) -> Unit,
	private val onException: () -> Unit  
) : ImageAnalysis.Analyzer {  
  
    override fun analyze(image: ImageProxy) {  
        when (image.format) {  
            in supportedImageFormats -> {  
                val bytes = image.planes.firstOrNull()?.buffer?.toByteArray()  
                val source = PlanarYUVLuminanceSource(
	                bytes,  
					image.width,  
					image.height,
					0,  
					0,  
					image.width,  
					image.height,  
					false  
				)  
                val binaryBmp = BinaryBitmap(HybridBinarizer(source))  
                try {  
                    val result = MultiFormatReader().apply {  
	                    setHints(  
                            mapOf(  
                                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
	                                BarcodeFormat.QR_CODE  
                                )  
                            )  
                        )  
                    }.decode(binaryBmp)  
                    onQRCodeScanned(result.text)  
                } catch (_: Exception) {  
                    onException()  
                } finally {  
                    image.close()  
                }  
            }  
        }  
    }  
}
```
### Activity Result

The [`rememberLauncherForActivityResult()`](https://developer.android.com/reference/kotlin/androidx/activity/compose/package-summary#rememberlauncherforactivityresult) API allows you to [get a result from an activity](https://developer.android.com/training/basics/intents/result) in your composable:

```
val selectImageFromGalleryResult = rememberLauncherForActivityResult(  
    contract = ActivityResultContracts.GetContent(),  
    onResult = { uri: Uri? ->  
		uri?.let { fileUri ->  
			val inputStream = context.contentResolver.openInputStream(fileUri)  
			val bitmap = BitmapFactory.decodeStream(inputStream)  
			QRCodeHelper.decodeBitmap(  
				context = context,  
				bitmap = bitmap,  
				onResult = { result ->  
					qrCodeRawValue = result ?: ""  
				},  
				onError = {  
					Log.d(TAG, "Detector Error")  
	            },  
				onNotFound = {  
					Log.d(TAG, "Not Found")  
	            },  
	            onException = {  
		            Log.d(TAG, "Exception")  
		        }
			)  
		}  
	}
)
 ```

This example demonstrates a simple  [`GetContent()`](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.GetContent)  contract. Tapping the button launches the request. The trailing lambda for  [`rememberLauncherForActivityResult()`](https://developer.android.com/reference/kotlin/androidx/activity/compose/package-summary#rememberlauncherforactivityresult)  is invoked once the user selects an image and returns to the launching activity. This loads the selected image using Coil’s  `rememberImagePainter()`  function.

Any subclass of  [`ActivityResultContract`](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContract)  can be used as the first argument to  [`rememberLauncherForActivityResult()`](https://developer.android.com/reference/kotlin/androidx/activity/compose/package-summary#rememberlauncherforactivityresult). This means that you can use this technique to request content from the framework and in other common patterns. You can also create your own  [custom contracts](https://developer.android.com/training/basics/intents/result#custom)  and use them with this technique.

### Requesting Runtime Permissions

The [Accompanist Permissions library](https://google.github.io/accompanist/permissions/) can also be used a layer above those APIs to map the current granted state for permissions into State that your Compose UI can use.

```
val cameraPermissionState = rememberPermissionState(  
	android.Manifest.permission.CAMERA  
)

when (cameraPermissionState.status) {  
	// If the camera permission is granted, then show screen with the feature enabled
	PermissionStatus.Granted -> {  
		// Camera permission Granted
		// hasCameraPermission = true
	}
	PermissionStatus.Denied -> {
		// hasCameraPermission = false  
		Column(  
		    modifier = Modifier.fillMaxSize()  
		) {  
			val textToShow = when {
				cameraPermissionState.status.shouldShowRationale -> {
					// If the user has denied the permission but the rationale can be shown,  
					// then gently explain why the app requires this permission  
					"The camera is important for this app. Please grant the permission."  
				}
				else -> {
					// If it's the first time the user lands on this feature, or the user  
					// doesn't want to be asked again for this permission, explain that the 
					// permission is required  
					"Camera permission required for this feature to be available."Please grant the permission"  
				}
			}
			Text(textToShow)
			Button(
				onClick = {
					cameraPermissionState.launchPermissionRequest()
				}
			) {  
				Text("Request permission") 
			}  
		}
	}
}
```

### Implementation of UI - QR Scanner Composable

```
val context = LocalContext.current  
val lifecycleOwner = LocalLifecycleOwner.current  
val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }  
var qrCodeRawValue by remember { mutableStateOf("") }

Column(  
	modifier = Modifier  
		.background(color = WhiteBackground)  
        .fillMaxSize(),  
	horizontalAlignment = Alignment.CenterHorizontally,  
	verticalArrangement = Arrangement.Center  
) {  
	Button(  
		onClick = {  
			when {
				!hasCameraPermission -> cameraPermissionState.launchPermissionRequest()
			}
		}  
  ) {  
		Text(text = "Scan QR")  
    }  
    Spacer(modifier = Modifier.height(height = 24.dp))  
    Button(  
		onClick = {  
			selectImageFromGalleryResult.launch("image/*")  
        }  
	) {  
		Text(text = "Select QR From Gallery/Photos")  
    }  
	Spacer(modifier = Modifier.height(height = 24.dp))  
	Text(  
        text = "Result:\n$qrCodeRawValue",  
        modifier = Modifier  
			.padding(  
				start = 32.dp,  
				end = 32.dp  
			),  
		textAlign = TextAlign.Center  
	)  
}

Column(  
    modifier = Modifier.fillMaxSize()  
) {  
	when {
		hasCameraPermission -> {
			AndroidView(  
				factory = { context ->  
					val previewView = PreviewView(context)  
	                val preview = androidx.camera.core.Preview.Builder().build()  
					val selector = CameraSelector.Builder()  
	                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)  
	                    .build()  
	                preview.setSurfaceProvider(previewView.surfaceProvider)  
	                val imageAnalysis = ImageAnalysis.Builder()  
	                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)  
	                    .build()  
	                imageAnalysis.setAnalyzer(  
	                    ContextCompat.getMainExecutor(context),  
						QRCodeAnalyzer(  
							supportedImageFormats = listOf(  
								ImageFormat.YUV_420_888,  
								ImageFormat.YUV_422_888,  
								ImageFormat.YUV_444_888,  
							),  
							onQRCodeScanned = { result ->  
								qrCodeRawValue = result ?: ""  
							},  
							onException = {  
								Log.d(TAG, "Exception")  
	                        }  
						)  
					)  
	                try {  
	                    cameraProviderFuture.get().bindToLifecycle(  
	                        lifecycleOwner,  
							selector,  
							preview,  
							imageAnalysis  
	                    )  
	                } catch (_: Exception) {  
	                    Log.d(TAG, "Exception")  
	                }  
					return@AndroidView previewView  
				},
				modifier = Modifier.weight(weight = 1f)  
			)  
		}
	}
}
```

## Test Result

![Record_2022-09-16-15-56-16](https://user-images.githubusercontent.com/52396271/190592353-9216943d-355d-4a1a-bb29-56a1d39e12f5.gif)