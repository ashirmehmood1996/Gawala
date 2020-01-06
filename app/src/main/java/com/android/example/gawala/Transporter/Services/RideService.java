package com.android.example.gawala.Transporter.Services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.example.gawala.Constants;
import com.android.example.gawala.Generel.Activities.MainActivity;
import com.android.example.gawala.Generel.App;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Activities.TransporterMainActivity;
import com.android.example.gawala.Transporter.Models.DistanceMatrixModel;
import com.android.example.gawala.Transporter.Utils.DistanceMatrixAsyncTask;
import com.android.example.gawala.Transporter.Utils.HttpRequestHelper;
import com.android.example.gawala.Transporter.Utils.UrlGenrator;
import com.android.example.gawala.Transporter.directionhelpers.FetchURL;
import com.android.example.gawala.Transporter.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;
// abstract over all logic
// when service is started we start to listen the location
// once lcation listening is started on each location change we check that how close we are to the fist stop in the list
// to check how close we are to a customer we can do the following
//One way (CHEAP BUT COMPLEX WITH GOOD ACCURACYLOW ACCURACY )(using polyline )//this way has an extra complexity of adding the stop in the list of steps because by default I didnt see there
// 1) we requests a full polyline
// 2) we need to find the point which is our current destination stop
// 3) we find the closest point on polyline from our current location
// 4) we cal caulate the time and distance of all the remaining points till the stop and when they reach to a certain thresh hold we notify the customer
// 5) the only problem is that if we have more than one stop next to each other then they are getting notified only a few seconds before delivery
// so to tackle the problem in the point number 5 we may add further checks like if two stops are nearer than a certain thresh hold then we may have to treat them along side of the current stop
//Another way(CHEAP LOW ACCURACY )(using one time distance matrix call )
//1) we requests one time the distance matrix api and get over all distance and time
//2) we measure the small displacements of the driver and add them to calculate the distance covered and them subtract the from total distance obtained by distance matrix api in order to get the remainng time and distance.
//3) then we send notifications over a certain thresh hold.
//NOTE:- in all of the ways we also have to suggest the bests order of stop later
//third way(EXPENSIVE HIGH accuracy)(making alot of distance matrix api calls along with the polyline calles for each consumer)
// 1) currently I have to do is call the polyline of first stop just for visual effects and save the json.
// 2) in on location change we make the calculation that if X meters is covered then we must call the distance matrix api for new calculations and notify the clients accordingly
// 3) once we recieve a click  that stop is covered we move the pointer to next stop and do the same for newt stop until all are done and abort the journey

// accordingly we send the notification and save record after all are served

public class RideService extends Service {
    private final IBinder binder = new MyLocalBinder();
    private Callbacks callbacks;
    private static final long FASTEST_INTERVAL = 5000; //change back to 100000
    private static final long REQUEST_INTERVAL = 3000;//change back to 15000
    public static final String RIDE_ARRAY_LIST_KEY = "rideArray";
    public static final String PROVIDER_ID = "providerId";

    public static int activeStopPosition;
//    public static boolean isRidingActive;

    private LocationResult mCurrentLocationResult;


    private ArrayList<ConsumerModel> mActiveRideArrayList;
    private String myID;
    private String providerId;
    private TextToSpeech textToSpeech;
    private PolylineOptions currentStopPolylineOptions;
    private DistanceMatrixModel distanceMatrixModel;
    private int DISTANCE_MATRIX_MIN_INTRVAL_DISTANCE = 50; // in meters
    private long MINIMUM_TIME_FOR_AGGRESIVE_ALERT = 100; //seconds

    private FusedLocationProviderClient client;
    private LocationCallback mLocationcallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            mCurrentLocationResult = locationResult;

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

            if (mActiveRideArrayList != null) { // to make sure that on start command has been called

                if (currentStopPolylineOptions == null) { //will be null in the beginning and when the new stop is assigned
                    callForPolylineApi();
                }
                if (distanceMatrixModel == null) { //will be null in the beginning  and when the new stop is assigned
                    callForDistanceMatrixApi();
                }

                //calculations

//                        if  the distance covered is greater than 100 meter then we may request another distance matrix api call
                if (mCurrentLocationResult.getLastLocation().distanceTo(mCurrentLocationResult.getLocations().get(0)) >= DISTANCE_MATRIX_MIN_INTRVAL_DISTANCE) {
                    callForDistanceMatrixApi();
                }
            }

