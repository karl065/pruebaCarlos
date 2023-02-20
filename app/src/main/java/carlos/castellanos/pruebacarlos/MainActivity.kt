@file:Suppress("SameParameterValue")

package carlos.castellanos.pruebacarlos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import carlos.castellanos.pruebacarlos.databinding.ActivityMainBinding
import carlos.castellanos.pruebacarlos.db.DBHelper
import carlos.castellanos.pruebacarlos.utils.LocationPermissionHelper
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), OnMapClickListener {

    private lateinit var dbHelper: DBHelper

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar("Decimetrix")
        init()
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun onMapReady() {
/*
        val source = GeoJsonSource.Builder("Source_id")
            .url("https://d2ad6b4ur7yvpq.cloudfront.net/naturalearth-3.3.0/ne_50m_populated_places_simple.geojson")
            .build()
        val layer = symbolLayer("layer-id", "source-id") {
            iconImage("icon-id")
            textField("text-field")
            textSize(12.0)
            textColor("red")
        }
        val iconDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.red_marker)
        val iconBitmap: Bitmap? = convertDrawableToBitmap(iconDrawable)
*/
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(16.0)
                .build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            //it.addImage("icon-id", iconBitmap!!)
            //it.addSource(source)
            //it.addLayer(layer)
            initLocationComponent()
            setupGesturesListener()
            binding.buttonLocation.setOnClickListener {
                initLocationComponent()
                setupGesturesListener()
                binding.mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .zoom(16.0)
                        .build()
                )
            }
            binding.mapStyleButton.setOnClickListener { setMapStyle() }
        }

    }

    private fun setMapStyle() {
        val currentStyleUrl = binding.mapView.getMapboxMap().getStyle()?.styleURI
        Log.d("Mapbox", "Style:$currentStyleUrl")

        when (currentStyleUrl) {
            "mapbox://styles/mapbox/streets-v11" -> {
                binding.mapView.getMapboxMap().loadStyleUri(
                    Style.SATELLITE_STREETS
                )
                binding.mapStyleButton.setImageResource(R.drawable.map_traffic)
            }
            "mapbox://styles/mapbox/satellite-streets-v11" -> {
                binding.mapView.getMapboxMap().loadStyleUri(
                    Style.TRAFFIC_DAY
                )
                binding.mapStyleButton.setImageResource(R.drawable.map_street)
            }
            "mapbox://styles/mapbox/traffic-day-v2" -> {
                binding.mapView.getMapboxMap().loadStyleUri(
                    Style.MAPBOX_STREETS
                )
                binding.mapStyleButton.setImageResource(R.drawable.map_satelite)
            }
        }
    }


    private fun setupGesturesListener() {
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }


    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    override fun onMapClick(point: Point): Boolean {

        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.red_marker
        )?.let {
            val annotationApi = binding.mapView.annotations
            annotationApi.cleanup()
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()

                .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))

                .withIconImage(it)

            pointAnnotationManager.create(pointAnnotationOptions)
            Log.d("MapBox", "Point:$point")

            binding.navigationButton.visibility = View.VISIBLE
            binding.navigationButton.setBackgroundColor(resources.getColor(R.color.mapboxBlue))
            binding.favoriteEmpty.visibility = View.VISIBLE
            binding.eliminateMarker.visibility = View.VISIBLE
            binding.eliminateMarker.setOnClickListener {
                annotationApi.cleanup()
                binding.navigationButton.visibility = View.GONE
                binding.favoriteEmpty.visibility = View.GONE
                binding.eliminateMarker.visibility = View.GONE
            }
        }
        return true
    }


    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {

            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun toolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        val ab = supportActionBar
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu)
            ab.setDisplayHomeAsUpEnabled(true)
            ab.title = title
        }

    }

    @SuppressLint("LogNotTimber")
    fun init() {
        val insNombre = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.nombre)
        val insEmail = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.correo)
        val loginMenu = binding.navView.menu.getItem(0)
        val logoutMenu = binding.navView.menu.getItem(1)
        val registroMenu = binding.navView.menu.getItem(2)
        dbHelper = DBHelper(this)
        val cursor = dbHelper.consultarUsuarios()
        if (cursor!!.moveToFirst()) {
            do {
                insNombre.text = cursor.getString(1).toString()
                insEmail.text = cursor.getString(2).toString()
            } while (cursor.moveToNext())

        }

        if (insEmail.text.isEmpty()) {
            binding.navView.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuLogin -> {
                        val intent = Intent(this@MainActivity, Login::class.java)
                        startActivity(intent)
                    }
                    R.id.menuRegistro -> {
                        val intent = Intent(applicationContext, Registro::class.java)
                        startActivity(intent)
                    }
                    R.id.nav_gallery -> {
                        Log.i("NAV_SLIDESHOW", "PREVIEW")
                        Toast.makeText(this, "SLIDE", Toast.LENGTH_SHORT).show()
                    }
                    R.id.nav_slideshow -> {
                        Log.i("NAV_SLIDESHOW", "PREVIEW")
                        Toast.makeText(this, "SLIDE", Toast.LENGTH_SHORT).show()
                    }
                }
                false
            }
        } else {
            loginMenu.isVisible = false
            registroMenu.isVisible = false
            logoutMenu.isVisible = true
            binding.navView.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menuLogout -> {
                        if (cursor.moveToFirst()) {
                            do {
                                val idUsuario = cursor.getString(0).toString()
                                dbHelper.eliminarUsuario(idUsuario)
                                logoutMenu.isVisible = false
                                loginMenu.isVisible = true
                                registroMenu.isVisible = true
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            } while (cursor.moveToNext())

                        }

                    }
                    R.id.nav_gallery -> {
                        Log.i("NAV_SLIDESHOW", "PREVIEW")
                        Toast.makeText(this, "SLIDE", Toast.LENGTH_SHORT).show()
                    }
                    R.id.nav_slideshow -> {
                        Log.i("NAV_SLIDESHOW", "PREVIEW")
                        Toast.makeText(this, "SLIDE", Toast.LENGTH_SHORT).show()
                    }
                }
                false
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
