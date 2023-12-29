package com.rodelag.tecnologia.cedulaprueba.ui.vistas

import android.content.Context
import android.graphics.Color
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
import java.io.File

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
    var textoDetectado: String by remember { mutableStateOf("No se ha detectado la cédula") }
    fun textoActual(texto: String) {
        textoDetectado = texto
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
                            textoDetectado = ::textoActual
                        )
                    }
                }
            )

            //INFO: Si se detecta una cédula usando el patrón, se muestra el texto detectado y se detiene la cámara.
            if (textoDetectado.esUnaCedula() && !fotoTomada) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White)
                        .padding(24.dp),
                    color = androidx.compose.ui.graphics.Color.Black,
                    text = "Cédula detectada: $textoDetectado",
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
    textoDetectado: (String) -> Unit
) {

    controladorDeLaCamara.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    controladorDeLaCamara.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(contexto),
        AnalizadorDeReconocimientoDeTexto(
            cedulaDeLaCotizacion = cedulaDeLaCotizacion,
            cedulaDetectada = textoDetectado
        )
    )

    controladorDeLaCamara.bindToLifecycle(cicloDeVida)
    vistaPrevia.controller = controladorDeLaCamara
}

private fun tomarFoto(contextoLocal: Context, controladorDeLaCamara: LifecycleCameraController, onFotoTomada: () -> Unit) {

    //INFO: Se crea el archivo donde se guardará la foto, Poner como nombre el ID de la Cotización, para poder asociar la foto con la cotización.
    val fotoOutputFile = File(contextoLocal.externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

    //INFO: Se tomará la foto y se guardará en el directorio de la aplicación, en la carpeta Pictures, acá por ejemplo podemos procesarla para enviar la foto a un servidor por medio de la API.
    controladorDeLaCamara.takePicture(
        ImageCapture.OutputFileOptions.Builder(fotoOutputFile).build(),
        ContextCompat.getMainExecutor(contextoLocal),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onFotoTomada()
            }

            override fun onError(exception: ImageCaptureException) {
                //INFO: Manejar el error...
            }
        }
    )
}