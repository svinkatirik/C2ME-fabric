package com.ishland.c2me.mixin.optimization.reduce_allocs.object_pooling_caching;

import com.ishland.c2me.common.optimization.reduce_allocs.PooledFeatureContext;
import com.ishland.c2me.common.optimization.reduce_allocs.SimpleObjectPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Random;

@Mixin(ConfiguredFeature.class)
public class MixinConfiguredFeature<FC extends FeatureConfig, F extends Feature<FC>> {

    @Shadow @Final public F feature;

    @Shadow @Final public FC config;

    /**
     * @author ishland
     * @reason pool FeatureContext
     */
    @Overwrite
    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos origin) {
        final SimpleObjectPool<PooledFeatureContext<?>> pool = PooledFeatureContext.POOL.get();
        final PooledFeatureContext<FC> context = (PooledFeatureContext<FC>) pool.alloc();
        try {
            context.reInit(Optional.empty(), world, chunkGenerator, random, origin, this.config);
            return this.feature.generate(context);
        } finally {
            context.reInit();
            pool.release(context);
        }
    }

    /**
     * @author ishland
     * @reason pool FeatureContext
     */
    @Overwrite
    public boolean generate(Optional<ConfiguredFeature<?, ?>> feature, StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos origin) {
        final SimpleObjectPool<PooledFeatureContext<?>> pool = PooledFeatureContext.POOL.get();
        final PooledFeatureContext<FC> context = (PooledFeatureContext<FC>) pool.alloc();
        try {
            context.reInit(feature, world, chunkGenerator, random, origin, this.config);
            return this.feature.generate(context);
        } finally {
            context.reInit();
            pool.release(context);
        }
    }

}