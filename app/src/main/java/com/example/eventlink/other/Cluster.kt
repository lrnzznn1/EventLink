package com.example.eventlink.other

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

// Classe per rappresentare un elemento del cluster
class MyClusterItem(
    @JvmField val pos: LatLng, // Posizione dell'elemento
    @JvmField val title: String, // Titolo dell'elemento
    @JvmField val desc: String, // Descrizione dell'elemento
    val icon: BitmapDescriptor, // Icona dell'elemento
    val tag: String // Tag dell'elemento
) : ClusterItem {
    override fun getPosition(): LatLng {
        return pos
    }
    override fun getTitle(): String {
        return title
    }
    override fun getSnippet(): String {
        return desc
    }
}


// Classe per personalizzare il rendering dei cluster
class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context,map,clusterManager){
    // Prima del rendering dell'elemento del cluster
    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.icon(item.icon) // Imposta l'icona dell'elemento
    }

    // Dopo il rendering dell'elemento del cluster
    override fun onClusterItemRendered(clusterItem: MyClusterItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem.tag // Imposta il tag dell'elemento come tag del marker
    }

    // Determina se rendere il cluster come un cluster o come un singolo marker
    override fun shouldRenderAsCluster(cluster: Cluster<MyClusterItem>): Boolean {
        return cluster.size > 1
    }
}