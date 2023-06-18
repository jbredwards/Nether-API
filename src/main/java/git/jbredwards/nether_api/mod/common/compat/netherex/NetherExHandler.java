package git.jbredwards.nether_api.mod.common.compat.netherex;

import git.jbredwards.nether_api.api.registry.INetherAPIRegistry;
import logictechcorp.netherex.NetherExConfig;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class NetherExHandler
{
    public static boolean doesXZShowFog() { return !NetherExConfig.client.visual.disableNetherFog; }

    public static boolean doesGravelGenerate() { return NetherExConfig.dimension.nether.generateGravel; }

    public static boolean doesSoulSandGenerate() { return NetherExConfig.dimension.nether.generateSoulSand; }

    public static void registerBiomes(@Nonnull INetherAPIRegistry registry) {

    }
}
