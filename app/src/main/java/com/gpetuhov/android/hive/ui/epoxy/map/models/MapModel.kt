package com.gpetuhov.android.hive.ui.epoxy.map.models

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.ui.epoxy.base.KotlinHolder
import com.gpetuhov.android.hive.util.Constants

@EpoxyModelClass(layout = R.layout.user_offer_map_view)
abstract class MapModel : EpoxyModelWithHolder<MapHolder>() {

    @EpoxyAttribute lateinit var onClick: () -> Unit

    private var map: GoogleMap? = null

    // If we annotate location with @EpoxyAttribute,
    // then MapModel will be rebind on every location change.
    private var location = Constants.Map.DEFAULT_LOCATION

    // This is called, when the model is bind to view.
    // Here we create map and init it with current location.
    override fun bind(holder: MapHolder) {
        // We need to call this for the map to show up
        holder.mapView.onCreate(null)

        holder.mapView.getMapAsync { googleMap ->
            map = googleMap
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.setOnMapClickListener { onClick() }
            updateMap(location)
        }
    }

    // This is needed to prevent rebinding MapModel on every location change
    // (and so map recreation).
    // If the map is null (model is not bind yet), just update location,
    // otherwise update map.
    fun updateMap(location: LatLng) {
        this.location = location

        if (map != null) {
            val zoom = Constants.Map.getZoomForLocation(location)
            val cameraPosition = CameraPosition.Builder().target(location).zoom(zoom).build()
            map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            map?.clear()
            map?.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("")
            )
        }
    }
}

class MapHolder : KotlinHolder() {
    val mapView by bind<MapView>(R.id.user_offer_map_view)
}