package com.techqamar.omkar_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.techqamar.omkar_project.models.PlacedOrderModel;
import com.techqamar.omkar_project.models.SingleProductModel;
import com.techqamar.omkar_project.networksync.CheckInternetConnection;
import com.techqamar.omkar_project.usersession.UserSession;
import com.whygraphics.multilineradiogroup.MultiLineRadioGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.techqamar.omkar_project.MapsActivityCurrentPlace.Address;
import static com.techqamar.omkar_project.MapsActivityCurrentPlace.MyPREFERENCES;

//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;

public class OrderDetails extends AppCompatActivity {

    @BindView(R.id.delivery_date)
    TextView deliveryDate;
    @BindView(R.id.no_of_items)
    TextView noOfItems;
    @BindView(R.id.total_amount)
    TextView totalAmount;
    @BindView(R.id.main_activity_multi_line_radio_group)
    MultiLineRadioGroup mainActivityMultiLineRadioGroup;
    @BindView(R.id.ordername)
    MaterialEditText ordername;
    @BindView(R.id.orderemail)
    MaterialEditText orderemail;
    @BindView(R.id.ordernumber)
    MaterialEditText ordernumber;
    @BindView(R.id.orderaddress)
    MaterialEditText orderaddress;
    @BindView(R.id.orderpincode)
    MaterialEditText orderpincode;
   // EditText ordername,orderemail,ordernumber;

    private ArrayList<SingleProductModel> cartcollect;
    private String payment_mode="COD",order_reference_id;
    private String placed_user_name,getPlaced_user_email,getPlaced_user_mobile_no;
    private UserSession session;
    private HashMap<String, String> user;
    private DatabaseReference mDatabaseReference;
    private String currdatetime;

    private String name, email,  mobile;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(Address)) {
            orderaddress.setText(sharedpreferences.getString(Address, ""));
        }

        getValues();

        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        //get User details if logged in
        user = session.getUserDetails();

        name = user.get(UserSession.KEY_NAME);
        email = user.get(UserSession.KEY_EMAIL);
        mobile = user.get(UserSession.KEY_MOBiLE);

//         ordername = (EditText)findViewById(R.id.ordername);

        ordername.setText(name);
        orderemail.setText(email);
        ordernumber.setText(mobile);



//         orderemail = (EditText)findViewById(R.id.orderemail);
//         ordernumber = (EditText)findViewById(R.id.ordernumber);


        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        productdetails();

    }

    private void productdetails() {

        Bundle bundle = getIntent().getExtras();

        //setting total price
        totalAmount.setText(bundle.get("totalprice").toString());

        //setting number of products
        noOfItems.setText(bundle.get("totalproducts").toString());

        cartcollect = (ArrayList<SingleProductModel>) bundle.get("cartproducts");

        //delivery date
        SimpleDateFormat formattedDate = new SimpleDateFormat("dd MMM, yyyy hh:mm aa");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 2);  // number of days to add
        String tomorrow = (formattedDate.format(c.getTime()));
        deliveryDate.setText(tomorrow);

        mainActivityMultiLineRadioGroup.setOnCheckedChangeListener(new MultiLineRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ViewGroup group, RadioButton button) {
                payment_mode=button.getText().toString();
            }
        });

        user = session.getUserDetails();

        placed_user_name=user.get(UserSession.KEY_NAME);
        getPlaced_user_email=user.get(UserSession.KEY_EMAIL);
        getPlaced_user_mobile_no=user.get(UserSession.KEY_MOBiLE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-hh-mm");
        currdatetime = sdf.format(new Date());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void PlaceOrder(View view) {

        if (validateFields(view)) {

            order_reference_id = getordernumber();

            //adding user details to the database under orders table

            String Key =  mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child(currdatetime).push().getKey();


            mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child(currdatetime).push().setValue(getProductObject());
            mDatabaseReference.child("orders_placed").child(Key).setValue(getProductObject());

            //adding products to the order
            for(SingleProductModel model:cartcollect){
          //      mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child(currdatetime).child("items").push().setValue(model);
                mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child("items").push().setValue(model);
                mDatabaseReference.child("orders_placed").child(Key).child("items").push().setValue(model);
            }



            mDatabaseReference.child("cart").child(getPlaced_user_mobile_no).removeValue();
            session.setCartValue(0);

            Intent intent = new Intent(OrderDetails.this, OrderPlaced.class);
            intent.putExtra("orderid",order_reference_id);
            startActivity(intent);
            finish();
        }
    }

    private void getValues() {

        //create new session object by passing application context
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        //get User details if logged in
        user = session.getUserDetails();


        name = user.get(UserSession.KEY_NAME);
        System.out.println("name: " + name);

       // name = ordername.getText().toString();

        email = user.get(UserSession.KEY_EMAIL);
        System.out.println("name: " + email);
       // email =  orderemail.getText().toString();
        mobile = user.get(UserSession.KEY_MOBiLE);
        System.out.println("name: " + mobile);
       // mobile =  ordernumber.getText().toString();


        // photo = user.get(UserSession.KEY_PHOTO);
    }


    private boolean validateFields(View view) {

        if (ordername.getText().toString().length() == 0 || orderemail.getText().toString().length() == 0 || ordernumber.getText().toString().length() == 0 || orderaddress.getText().toString().length() == 0 ||
                orderpincode.getText().toString().length() == 0) {
            Snackbar.make(view, "Kindly Fill all the fields", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return false;
        } else if (orderemail.getText().toString().length() < 4 || orderemail.getText().toString().length() > 30) {
            orderemail.setError("Email Must consist of 4 to 30 characters");
            return false;
        } else if (!orderemail.getText().toString().matches("^[A-za-z0-9.@]+")) {
            orderemail.setError("Only . and @ characters allowed");
            return false;
        } else if (!orderemail.getText().toString().contains("@") || !orderemail.getText().toString().contains(".")) {
            orderemail.setError("Email must contain @ and .");
            return false;
        } else if (ordernumber.getText().toString().length() < 4 || ordernumber.getText().toString().length() > 12) {
            ordernumber.setError("Number Must consist of 10 Digits");
            return false;
        } else if (orderpincode.getText().toString().length() < 6 || orderpincode.getText().toString().length() > 8){
            orderpincode.setError("Pincode must be of 6 digits");
            return false;
        }

        return true;
    }

    public Object getProductObject() {
        return new PlacedOrderModel(order_reference_id,noOfItems.getText().toString(),totalAmount.getText().toString(),deliveryDate.getText().toString(),payment_mode,ordername.getText().toString(),orderemail.getText().toString(),ordernumber.getText().toString(),orderaddress.getText().toString(),orderpincode.getText().toString(),placed_user_name,getPlaced_user_email,getPlaced_user_mobile_no);
    }

    public String getordernumber() {

        return currdatetime.replaceAll("-","");
    }
}
