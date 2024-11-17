package io.timecategory


import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

/**
 * Represents a utility class for enhancing Java's time-related classes
 * (e.g., LocalDateTime, LocalDate, LocalTime) with dynamic Groovy methods.
 * Provides functionality to use custom temporal operations, such as adding or
 * subtracting time units directly from numeric types.
 * @author: ionia.kim
 * @since: 2024-11-16
 */
class TimeCategory8 {
    /**
     * Main entry point for applying enhancements. Temporarily replaces the
     * metaClass of relevant classes with augmented capabilities, executes the
     * provided closure, and restores the original metaClasses afterwards.
     *
     * @param closure The closure containing the code that utilizes the enhanced time capabilities.
     */
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

    /**
     * Dynamically adds properties to `Integer` and `Long` to enable time unit operations.
     * For example, 5.seconds or 3.years.
     */
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

    /**
     * Enhances time-related classes (e.g., LocalDateTime, LocalDate) to support dynamic
     * addition and subtraction of temporal units and custom time operations.
     */
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

    /**
     * Saves the original metaClass of a given class to allow restoration
     * after dynamic modifications.
     *
     * @param clazz The class whose metaClass should be saved.
     * @param originalMetaClasses Map to store the original metaClass.
     */
    private static void saveMetaClass(Class clazz, Map originalMetaClasses) {
        originalMetaClasses[clazz] = GroovySystem.metaClassRegistry.getMetaClass(clazz)
    }
}

/**
 * Wraps a temporal unit (e.g., DAYS, HOURS) along with its amount for dynamic operations.
 */
class TemporalUnitWrapper {
    final int amount
    final ChronoUnit unit

    /**
     * Constructs a wrapper for a temporal unit with its corresponding amount.
     *
     * @param amount The number of units.
     * @param unit The temporal unit (e.g., ChronoUnit.DAYS).
     */
    TemporalUnitWrapper(int amount, ChronoUnit unit) {
        this.amount = amount
        this.unit = unit
    }

    /**
     * Combines this temporal unit with another to create a compound operation.
     *
     * @param other Another TemporalUnitWrapper or Temporal object.
     * @return A TemporalOperationWrapper containing the combined operation.
     */
    TemporalOperationWrapper plus(Object other) {
        if (!(other instanceof TemporalUnitWrapper || other instanceof Temporal)) {
            throw new UnsupportedOperationException("Unsupported type: ${other.class}")
        }
        new TemporalOperationWrapper().add(this).add(other)
    }

    /**
     * Subtracts another temporal unit from this one.
     *
     * @param other Another TemporalUnitWrapper or Temporal object.
     * @return A TemporalOperationWrapper containing the resultant operation.
     */
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

/**
 * Encapsulates a sequence of temporal operations (additions or subtractions)
 * that can be applied to a Temporal object.
 */
class TemporalOperationWrapper {
    private final List<Object> operations = []

    /**
     * Adds an operation to the list of temporal operations.
     *
     * @param item The operation to add (e.g., a TemporalUnitWrapper).
     * @return The updated TemporalOperationWrapper.
     */
    TemporalOperationWrapper add(Object item) {
        operations << item
        this
    }

    /**
     * Subtracts a temporal unit by adding its negated version.
     *
     * @param unitWrapper The temporal unit to subtract.
     * @return The updated TemporalOperationWrapper.
     */
    TemporalOperationWrapper subtract(TemporalUnitWrapper unitWrapper) {
        operations << new TemporalUnitWrapper(-unitWrapper.amount, unitWrapper.unit)
        this
    }

    /**
     * Negates all operations in this wrapper (e.g., inverting additions and subtractions).
     *
     * @return The updated TemporalOperationWrapper with negated operations.
     */
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

    /**
     * Applies the sequence of operations to a base Temporal object.
     *
     * @param temporal The base Temporal object to apply operations to.
     * @return The resulting Temporal object after applying all operations.
     */
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
