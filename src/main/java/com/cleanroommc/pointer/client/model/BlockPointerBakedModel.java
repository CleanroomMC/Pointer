package com.cleanroommc.pointer.client.model;

import com.cleanroommc.pointer.block.BlockPointer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.minecraft.util.EnumFacing.DOWN;

public class BlockPointerBakedModel implements IBakedModel {

    private IBakedModel original;
    private TextureAtlasSprite particle;

    @Override
    public @Nonnull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }
        IExtendedBlockState extState = (IExtendedBlockState) state;

        EnumFacing topFacing = extState.getValue(BlockPointer.TOP_FACING);
        if (topFacing == null) {
            return Collections.emptyList();
        }

        EnumFacing frontFacing = extState.getValue(BlockPointer.FRONT_FACING);
        if (frontFacing == null) {
            return Collections.emptyList();
        }

        if (this.original == null) {
            synchronized (this) {
                this.original = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                        .getModel(new ModelResourceLocation(BlockPointer.INSTANCE.getRegistryName(), "normal"));
            }
        }

        List<BakedQuad> quads = this.original.getQuads(extState, null, rand);

        final Matrix4f transformMatrix = new Matrix4f();
        final Tuple4f vertexTransformingVec = new Vector4f();

        // Transform the matrix so the top and front facings of the model matches the block in-world
        transformMatrixByFacings(transformMatrix, topFacing, frontFacing);
        
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad b : quads) {
            result.add(rebakeQuad(b, transformMatrix, vertexTransformingVec));
        }
        return result;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @Nonnull TextureAtlasSprite getParticleTexture() {
        if (this.particle == null) {
            synchronized (this) {
                this.particle = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                        .getModel(new ModelResourceLocation(BlockPointer.INSTANCE.getRegistryName(), "normal")).getParticleTexture();
            }
        }
        return this.particle;
    }

    @Override
    public @Nonnull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    private void transformMatrixByFacings(Matrix4f transformMatrix, EnumFacing topFacing, EnumFacing frontFacing) {
        Matrix4f intermediaryMatrix = new Matrix4f();
        transformMatrix.setIdentity();
        moveToPivot(transformMatrix, intermediaryMatrix, true);
        if (topFacing.getAxis() == Axis.Y) {
            switch (frontFacing) {
                case NORTH:
                    rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI));
                    break;
                case EAST:
                    rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                    break;
                case WEST:
                    rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                    break;
            }
            if (topFacing == DOWN) {
                rotateX(transformMatrix, intermediaryMatrix, (float) (Math.PI));
                rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI));
            }
        } else {
            switch (topFacing) {
                case WEST:
                    rotateZ(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                    switch (frontFacing) {
                        case DOWN:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                            break;
                        case UP:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (+Math.PI / 2));
                            break;
                        case SOUTH:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI));
                    }
                    break;
                case EAST:
                    rotateZ(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                    switch (frontFacing) {
                        case DOWN:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                            break;
                        case UP:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                            break;
                        case SOUTH:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI));
                    }
                    break;
                case NORTH:
                    rotateX(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                    switch (frontFacing) {
                        case DOWN:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI));
                            break;
                        case EAST:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                            break;
                        case WEST:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                    }
                    break;
                default:
                    rotateX(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                    switch (frontFacing) {
                        case UP:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI));
                            break;
                        case EAST:
                        case SOUTH:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (Math.PI / 2));
                            break;
                        case WEST:
                            rotateY(transformMatrix, intermediaryMatrix, (float) (-Math.PI / 2));
                    }
            }
        }
        moveToPivot(transformMatrix, intermediaryMatrix, false);
    }

    private void moveToPivot(Matrix4f matrix, Matrix4f intermediary, boolean positive) {
        intermediary.setIdentity();
        float pivot = positive ? .5F : -.5F;
        intermediary.m03 = pivot;
        intermediary.m13 = pivot;
        intermediary.m23 = pivot;
        matrix.mul(intermediary);
    }

    private void rotateX(Matrix4f matrix, Matrix4f intermediary, float angle) {
        intermediary.setIdentity();
        intermediary.rotX(angle);
        matrix.mul(intermediary);
    }

    private void rotateY(Matrix4f matrix, Matrix4f intermediary, float angle) {
        intermediary.setIdentity();
        intermediary.rotY(angle);
        matrix.mul(intermediary);
    }

    private void rotateZ(Matrix4f matrix, Matrix4f intermediary, float angle) {
        intermediary.setIdentity();
        intermediary.rotZ(angle);
        matrix.mul(intermediary);
    }

    private BakedQuad rebakeQuad(BakedQuad b, Matrix4f transformMatrix, Tuple4f vertexTransformingVec) {
        int[] newQuad = new int[28];
        int[] quadData = b.getVertexData();
        for (int k = 0; k < 4; ++k) {
            // Getting the offset for the current vertex.
            int vertexIndex = k * 7;
            vertexTransformingVec.x = Float.intBitsToFloat(quadData[vertexIndex]);
            vertexTransformingVec.y = Float.intBitsToFloat(quadData[vertexIndex + 1]);
            vertexTransformingVec.z = Float.intBitsToFloat(quadData[vertexIndex + 2]);
            vertexTransformingVec.w = 1;

            // Transforming it by the model matrix.
            transformMatrix.transform(vertexTransformingVec);

            // Converting the new data to ints.
            int x = Float.floatToRawIntBits(vertexTransformingVec.x);
            int y = Float.floatToRawIntBits(vertexTransformingVec.y);
            int z = Float.floatToRawIntBits(vertexTransformingVec.z);

            // Vertex position data
            newQuad[vertexIndex] = x;
            newQuad[vertexIndex + 1] = y;
            newQuad[vertexIndex + 2] = z;

            newQuad[vertexIndex + 3] = quadData[vertexIndex + 3];

            newQuad[vertexIndex + 4] = quadData[vertexIndex + 4]; //texture
            newQuad[vertexIndex + 5] = quadData[vertexIndex + 5];

            // Vertex brightness
            newQuad[vertexIndex + 6] = quadData[vertexIndex + 6];
        }
        return new BakedQuad(newQuad, b.getTintIndex(), b.getFace(), b.getSprite(), false, DefaultVertexFormats.BLOCK);
    }

}
