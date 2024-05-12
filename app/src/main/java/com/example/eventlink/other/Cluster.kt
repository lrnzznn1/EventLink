package com.example.eventlink.other

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MyClusterItem(
    @JvmField val pos: LatLng,
    @JvmField val title: String,
    @JvmField val desc: String,
    val icon: BitmapDescriptor,
    val tag: String
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
class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context,map,clusterManager){
    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.icon(item.icon)
    }
    override fun onClusterItemRendered(clusterItem: MyClusterItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem.tag
    }
}