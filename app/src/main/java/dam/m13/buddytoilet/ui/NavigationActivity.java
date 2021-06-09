package dam.m13.buddytoilet.ui;

/**
 * @version 1.0
 */

// API: https://docs.mapbox.com/android/maps/api-reference/
// Mapbox in fragment: https://stackoverflow.com/questions/43158663/how-to-setinfowindowadapter-for-particular-markers-only
// Mapbox Interfaces: https://docs.mapbox.com/android/maps/api/9.5.0/com/mapbox/mapboxsdk/maps/MapboxMap.html
// Location Component: https://stackoverflow.com/questions/59789868/how-to-add-a-location-button-in-mapbox-like-google-maps-setmylocationbuttonenab
// Scale Bar: https://docs.mapbox.com/android/plugins/examples/scalebar-plugin/

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.geojson.Point;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dam.m13.buddytoilet.R;
import dam.m13.buddytoilet.models.User;
import dam.m13.buddytoilet.models.WCPoint;

/**
 * Display {@link SymbolLayer} icons on the map.
 */
public class NavigationActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener,
        MapboxMap.OnMapLongClickListener, MapboxMap.OnMarkerClickListener
{

    public static User currentUserProfileData;

    public static MapboxMap mainMapboxMap;
    public static MapView mapboxMapView;

    private Boolean validatedFilter = true;

    private WCPoint newWCPointToAdd;
    private WCPoint chosenWCPoint;
    private String[] wcpointsNames;

    // Firebase User
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference databaseReference;

    // Navigation drawer menu
    private DrawerLayout drawerLayout;
    private TextView usernameTextView;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SearchView searchView;
    private ListView wcpointslistView;

    //Link: https://www.xspdf.com/resolution/51812992.html
    // FloatingActionButton
    private FloatingActionButton current_location_button;
    private FloatingActionButton updatedValidatedFilterButton;


    // Map
    private PermissionsManager permissionsManager;
    private Map<String,WCPoint> wcPointMap;
    private Map<String, MarkerOptions> markerOptionsIntegerMap;
    private LocationComponent lastKnownLocation; // coming from deprected "LocationLayerPlugin";
    private ScaleBarPlugin scaleBarPlugin;

    private boolean addedMarkerOptions = false;
    private MarkerOptions addMarkerOptions;

    private SharedPreferences loginSharedPreferences;
    private SharedPreferences filterSharedPreferences;

    // Marler Info Layout
    //private WCPoint chosenWCPoint;
    private LinearLayout chosenMarkerInfoLayout;
    private TextView chosenTitleField;
    private TextView chosenDistanceField;
    private TextView chosenLatField;
    private TextView chosenLngField;

    private ImageView wcSignal;
    private ImageView unisexSignal;
    private ImageView adaptedSignal;
    private ImageView diaperChangerSignal;

    private LinearLayout addMarkerInfoLayout;
    private TextView addTitleField;
    private TextView addDistanceField;
    private TextView addLatField;
    private TextView addLngField;

    // SearchBar content
    //private String[] wcpointsName = new String[]{"WCPoint 1","WCPoint 2"};
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        loginSharedPreferences = getSharedPreferences(getResources().getString(R.string.login_data), Context.MODE_PRIVATE);
        filterSharedPreferences = getSharedPreferences(getResources().getString(R.string.filter_data), Context.MODE_PRIVATE);

        /*
        loginSharedPreferences.edit().clear();
        loginSharedPreferences.edit().commit();
        filterSharedPreferences.edit().clear();
        filterSharedPreferences.edit().commit();
        */

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.

        /** FALLO TOKEN AKI SIEMPRE **/
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token)); //TODO: aqui

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_navigation);
        drawerLayout = (DrawerLayout) findViewById(R.id.mainActivityDrawerLayout);

        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        current_location_button = (FloatingActionButton) findViewById(R.id.currentLocationButton);
        updatedValidatedFilterButton = (FloatingActionButton) findViewById(R.id.updatedValidatedFilterButton);

        chosenMarkerInfoLayout = (LinearLayout) findViewById(R.id.chosenMarkerInfoLayout);
        chosenTitleField = (TextView) findViewById(R.id.titleField);
        chosenDistanceField = (TextView) findViewById(R.id.distanceFieldValue);
        chosenLatField = (TextView) findViewById(R.id.latField);
        chosenLngField = (TextView) findViewById(R.id.lngField);

        addMarkerInfoLayout = (LinearLayout) findViewById(R.id.addMarkerInfoLayout);
        addTitleField = (TextView) findViewById(R.id.addTitleField);
        addDistanceField = (TextView) findViewById(R.id.addDistanceFieldValue);
        addLatField = (TextView) findViewById(R.id.addLatField);
        addLngField = (TextView) findViewById(R.id.addLngField);

        wcSignal = (ImageView) findViewById(R.id.wcImage);
        unisexSignal = (ImageView) findViewById(R.id.gendersSignal);
        adaptedSignal = (ImageView) findViewById(R.id.adaptedSignal);
        diaperChangerSignal = (ImageView) findViewById(R.id.diaperChangerSignal);

        searchView = (SearchView) findViewById(R.id.searchView);
        wcpointslistView = (ListView) findViewById(R.id.wcpointsListView);

        mapboxMapView = (MapView) findViewById(R.id.mapboxMapView); // MapView contains its own LifeCycle methods for managing OpenGL ES cycles

        //check if there is an Intent with a "user" key. If not checked a Bundle error is launched
        if(getIntent().hasExtra(getResources().getString(R.string.user_intent))){
            currentUserProfileData = (User) getIntent().getExtras().get(getResources().getString(R.string.user_intent));
        }

        markerOptionsIntegerMap = new HashMap<>();
        wcPointMap = new HashMap<String, WCPoint>();
        getUserProfileDataFromDB(); // get User's data from Realtime Database using the credentials of current FirebaseUser and save it on User

        //Initialize mapView
        mapboxMapView.onCreate(savedInstanceState); // call the MapView lifecycle's methods and override the previous ones (go below)
        mapboxMapView.getMapAsync(this);

        chosenMarkerInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWCPointDataMenu();
            }
        });

        addMarkerInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWCPointMenu();
            }
        });
    }


    // SETUP MAP SETTINGS AND LOAD IT

    /**
     * Setup and initialize the mapview. Methode run by getMapAsync()
     * @param mapboxMap
     */
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        // save current mapbox instance inside the inner var
        mainMapboxMap = mapboxMap;

        // Initialize all implemented listeners for the map
        mainMapboxMap.addOnMapClickListener(this); // set listening mapboxMap touch On
        mainMapboxMap.addOnMapLongClickListener(this);
        mainMapboxMap.setOnMarkerClickListener(this);

        //2.0
        // Set an Style to mapbox View
        mainMapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if(enableLocationComponent(style)){
                    getWCPointFromDB();
                };

                /**
                 * enable ScaleBar component (distance measurement)
                 */
                scaleBarPlugin = new ScaleBarPlugin(mapboxMapView, mapboxMap);
                ScaleBarOptions scaleBarOptions = new ScaleBarOptions(NavigationActivity.this)
                        .setTextColor(R.color.primaryLightColor)
                        .setTextSize(30f)
                        .setMetricUnit(true)
                        .setMarginLeft(45f)
                        .setMarginTop(120f);

                scaleBarPlugin.create(scaleBarOptions);

                /**
                 * enable Compass component
                 */
                UiSettings uiSettings = mainMapboxMap.getUiSettings();
                uiSettings.setCompassEnabled(true);
            }
        });
    };


    // MARKERS CLICK MANAGEMENT

    /**
     * Set the actions after clicking over a Marker
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        String wcPointId = removePointCharacterFromLatOrLng(String.valueOf(marker.getPosition().getLatitude()));
        wcPointId += removePointCharacterFromLatOrLng(String.valueOf(marker.getPosition().getLongitude()));

        if(wcPointMap.containsKey(wcPointId)) {
            newWCPointToAdd = null;
            chosenWCPoint = wcPointMap.get(wcPointId);
            setChosenMarkerInfoLayout(); //get the data of chosen WCPoint through its Marker
        } else {
            if(newWCPointToAdd == null){
                chosenWCPoint = null;
                newWCPointToAdd = new WCPoint(
                        marker.getPosition().getLatitude(),
                        marker.getPosition().getLongitude()
                );
                newWCPointToAdd.setName(getResources().getString(R.string.new_wcpoint_default_name));
                setChosenMarkerInfoLayout();
                //newChosenWCPoint = newWCPoint;
            }
        }
        return true; // if false a popup shows up, otherwise the pop never shows up
    }

    /**
     * Set the actions after clicking inside any area of mapview except over a Marker
     * @param point
     * @return
     */
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        cleanMapContent();
        getWCPointFromDB(); // reload savedPoints
        return false;
    }

    /**
     * Set the actions after make a long click inside any area of mapview except over a Marker
     * @param point
     * @return
     */
    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {

        chosenWCPoint = null;
        mainMapboxMap.clear();

        // create an instance of a new Point
        //1.0
        Point addedPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        LatLng addedLatLng = new LatLng(addedPoint.latitude(),addedPoint.longitude());

        addMarkerOptions = new MarkerOptions();
        addMarkerOptions.position(addedLatLng);
        mainMapboxMap.addMarker(addMarkerOptions);

        WCPoint wcPoint = new WCPoint(
                point.getLatitude(),
                point.getLongitude()
        );
        wcPoint.setName(getResources().getString(R.string.new_wcpoint_default_name));
        newWCPointToAdd = wcPoint;
        setAddMarkerInfoLayout(wcPoint);

        addSavedMarkers(); // reload savedPoints
        return false;
    }


    /**
     * Clean MapView, variables bounded and data layouts
     */
    private void cleanMapContent(){
        newWCPointToAdd = null;
        chosenWCPoint = null;
        searchView.setIconified(true); //remove the focus over SearchView
        wcpointslistView.setVisibility(View.GONE); //remove the ListView content

        chosenMarkerInfoLayout.setVisibility(View.GONE);
        addMarkerInfoLayout.setVisibility(View.GONE);
        mainMapboxMap.clear();
        addMarkerOptions = null;
    }


    // FILL AND CLEAN "addMarkerInfoLayout" WITH NEW WCPOINT's DATA

    /**
     * Load the data of new Marker into Info Layout at the Bottom of mapview
     * @param addWCPoint
     */
    public void setAddMarkerInfoLayout(WCPoint addWCPoint){
        clearChosenMarkerInfoLayout();
        clearAddMarkerInfoLayout();

        if(addWCPoint != null){
            newWCPointToAdd = addWCPoint;
            addTitleField.setText(addWCPoint.getName());

            //get the distance between user's current position and the chosen WCPoint
            Double distance = distanceBetweenCurrentPositionAndWCPoint(addWCPoint);
            if(distance < 1.0){
                addDistanceField.setText(String.format("%.0f",distance*1000)+"m");
            } else {
                addDistanceField.setText(String.format("%.2f",distance)+"Km");
            }

            addLatField.setText(String.valueOf(addWCPoint.getLat()));
            addLngField.setText(String.valueOf(addWCPoint.getLng()));

            addMarkerInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remove the data of new Marker loaded into Info Layout at the Bottom of mapview and hide the Info Layout itself
     */
    public void clearAddMarkerInfoLayout(){
        addTitleField.setText("");
        addDistanceField.setText("");
        addLatField.setText("");
        addLngField.setText("");
        addMarkerInfoLayout.setVisibility(View.GONE);
    }


    // FILL AND CLEAN "chosenMarkerInfoLayout" WITH CHOSEN WCPOINT's DATA

    /**
     * Load the data of selected Marker into Info Layout at the Bottom of mapview
     */
    public void setChosenMarkerInfoLayout(){
        clearAddMarkerInfoLayout();
        clearChosenMarkerInfoLayout();
        if(chosenWCPoint != null){
            chosenTitleField.setText(chosenWCPoint.getName());

            //get the distance between user's current position and the chosen WCPoint
            Double distance = distanceBetweenCurrentPositionAndWCPoint(chosenWCPoint);
            if(distance < 1.0){
                chosenDistanceField.setText(String.format("%.0f",distance*1000)+"m");
            } else {
                chosenDistanceField.setText(String.format("%.2f",distance)+"Km");
            }

            chosenLatField.setText(String.valueOf(chosenWCPoint.getLat()));
            chosenLngField.setText(String.valueOf(chosenWCPoint.getLng()));

            if(chosenWCPoint.isUnisex()){ unisexSignal.setVisibility(View.GONE); }
            if(!chosenWCPoint.isAdapted()){ adaptedSignal.setVisibility(View.GONE); }
            if(!chosenWCPoint.isDiaperChanger()){ diaperChangerSignal.setVisibility(View.GONE); }

            chosenMarkerInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remove the data of selected Marker loaded into Info Layout at the Bottom of mapview and hide the Info Layout itself
     */
    public void clearChosenMarkerInfoLayout(){

        unisexSignal.setVisibility(View.VISIBLE);
        adaptedSignal.setVisibility(View.VISIBLE);
        diaperChangerSignal.setVisibility(View.VISIBLE);

        chosenTitleField.setText("");
        chosenDistanceField.setText("");
        chosenLatField.setText("");
        chosenLngField.setText("");

        chosenMarkerInfoLayout.setVisibility(View.GONE);
    }


    // SEARCH BAR

    /**
     * Refresh the available Serach Bar's content
     * @param wcpointsName
     */
    private void refreshSearchBarContent(String[] wcpointsName){

        //Search View and ListView management
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wcpointsName);
        wcpointslistView.setAdapter(arrayAdapter);

        wcpointslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addMarkerInfoLayout.setVisibility(View.GONE);
                chosenMarkerInfoLayout.setVisibility(View.GONE);
                for(WCPoint wcPoint : wcPointMap.values()){
                    if(wcPoint.getName().contains(wcpointsName[position])){
                        getWCPointLocation(wcPoint);
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                NavigationActivity.this.arrayAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                NavigationActivity.this.arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wcpointslistView.setVisibility(View.VISIBLE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                wcpointslistView.setVisibility(View.GONE);
                return false;
            }
        });
    }


    // NAVIGATION DRAWER MENU MANAGEMENT

    /**
     * manage the open action of DrawerLayout
     * @param drawerLayout
     */
    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * click hamburguer icon to open navigation drawer menu
     * @param view
     */
    public void openNavigationDrawerMenu(View view){
        // open drawer
        if(currentUserProfileData != null){ usernameTextView.setText(currentUserProfileData.getUsername()); }
        openDrawer(drawerLayout);
    }

    /**
     * manage the close action of drawer layout
     * @param drawerLayout
     */
    private void closeDrawer(DrawerLayout drawerLayout) {
        // close drawer layout (Navigation Drawer Menu)
        // check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            // When drawer is open
            // close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * click any header area to close navigation drawer menu
     * @param view
     */
    public void closeNavigationDrawerMenu(View view){
        // close drawer
        closeDrawer(drawerLayout);
    }


    // NAVIGATION DRAWER MENU INNER ACTIVITIES

    /**
     * open user's profile management
     * @param view
     */
    public void openProfileUser(View view){
        Intent i = new Intent(NavigationActivity.this, ProfileActivity.class);
        i.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
        startActivity(i);
    }

    /**
     * open user's profile own comments
     * @param view
     */
    public void openFavouriteComments(View view){
        //Intent intent = new Intent(NavigationActivity.this, ProfileUserCommentsActivity.class);
        //startActivity(intent);
        Toast.makeText(NavigationActivity.this,"User own comments", Toast.LENGTH_LONG).show();
    }

    /**
     * open filters
     * @param view
     */
    public void openFilters(View view){
        Intent i = new Intent(NavigationActivity.this, FilterActivity.class);
        i.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
        startActivity(i);
    }


    /**
     * logout user and send the application back to login screen
     * @param view
     */
    public void logoutUser(View view){
        signOut();
    }

    /**
     * make a signOut and return to login layout
     */
    private void signOut(){
        if(mAuth != null){
            mAuth.signOut();
        } else {
            FirebaseAuth.getInstance().signOut();
        }

        //remove User's credential data from shared Preferences
        LoginActivity.cleanUserDataFromSharedPreferences(loginSharedPreferences);

        Toast.makeText(NavigationActivity.this,getResources().getString(R.string.see_you_soon), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NavigationActivity.this, LoginActivity.class)); // load the Main Screen
    }


    // GET WCPOINTS FROM DATABASE

    /**
     * Load the got WCPoints from DB into mapview
     */
    private void getWCPointFromDB(){

        //Link: https://www.youtube.com/watch?v=WeoryL3XyA4
        Query query = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.wcpoints_collection));

        //query.addValueEventListener(valueEventListener);
        //query.orderByChild("free").equalTo(false).addValueEventListener(valueEventListener);

        Double westLng = coordinatesRespectToCurrentPosition(
                Double.parseDouble(filterSharedPreferences.getString(getResources().getString(R.string.distance),"1.0")), //convert String to Double
                180.0)
                .latitude();
        Double eastLng = coordinatesRespectToCurrentPosition(
                Double.parseDouble(filterSharedPreferences.getString(getResources().getString(R.string.distance),"1.0")),  //convert String to Double
                0.0)
                .latitude();
        Double northLat = coordinatesRespectToCurrentPosition(
                Double.parseDouble(filterSharedPreferences.getString(getResources().getString(R.string.distance),"1.0")),  //convert String to Double
                90.0)
                .longitude();
        Double southLat = coordinatesRespectToCurrentPosition(
                Double.parseDouble(filterSharedPreferences.getString(getResources().getString(R.string.distance),"1.0")), //convert String to Double
                -90.0)
                .longitude();

        //restrict the query to lng value and comparing it to our current location
        query.orderByChild("lng").startAt(westLng).endAt(eastLng).addValueEventListener(valueEventListener);
        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    wcPointMap = filterWCPointsByLatitude(wcPointMap, southLat, northLat);
                    wcPointMap = validatedFilter(wcPointMap);
                    if(stringToBoolean(filterSharedPreferences.getString(getResources().getString(R.string.filter), getResources().getString(R.string.false_value))))
                    { wcPointMap = filterWCPoints(wcPointMap); }
                    addSavedMarkers();
                }
            }
        });
    }


    /**
     * Get WCPoints from DB
     */
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            wcPointMap = new HashMap<String, WCPoint>();
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    WCPoint wcPoint = snapshot.getValue(WCPoint.class);
                    if(!wcPointMap.containsKey(wcPoint.getId())){
                        wcPointMap.put(wcPoint.getId(),wcPoint);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            //
        }
    };


    /**
     * Filter mapped data by a latitude range
     * @param wcPointList
     * @param southLat
     * @param northLat
     * @return
     */
    private Map<String,WCPoint> filterWCPointsByLatitude(Map<String,WCPoint> wcPointList, Double southLat, Double northLat){
        Map<String, WCPoint> filteredWCPoints = new HashMap<>();
        for(WCPoint currentWCPoint : wcPointList.values()){

            //filter by lat value (min and max)
            if(southLat <= currentWCPoint.getLat() && currentWCPoint.getLat() <= northLat && !filteredWCPoints.containsKey(currentWCPoint.getId())){
                filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);
            }
        }
        return filteredWCPoints;
    }


    /**
     * Filter mapped data each filter state
     * @param wcPointList
     * @return
     */
    private Map<String,WCPoint> filterWCPoints(Map<String,WCPoint> wcPointList){
        final String[] signArray = getResources().getStringArray(R.array.filter_values);
        Map<String, WCPoint> filteredWCPoints = new HashMap<>();

        Boolean free = filterSharedPreferences.getString(signArray[0], getResources().getString(R.string.false_value)).matches(getResources().getString(R.string.true_value));
        Boolean unisex = filterSharedPreferences.getString(signArray[1], getResources().getString(R.string.false_value)).matches(getResources().getString(R.string.true_value));
        Boolean adapted = filterSharedPreferences.getString(signArray[2], getResources().getString(R.string.false_value)).matches(getResources().getString(R.string.true_value));
        Boolean diaper = filterSharedPreferences.getString(signArray[3], getResources().getString(R.string.false_value)).matches(getResources().getString(R.string.true_value));

        //check all filter params are set as "false"
        if( (free || unisex || adapted || diaper) == false){
            filteredWCPoints = wcPointList;
        }

        //search for any minimal match in order to add the valuated WCPoint
        else{
            for(WCPoint currentWCPoint : wcPointList.values()) {
                if (free) {
                    if (currentWCPoint.isFree() && !filteredWCPoints.containsKey(currentWCPoint.getId())) {
                        filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);
                    }
                }
                if (unisex) {
                    if (currentWCPoint.isUnisex() && !filteredWCPoints.containsKey(currentWCPoint.getId())) {
                        filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);
                    }
                }
                if (adapted) {
                    if (currentWCPoint.isAdapted() && !filteredWCPoints.containsKey(currentWCPoint.getId())) {
                        filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);
                    }
                }
                if (diaper) {
                    if (currentWCPoint.isDiaperChanger() && !filteredWCPoints.containsKey(currentWCPoint.getId())) {
                        filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);
                    }
                }
            }
        }
        return filteredWCPoints;
    }


    // ACTIVATE/DEACTIVATE THE VALIDATED FILTER

    /**
     * Change the validated filter value
     * @param view
     */
    public void updateValidatedFilter(View view){

        validatedFilter = !validatedFilter;
        if(validatedFilter){
            //Log.e("OFF",getResources().getString(R.string.validated_filter_off));
            updatedValidatedFilterButton.setSupportBackgroundTintList(
                    ColorStateList.valueOf(
                            getResources().getColor(R.color.green)));

        } else {
            //Log.e("ON",getResources().getString(R.string.validated_filter_on));
            updatedValidatedFilterButton.setSupportBackgroundTintList(
                    ColorStateList.valueOf(
                            getResources().getColor(R.color.google)));
        }

        showValidatedFilterStatus();

        addMarkerInfoLayout.setVisibility(View.GONE);
        chosenMarkerInfoLayout.setVisibility(View.GONE);
        mainMapboxMap.clear();
        getWCPointFromDB();

        //remove SerachBar's content
        wcpointsNames = new String[0];
        refreshSearchBarContent(wcpointsNames); //refresh the content printed in SearchBar
    }

    /**
     * Show a text indicating the current status of validated filter
     */
    public void showValidatedFilterStatus(){
        if(validatedFilter){ Toast.makeText(NavigationActivity.this, getResources().getString(R.string.validated_filter_on) , Toast.LENGTH_SHORT).show(); }
        else Toast.makeText(NavigationActivity.this, getResources().getString(R.string.validated_filter_off) , Toast.LENGTH_SHORT).show();
    }

    /**
     * Filter mapped data by Validated or No validated attribute value
     * @param wcPointList
     * @return
     */
    private Map<String,WCPoint> validatedFilter(Map<String,WCPoint> wcPointList){
        Map<String, WCPoint> filteredWCPoints = new HashMap<>();
        for(WCPoint currentWCPoint : wcPointList.values()){
            if(validatedFilter){
                if(currentWCPoint.isValidated()){
                    filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);}
            } else {
                if(!currentWCPoint.isValidated()){
                    filteredWCPoints.put(currentWCPoint.getId(), currentWCPoint);}
            }
        }
        return filteredWCPoints;
    }


    // ADD WCPOINTS TO MAP

    /**
     * Load WCPoints into mapview
     */
    private void addSavedMarkers() {

        if(wcPointMap != null && 0 < wcPointMap.size()) {
            wcpointsNames = new String[wcPointMap.size()];

            int i = 0;
            for (String wcPointId : wcPointMap.keySet()) {
                WCPoint wcPoint = wcPointMap.get(wcPointId);

                //Log.e("WCPoint",wcPoint.getId());
                if(wcPoint!=null){
                    wcpointsNames[i] = wcPoint.getName();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(wcPoint.getLat(), wcPoint.getLng()));
                    markerOptions.title(wcPoint.getName());

                    markerOptions.snippet(
                            "Lat: " + String.valueOf(wcPoint.getLat())+
                                    "\n,Lng: " + String.valueOf(wcPoint.getLng()));

                    //CustomInfoWindow addMarkerWindow = new CustomInfoWindow(NavigationActivity.this);
                    //mainMapboxMap.setInfoWindowAdapter(addMarkerWindow);

                    if (wcPoint.isFree()) {
                        markerOptions.icon(IconFactory.getInstance(NavigationActivity.this).fromResource(R.drawable.wc_point_icon_green_32));
                    } else {
                        markerOptions.icon(IconFactory.getInstance(NavigationActivity.this).fromResource(R.drawable.wc_point_icon_blue_32));
                    }
                    mainMapboxMap.addMarker(markerOptions);
                }
                i++;
            }
            refreshSearchBarContent(wcpointsNames); //refresh the content printed in SearchBar
        }
    }


    // DATA/REMOVE USER MARKER'S POPUP MENU MANAGEMENT

    /**
     * Popup menu when an existing WCPoint inside mapview is clicked
     */
    public void getWCPointDataMenu(){

        AlertDialog.Builder alert = new AlertDialog.Builder(NavigationActivity.this);
        alert.setCancelable(true);
        final AlertDialog alertDialog = alert.create();

        /**
         * Admin User's menu
         */
        if(currentUserProfileData.isAdmin()){
            View mView = getLayoutInflater().inflate(R.layout.popup_menu_admin_data_wcpoint, null);

            Button validateMarkerButton = (Button) mView.findViewById(R.id.validateMarkerButton);
            Button removeMarkerButton = (Button) mView.findViewById(R.id.removeMarkerButton);
            Button userInfoMarkerButton = (Button) mView.findViewById(R.id.adminDataMarkerButton);
            alertDialog.setView(mView);
            alertDialog.show();

            if(validatedFilter){
                validateMarkerButton.setVisibility(View.GONE);
            }
            else {
                validateMarkerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * Validate the chosen WCPoint
                         */
                        chosenMarkerInfoLayout.setVisibility(View.GONE);
                        if (!chosenWCPoint.isValidated()) {
                            chosenWCPoint.setValidated(true);
                            chosenWCPoint.save();
                            Toast.makeText(NavigationActivity.this, "VALIDATED WCPoint " + chosenWCPoint.getId(), Toast.LENGTH_SHORT).show();
                        }

                        alertDialog.dismiss();
                        cleanMapContent();
                        getWCPointFromDB();
                    }
                });
            }

            removeMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /**
                     * Remove the chosen WCPoint
                     */
                    Toast.makeText(NavigationActivity.this, "DELETED WCPoint "+chosenWCPoint.getId(), Toast.LENGTH_SHORT).show();
                    chosenMarkerInfoLayout.setVisibility(View.GONE);
                    chosenWCPoint.delete();

                    alertDialog.dismiss();
                    cleanMapContent();
                    getWCPointFromDB();
                }
            });

            userInfoMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * Go to get all WCPoint data info
                     */

                    Intent intent = new Intent(NavigationActivity.this, WCPointActivity.class);
                    intent.putExtra(getResources().getString(R.string.wcpoint_intent), chosenWCPoint);
                    intent.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
                    startActivity(intent);

                    //Toast.makeText(NavigationActivity.this,"WCPOINT DATA (ADMIN)", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            });
        }

        /**
         * Common User's menu
         */
        else {
            View mView = getLayoutInflater().inflate(R.layout.popup_menu_user_data_wcpoint, null);

            Button userInfoMarkerButton = (Button) mView.findViewById(R.id.userDataMarkerButton);
            alertDialog.setView(mView);
            alertDialog.show();

            userInfoMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * Go to get all WCPoint data info
                     */

                    Intent intent = new Intent(NavigationActivity.this, WCPointActivity.class);
                    intent.putExtra(getResources().getString(R.string.wcpoint_intent), chosenWCPoint);
                    intent.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
                    startActivity(intent);

                    //Toast.makeText(NavigationActivity.this, "WCPOINT DATA (COMMON USER)", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            });
        }
    }


    // ADD/REQUEST MARKER'S POPUP MENU MANAGEMENT

    /**
     * Popup menu when a Marker for a new WCPoint is clicked
     */
    public void addWCPointMenu(){
        AlertDialog.Builder alert = new AlertDialog.Builder(NavigationActivity.this);
        alert.setCancelable(true);
        final AlertDialog alertDialog = alert.create();

        /**
         * Admin User's menu
         */
        if(currentUserProfileData.isAdmin()){
            View mView = getLayoutInflater().inflate(R.layout.popup_menu_admin_add_wcpoint, null);
            Button addMarkerButton = (Button) mView.findViewById(R.id.addMarkerButton);
            alertDialog.setView(mView);
            alertDialog.show();

            addMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(newWCPointToAdd != null){
                        Intent intent = new Intent(NavigationActivity.this, AddWCPointActivity.class);
                        intent.putExtra(getResources().getString(R.string.wcpoint_intent), newWCPointToAdd);
                        intent.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
                        startActivity(intent);

                        //Log.e("WCPOINT: ",currentUserProfileData.getId());
                        //Log.e("USER: ",currentUserProfileData.getId());
                    } else
                        Toast.makeText(NavigationActivity.this, getResources().getString(R.string.no_wcpoint_chosen)+" (ADMIN)", Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();
                }
            });
        }

        /**
         * Common User's menu
         */
        else {
            View mView = getLayoutInflater().inflate(R.layout.popup_menu_user_request_add_wcpoint, null);
            Button requestAddMarkerButton = (Button) mView.findViewById(R.id.requestAddMarkerButton);
            alertDialog.setView(mView);
            alertDialog.show();

            requestAddMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(newWCPointToAdd != null){
                        Intent intent = new Intent(NavigationActivity.this, AddWCPointActivity.class);
                        intent.putExtra(getResources().getString(R.string.wcpoint_intent), newWCPointToAdd);
                        intent.putExtra(getResources().getString(R.string.user_intent), currentUserProfileData);
                        startActivity(intent);
                    } else
                        Toast.makeText(NavigationActivity.this,  getResources().getString(R.string.no_wcpoint_chosen)+" (COMMON USER)", Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();
                    alertDialog.cancel();
                }
            });
        }
    }


    // GET USER'S DATA

    /**
     * Get Current User's data from DB and keep it
     */
    private void getUserProfileDataFromDB(){
        if(mUser != null){
            String userID = mUser.getUid(); // get the UID from user
            databaseReference = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.users_collection)); // get an instance to get "Users" collection's content
            //Toast.makeText(ProfileUserManagementActivity.this,"USER: "+userID, Toast.LENGTH_SHORT).show();

            databaseReference.child(userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        currentUserProfileData = task.getResult().getValue(User.class);
                        if(currentUserProfileData.isAdmin()){ updatedValidatedFilterButton.setVisibility(View.VISIBLE); }
                        else updatedValidatedFilterButton.setVisibility(View.GONE);
                    } else Toast.makeText(NavigationActivity.this, getResources().getString(R.string.user_data_not_found), Toast.LENGTH_SHORT);
                }
            });
        }
    }


    // CURRENT LOCATION BUTTONS (Zoom x15)

    /**
     * move the mapview into current User's location
     * @param view
     */
    public void getCurrentLocation(View view)
    {
        lastKnownLocation = mainMapboxMap.getLocationComponent();
        CameraPosition currentCameraPosition = mainMapboxMap.getCameraPosition();

        // if zoom value is less than x12 load current position with x12. Otherwise load the current position with the same xValue
        Double zoomValue = (currentCameraPosition.zoom<12.0) ? 12 : currentCameraPosition.zoom;

        if (lastKnownLocation != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(
                            lastKnownLocation.getLastKnownLocation().getLatitude(),
                            lastKnownLocation.getLastKnownLocation().getLongitude())) // Sets the new camera position
                    .zoom(zoomValue) // load the same zoom's position (map's scale [0-22])
                    .bearing(0) // direction that the camera is pointing in, measured in degrees clockwise from north
                    .tilt(0) // camera's angle from nadir (directly facing the Earth) that uses degrees as unit [0-60]
                    .build(); // Creates a CameraPosition from the builder

            //time of camera to run the animation which sets the user's current position
            mainMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1000);
        }
    }


    //WCPOINT LOCATION

    /**
     * move the mapview into the chosen WCPoint
     * @param wcpoint
     */
    public void getWCPointLocation(WCPoint wcpoint)
    {
        if (wcpoint != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(
                            wcpoint.getLat(),
                            wcpoint.getLng())) // Sets the new camera position
                    .zoom(17.0) // load the same zoom's position (map's scale [0-22])
                    .bearing(0) // direction that the camera is pointing in, measured in degrees clockwise from north
                    .tilt(0) // camera's angle from nadir (directly facing the Earth) that uses degrees as unit [0-60]
                    .build(); // Creates a CameraPosition from the builder

            //time of camera to run the animation which sets the user's current position
            mainMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1000);

            //searchView.clearFocus();
            searchView.setIconified(true); //remove the focus over SearchView
            wcpointslistView.setVisibility(View.GONE); //remove the ListView content
        }
    }


    //STYLE'S MAP

    /**
     * Show MapStyle's menu in order to choose one. The chosen style is appled in mapView
     * @param view
     */
    public void showStyleOptionsMenu(View view){
        List<String> options = new ArrayList<String>();
        options.add(getResources().getString(R.string.normal_style));
        options.add(getResources().getString(R.string.satellite_style));
        options.add(getResources().getString(R.string.dark_style));
        ArrayAdapter<String> popupAdapter = new ArrayAdapter<String>(this, R.layout.popup_menu_map_style_option, options);

        // get the view's parent's layout in order to attach the popup menu's width to it instead of to view
        View parentView = view.getRootView().findViewById(R.id.changeStyleButtonLayout);

        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(popupAdapter);
        listPopupWindow.setAnchorView(parentView);
        listPopupWindow.setOnItemClickListener((parent, itemView, position, id) -> {
            String chosenOption = options.get(position);
            if(chosenOption.contentEquals(options.get(0))){
                //registerWCPoint(mUser, addMarkerOptions);
                mainMapboxMap.setStyle(Style.MAPBOX_STREETS);
                Toast.makeText(NavigationActivity.this, getResources().getString(R.string.normal_style_loaded_text),Toast.LENGTH_SHORT).show();
            }
            else if(chosenOption.contentEquals(options.get(1))){
                mainMapboxMap.setStyle(Style.SATELLITE);
                Toast.makeText(NavigationActivity.this, getResources().getString(R.string.satellite_style_loaded_text),Toast.LENGTH_SHORT).show();
            }
            else if(chosenOption.contentEquals(options.get(2))){
                mainMapboxMap.setStyle(Style.DARK);
                Toast.makeText(NavigationActivity.this, getResources().getString(R.string.dark_style_loaded_text),Toast.LENGTH_SHORT).show();
            }
            listPopupWindow.dismiss();
        });
        listPopupWindow.show();
    }


    // LOCATION PERMISSIONS MANAGEMENT

    /**
     * Enable Location Permissions on the device for this application
     * Link: https://docs.mapbox.com/android/core/guides/#locationengine
     * @param loadedMapStyle
     */
    @SuppressWarnings("MissingPermission")
    private boolean enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .build();

            // Create a customized setup for locationComponent
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build();

            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameters
            // Get an instance of the component
            lastKnownLocation = mainMapboxMap.getLocationComponent();

            // Activate with options
            lastKnownLocation.activateLocationComponent(locationComponentActivationOptions);
            //locationComponent.activateLocationComponent(this, loadedMapStyle);

            // Enable to make component visible
            lastKnownLocation.setLocationComponentEnabled(true);

            // Set the component's camera mode
            lastKnownLocation.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            lastKnownLocation.setRenderMode(RenderMode.COMPASS);
            //locationComponent.setRenderMode(RenderMode.NORMAL);

            return true;
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            return false;
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_granted, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mainMapboxMap.getStyle());
            //Toast.makeText(this, R.string.user_location_permission_granted, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //LATLNG VALUES MANAGEMENT

    /**
     * Calculate the distance between the current position and the passed WCPoint
     * @param wcPoint
     * @return
     */
    private double distanceBetweenCurrentPositionAndWCPoint(WCPoint wcPoint){
        Location currentLocation = getCurrentPosition();
        Point currentPosition = Point.fromLngLat(currentLocation.getLatitude(), currentLocation.getLongitude());
        Point wcPointPosition = Point.fromLngLat(wcPoint.getLat(),wcPoint.getLng());
        return TurfMeasurement.distance(currentPosition, wcPointPosition, TurfConstants.UNIT_KILOMETERS);
    }

    /**
     * Return a Point object when requesting the coordinates using a radius distance and the orientation (0-360 degrees)
     * in respect of current user's geolocated position.
     * @param distance
     * @param bearing
     * @return
     */
    private Point coordinatesRespectToCurrentPosition(Double distance, Double bearing){
        Point currentPosition = getPointFromCurrentLocation(getCurrentPosition());
        return TurfMeasurement.destination(currentPosition, distance, bearing, TurfConstants.UNIT_KILOMETERS);
    }


    // Get a latlng object, cast its lat and lng into an string and remove the points inside
    private String removePointCharacterFromLatOrLng(String latOrLng){
        return latOrLng.replace(".","");
    }

    private String LatLngToString(LatLng latLng){
        String wcPointId = removePointCharacterFromLatOrLng(String.valueOf(latLng.getLatitude()));
        wcPointId += removePointCharacterFromLatOrLng(String.valueOf(latLng.getLongitude()));
        return wcPointId;
    }


    // GET CURRENT POSITION

    /**
     * Get a Location object with current position
     * @return
     */
    private Location getCurrentPosition(){
        return mainMapboxMap.getLocationComponent().getLastKnownLocation();
    }

    /**
     * Get the Point of entered Location object
     * @param location
     * @return
     */
    private Point getPointFromCurrentLocation(Location location){
        return Point.fromLngLat(location.getLatitude(), location.getLongitude());
    }

    public String booleanToString(Boolean attributeValue){ return attributeValue ? "true" : "false"; }
    public Boolean stringToBoolean(String attributeValue){ return attributeValue.matches("false")? false : true; }


    // OVERRIDE THE CURRENT ACTIVITY LIFECYCLES

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        getUserProfileDataFromDB();
        mapboxMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapboxMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapboxMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapboxMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapboxMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mainMapboxMap != null){
            mainMapboxMap.removeOnMapClickListener(this);
            mainMapboxMap.removeOnMapLongClickListener(this);}
        mapboxMapView.onDestroy();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapboxMapView.onSaveInstanceState(outState);
    }
}