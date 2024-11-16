import java.time.*

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
    def threeWeeksLater = 4.weeks + 1.days + today - 1.weeks - 1.days
    println "3 weeks later: $threeWeeksLater"

    // Add 1 year
    def nextYear = today + 1.years
    println "1 year later: $nextYear"

}
