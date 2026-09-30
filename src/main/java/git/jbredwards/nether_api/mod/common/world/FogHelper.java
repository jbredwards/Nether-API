package git.jbredwards.nether_api.mod.common.world;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
final class FogHelper
{
    @Nonnull
    private static final int[] WEIGHTS = {0, 1, 4, 6, 4, 1, 0};

    static final int DIAMETER = WEIGHTS.length;
    static final int RADIUS = DIAMETER >> 1;

    @Nonnull
    static double[] getWeights(final double offset) {
        @Nonnull final double[] weights = new double[DIAMETER];

        if(offset < 0.5) for(int i = DIAMETER - 2; i > -1; i--) weights[i] = getWeight(offset - 0.5 + 1, i);
        else for(int i = DIAMETER - 1; i > 0; i--) weights[i] = getWeight(offset - 0.5, i - 1); // Mirror at 0.5.

        return weights;
    }

    // Finds the weighted average of the two points, where more weight is given to the closer point.
    private static double getWeight(final double offset, final int index) {
        return offset * WEIGHTS[index] + (1 - offset) * WEIGHTS[index + 1];
    }
}
