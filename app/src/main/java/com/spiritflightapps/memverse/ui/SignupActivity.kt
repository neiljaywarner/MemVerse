package com.spiritflightapps.memverse.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.spiritflightapps.memverse.R
import com.spiritflightapps.memverse.network.*
import com.spiritflightapps.memverse.utils.Analytics
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.okButton
import org.jetbrains.anko.startActivity
import retrofit2.HttpException

class SignupActivity : AppCompatActivity() {

    private val memverseBearerDeferredApi: MemverseApi by lazy {
        ServiceGenerator.createBearerKeyDeferredService(
                TwitterAuthUtils.generateEncodedBearerTokenCredentials())
    }

    companion object {
        private val TAG = SignupActivity::class.java.simpleName
        const val EXTRA_EMAIL = "extra_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptSignup()
                return@OnEditorActionListener true
            }
            false
        })

        button_signup.setOnClickListener {
            attemptSignup()
        }

        if (intent.hasExtra(EXTRA_EMAIL)) {
            email.setText(intent.getStringExtra(EXTRA_EMAIL))
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptSignup() {
        Analytics.trackEvent("attempt_sigup")

        // Reset errors.
        email.error = null
        password.error = null
        name.error = null


        // Store values at the time of the login attempt.
        val nameStr = name.text.toString().trim()
        val emailStr = email.text.toString().trim()
        val passwordStr = password.text.toString()
        val passwordConfirmationStr = confirm_password.text.toString()

        var cancel = false
        var focusView: View? = null


        if (nameStr.isBlank()) {
            password.error = getString(R.string.error_invalid_name)
            focusView = name
            cancel = true
        }

        if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        if (!isPasswordValid(passwordConfirmationStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = confirm_password
            cancel = true
        }

        if (passwordStr != passwordConfirmationStr) {
            password.error = getString(R.string.error_password_and_confirm_must_match)
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
            signupAsync(nameStr, emailStr, passwordStr)
        }

    }

    //https://firebase.google.com/docs/crashlytics/customize-crash-reports?authuser=0

    private fun trackSignupSuccess() {
        Analytics.trackEvent(FirebaseAnalytics.Event.SIGN_UP)
        Analytics.addUserProperty(FirebaseAnalytics.UserProperty.SIGN_UP_METHOD, "android_app")
        Crashlytics.setString("signup_method", "android_app")
    }

    //todo: https://developer.android.com/reference/android/util/Patterns
    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String) = password.length > 5

    //*** ** testing signin
    private fun signupAsync(name: String, email: String, password: String) = launch(UI) {
        val progress = indeterminateProgressDialog(message = "Please wait...")
        progress.show()
        try {

            Crashlytics.log("About to do signup api call")
            Log.d(TAG, "in signupasync, about to do api call")

            val testUserRequest = RegisterUserRequest(RegisterUser(name, email, password))
            val result = memverseBearerDeferredApi.signup(testUserRequest).await()
            Log.d(TAG, "signupresult = $result")
            trackSignupSuccess()
            alert("Signup success!  Please check your email and confirm email address then login")
            {
                okButton { startActivity<LoginActivity>(LoginActivity.EXTRA_EMAIL to email) }
            }.show()
        } catch (httpException: HttpException) {
            Log.e(TAG, "httpException in Signup - code=${httpException.code()}; msg = ${httpException.message()}")
            Crashlytics.log("httpException in Signup - code=${httpException.code()}; msg = ${httpException.message()}\"")
            Crashlytics.logException(httpException)
            alert("Sorry, there was an error signing up, please email njwandroid@gmail.com or try again later") { okButton { } }.show()
        } catch (exception: Exception) {
            Log.e(TAG, "exception = ${exception.message}")
            alert("Sorry, there was an error signing up, please email njwandroid@gmail.com or try again later") { okButton { } }.show()
            Crashlytics.logException(exception)
        } finally {
            progress.hide()
        }
        //TODO: Fix me, get support address someday, ideally android-app@memverse.com which can forward to doorbell feedback address.
    }

}