            updateMyLocationInFirebase(locationResult);
        }
    };

    @Override
    public void onCreate() {
        createLocationRequest();
        initTextToSpeach();
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        activeStopPosition = 0;
//        isRidingActive=false;
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification().build());
        mActiveRideArrayList = (ArrayList<ConsumerModel>) intent.getSerializableExtra(RIDE_ARRAY_LIST_KEY);
        providerId = intent.getStringExtra(PROVIDER_ID);
        return super.onStartCommand(intent, flags, startId);
    }

    //location related
    private void createLocationRequest() {
        //this request is to chec the necessary settings

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(REQUEST_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(0);//later turn to some bigger value
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(/*RideService.this,*/ locationSettingsResponse -> {
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            requestLocationUpdates(locationRequest);

        });
        task.addOnFailureListener(/*this,*/ new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Toast.makeText(getApplicationContext(), "resolvable failure", Toast.LENGTH_SHORT).show();
                    // FIXME: if time an on production level  9/20/2019 later use this reolution code experimentally to resolve the issue
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProviderClientsFragment.this,
////                           REQUEST_CHECK_SETTINGS);
//               } catch (IntentSender.SendIntentException sendEx) {
//                   // Ignore the error.
//               }

                } else {
                    Toast.makeText(getApplicationContext(), "non resolvable failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestLocationUpdates(LocationRequest request) {
        //this will cause the location to be uptoTime in a node in firebase but I dont want too much of that
//        LocationRequest request = new LocationRequest();
//        request.setInterval(REQUEST_INTERVAL);
//        request.setFastestInterval(FASTEST_INTERVAL);
//        request.setSmallestDisplacement(10);
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        client = LocationServices.getFusedLocationProviderClient(this);
        //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, mLocationcallback, null);// FIXME:  if time 8/8/2019 detach the listener when the user logs out or activity is teminated

        } else {
            Toast.makeText(getApplicationContext(), "no permissions", Toast.LENGTH_SHORT).show();
        }
    }


    private void callForPolylineApi() {
        final LatLng currentLocation = new LatLng(mCurrentLocationResult.getLocations().get(0).getLatitude(), mCurrentLocationResult.getLocations().get(0).getLongitude());

        final LatLng stopLocation = new LatLng(Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLatitude()),
                Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLongitude()));

        TaskLoadedCallback taskLoadedCallback = values -> {
            currentStopPolylineOptions = (PolylineOptions) values[0];
            if (callbacks != null)//notify to activty if alive else it will be retrieved while it gets alive
                callbacks.updatePolyline(currentStopPolylineOptions);

        };
        new FetchURL(taskLoadedCallback, false).execute(getDirectionApiUrl(currentLocation, stopLocation), "driving");

    }

    // TODO: LATER if time  8/15/2019 create this one on your own and place in httphelper util class and add direction mode later if needed
    private String getDirectionApiUrl(LatLng origin, LatLng dest/*, boolean isFullRoute *//*, String directionMode*/) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        //String mode = "mode=" + directionMode;

        // Building the parameters to the web service
        String parameters = null;
//        if (isFullRoute) {
//            StringBuilder wayPoints = new StringBuilder("waypoints=");
//            for (int i = 0; i < mActiveRideArrayList.size(); i++) {
//
//                ConsumerModel curentConsumerModel = mActiveRideArrayList.get(i);
//                Double currentLat = Double.parseDouble(curentConsumerModel.getLatitude());
//                Double currentLng = Double.parseDouble(curentConsumerModel.getLongitude());
//                wayPoints.append("via:").append(currentLat).append(",").append(currentLng);
//                if (i != mActiveRideArrayList.size() - 1) {//if its not the last one then add pipe
//                    wayPoints.append("|");
//                }
//            }
//            parameters = str_origin + "&" + str_dest + "&" + wayPoints.toString();
//        } else {
        parameters = str_origin + "&" + str_dest;
//        }


        // Output format
        String output = "json";

