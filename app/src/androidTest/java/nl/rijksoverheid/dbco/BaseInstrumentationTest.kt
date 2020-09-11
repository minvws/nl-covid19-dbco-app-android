package nl.rijksoverheid.dbco

import com.microsoft.appcenter.espresso.Factory
import com.microsoft.appcenter.espresso.ReportHelper
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseInstrumentationTest {
    @JvmField
    @Rule
    val reportHelper: ReportHelper = Factory.getReportHelper()

    @Before
    open fun setup(){
        reportHelper.label("Starting test for ${javaClass.kotlin}")
    }

    @After
    open fun tearDown() {
        reportHelper.label("End of test")
    }
}