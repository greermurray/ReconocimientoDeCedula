package com.rodelag.tecnologia.cedulaprueba.ui.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PantallaSinPermiso(
    solicitarPermiso: () -> Unit
) {

    ContenidoSinPermiso(
        solicitarPermiso = solicitarPermiso
    )
}

@Composable
fun ContenidoSinPermiso(
    solicitarPermiso: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Otorgue permiso para usar la cámara para utilizar la funcionalidad principal de esta aplicación."
            )
            Button(onClick = solicitarPermiso) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Camara")
                Text(text = "Conceder permiso")
            }
        }
    }
}