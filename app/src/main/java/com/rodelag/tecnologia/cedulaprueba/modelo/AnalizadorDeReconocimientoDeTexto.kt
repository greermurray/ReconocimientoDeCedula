package com.rodelag.tecnologia.cedulaprueba.modelo

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AnalizadorDeReconocimientoDeTexto(private val textoDetectado: (String) -> Unit) : ImageAnalysis.Analyzer {

    companion object {
        const val TIEMPO_DE_ESPERA_DEL_ACELERADOR_MS = 1_000L
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val reconocedorDeTexto: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val imagenMultimedia: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
            val imagenDeEntrada: InputImage = InputImage.fromMediaImage(imagenMultimedia, imageProxy.imageInfo.rotationDegrees)

            suspendCoroutine { continuation ->
                reconocedorDeTexto.process(imagenDeEntrada)
                    .addOnSuccessListener { textoDeLaImagen: Text ->
                        for (bloqueDeTexto in textoDeLaImagen.textBlocks) {
                            for (lineaDeTexto in bloqueDeTexto.lines) {
                                for (elemento in lineaDeTexto.elements) {
                                    //INFO: Aquí se puede hacer para que solo se detecte el texto deseado, por ejemplo, que solo detecte el número de la cédula que sea igual a la de la cotización.
                                    if (elemento.text == "8-800-682") {
                                        textoDetectado(elemento.text)
                                    }
                                }
                            }
                        }
                    }
                    .addOnCompleteListener {
                        continuation.resume(Unit)
                    }
            }

            delay(TIEMPO_DE_ESPERA_DEL_ACELERADOR_MS)
        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }
}