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
