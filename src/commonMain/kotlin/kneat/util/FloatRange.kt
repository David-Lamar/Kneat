package kneat.util

//TODO: Add annotation processor for this

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS,
AnnotationTarget.PROPERTY)
annotation class FloatRange(
    val from: Double = Double.NEGATIVE_INFINITY,
    val to: Double = Double.POSITIVE_INFINITY,
    val fromInclusive: Boolean = true,
    val toInclusive: Boolean = true
)