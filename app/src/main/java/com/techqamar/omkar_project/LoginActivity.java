package com.techqamar.omkar_project;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
//import android.support.v4.content.res.ResourcesCompat;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.techqamar.omkar_project.GoogleMap.MapsActivity;
import com.techqamar.omkar_project.Modules.userdata;
import com.techqamar.omkar_project.networksync.CheckInternetConnection;
import com.techqamar.omkar_project.networksync.LoginRequest;
import com.techqamar.omkar_project.usersession.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;


public class LoginActivity extends AppCompatActivity {

    private EditText edtemail,edtpass;
    private String email,pass,sessionmobile,sessionname,sessionemail;
    private TextView appname,forgotpass,registernow;
    private RequestQueue requestQueue;
    private UserSession session;
    public static final String TAG = "MyTag";
    private int cartcount, wishlistcount;
   
    public String uid;
    //firebase
    private FirebaseAuth auth;
    private FirebaseUser user;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mDatabaseReference ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();



        if (null != auth.getCurrentUser()) {
            startActivity(new Intent(LoginActivity.this, MapsActivityCurrentPlace.class));
            finish();
        }


        Log.e("Login CheckPoint","LoginActivity started");
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

//        Typeface typeface = ResourcesCompat.getFont(this, R.font.app_font);
//        appname = findViewById(R.id.appname);
//        appname.setTypeface(typeface);

        edtemail= findViewById(R.id.email);
        edtpass= findViewById(R.id.password);

        Bundle registerinfo=getIntent().getExtras();
        if (registerinfo!=null) {
                edtemail.setText(registerinfo.getString("email"));
        }

        session= new UserSession(getApplicationContext());

        requestQueue = Volley.newRequestQueue(LoginActivity.this);//Creating the RequestQueue

        //if user wants to register
        registernow= findViewById(R.id.register_now);
        registernow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,Register.class));
                finish();
            }
        });

        //if user forgets password
        forgotpass=findViewById(R.id.forgot_pass);
