package com.rodelag.tecnologia.cedulaprueba.ui.vistas

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.rodelag.tecnologia.cedulaprueba.modelo.AnalizadorDeReconocimientoDeTexto
import com.rodelag.tecnologia.cedulaprueba.comun.esUnaCedula
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.File
import java.io.InputStream

@Composable
fun PantallaCamara() {
    ContenidoCamara()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoCamara() {

    val contextoLocal: Context = LocalContext.current
    val cicloDeVidaLocal: LifecycleOwner = LocalLifecycleOwner.current
    val controladorDeLaCamara: LifecycleCameraController = remember { LifecycleCameraController(contextoLocal) }
    var cedulaDetectada: String by remember { mutableStateOf("No se ha detectado la cédula") }

    fun cedulaActual(cedula: String) {
        cedulaDetectada = cedula
    }

    var fotoTomada: Boolean by remember { mutableStateOf(false) }

    //INFO: Aquí se puede cambiar el número de la cédula que se desea detectar.
    val cedulaDeLaCotizacion: String by remember { mutableStateOf("8-800-682") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Rodelag Prueba") }) },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        //INFO: Inicia el reconocimiento de texto.
                        iniciarReconocimientoDeTexto(
                            cedulaDeLaCotizacion = cedulaDeLaCotizacion,
                            contexto = context,
                            controladorDeLaCamara = controladorDeLaCamara,
                            cicloDeVida = cicloDeVidaLocal,
                            vistaPrevia = previewView,
                            cedulaDetectada = ::cedulaActual
                        )
                    }
                }
            )

            //INFO: Si se detecta una cédula usando el patrón, se muestra el texto detectado y se detiene la cámara.
            if (cedulaDetectada.esUnaCedula() && !fotoTomada) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White)
                        .padding(24.dp),
                    color = androidx.compose.ui.graphics.Color.Black,
                    text = "Cédula detectada: $cedulaDetectada",
                )

                //INFO: Se toma la foto.
                tomarFoto(contextoLocal, controladorDeLaCamara) {
                    fotoTomada = true
                    controladorDeLaCamara.unbind()
                }
            }
        }
    }
}

private fun iniciarReconocimientoDeTexto(
    cedulaDeLaCotizacion: String,
    contexto: Context,
    controladorDeLaCamara: LifecycleCameraController,
    cicloDeVida: LifecycleOwner,
    vistaPrevia: PreviewView,
    cedulaDetectada: (String) -> Unit
) {

    controladorDeLaCamara.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    controladorDeLaCamara.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(contexto),
        AnalizadorDeReconocimientoDeTexto(
            cedulaDeLaCotizacion = cedulaDeLaCotizacion,
            cedulaDetectada = cedulaDetectada
        )
    )

    controladorDeLaCamara.bindToLifecycle(cicloDeVida)
    vistaPrevia.controller = controladorDeLaCamara
}

@OptIn(DelicateCoroutinesApi::class)
private fun tomarFoto(contextoLocal: Context, controladorDeLaCamara: LifecycleCameraController, onFotoTomada: () -> Unit) {

    //INFO: Se crea el archivo donde se guardará la foto, Poner como nombre el ID de la Cotización, para poder asociar la foto con la cotización.
    val fotoOutputFile = File(contextoLocal.externalMediaDirs.first(), "${System.currentTimeMillis()}.png")

    //INFO: Se tomará la foto y se guardará en el directorio de la aplicación, en la carpeta Pictures, acá por ejemplo podemos procesarla para enviar la foto a un servidor por medio de la API.
    controladorDeLaCamara.takePicture(
        ImageCapture.OutputFileOptions.Builder(fotoOutputFile).build(),
        ContextCompat.getMainExecutor(contextoLocal),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                //INFO: Llamar a la función enviarImagen después de que la imagen se haya guardado con éxito
                val fotoUri = Uri.fromFile(fotoOutputFile)

                GlobalScope.launch(Dispatchers.IO) {
                    enviarImagen(contextoLocal, fotoUri)
                }

                onFotoTomada()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("RODELAG", "Error al tomar la foto: ${exception.message}")
                //INFO: Manejar el error...
            }
        }
    )
}

fun enviarImagen(contextoLocal: Context, fotoUri: Uri) {
    val client = OkHttpClient()

    //INFO: Obtener el InputStream del Uri
    val inputStream: InputStream? = contextoLocal.contentResolver.openInputStream(fotoUri)
    val bytes = inputStream?.readBytes()

    //INFO: Crear un RequestBody a partir de los bytes del archivo
    val requestBodyFile = bytes?.toRequestBody("image/png".toMediaTypeOrNull())

    //INFO: Obtener el nombre del archivo
    val nombreArchivo = File(fotoUri.path!!).name

    //INFO: Verificar si el archivo de la imagen existe
    if (!File(fotoUri.path!!).exists()) {
        return
    }

    //INFO: Crear un cuerpo de solicitud de varias partes
    val requestBody = requestBodyFile?.let {
        MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "imagenes[]",
            nombreArchivo,
            it
        )
        .addFormDataPart("productoElconix", "false")
        .addFormDataPart("carpetaAmazonS3", "cedulasfacturacion")
        .addFormDataPart("bucketAmazonS3", "rodelag-imagenes")
        .build()
    }

    //INFO: Crear una solicitud POST
    val request = requestBody?.let {
        Request.Builder()
        .url("https://dev.rodelag.com/amazonS3/")
        .post(it)
        .addHeader("Authorization", "Bearer TOKEN")
        .build()
    }

    //INFO: Realizar la solicitud
    if (request != null) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Error inesperado: $response")
            Log.e("RODELAG","${response.body?.string()}")
        }
    }
}