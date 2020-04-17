package it.polito.mad.mhackeroni

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern

class Validation {
    companion object {

        val isValidEmail: (CharSequence?) -> Boolean = { target ->
            target != null && !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target)
                .matches()
        }

        val isValidNickname: (CharSequence?) -> Boolean = { target ->
            target != null && !TextUtils.isEmpty(target) &&
                    Pattern.compile("^(?=.{5,20}\$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])")
                        .matcher(target).matches()
        }

        val isValidPhoneNumber: (CharSequence?) -> Boolean = { target ->
            target != null && Pattern.compile("^[+]?[0-9]{10,13}\$").matcher(target).matches()
        }

        val isValidLocation: (CharSequence?) -> Boolean = { target ->
            target != null && target.length >= 2 && target.length <= 64
        }
    }
}