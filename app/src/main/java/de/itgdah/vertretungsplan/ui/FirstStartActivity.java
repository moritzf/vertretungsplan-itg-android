package de.itgdah.vertretungsplan.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.web.LoginConstants;

public class FirstStartActivity extends FragmentActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firststart_activity);

        getFragmentManager().beginTransaction().replace(R.id.firststart_container,new FirstStart_Welcome()).commit();

    }

    public static class FirstStart_Welcome extends Fragment{

        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            Button next=(Button) getView().findViewById(R.id.firststart_welcome);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().beginTransaction().replace(R.id.firststart_container, new FirstStart_Login()).commit();
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.firststart_welcome, container, false);
            return rootView;

        }

    }

    public static class FirstStart_Login extends Fragment{

        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);

            Button login=(Button) getView().findViewById(R.id.firststart_login);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText username=(EditText) getView().findViewById(R.id.firststart_username);
                    EditText password=(EditText) getView().findViewById(R.id.firststart_password);
                    String usernameValue=username.getText().toString();
                    String passwordValue=password.getText().toString();

                    if ( usernameValue.equals(LoginConstants.USER_USERNAME)
                      && passwordValue.equals(LoginConstants.USER_PASSWORD) ){

                        SharedPreferences.Editor editor= getActivity().getSharedPreferences("app_activation",MODE_PRIVATE).edit();
                        editor.putBoolean("app_activated",true);
                        editor.commit();

                        Intent intent=new Intent(getActivity().getApplicationContext(),MyVertretungsplanActivity.class);
                        startActivity(intent);
                    }else{
                        TextView errorText=(TextView) getView().findViewById(R.id.firststart_error);
                        errorText.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.firststart_login, container, false);
            return rootView;

        }

    }

}
