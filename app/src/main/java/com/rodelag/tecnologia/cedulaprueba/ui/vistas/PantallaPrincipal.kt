package com.rodelag.tecnologia.cedulaprueba.ui.vistas

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PantallaPrincipal() {

    val permisoCamara: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    ContenidoPrincipal(
        tienePermiso = permisoCamara.status.isGranted,
        solicitarPermiso = permisoCamara::launchPermissionRequest
    )
}

@Composable
private fun ContenidoPrincipal(
    tienePermiso: Boolean,
    solicitarPermiso: () -> Unit
) {

    if (tienePermiso) {
        PantallaCamara()
    } else {
        PantallaSinPermiso(solicitarPermiso)
    }
}