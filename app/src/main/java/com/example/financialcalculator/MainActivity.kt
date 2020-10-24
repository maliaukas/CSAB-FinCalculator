package com.example.financialcalculator

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private lateinit var a: BigDecimal
    private lateinit var b: BigDecimal

    private val maxValue: BigDecimal = BigDecimal("1000000000000.000000")

    private fun checkOverflow(num: BigDecimal): Boolean {
        return num.abs() < maxValue
    }

    private val argPattern =
        Pattern.compile("([+-])?((\\d{1,3}( \\d{3})*(\\.\\d{0,6})?)|(\\.\\d{1,6}))")

    private fun argPreprocess(edit: EditText): String {
        return edit.text.toString().trim().replace(',', '.')
    }

    private fun argCheckPattern(arg: String): Boolean {
        if (arg.contains(" ") && !argPattern.matcher(arg).matches()) {
            return false
        }
        return true
    }

    private fun initializeVars(): Boolean {
        var first = argPreprocess(edtArg1)
        var second = argPreprocess(edtArg2)

        if (first.isEmpty() || second.isEmpty()) {
            twAnswer.text = getString(R.string.input_empty)
            return false
        }

        if (!argCheckPattern(first) || !argCheckPattern(second)) {
            twAnswer.text = getString(R.string.input_err) // + " - pattern!!!"
            return false
        } else {
            first = first.replace(" ", "")
            second = second.replace(" ", "")
        }

        try {
            a = BigDecimal(first).setScale(6)
            b = BigDecimal(second).setScale(6)

        } catch (e: Exception) {
            twAnswer.text = getString(R.string.input_err)
            return false
        }

        if (!checkOverflow(a) || !checkOverflow(b)) {
            twAnswer.text = getString(R.string.input_overflow)
            return false
        }

        return true
    }

    private fun buttonClickHandler(v: View) {
        if (initializeVars()) {
            val result = when (v.id) {
                R.id.btnSub -> a - b
                R.id.btnAdd -> a + b
                R.id.btnMul -> a * b
                R.id.btnDiv -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        twAnswer.text = getString(R.string.err_div_by_zero)
                        return
                    } else {
                        a / b
                    }
                }
                else -> null
            }

            if (!checkOverflow(result!!)) {
                twAnswer.text = getString(R.string.res_overflow)
                return
            }

            val dfs = DecimalFormatSymbols(Locale.getDefault())
            dfs.decimalSeparator = '.'

            val df = DecimalFormat()
            df.maximumFractionDigits = 6
            df.minimumFractionDigits = 0
            df.isGroupingUsed = true
            df.roundingMode = RoundingMode.HALF_UP
            df.decimalFormatSymbols = dfs

            twAnswer.text = df.format(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAdd.setOnClickListener(this::buttonClickHandler)
        btnSub.setOnClickListener(this::buttonClickHandler)
        btnMul.setOnClickListener(this::buttonClickHandler)
        btnDiv.setOnClickListener(this::buttonClickHandler)
    }

}
