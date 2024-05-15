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

// Custom cluster item class implementing ClusterItem interface
class MyClusterItem(
    @JvmField val pos: LatLng,
    @JvmField val title: String,
    @JvmField val desc: String,
    val icon: BitmapDescriptor,
    val tag: String
) : ClusterItem {
    // Override methods from ClusterItem interface
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


// Custom cluster renderer extending DefaultClusterRenderer
class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context,map,clusterManager){
    // Override method to customize cluster item rendering
    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        // Set custom icon for cluster item marker
        markerOptions.icon(item.icon)
    }

    // Override method to handle cluster item after rendering
    override fun onClusterItemRendered(clusterItem: MyClusterItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        // Attach tag to cluster item marker
        marker.tag = clusterItem.tag
    }
    override fun shouldRenderAsCluster(cluster: Cluster<MyClusterItem>): Boolean {
        return cluster.size > 1
    }
}