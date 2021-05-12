package ro.example.chaty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    EditText email, password;
    Button signIn;
    TextView recoverPass;
    FirebaseAuth auth;
    ProgressDialog progressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = findViewById(R.id.topbar_signin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFloatingLabelError();

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signIn = findViewById(R.id.signin_btn);
        recoverPass = findViewById(R.id.recoverpass);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(SignInActivity.this, "All fields must be completed!",Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Authentication Failed!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        recoverPass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showRecoverPassDialog();
            }
        });

        progressdialog = new ProgressDialog(this);

    }



    private void setupFloatingLabelError() {
        final TextInputLayout floatingPasswordLabel =  findViewById(R.id.signin_password_input_layout);
        Objects.requireNonNull(floatingPasswordLabel.getEditText()).addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                if (text.length() > 0 && text.length() <= 7) {
                    floatingPasswordLabel.setError(getString(R.string.password_length));
                    floatingPasswordLabel.setErrorEnabled(true);
                } else {
                    floatingPasswordLabel.setErrorEnabled(false);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showRecoverPassDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover password");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailinput = new EditText(this);
        emailinput.setHint("Email");
        emailinput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailinput.setMinEms(16);

        linearLayout.addView(emailinput);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String email = emailinput.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email){
        progressdialog.setMessage("Sending email ...");
        progressdialog.show();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressdialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(SignInActivity.this,"Email sent", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SignInActivity.this,"Failed..", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressdialog.dismiss();
                Toast.makeText(SignInActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}