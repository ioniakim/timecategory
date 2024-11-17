package io.timecategory


import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
            "hours"  : ChronoUnit.HOURS,
            "days"   : ChronoUnit.DAYS,
            "weeks"  : ChronoUnit.WEEKS,
            "months" : ChronoUnit.MONTHS,
            "years"  : ChronoUnit.YEARS
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
            temporalClass.metaClass.plus = { arg ->
                if (arg instanceof TemporalOperationWrapper) {
                    arg.applyTo(delegate) // Apply all operations
                } else if (arg instanceof TemporalUnitWrapper) {
                    delegate.plus(arg.amount, arg.unit) // Unpack TemporalUnitWrapper
                } else {
                    throw new UnsupportedOperationException("Cannot add ${arg.class}")
                }
            }

            temporalClass.metaClass.minus = { arg ->
                if (arg instanceof TemporalOperationWrapper) {
                    arg.negate().applyTo(delegate) // Apply negated operations
                } else if (arg instanceof TemporalUnitWrapper) {
                    delegate.minus(arg.amount, arg.unit) // Unpack TemporalUnitWrapper
                } else {
                    throw new UnsupportedOperationException("Cannot subtract ${arg.class}")
                }
            }
        }

        // Add reverse operations to TemporalUnitWrapper
        TemporalUnitWrapper.metaClass.plus = { Temporal temporal ->
            new TemporalOperationWrapper().add(delegate).applyTo(temporal)
        }

        TemporalUnitWrapper.metaClass.minus = { Temporal temporal ->
            new TemporalOperationWrapper().subtract(delegate).applyTo(temporal)
        }

        // Ensure TemporalOperationWrapper resolves itself dynamically
        TemporalOperationWrapper.metaClass.plus = { other ->
            if (other instanceof TemporalUnitWrapper) {
                delegate.add(other)
            } else if (other instanceof Temporal) {
                delegate.applyTo(other)
            } else {
                throw new UnsupportedOperationException("Cannot add ${other.class}")
            }
        }

        TemporalOperationWrapper.metaClass.minus = { other ->
            if (other instanceof TemporalUnitWrapper) {
                delegate.subtract(other)
            } else if (other instanceof Temporal) {
                delegate.applyTo(other)
            } else {
                throw new UnsupportedOperationException("Cannot subtract ${other.class}")
            }
        }

        // Automatically resolve TemporalOperationWrapper to Temporal when printing
        TemporalOperationWrapper.metaClass.toString = { ->
            throw new UnsupportedOperationException("Cannot resolve TemporalOperationWrapper without a base Temporal")
        }
    }

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

    TemporalOperationWrapper plus(Object other) {
        if (!(other instanceof TemporalUnitWrapper || other instanceof Temporal)) {
            throw new UnsupportedOperationException("Unsupported type: ${other.class}")
        }
        new TemporalOperationWrapper().add(this).add(other)
    }

    TemporalOperationWrapper minus(Object other) {
        if (!(other instanceof TemporalUnitWrapper || other instanceof Temporal)) {
            throw new UnsupportedOperationException("Unsupported type: ${other.class}")
        }
        new TemporalOperationWrapper().add(this).subtract(other)
    }

    TemporalOperationWrapper plus(TemporalOperationWrapper operation) {
        operation.add(this)
    }

    TemporalOperationWrapper minus(TemporalOperationWrapper operation) {
        operation.subtract(this)
    }
}

class TemporalOperationWrapper {
    private final List<Object> operations = []

    TemporalOperationWrapper add(Object item) {
        operations << item
        this
    }

    TemporalOperationWrapper subtract(TemporalUnitWrapper unitWrapper) {
        operations << new TemporalUnitWrapper(-unitWrapper.amount, unitWrapper.unit)
        this
    }

    TemporalOperationWrapper negate() {
        operations.replaceAll { op ->
            if (op instanceof TemporalUnitWrapper) {
                new TemporalUnitWrapper(-op.amount, op.unit)
            } else {
                op
            }
        }
        this
    }

    Temporal applyTo(Temporal temporal) {
        if (temporal == null) {
            throw new UnsupportedOperationException(
                    "Cannot resolve TemproalOperationWrapper without a base Temporal object"
            )
        }
        operations.inject(temporal) { result, operation ->
            if (operation instanceof TemporalUnitWrapper) {
                result.plus(operation.amount, operation.unit)
            } else if (operation instanceof Temporal) {
                result
            } else {
                throw new UnsupportedOperationException("Cannot apply ${operation.class}")
            }
        }
    }
}
