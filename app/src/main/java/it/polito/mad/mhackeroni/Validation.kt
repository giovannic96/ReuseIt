package it.polito.mad.mhackeroni

import android.text.InputFilter
import android.text.Spanned
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

    class DecimalDigitsInputFilter(digitsBeforeZero: Int?, digitsAfterZero: Int?) :
        InputFilter {
        private val mDigitsBeforeZero: Int
        private val mDigitsAfterZero: Int
        private val mPattern: Pattern
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val replacement = source.subSequence(start, end).toString()
            val newVal = (dest.subSequence(0, dstart).toString() + replacement
                    + dest.subSequence(dend, dest.length).toString())
            val matcher = mPattern.matcher(newVal)
            if (matcher.matches()) return null
            return if (TextUtils.isEmpty(source)) dest.subSequence(dstart, dend) else ""
        }

        companion object {
            private const val DIGITS_BEFORE_ZERO_DEFAULT = 100
            private const val DIGITS_AFTER_ZERO_DEFAULT = 100
        }

        init {
            mDigitsBeforeZero =
                digitsBeforeZero ?: DIGITS_BEFORE_ZERO_DEFAULT
            mDigitsAfterZero =
                digitsAfterZero ?: DIGITS_AFTER_ZERO_DEFAULT
            mPattern = Pattern.compile(
                "-?[0-9]{0," + mDigitsBeforeZero + "}+((\\.[0-9]{0," + mDigitsAfterZero
                        + "})?)||(\\.)?"
            )
        }
    }
}