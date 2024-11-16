// TODO Save this code to be used in other source code
// TODO Add associativity between time unit and time object
// TODO Figure out any potential problems caused by time zones

import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

class TimeCategory8 {
    static void use(Closure closure) {
        def originalMetaClasses = [:]

        // Save original metaClasses for restoration later
        saveMetaClass(Integer, originalMetaClasses)
        saveMetaClass(Long, originalMetaClasses)
        saveMetaClass(LocalDateTime, originalMetaClasses)
        saveMetaClass(LocalDate, originalMetaClasses)
        saveMetaClass(LocalTime, originalMetaClasses)

        // Add custom properties to Integer/Long for time units
        enhanceIntegerAndLong()
        enhanceTimeClasses()

        try {
            closure()
        } finally {
            // Restore original metaClasses
            originalMetaClasses.each { clazz, metaClass ->
                GroovySystem.metaClassRegistry.setMetaClass(clazz, metaClass)
            }
        }
    }

    private static void enhanceIntegerAndLong() {
        def timeUnits = [
            "seconds": ChronoUnit.SECONDS,
            "minutes": ChronoUnit.MINUTES,
            "hours": ChronoUnit.HOURS,
            "days": ChronoUnit.DAYS,
            "weeks": ChronoUnit.WEEKS,
            "months": ChronoUnit.MONTHS,
            "years": ChronoUnit.YEARS
        ]

        // Add time unit properties to Integer and Long
        [Integer, Long].each { numberClass ->
            timeUnits.each { unitName, chronoUnit ->
                numberClass.metaClass."get${unitName.capitalize()}" = {
                    new TemporalUnitWrapper(delegate as int, chronoUnit)
                }
            }
        }
    }

    private static void enhanceTimeClasses() {
        // Enhance LocalDateTime, LocalDate, and LocalTime to support addition and subtraction
        [LocalDateTime, LocalDate, LocalTime].each { temporalClass ->
            temporalClass.metaClass.plus = { TemporalUnitWrapper wrapper ->
                delegate.plus(wrapper.amount, wrapper.unit)
            }
            temporalClass.metaClass.minus = { TemporalUnitWrapper wrapper ->
                delegate.minus(wrapper.amount, wrapper.unit)
            }
        }

        
        // Add reverse operations to TemporalUnitWrapper
        TemporalUnitWrapper.metaClass.plus = { Temporal temporal ->
            temporal.plus(delegate.amount, delegate.unit)
        }
        TemporalUnitWrapper.metaClass.minus = { Temporal temporal ->
            temporal.minus(delegate.amount, delegate.unit)
        }    }

    private static void saveMetaClass(Class clazz, Map originalMetaClasses) {
        originalMetaClasses[clazz] = GroovySystem.metaClassRegistry.getMetaClass(clazz)
    }
}

class TemporalUnitWrapper {
    final int amount
    final ChronoUnit unit

    TemporalUnitWrapper(int amount, ChronoUnit unit) {
        this.amount = amount
        this.unit = unit
    }
}



// Example usage
TimeCategory8.use {
    def now = LocalDateTime.now()
    println "Now: $now"

    // Add 2 hours
    def later = now + 2.hours
    println "2 hours later: $later"

    // Subtract 3 days
    def earlier = now - 3.days
    println "3 days earlier: $earlier"

    // Add 45 minutes
    def moreMinutes = now + 45.minutes
    println "45 minutes later: $moreMinutes"

    // Subtract 1 week
    def lessWeeks = now - 1.weeks
    println "1 week earlier: $lessWeeks"

    // Add 2 days 1 hour 2 minutes
    def theDay = 2.days + now + 2.minutes + 1.hours
    println "2 day 1 hour 2 minute later: $theDay"

    // LocalDate
    def today = LocalDate.now()
    println "Today: $today"

    // Add 2 days
    def twoDaysLater = today + 2.days
    println "2 days later: $twoDaysLater"

    // Add 3 weeks
    def threeWeeksLater = 4.weeks + today - 1.weeks
    println "3 weeks later: $threeWeeksLater"

    // Add 1 year
    def nextYear = today + 1.years
    println "1 year later: $nextYear"

}
