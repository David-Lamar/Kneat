package kneat.evolution.network

import kotlin.math.*

/**
 * A function used by a [Node] in a neural network to determine which value it should emit. This imitates
 * the Action Potential of a biological neuron.
 *
 * TODO: The descriptions here are as accurate as my (David's) mathematical knowledge permits. I would like
 *  to make them more comprehensive but I don't want to directly plagiarize somebody else's work.
 *  As such, to find more information about these (and potentially more accurate information), I've
 *  found that paperswithcode.com has a pretty great selection of information about these different
 *  activation functions.
 *
 * TODO: Add more activations; this is only a small subset of the ones listed [here](https://en.wikipedia.org/wiki/Activation_function)
 */
interface Activation {
    /**
     * Takes in an [action] and emits a [Float] value
     */
    fun activate(action: Float) : Float

    /**
     * Used to provide a value between 0 and 1 and is generally used to indicate the probability that a
     * classification is "accurate"
     *
     * TODO: Insert diagram
     */
    object Sigmoid : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return 1f / (1f + exp(-newAction))
        }
    }

    /**
     * Used to provide a value between -1 and 1 and is generally used to classify between two different outputs
     * as negative values are strongly mapped negative, positive values are strongly mapped positive, and
     * zero values are strongly mapped zero.
     *
     * TODO: Insert diagram
     */
    object HyperbolicTangent : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 2.5f * action))
            return tanh(newAction)
        }
    }

    /**
     * Used to provide a value between -1 and 1 and is generally used to classify between two different outputs
     * that may have many different criteria that influence classification.
     *
     * TODO: Insert diagram
     */
    object Sin : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return sin(newAction)
        }
    }

    /**
     * Used to provide a value between 0 and 1 and is generally used to normalize a value around "0";
     * low negative values are mapped strongly to 0, high positive numbers are mapped strongly to 0 and
     * all values between -1 and 1 are mapped directly.
     *
     * TODO: Insert diagram
     */
    object Gaussian : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-3.4f, min(3.4f, action))
            return exp(-5f * newAction.pow(2f)) //TODO: Ensure that converting to float has no negative effect
        }
    }

    /**
     * Commonly referred to as ReLU, provides the the passed in value if positive, otherwise 0
     *
     * TODO: Insert diagram
     */
    object RectifiedLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) action else 0f
        }
    }

    /**
     * Similar to [RectifiedLinearUnit] where positive values are mapped directly, this differs in that
     * the negative values are mapped to values approaching zero.
     *
     * TODO: Insert diagram
     */
    object ExponentialLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) action else exp(action) - 1
        }
    }

    /**
     * Commonly referred to as LeLU or Leaky ReLu, similar to [RectifiedLinearUnit] where if the value passed
     * in is positive it's returned directly; however, this activation function also supports negative
     * numbers with a very shallow slope.
     *
     * TODO: Insert diagram
     */
    object LeakyRectifiedLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) action else .005f * action
        }
    }
    /**
     * Commonly referred to as SELU, provides values from negative Infinity to positive infinity, but allows
     * the network to self-normalize.
     *
     * TODO: Insert diagram
     */
    object ScaledExponentialLinearUnit : Activation {
        override fun activate(action: Float): Float {
            val lambda = 1.0507009873554804934193349852946
            val alpha = 1.6732632423543772848170429916717
            val positive = (lambda * action).toFloat()
            val negative = (lambda * alpha * (exp(action) - 1) ).toFloat()
            return if (action > 0) positive else negative
        }
    }

    /**
     * A smooth approximation to [RectifiedLinearUnit]; negative values are still mapped very strongly to 0, however
     * small negative numbers may be mapped to a negative value.
     *
     * TODO: Insert diagram
     */
    object Softplus : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return .2f * ln(1f + exp(newAction))
        }
    }

    /**
     * Simply returns the same value that is passed in
     *
     * TODO: Insert diagram
     */
    object Identity : Activation {
        override fun activate(action: Float): Float {
            return action
        }
    }

    /**
     * Returns the value passed in clamped (or will not exceed) between -1 and 1
     * TODO: Insert diagram
     */
    object Clamped : Activation {
        override fun activate(action: Float): Float {
            return max(-1f, min(1f, action))
        }
    }

    /**
     * Returns the 1 / value when the value is not 0, otherwise 0
     *
     * TODO: Insert diagram
     */
    object Inverse : Activation {
        override fun activate(action: Float): Float {
            return if (action == 0f) action else 1f / action
        }
    }

    /**
     * Returns the natural logarithm to the value passed in
     *
     * TODO: Insert diagram
     */
    object Logarithmic : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(1f.pow(-7f), action) //TODO: Double check 1e-7 == 1f.pow(-7)
            return ln(newAction)
        }
    }

    /**
     * Returns e to the power of the action (action is clamped between -60 and 60)
     *
     * TODO: Insert diagram
     */
    object Exponential : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, action))
            return exp(newAction)
        }
    }

    /**
     * Returns the absolute value of the value passed in
     *
     * TODO: Insert diagram
     */
    object AbsoluteValue : Activation {
        override fun activate(action: Float): Float {
            return abs(action)
        }
    }

    /**
     * Commonly referred to as the "Mexican Hat" activation function, similar to a [Gaussian] activation
     * function but non-smooth.
     *
     * TODO: Insert diagram
     */
    object Hat : Activation {
        override fun activate(action: Float): Float {
            return max(0f, 1f - abs(action))
        }
    }

    /**
     * Returns the value passed in squared
     *
     * TODO: Insert diagram
     */
    object Square : Activation {
        override fun activate(action: Float): Float {
            return action.pow(2f)
        }
    }

    /**
     * Returns the value passed in cubed
     * TODO: Insert diagram
     */
    object Cubic : Activation {
        override fun activate(action: Float): Float {
            return action.pow(3f)
        }
    }
}