//        &waypoints = via:-37.81223 % 2 C144 .96254 % 7 Cvia:
//        -34.92788 % 2 C138 .60008
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getResources().getString(R.string.directions_api);
    }


    private void callForDistanceMatrixApi() {
        final LatLng currentLocation = new LatLng(mCurrentLocationResult.getLocations().get(0).getLatitude(), mCurrentLocationResult.getLocations().get(0).getLongitude());

        final LatLng stopLocation = new LatLng(Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLatitude()),
                Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLongitude()));

        String url = UrlGenrator.generateDistanceMatrixUrl(currentLocation, stopLocation, getResources().getString(R.string.distance_matrix_api_key));

        new DistanceMatrixAsyncTask() {
            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    Toast.makeText(getApplicationContext(), "response was null", Toast.LENGTH_LONG).show();
                } else {
////                    System.out.println("response :" + s);
////                    Snackbar.make(drawer, "riding now", Snackbar.LENGTH_LONG).show();
//
//                    // FIXME: later iof needed 9/16/2019 crashes here as its still running while app shutdowns
//                    ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_onduty));
////                    System.out.println("json string :" + s);
                    distanceMatrixModel = HttpRequestHelper.parseDistanceMatrixJson(s);
//
                    if (callbacks != null) {
                        String distance = distanceMatrixModel.getDistance()/*distanceArray[0] + " meters"*/;
                        String time = distanceMatrixModel.getDuration();
                        String speed = String.format("%.2f m/sec", mCurrentLocationResult.getLocations().get(0).getSpeed());
                        callbacks.setDistanceTimeSpeed(distance, time, speed);
                    }
                    if (mActiveRideArrayList != null && !mActiveRideArrayList.isEmpty()) {
                        ConsumerModel consumerModel = mActiveRideArrayList.get(activeStopPosition);
                        if (consumerModel.getAlertNotificationTime() != 0) {//if time is zero then the notification was already sent
                            if (distanceMatrixModel.getDurationLong() <= consumerModel.getAlertNotificationTime()) {
                                consumerModel.setAlertNotificationTime(0);//this means that the notification has already been sent
                                long timeRemaining = distanceMatrixModel.getDurationLong();
                                String title = "Milk Alert";
                                String message = "your milk is about to arrive in " + timeRemaining + "seconds";
                                sendNotificationToConsumer(mActiveRideArrayList.get(activeStopPosition).getId(), title, message, Constants.Notification.TYPE_ALERT);

//                        solve the bug || may be array list was not populated an location was 0 which can be made a check
                            }
                        }
                    }

                }
                super.onPostExecute(s);
            }
        }.execute(url);

    }


    private void sendNotificationToConsumer(String id, String title, String message, String type) {
        // TODO: 8/4/2019  LATER at the time of production for now generating both  in current app and in customers app later only customer will be notified
        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", title);
        notificationMap.put("message", message);
        notificationMap.put("type", type);
        notificationMap.put("sender_id", myID);
        notificationMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");

//        for (int i = 0; i < 100; i++) {
//            String newtitle = title + "  " + i;
//            notificationMap.put("title", newtitle);
        rootRef.child("notifications")
                .child(id)//reciever id
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//sender id
                .push()//notification id
                .setValue(notificationMap);//for now no need for completion listener
//        }


        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                this, //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);
        //creating a notification
        NotificationCompat.Builder downloadNotificationBuilder;
        downloadNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_add_location_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)//fixme LATER   if needed
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, downloadNotificationBuilder.build());

    }

    private void updateMyLocationInFirebase(LocationResult locationResult) {
        rootRef.child("locationUpdates").child(FirebaseAuth.getInstance().getCurrentUser().getUid())// FIXME: LATER if time 9/8/2019 fix float to string cast exception
                .setValue(locationResult).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //                                Toast.makeText(TransporterMainActivity.this, "node updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "error updating firebase", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * this method is responsible for changing the current stop locartion to next and can call o abort journey if deemed necessary
     */
    public void deliveredToCurrentStop() {
        mActiveRideArrayList.get(activeStopPosition).setDelivered(true);
        sendMessageToConsumer(mActiveRideArrayList.get(activeStopPosition).getId());
        activeStopPosition++;
        if (activeStopPosition >= mActiveRideArrayList.size()) { //then there are no stops further and we need to finish journey
            abortJourney();
            return;
        }

        sendStartMessageToConsumer(mActiveRideArrayList.get(activeStopPosition).getId());// this will indicate that transporter is headed to this consumer
        speak("Now heading towards " + mActiveRideArrayList.get(activeStopPosition).getName());
        currentStopPolylineOptions = null; // they will be called again in on location changed
        distanceMatrixModel = null;
    }

    private void sendStartMessageToConsumer(String consumerId) {
        String title = "Acknowledgement";
        String message = "Transporter is headed to deliver your order";
        String type = Constants.Notification.TYPE_GENERAL;
        sendNotificationToConsumer(consumerId, title, message, type);
    }

    private void sendMessageToConsumer(String consumerId) {
        String title = "Acknowledgement";
        String message = "milk is delivered to you";
        String type = Constants.Notification.TYPE_GENERAL;
        sendNotificationToConsumer(consumerId, title, message, type);
    }


    public PolylineOptions getPolyLineOptions() {
        return this.currentStopPolylineOptions;
    }

    private void abortJourney() {
        speak("Saving");
        if (client != null) {
            client.removeLocationUpdates(mLocationcallback);
            client = null;
        }
        activeStopPosition = 0;
        addSessionToDatabase();
        mActiveRideArrayList.clear();
        if (callbacks != null) {
            callbacks.abortJourney();
        }
    }


    private void addSessionToDatabase() {
        DatabaseReference permanentDataRef = rootRef
                .child("data").child(providerId)//providerID
                .child("permanent_data").push();

        HashMap<String, Object> mainMap = new HashMap<>();
        HashMap<String, Object> clientsMap = new HashMap<>();
        for (ConsumerModel consumerModel : mActiveRideArrayList) {
            String id = consumerModel.getId();
//            String time_stamp=//fixme include later if time and neded   //the time at which this stop was visited
            String name = consumerModel.getName();
            float amounOfMilk = consumerModel.getAmountOfMilk();
            boolean status = consumerModel.isDelivered();


            HashMap<String, Object> clientDatamap = new HashMap<>();
            clientDatamap.put("name", name);//client name

            HashMap<String, Object> goodsMap = new HashMap<>();
            for (AcquiredGoodModel acquiredGoodModel : consumerModel.getDemandArray()) {
//                String demandUnits=acquiredGoodModel.getDemand();
                GoodModel goodModel = acquiredGoodModel.getGoodModel();
                goodsMap.put(goodModel.getId(), acquiredGoodModel);
            }

//            clientDatamap.put("milk_amount", amounOfMilk);
            clientDatamap.put("goods", goodsMap);
            clientDatamap.put("status", status);

            clientsMap.put(id, clientDatamap);
        }
        mainMap.put("clients", clientsMap);
        mainMap.put("transporter_name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        mainMap.put("time_stamp", Calendar.getInstance().getTimeInMillis());//the time at ehich this session was put to an end
//        mainMap.put("milk_price", mMilkPrice); //adding price just because it can change from day to day
        mainMap.put("transporter_id", myID); //transporter id
        permanentDataRef.setValue(mainMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "session successfully uploaded to cloud database", Toast.LENGTH_SHORT).show();
                stopForeground(false);
                stopSelf();
            }
        });
    }


    private void initTextToSpeach() {
        textToSpeech = new TextToSpeech(this, i -> {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getApplicationContext(), "text to speech not working", Toast.LENGTH_SHORT).show();
//                        Log.d("mylog", "onInit: error in TEXT TO SPEECH");
                }

            } else {
                Toast.makeText(getApplicationContext(), "error in on init of text to speec", Toast.LENGTH_SHORT).show();
//                    Log.d("mylog", "onInit: error in TEXT TO SPEECH 1");
            }
        });
    }

    private void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }


    private NotificationCompat.Builder createNotification() {
        //creating an intent and passing it through a pending intent which will be called when notification is clicked
        Intent activityIntent = new Intent(this, TransporterMainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                this, //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);
        //creating a notification
        NotificationCompat.Builder downloadNotificationBuilder;
        downloadNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_add_white_24dp)
                .setContentTitle("Riding ")
                .setContentText("Riding...")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
//                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
//                .setProgress(100, 0, true)
                .setOnlyAlertOnce(true);
        return downloadNotificationBuilder;
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.removeLocationUpdates(mLocationcallback);
        }

        stopForeground(true); // then display another notification that tells that Ride was completed and is saved i history
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public ArrayList<ConsumerModel> getActiveRideArrayList() {
        return mActiveRideArrayList;
    }

    public class MyLocalBinder extends Binder {
        public RideService getService() {
            return RideService.this;
        }
    }


    public interface Callbacks {

        void updatePolyline(PolylineOptions currentStopPolylineOptions);

        void setDistanceTimeSpeed(String distance, String time, String speed);

        void abortJourney();

    }
}
