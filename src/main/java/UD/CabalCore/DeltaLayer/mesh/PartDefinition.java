package UD.CabalCore.DeltaLayer.mesh;

public final class PartDefinition {
    public final int u0;
    public final int v0;
    public final int width;
    public final int height;
    public final int depth;

    public final float minX;
    public final float minY;
    public final float minZ;

    public final float inflate;

    public PartDefinition(
            int u0,
            int v0,
            int width,
            int height,
            int depth,
            float minX,
            float minY,
            float minZ,
            float inflate
    ) {
        this.u0 = u0;
        this.v0 = v0;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.inflate = inflate;
    }

    public float maxX() {
        return minX + width;
    }

    public float maxY() {
        return minY + height;
    }

    public float maxZ() {
        return minZ + depth;
    }
}