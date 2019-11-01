package reform.ko.internal

// number of processors at startup for consistent prop initialization
internal val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()

internal fun systemProp(
        propertyName: String
): String? =
        try {
            System.getProperty(propertyName)
        } catch (e: SecurityException) {
            null
        }
internal fun systemProp(
        propertyName:String,
        defaultValue: Boolean
): Boolean =
        try {
            System.getProperty(propertyName)?.toBoolean() ?: defaultValue
        } catch (e: SecurityException) {
            defaultValue
        }