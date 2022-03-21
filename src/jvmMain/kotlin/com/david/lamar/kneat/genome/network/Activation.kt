package com.david.lamar.kneat.genome.network

import kotlin.math.*

interface Activation {

    fun activate(action: Float) : Float

    object Sigmoid : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return 1f / (1f + exp(-newAction))
        }
    }

    object HyperbolicTangent : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 2.5f * action))
            return tanh(newAction)
        }
    }

    object Sin : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return sin(newAction)
        }
    }

    object Gaussian : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-3.4f, min(3.4f, action))
            return exp(-5f * newAction.pow(2f)) //TODO: Ensure that converting to float has no negative effect
        }
    }

    object RectifiedLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) action else 0f
        }
    }

    object ExponentialLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) 0f else exp(action) - 1
        }
    }

    object LeakyRectifiedLinearUnit : Activation {
        override fun activate(action: Float): Float {
            return if (action > 0) 0f else .005f * action
        }
    }

    object ScaledExponentialLinearUNit : Activation {
        override fun activate(action: Float): Float {
            val lambda = 1.0507009873554804934193349852946
            val alpha = 1.6732632423543772848170429916717
            val positive = (lambda * action).toFloat()
            val negative = (lambda * alpha * (exp(action) - 1) ).toFloat()
            return if (action > 0) positive else negative
        }
    }

    object Softplus : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, 5f * action))
            return .2f * ln(1f + exp(newAction))
        }
    }

    object Identity : Activation {
        override fun activate(action: Float): Float {
            return action
        }
    }

    object Clamped : Activation {
        override fun activate(action: Float): Float {
            return max(-1f, min(1f, action))
        }
    }

    object Inverse : Activation {
        override fun activate(action: Float): Float {
            return if (action == 0f) action else 1f / action
        }
    }

    object Logarithmic : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(1f.pow(-7f), action) //TODO: Double check 1e-7 == 1f.pow(-7)
            return ln(newAction)
        }
    }

    object Exponential : Activation {
        override fun activate(action: Float): Float {
            val newAction = max(-60f, min(60f, action))
            return exp(newAction)
        }
    }

    object AbsoluteValue : Activation {
        override fun activate(action: Float): Float {
            return abs(action)
        }
    }

    object Hat : Activation {
        override fun activate(action: Float): Float {
            return max(0f, 1f - abs(action))
        }
    }

    object Square : Activation {
        override fun activate(action: Float): Float {
            return action.pow(2f)
        }
    }

    object Cubic : Activation {
        override fun activate(action: Float): Float {
            return action.pow(3f)
        }
    }
}
