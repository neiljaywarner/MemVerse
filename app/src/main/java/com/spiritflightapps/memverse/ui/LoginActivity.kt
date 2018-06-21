package com.spiritflightapps.memverse.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.network.BearerTokenResponse
import com.spiritflightapps.memverse.network.PasswordTokenRequest
import com.spiritflightapps.memverse.network.ServiceGenerator
import com.spiritflightapps.memverse.network.TwitterAuthUtils
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.defaultSharedPreferences
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authToken = defaultSharedPreferences.getString(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, "")
        if (authToken.isBlank()) {
            ServiceGenerator.setPasswordAuthToken(authToken)

            // TODO: newIntent pattern
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
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
        val twitterApi = ServiceGenerator.createBearerKeyService(
                TwitterAuthUtils.generateEncodedBearerTokenCredentials())

        val passwordTokenRequest = PasswordTokenRequest(username = email, password = password)
        val bearerTokenCall = twitterApi.getBearerToken(passwordTokenRequest)
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
                    ServiceGenerator.AUTH_TOKEN_PREFS_KEY

                    defaultSharedPreferences.edit().apply {
                        putString(ServiceGenerator.AUTH_TOKEN_PREFS_KEY, authToken)
                        apply()
                    }
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)


                } else {
                    Toast.makeText(this@LoginActivity, "sorry, something went wrong with network call; please try again ", Toast.LENGTH_LONG).show()

                    Log.e(TAG, "Response invalid, check consumer key/secret combination if 403")
                }


            }

            override fun onFailure(call: Call<BearerTokenResponse>, t: Throwable) {
                Log.e(TAG, "bearerTokenCall Failure:${call.request()} ${t.message}")

                showProgress(false)
                Snackbar.make(button_signin, "Can't login with these credentials; please check username/password combination.", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic, such as what's build into android pattern
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
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