//        forgotpass.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                startActivity(new Intent(LoginActivity.this,ForgotPassword.class));
//            }
//        });


        //Validating login details
        Button button=findViewById(R.id.login_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email=edtemail.getText().toString();
                System.out.println("name: " +email);

                pass=edtpass.getText().toString();
                System.out.println("name: " +pass);

                if (validateUsername(email) && validatePassword(pass)) { //Username and Password Validation

                    //Progress Bar while connection establishes

                          final KProgressHUD progressDialog=  KProgressHUD.create(LoginActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Please wait")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();


                    //authenticate user
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.

                                    if (!task.isSuccessful()) {
                                        // there was an error
                                        if (pass.length() < 8) {
                                            edtpass.setError(getString(R.string.minimum_password));
                                        } else {
                                            Toasty.error(LoginActivity.this, "Bad Response From Server", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {


                                        uid = auth.getInstance().getCurrentUser().getUid();
                                        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                                        System.out.println("name: onDataChange else" );


                                        ValueEventListener postListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                System.out.println("name: onDataChange");

                                                 sessionname = dataSnapshot.child("name").getValue(String.class);
                                                System.out.println("name: " +sessionname);
                                                sessionmobile = dataSnapshot.child("mobile").getValue(String.class);
                                                System.out.println("name: " +sessionmobile);
                                                sessionemail = dataSnapshot.child("email").getValue(String.class);
                                                System.out.println("name: " +sessionemail);


//                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                                                    sessionname = ds.child("name").getValue(String.class);
//
//                                                    sessionmobile = ds.child("mobile").getValue(String.class);
//
//                                                    sessionemail = ds.child("email").getValue(String.class);
//
//
//                                                    Log.d("TAG", sessionemail + " / " + sessionname + "/" + sessionmobile);
//
//                                                }
                                                Log.d("TAG", sessionemail + " / " + sessionname + "/" + sessionmobile);
//                                                // Get Post object and use the values to update the UI
//                                                userdata user = dataSnapshot.getValue(userdata.class);
//                                                // [START_EXCLUDE]
//
//                                                String sessionname=user.name;
//                                                System.out.println("name: " +sessionname);
//                                                String sessionmobile=user.mobile;
//                                                System.out.println("name: " +sessionmobile);
//                                                String sessionemail=user.email;
//                                                System.out.println("name: " +sessionemail);
                                                // [END_EXCLUDE]
                                                //create shared preference and store data

                                                session.createLoginSession(sessionname, sessionemail, sessionmobile);
                                                countFirebaseValues();


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // Getting Post failed, log a message
                                                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                                // [START_EXCLUDE]
                                                Toast.makeText(LoginActivity.this, "Failed to load post.",
                                                        Toast.LENGTH_SHORT).show();
                                                // [END_EXCLUDE]
                                            }
                                        };
                                        mDatabaseReference.addListenerForSingleValueEvent(postListener);

                                        Toasty.success(LoginActivity.this, " LOG IN SUCCESS FULL!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });


//                    LoginRequest loginRequest = new LoginRequest(email, pass, new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//
//                            progressDialog.dismiss();
//                            // Response from the server is in the form if a JSON, so we need a JSON Object
//                            try {
//                                JSONObject jsonObject = new JSONObject(response);
//                                if (jsonObject.getBoolean("success")) {
//
//                                    //Passing all received data from server to next activity
//                                    String sessionname = jsonObject.getString("name");
//                                    sessionmobile = jsonObject.getString("mobile");
//                                    String sessionemail =  jsonObject.getString("email");
//                                    String sessionphoto =  jsonObject.getString("url");
//
//                                    //create shared preference and store data
//                                    session.createLoginSession(sessionname,sessionemail,sessionmobile,sessionphoto);
//
//                                    //count value of firebase cart and wishlist
//                                    countFirebaseValues();
//
//                                    Intent loginSuccess = new Intent(LoginActivity.this, MainActivity.class);
//                                    startActivity(loginSuccess);
//                                    finish();
//                                } else {
//                                    if(jsonObject.getString("status").equals("INVALID"))
//                                        Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
//                                    else{
//                                        Toast.makeText(LoginActivity.this, "Passwords Don't Match", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                Toast.makeText(LoginActivity.this, "Bad Response From Server", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            progressDialog.dismiss();
//                            if (error instanceof ServerError)
//                                Toast.makeText(LoginActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
//                            else if (error instanceof TimeoutError)
//                                Toast.makeText(LoginActivity.this, "Connection Timed Out", Toast.LENGTH_SHORT).show();
//                            else if (error instanceof NetworkError)
//                                Toast.makeText(LoginActivity.this, "Bad Network Connection", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    loginRequest.setTag(TAG);
//                    requestQueue.add(loginRequest);
                }

            }
        });


    }

    private void countFirebaseValues() {

        mDatabaseReference.child("cart").child(sessionmobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "");
                    session.setCartValue((int)dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child("wishlist").child(sessionmobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "");
                session.setWishlistValue((int)dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean validatePassword(String pass) {


        if (pass.length() < 4 || pass.length() > 20) {
            edtpass.setError("Password Must consist of 4 to 20 characters");
            return false;
        }
        return true;
    }

    private boolean validateUsername(String email) {

        if (email.length() < 4 || email.length() > 30) {
            edtemail.setError("Email Must consist of 4 to 30 characters");
            return false;
        } else if (!email.matches("^[A-za-z0-9.@]+")) {
            edtemail.setError("Only . and @ characters allowed");
            return false;
        } else if (!email.contains("@") || !email.contains(".")) {
            edtemail.setError("Email must contain @ and .");
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Login CheckPoint","LoginActivity resumed");
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        }

    @Override
    protected void onStop () {
        super.onStop();
        Log.e("Login CheckPoint","LoginActivity stopped");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
