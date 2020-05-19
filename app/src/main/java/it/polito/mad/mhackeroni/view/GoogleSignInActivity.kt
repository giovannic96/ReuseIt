package it.polito.mad.mhackeroni.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import it.polito.mad.mhackeroni.R
import it.polito.mad.mhackeroni.model.Profile
import it.polito.mad.mhackeroni.utilities.FirebaseRepo
import kotlinx.android.synthetic.main.google_signin.*


class GoogleSignInActivity : AppCompatActivity(), View.OnClickListener {

    private val RC_SIGN_IN = 9010
    private lateinit var mGoogleSignInClient:GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_signin)

        val isLogout = intent.getBooleanExtra(getString(R.string.logout_title), false)

        sign_in_button.setSize(SignInButton.SIZE_STANDARD)
        sign_in_button.setOnClickListener(this)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Do the logout if we pressed Logout button
        if(isLogout) {
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    Toast.makeText(applicationContext, R.string.logout_success, Toast.LENGTH_LONG).show()
                }
        }
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        val account = auth.currentUser
        updateUI(account)
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user != null) { //the user has already signed in to your app with Google.
            FirebaseRepo.INSTANCE.isLogged = true
            var name: String?
            var email:String?
            var photoUrl: Uri?
            var emailVerified: Boolean
            var uid: String

            FirebaseRepo.INSTANCE.setProfile(Profile(), user.uid)

            user.let {
                // Name, email address, and profile photo Url
                name = user.displayName
                email = user.email
                photoUrl = user.photoUrl

                // Check if user's email is verified
                emailVerified = user.isEmailVerified

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getToken() instead.
                uid = user.uid
            }

            sharedPref = applicationContext.getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(getString(R.string.uid), uid)
                commit()
            }

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        //else //the user has not yet signed in to your app with Google
            //TODO()
    }

    override fun onClick(v: View) {
        val id = sign_in_button.id
        when (v.id) {
            id -> signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!) // Signed in successfully, show authenticated UI.
            FirebaseRepo.INSTANCE.isLogged = true
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            updateUI(null)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }
}
