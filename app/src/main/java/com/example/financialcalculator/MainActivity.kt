package com.example.financialcalculator

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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
    private lateinit var c: BigDecimal
    private lateinit var d: BigDecimal

    private val maxValue: BigDecimal = BigDecimal("1000000000000.000000")

    private fun hasOverflow(vararg num: BigDecimal): Boolean {
        for (n in num) {
            if (n.abs() > maxValue) {
                return true
            }
        }
        return false
    }


    private fun argPreprocess(edit: EditText): String {
        return edit.text.toString().trim().replace(',', '.')
    }

    private val argPattern =
        Pattern.compile("([+-])?((\\d{1,3}( \\d{3})*(\\.\\d{0,6})?)|(\\.\\d{1,6}))")

    private fun argCheckPattern(vararg args: String): Boolean {
        for (arg in args) {
            if (arg.contains(" ") && !argPattern.matcher(arg).matches()) {
                return false
            }
        }
        return true
    }

    private fun isAnyEmpty(vararg args: String): Boolean {
        for (s in args) {
            if (s.isEmpty()) {
                return true
            }
        }
        return false
    }

    private fun removeSpaces(s: String): String {
        return s.replace(" ", "")
    }

    private fun initializeVars(): Boolean {
        var first = argPreprocess(edtArg1)
        var second = argPreprocess(edtArg2)
        var third = argPreprocess(edtArg3)
        var fourth = argPreprocess(edtArg4)

        val arguments = arrayOf(first, second, third, fourth)

        if (isAnyEmpty(*arguments)) {
            setResult(getString(R.string.input_empty))
            return false
        }

        if (!argCheckPattern(*arguments)) {
            setResult(getString(R.string.input_err)) // + " - pattern!!!"
            return false
        } else {
            first = removeSpaces(first)
            second = removeSpaces(second)
            third = removeSpaces(third)
            fourth = removeSpaces(fourth)
        }

        try {
            a = BigDecimal(first).setScale(10, RoundingMode.HALF_UP)
            b = BigDecimal(second).setScale(10, RoundingMode.HALF_UP)
            c = BigDecimal(third).setScale(10, RoundingMode.HALF_UP)
            d = BigDecimal(fourth).setScale(10, RoundingMode.HALF_UP)

        } catch (e: Exception) {
            setResult(getString(R.string.input_err))
            return false
        }

        if (hasOverflow(a, b, c, d)) {
            setResult(getString(R.string.input_overflow))
            return false
        }

        return true
    }

    private fun isFirstPriority(): Boolean {
        return spOps1.selectedItemPosition > spOps3.selectedItemPosition
    }

    private fun setResult(s: String, clearRounded: Boolean = true) {
        twAnswer.text = s
        if (clearRounded) {
            twAnswerRounded.text = ""
        }
    }

    private fun setResult(ans: BigDecimal) {
        twAnswer.text = decimalFormat.format(ans)
        twAnswerRounded.text = decimalFormatRounded.format(ans)
    }


    private fun operate(first: BigDecimal, second: BigDecimal, operation: Int): BigDecimal {
        return when (operation) {
            0 -> first + second
            1 -> first - second
            2 -> {
                if (second.compareTo(BigDecimal.ZERO) == 0) {
                    setResult(getString(R.string.err_div_by_zero))
                    throw Exception(getString(R.string.err_div_by_zero))
                } else {
                    first / second
                }
            }
            3 -> first * second
            else -> throw Exception("Wrong operation code!")
        }
    }

    private fun btnResClickHandler(v: View) {
        if (initializeVars()) {
            val bc: BigDecimal
            try {
                bc = operate(b, c, spOps2.selectedItemPosition)
            } catch (e: Exception) {
                return
            }

            val abcd: BigDecimal
            if (isFirstPriority()) {
                val abc: BigDecimal
                try {
                    abc = operate(a, bc, spOps1.selectedItemPosition)
                    abcd = operate(abc, d, spOps3.selectedItemPosition)
                } catch (e: Exception) {
                    return
                }
            } else {
                val bcd: BigDecimal
                try {
                    bcd = operate(bc, d, spOps3.selectedItemPosition)
                    abcd = operate(a, bcd, spOps1.selectedItemPosition)
                } catch (e: Exception) {
                    return
                }
            }

            if (hasOverflow(abcd)) {
                setResult(getString(R.string.res_overflow))
                return
            }

            setResult(abcd)
        }
    }


    private val decimalFormat = DecimalFormat()
    private val decimalFormatRounded = DecimalFormat()

    private fun initializeDecimalFormat() {
        val dfs = DecimalFormatSymbols(Locale.getDefault())
        dfs.decimalSeparator = '.'

        decimalFormat.maximumFractionDigits = 6
        decimalFormat.minimumFractionDigits = 0
        decimalFormat.isGroupingUsed = true
        decimalFormat.roundingMode = RoundingMode.HALF_UP
        decimalFormat.decimalFormatSymbols = dfs

        decimalFormatRounded.maximumFractionDigits = 0
        decimalFormatRounded.isGroupingUsed = true
        decimalFormat.roundingMode = RoundingMode.HALF_UP
        decimalFormatRounded.decimalFormatSymbols = dfs
    }

    private fun changeRoundingMode(position: Int) {
        decimalFormatRounded.roundingMode =
            when (position) {
                0 -> RoundingMode.HALF_UP
                1 -> RoundingMode.HALF_EVEN
                2 -> RoundingMode.DOWN
                else -> RoundingMode.UNNECESSARY
            }
        btnResClickHandler(btnRes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeDecimalFormat()

        spRounding.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                changeRoundingMode(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnRes.setOnClickListener(this::btnResClickHandler)
    }
}
