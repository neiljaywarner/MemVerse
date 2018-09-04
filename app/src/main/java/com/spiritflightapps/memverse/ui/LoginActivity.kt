package com.spiritflightapps.memverse.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.hawk.Hawk
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.network.BearerTokenResponse
import com.spiritflightapps.memverse.network.PasswordTokenRequest
import com.spiritflightapps.memverse.network.ServiceGenerator
import com.spiritflightapps.memverse.network.TwitterAuthUtils
import com.spiritflightapps.memverse.utils.Prefs
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A login screen that offers login via email/password.
 */

class LoginActivity : AppCompatActivity() {
    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // put in baseactivity or mainapplication
    // TODO: Abstract this out into helper class so we can have multiple analytics
    // is easy to have BaseActivity that
    private val mFirebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    private fun checkForTestDevice() {
        if (isTestDevice()) {
            mFirebaseAnalytics.setUserProperty("TestDevice", "True")
            mFirebaseAnalytics.logEvent("test_device_start", Bundle())
        } else {
            mFirebaseAnalytics.setUserProperty("TestDevice", "False")
        }
    }

    private fun isTestDevice(): Boolean {
        val testLabSetting = Settings.System.getString(contentResolver, "firebase.test.lab")
        return "true" == testLabSetting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: Move to mainapplication
        Hawk.init(applicationContext).build()

        checkForTestDevice()

        val authToken = Hawk.get(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, "")
        if (authToken.isNotBlank()) {
            ServiceGenerator.setPasswordAuthToken(authToken)
            trackAutoLogin()
            startActivity(intentFor<MainActivity>())
            finish()
            return
        }
        setContentView(R.layout.activity_login)

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        button_signin.setOnClickListener { attemptLogin() }

        button_signup.setOnClickListener {
            trackSignup()
            browse("https://www.memverse.com/users/sign_up")
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {


        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true)
            // Use this progress when login is going via retrofit call
            showProgress(true)
            login(emailStr.trim(), passwordStr)
        }
    }


    private fun login(email: String, password: String) {
        Log.d(TAG, "*** Retrieving bearer token.")
        val memVerseApi = ServiceGenerator.createBearerKeyService(
                TwitterAuthUtils.generateEncodedBearerTokenCredentials())

        val passwordTokenRequest = PasswordTokenRequest(username = email, password = password)
        val bearerTokenCall = memVerseApi.getBearerToken(passwordTokenRequest)
        // TODO: Consider https://auth0.com/docs/api-auth/grant/authorization-code-pkce

        // also consider https://github.com/openid/AppAuth-Android
        // also consider https://github.com/auth0/Auth0.Android


        bearerTokenCall.enqueue(object : Callback<BearerTokenResponse> {
            override fun onResponse(call: Call<BearerTokenResponse>, response: Response<BearerTokenResponse>) {
                Log.d(TAG, "bearerTokenCall:Response code: ${response.code()}")
                showProgress(false)

                if (response.isSuccessful) {
                    val bearerTokenResponse = response.body()

                    Log.d(TAG, "bearerTokenCall:token_type=" + bearerTokenResponse!!.tokenType)
                    val authToken = bearerTokenResponse.accessToken
                    ServiceGenerator.setPasswordAuthToken(authToken)

                    Hawk.put(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, authToken)
                    Prefs.saveToPrefs(this@LoginActivity, Prefs.EMAIL, email)
                    trackLoginSuccess()
                    startActivity(intentFor<MainActivity>())
                } else {
                    trackLoginNot200()
                    //TODO: Consider reporting to analytics if this happens and user has connectivity, not airplane mode, etc.
                    Toast.makeText(this@LoginActivity, "sorry, something went wrong with network call; please try again ", Toast.LENGTH_LONG).show()

                    Log.e(TAG, "Response invalid, check consumer key/secret combination if 403")
                    Crashlytics.logException(Exception("Login fail: code=${response.code()}"))
                }


            }

            override fun onFailure(call: Call<BearerTokenResponse>, t: Throwable) {
                trackLoginFail()
                Crashlytics.log("login call onFailure; could be bad username/pass ${call.request()} ${t.message}")
                showProgress(false)
                Snackbar.make(button_signin, "Can't login with these credentials; please check username/password combination.", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun trackLoginSuccess() {
        val bundle = Bundle()
        //todo: use bundleOf in ktx
        bundle.putString(FirebaseAnalytics.Param.METHOD, "regular")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    private fun trackAutoLogin() {
        mFirebaseAnalytics.logEvent("autologin", Bundle())
    }

    private fun trackLoginNot200() {
        mFirebaseAnalytics.logEvent("login_fail_not_200", Bundle())
    }

    private fun trackLoginFail() {
        mFirebaseAnalytics.logEvent("login_fail", Bundle())
    }

    private fun trackSignup() {
        mFirebaseAnalytics.logEvent("clicked_signup_link", Bundle())
    }



    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic, such as what's build into android pattern
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 6
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {

        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }


}
